package com.example.online.grocery.service;

import com.example.online.grocery.entity.Item;
import com.example.online.grocery.entity.Order;
import com.example.online.grocery.entity.OrderItem;
import com.example.online.grocery.exceptions.ItemNotFoundException;
import com.example.online.grocery.exceptions.NotValidOrderException;
import com.example.online.grocery.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService {
    @Autowired
    ItemRepository itemRepository;

    public OrderService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public String processOrder(Order order) throws ItemNotFoundException ,NotValidOrderException{
        double totalPrice = 0.0;
        double discount = 0.0;
        if(order.getOrderItems().size()==0){
            throw new NotValidOrderException("No items found in the order hence it is not valid.");
        }
        Order modifiedOrder = updateOrderCombineSameItemsToGetDiscount(order);
        for (OrderItem orderItem : modifiedOrder.getOrderItems()) {
            Optional<Item> item = itemRepository.findByName(orderItem.getName());
            Item validItem = item.orElseThrow(() -> new ItemNotFoundException("Item added is not valid."));
            orderItem.setPrice(validItem.getUnitPrice());
            if (validItem.getType() == Item.ProductType.VEGETABLE)
                totalPrice += orderItem.getPrice() * (orderItem.getWeight() / 100);
            else
                totalPrice += orderItem.getPrice() * orderItem.getQuantity();
            if (validItem.getType() == Item.ProductType.BREAD) {
                discount = calculateBreadDiscount(orderItem);
                orderItem.setDiscount(discount);

            } else if (validItem.getType() == Item.ProductType.VEGETABLE) {
                discount = calculateDiscountForVegetable(orderItem);
                orderItem.setDiscount(discount);
            } else if (validItem.getType() == Item.ProductType.GERMAN_BEER||validItem.getType() == Item.ProductType.DUTCH_BEER||validItem.getType() == Item.ProductType.BELGIUM_BEER) {
                discount = calculateDiscountForBeer(orderItem);
                orderItem.setDiscount(discount);
            }

            totalPrice -= discount;
        }
        modifiedOrder.setTotalPrice(totalPrice);
        return generateReceipt(modifiedOrder);
    }

    public List<String> getAllDiscountRules() {
        List<String> discountRules = new ArrayList<>();
        itemRepository.findAll().forEach(item -> discountRules.add(item.getDiscountRule()));
        return discountRules;
    }

    public Map<String, Double> getPricesItemWise() {
        Map<String, Double> pricesPerItem = new HashMap<>();
        itemRepository.findAll().forEach(item -> pricesPerItem.put(item.getName(), item.getUnitPrice()));
        return pricesPerItem;
    }

    private double calculateBreadDiscount(OrderItem orderItem) {
        int age = orderItem.getAge();
        int quantity = orderItem.getQuantity();
        if (age <= 1) return 0.0;
        else if (age <= 3) {
            return orderItem.getPrice() * Math.max(0, (quantity / 2));

        } else if (age <= 6) {
            return orderItem.getPrice() * Math.max(0, 2 * (quantity / 3));
        } else {
            throw new IllegalArgumentException("Bread older than six days can not be added to the order.");
        }
    }

    private double calculateDiscountForVegetable(OrderItem orderItem) {
        double weight = orderItem.getWeight();
        double price = orderItem.getPrice() * weight / 100;
        if (weight <= 100) {
            return price * 0.05;
        } else if (weight <= 500) {
            return price * 0.07;
        } else {
            return price * 0.10;
        }
    }

    private double calculateDiscountForBeer(OrderItem orderItem) {
        String origin = orderItem.getOrigin();
        int quantity = orderItem.getQuantity();
        if (quantity >= 6 && origin.equals("Belgium")) {
            return 3.0;
        } else if (quantity >= 6 && origin.equals("Dutch")) {
            return 2.0;
        } else if (quantity >= 6 && origin.equals("German")) {
            return 4.0;
        } else {
            return 0.0;
        }
    }

    private String generateReceipt(Order processedOrder) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Order details:\n");
        for (OrderItem orderItem : processedOrder.getOrderItems()) {
            if (orderItem.getName().equals("vegetable")) {
                receipt.append(String.format("%.1f g x %s: €%.2f\n", orderItem.getWeight(), orderItem.getName(), orderItem.getPrice() * (orderItem.getWeight() / 100) - orderItem.getDiscount()));
            } else if (orderItem.getName().equals("bread")) {
                receipt.append(String.format("%d x %s (%d days old): €%.2f\n", orderItem.getQuantity(), orderItem.getName(), orderItem.getAge(), (orderItem.getPrice() * orderItem.getQuantity()) - orderItem.getDiscount()));

            } else
                receipt.append(String.format("%d x %s: €%.2f\n", orderItem.getQuantity(), orderItem.getName(), orderItem.getPrice() * orderItem.getQuantity() - orderItem.getDiscount()));
        }
        receipt.append(String.format("Total: €%.2f\n", processedOrder.getTotalPrice()));
        return receipt.toString();
    }

    private Order updateOrderCombineSameItemsToGetDiscount(Order order) {
        int totalDutchBeerInOrder = 0;
        int totalGermanBeerInOrder = 0;
        int totalBelgiumBeerInOrder = 0;
        double totalWeightOfVegetablesInOrder = 0.0;
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getName().equals("German beer")||orderItem.getName().equals("Dutch beer")||orderItem.getName().equals("Belgium beer")) {
                if (orderItem.getOrigin().equals("Dutch"))
                    totalDutchBeerInOrder += orderItem.getQuantity();
                if (orderItem.getOrigin().equals("German"))
                    totalGermanBeerInOrder += orderItem.getQuantity();
                if (orderItem.getOrigin().equals("Belgium"))
                    totalBelgiumBeerInOrder += orderItem.getQuantity();
            }
            if (orderItem.getName().equals("vegetable")) {
                totalWeightOfVegetablesInOrder += orderItem.getWeight();
            }
        }
        List<OrderItem> orderItems = order.getOrderItems();
        if (totalWeightOfVegetablesInOrder > 0) {
            List<OrderItem> itemsToBeCombined = new ArrayList<>();
            for (OrderItem orderItem:orderItems) {
                if (orderItem.getName().equals("vegetable")) {
                    itemsToBeCombined.add(orderItem);
                }
            }
            orderItems.removeAll(itemsToBeCombined);
            orderItems.add(new OrderItem("vegetable", 0, 0, totalWeightOfVegetablesInOrder, ""));
            order.setOrderItems(orderItems);
        }
        if (totalDutchBeerInOrder >= 6) {
            List<OrderItem> itemsToBeCombined = new ArrayList<>();
            for (OrderItem orderItem:orderItems) {
                if (orderItem.getName().equals("Dutch beer")) {
                    itemsToBeCombined.add(orderItem);
                }
            }
            orderItems.removeAll(itemsToBeCombined);
            orderItems.add(new OrderItem("Dutch beer", totalDutchBeerInOrder, 0, 0, "Dutch"));
            order.setOrderItems(orderItems);
        }
        if (totalGermanBeerInOrder >= 6) {
            List<OrderItem> itemsToBeCombined = new ArrayList<>();
            for (OrderItem orderItem:orderItems) {
                if (orderItem.getName().equals("German beer") && orderItem.getOrigin().equals("German")) {
                    itemsToBeCombined.add(orderItem);
                }
            }
            orderItems.removeAll(itemsToBeCombined);
            orderItems.add(new OrderItem("German beer", totalGermanBeerInOrder, 0, 0, "German"));
            order.setOrderItems(orderItems);
        }
        if (totalBelgiumBeerInOrder >= 6) {
            List<OrderItem> itemsToBeCombined = new ArrayList<>();
            for (OrderItem orderItem:orderItems) {
                if (orderItem.getName().equals("Belgium beer") && orderItem.getOrigin().equals("Belgium"))
                    itemsToBeCombined.add(orderItem);
            }
            if(itemsToBeCombined.size()>1) {
                orderItems.removeAll(itemsToBeCombined);
                orderItems.add(new OrderItem("Belgium beer", totalBelgiumBeerInOrder, 0, 0, "Belgium"));
                order.setOrderItems(orderItems);
            }
        }
        return order;
    }

}

