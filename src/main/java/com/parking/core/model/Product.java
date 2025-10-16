package com.parking.core.model;

/**
 * The Product class represents a product with an identifier, the number of hours,
 * and the price associated with it.
 */
public class Product {
    String id;
    Integer hours;
    Long price;

    
    public Product(Integer hours, Long price) {
        this.hours = hours;
        this.price = price;
    }
    public Product(){}


    public Integer getHours() {
        return hours;
    }
    public void setHours(Integer hours) {
        this.hours = hours;
    }
    public Long getPrice() {
        return price;
    }
    public void setPrice(Long price) {
        this.price = price;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    
}
