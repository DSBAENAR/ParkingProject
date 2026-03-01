package com.parking.core.payment.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.parking.core.enums.Currencies;
import com.parking.core.model.Product;
import com.parking.core.payment.Requests.InvoiceRequest;
import com.parking.core.payment.response.InvoiceResponse;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceItem;
import com.stripe.model.TaxRate;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceItemCreateParams;
import com.stripe.param.TaxRateCreateParams;

class InvoiceServiceTest {

    private InvoiceService invoiceService;
    private MockedStatic<TaxRate> taxRateStatic;
    private MockedStatic<Invoice> invoiceStatic;
    private MockedStatic<InvoiceItem> invoiceItemStatic;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService();
        taxRateStatic = Mockito.mockStatic(TaxRate.class);
        invoiceStatic = Mockito.mockStatic(Invoice.class);
        invoiceItemStatic = Mockito.mockStatic(InvoiceItem.class);
    }

    @AfterEach
    void tearDown() {
        taxRateStatic.close();
        invoiceStatic.close();
        invoiceItemStatic.close();
    }

    @Test
    @DisplayName("createAnInvoceForAUser - success")
    void createInvoice_success() throws Exception {
        TaxRate rate = mock(TaxRate.class);
        when(rate.getId()).thenReturn("txr_123");
        taxRateStatic.when(() -> TaxRate.create(any(TaxRateCreateParams.class))).thenReturn(rate);

        Invoice invoice = mock(Invoice.class);
        when(invoice.getId()).thenReturn("inv_123");
        when(invoice.getCurrency()).thenReturn("cop");
        when(invoice.getCustomer()).thenReturn("cus_123");
        invoiceStatic.when(() -> Invoice.create(any(InvoiceCreateParams.class))).thenReturn(invoice);

        InvoiceItem item = mock(InvoiceItem.class);
        when(item.getDescription()).thenReturn("Time in parking");
        when(item.getAmount()).thenReturn(50000L);
        when(item.getCurrency()).thenReturn("cop");
        invoiceItemStatic.when(() -> InvoiceItem.create(any(InvoiceItemCreateParams.class))).thenReturn(item);

        Product product = new Product(2, 25000L);
        InvoiceRequest request = new InvoiceRequest("cus_123", Currencies.COP, product);

        InvoiceResponse response = invoiceService.createAnInvoceForAUser(request);

        assertNotNull(response);
        assertEquals("inv_123", response.id());
        assertEquals(Currencies.COP, response.currency());
        assertEquals("cus_123", response.customer());
        assertNotNull(response.tax());
        assertNotNull(response.productInfo());
    }

    @Test
    @DisplayName("createAnInvoceForAUser - tax rate creation fails")
    void createInvoice_taxRateFails() {
        taxRateStatic.when(() -> TaxRate.create(any(TaxRateCreateParams.class)))
                .thenThrow(new RuntimeException("Tax rate error"));

        Product product = new Product(1, 10000L);
        InvoiceRequest request = new InvoiceRequest("cus_123", Currencies.COP, product);

        assertThrows(RuntimeException.class, () -> invoiceService.createAnInvoceForAUser(request));
    }

    @Test
    @DisplayName("createAnInvoceForAUser - invoice creation fails")
    void createInvoice_invoiceFails() {
        TaxRate rate = mock(TaxRate.class);
        when(rate.getId()).thenReturn("txr_123");
        taxRateStatic.when(() -> TaxRate.create(any(TaxRateCreateParams.class))).thenReturn(rate);

        invoiceStatic.when(() -> Invoice.create(any(InvoiceCreateParams.class)))
                .thenThrow(new RuntimeException("Invoice error"));

        Product product = new Product(1, 10000L);
        InvoiceRequest request = new InvoiceRequest("cus_123", Currencies.COP, product);

        assertThrows(RuntimeException.class, () -> invoiceService.createAnInvoceForAUser(request));
    }
}
