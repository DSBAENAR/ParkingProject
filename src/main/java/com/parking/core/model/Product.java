package com.parking.core.model;

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
