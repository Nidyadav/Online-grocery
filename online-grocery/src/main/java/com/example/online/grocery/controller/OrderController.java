package com.example.online.grocery.controller;

import com.example.online.grocery.entity.Order;
import com.example.online.grocery.exceptions.ItemNotFoundException;
import com.example.online.grocery.exceptions.NotValidOrderException;
import com.example.online.grocery.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> placeOrderAndGetReceipt(@RequestBody Order order) throws NotValidOrderException{
        return ResponseEntity.ok(orderService.processOrder(order));
    }
    @ExceptionHandler(NotValidOrderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleNotValidOrderException(NotValidOrderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleItemNotFoundException(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
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
