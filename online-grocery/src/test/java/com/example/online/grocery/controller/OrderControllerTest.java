package com.example.online.grocery.controller;


import com.example.online.grocery.entity.Order;
import com.example.online.grocery.entity.OrderItem;
import com.example.online.grocery.exceptions.NotValidOrderException;
import com.example.online.grocery.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    OrderService orderService;

    @Autowired
    ObjectMapper objectMapper;
    Order order;

    @Test
    void testGetPrices() throws Exception {
        Map<String, Double> prices = new HashMap<>();
        prices.put("bread", 1.0);
        prices.put("vegetable", 1.0);
        Mockito.when(orderService.getPricesItemWise()).thenReturn(prices);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bread").value(1.0))
                .andExpect(jsonPath("$.vegetable").value(1.0));

    }

    @Test
    void givenNoItemsInDbGetPricesShouldReturnEmpty() throws Exception {
        Mockito.when(orderService.getPricesItemWise()).thenReturn(Collections.emptyMap());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetDiscountRules() throws Exception {
        List<String> discountRules = new ArrayList<>();
        discountRules.add("No discount on bread one day old or newer.On breads 3 days old buy 1 take 2.");
        discountRules.add("5% discount if you buy up to 100g in same order.7% discount if you buy 100 to 500g. 10% discount if you buy more than 500g.");
        discountRules.add(" € 2,00 for each Dutch beer pack.");
        discountRules.add(" € 4,00 for each German beer pack.");
        discountRules.add(" € 3,00 for each Belgium beer pack.");
        Mockito.when(orderService.getAllDiscountRules()).thenReturn(discountRules);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/discount_rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(discountRules.size()))
                .andExpect(jsonPath("$[2]").value(" € 2,00 for each Dutch beer pack."));
    }

    @Test
    void givenNoItemsInDBGetDiscountRulesShouldReturnBlank() throws Exception {
        Mockito.when(orderService.getAllDiscountRules()).thenReturn(Collections.EMPTY_LIST);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/discount_rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenEmptyOrderShouldThrowNotValidOrderException() throws Exception {
        order = new Order(Collections.EMPTY_LIST);
        Mockito.when(orderService.processOrder(order)).thenThrow(NotValidOrderException.class);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/place_order")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk());
    }

    @Test
    void givenValidOrderStatusOKShouldBeReturned() throws Exception {
       List<OrderItem> orderItems=new ArrayList<>();
       orderItems.add(new OrderItem("bread",3,3));
       order=new Order(orderItems);
        String receipt="Order details:\n" +
                "3 x bread (3 days old): €2.00\n" +
                "Total: €2.00";
        Mockito.when(orderService.processOrder(order)).thenReturn(receipt);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/place_order")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk());

    }
}
