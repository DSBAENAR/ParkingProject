package com.parking.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class UserInvoice {
    @Id
    String id;

    @Transient
    Product product;

    public UserInvoice(String id, Product product) {
        this.id = id;
        this.product = product;
    }

    public UserInvoice(){}
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
    

    

}
