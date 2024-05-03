package com.example.online.grocery.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;

@Entity
@Table
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int orderItemId;
    private String name;
    private int quantity;

    private double price;
    private double discount;
    @Nullable
    private int age;
    @Nullable
    private String origin;
    @Nullable
    private double weight;
    @ManyToOne
    @JoinColumn(name = "itemId")
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    private Item item;
    @ManyToOne
    @JoinColumn(name = "orderId")
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    private Order order;

    public OrderItem(int orderItemId, String name, int quantity, double price, double discount, int age, String origin, double weight, Item item, Order order) {
        this.orderItemId = orderItemId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.discount = discount;
        this.age = age;
        this.origin = origin;
        this.weight = weight;
        this.item = item;
        this.order = order;
    }

    public OrderItem(String name, int quantity, int age, double weight, String origin) {
        this.name = name;
        this.quantity = quantity;
        this.age = age;
        this.weight = weight;
        this.origin = origin;
    }

    public OrderItem(String name, int quantity, int age) {
        this.name = name;
        this.quantity = quantity;
        this.age = age;
    }

    public OrderItem(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    public OrderItem(String name, int quantity, String origin) {
        this.name = name;
        this.quantity = quantity;
        this.origin = origin;
    }

    public OrderItem() {
    }

    public int getQuantity() {
        return quantity;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getAge() {
        return age;
    }

    public String getOrigin() {
        return origin;
    }

    public double getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }
}
