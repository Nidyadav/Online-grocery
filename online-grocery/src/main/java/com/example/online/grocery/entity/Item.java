package com.example.online.grocery.entity;

import jakarta.persistence.*;

@Entity
@Table
public class Item {
    public enum ProductType{
        BREAD,
        VEGETABLE,
        BEER,
        DUTCH_BEER,
        GERMAN_BEER,
        BELGIUM_BEER
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int itemId;
    private String name;
    private  ProductType type;
    private  double unitPrice;
    private  String discountRule;


    public Item(int itemId, String name, ProductType type, double unitPrice, String discountRule) {
        this.itemId = itemId;
        this.name = name;
        this.type = type;
        this.unitPrice = unitPrice;
        this.discountRule = discountRule;

    }

    public Item(String name,double unitPrice,ProductType type) {
        this.name = name;
        this.type = type;
        this.unitPrice = unitPrice;
    }

    public Item() {
    }

    public Item(String name, ProductType type, double unitPrice, String discountRule) {
        this.name = name;
        this.type = type;
        this.unitPrice = unitPrice;
        this.discountRule = discountRule;
    }

    public int getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public ProductType getType() {
        return type;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getDiscountRule() {
        return discountRule;
    }
}
