package com.example.online.grocery.service;

import com.example.online.grocery.entity.Item;
import com.example.online.grocery.entity.Order;
import com.example.online.grocery.entity.OrderItem;
import com.example.online.grocery.exceptions.ItemNotFoundException;
import com.example.online.grocery.exceptions.NotValidOrderException;
import com.example.online.grocery.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService {

    private final ItemRepository itemRepository;

    public OrderService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public String processOrder(Order order) throws ItemNotFoundException, NotValidOrderException, IllegalArgumentException {
        double totalPrice = 0.0;
        double discount;
        if (order.getOrderItems().isEmpty()) {
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
            switch (validItem.getType()) {
                case BREAD -> discount = calculateBreadDiscount(orderItem);
                case VEGETABLE -> discount = calculateDiscountForVegetable(orderItem);
                case DUTCH_BEER, GERMAN_BEER, BELGIUM_BEER -> discount = calculateDiscountForBeer(orderItem);
                default -> discount = 0.0;
            }
            orderItem.setDiscount(discount);
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
        int packOfSix = orderItem.getQuantity() / 6;
        if (packOfSix <= 0) return 0.0;
        switch (origin) {
            case "Dutch" -> {
                return 2.0 * packOfSix;
            }
            case "German" -> {
                return 4.0 * packOfSix;
            }
            case "Belgium" -> {
                return 3.0 * packOfSix;
            }
            default -> {
                return 0.0;
            }
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
        Map<String, Integer> beerQuantities = new HashMap<>();
        double totalWeightOfVegetablesInOrder = 0.0;

        for (OrderItem orderItem : order.getOrderItems()) {
            switch (orderItem.getName()) {
                case "Dutch beer", "German beer", "Belgium beer" ->
                        beerQuantities.merge(orderItem.getOrigin(), orderItem.getQuantity(), Integer::sum);
                case "vegetable" -> totalWeightOfVegetablesInOrder += orderItem.getWeight();
            }
        }

        combineVegetableItems(order, totalWeightOfVegetablesInOrder);
        combineBeerItems(order, "Dutch", beerQuantities.getOrDefault("Dutch", 0));
        combineBeerItems(order, "German", beerQuantities.getOrDefault("German", 0));
        combineBeerItems(order, "Belgium", beerQuantities.getOrDefault("Belgium", 0));

        return order;
    }

    private void combineVegetableItems(Order order, double totalWeight) {
        if (totalWeight > 0) {
            List<OrderItem> itemsToBeCombined = order.getOrderItems().stream()
                    .filter(orderItem -> orderItem.getName().equals("vegetable")).toList();

            order.getOrderItems().removeAll(itemsToBeCombined);
            order.getOrderItems().add(new OrderItem("vegetable", 0, 0, totalWeight, ""));
        }
    }

    private void combineBeerItems(Order order, String origin, int totalQuantity) {
        if (totalQuantity >= 6) {
            List<OrderItem> itemsToBeCombined = order.getOrderItems().stream()
                    .filter(orderItem -> orderItem.getName().equals(origin + " beer") && orderItem.getOrigin().equals(origin)).toList();

            order.getOrderItems().removeAll(itemsToBeCombined);
            order.getOrderItems().add(new OrderItem(origin + " beer", totalQuantity, 0, 0, origin));
        }
    }

}

