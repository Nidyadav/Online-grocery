package com.example.online.grocery.controller;

import com.example.online.grocery.entity.Order;
import com.example.online.grocery.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    @Autowired
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping(value = "/place_order", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> placeOrderAndGetReceipt(@RequestBody Order order) {

        return ResponseEntity.ok(orderService.processOrder(order));
    }
    @GetMapping(path = "/discount_rules")
    public List<String> getDiscountRules() {
        return orderService.getAllDiscountRules();
    }

    @GetMapping(path = "/prices")
    public Map<String, Double> getPrices() {
        return orderService.getPricesItemWise();
    }
}
