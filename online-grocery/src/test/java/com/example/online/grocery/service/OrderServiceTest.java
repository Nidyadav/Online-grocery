package com.example.online.grocery.service;

import com.example.online.grocery.entity.Item;
import com.example.online.grocery.entity.Order;
import com.example.online.grocery.entity.OrderItem;
import com.example.online.grocery.exceptions.NotValidOrderException;
import com.example.online.grocery.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderServiceTest {
    ItemRepository itemRepository;
    Item bread;
    Item vegetable;
    Item dutch_Beer;
    Item german_Beer;
    Item belgium_Beer;
    OrderItem orderItem1 = Mockito.mock(OrderItem.class);
    OrderItem orderItem2 = Mockito.mock(OrderItem.class);
    OrderItem orderItem3 = Mockito.mock(OrderItem.class);
    List<OrderItem> orderItems = new ArrayList<>();
    OrderService orderService;
    Order order;
   List<Item> items;
    Map<String,Double> pricePerItem;
    @Nested
    class TestProcessOrder {
        @BeforeEach
        void setup() {
            itemRepository = Mockito.mock(ItemRepository.class);
            bread = new Item("bread", 1.0, Item.ProductType.BREAD);
            vegetable = new Item("vegetable", 1.0, Item.ProductType.VEGETABLE);
            dutch_Beer = new Item("Dutch beer", 0.50, Item.ProductType.DUTCH_BEER);
            german_Beer = new Item("German beer", 1.0, Item.ProductType.GERMAN_BEER);
            belgium_Beer = new Item("Belgium beer", 0.75, Item.ProductType.BELGIUM_BEER);
            orderService = new OrderService(itemRepository);
            order = new Order(orderItems);
        }

        @Test
        void givenEmptyOrderReceiptShouldThrowNotValidOrderException() {

            assertThrows(NotValidOrderException.class, () -> orderService.processOrder(order));
        }

        @Test
        void givenBreadOneDayOldNoDiscountShouldBeApplied() {
            OrderItem oneDayOldBread = new OrderItem("bread", 3, 1);
            orderItems.add(oneDayOldBread);
            Mockito.when(itemRepository.findByName("bread")).thenReturn(Optional.of(bread));

            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    3 x bread (1 days old): €3.00
                    Total: €3.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }
        @Test
        void givenNewBreadNoDiscountShouldBeApplied() {
            OrderItem newBread = new OrderItem("bread", 3, 0);
            orderItems.add(newBread);
            Mockito.when(itemRepository.findByName("bread")).thenReturn(Optional.of(bread));

            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    3 x bread (0 days old): €3.00
                    Total: €3.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }

        @Test
        void shouldThrowExceptionIfBreadOlderThanSixDaysAdded() {
            OrderItem breadOldThanSixDays = new OrderItem("bread", 5, 7);
            orderItems.add(breadOldThanSixDays);
            Mockito.when(itemRepository.findByName("bread")).thenReturn(Optional.of(bread));
            assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(order));
        }

        @Test
        void givenOrderWithFiveBreadsSixDaysOldAndTwoBreadsWithThreeDaysOld() {
            OrderItem sixDaysOldBread = new OrderItem("bread", 5, 6);
            OrderItem threeDaysOldBread = new OrderItem("bread", 2, 3);
            orderItems.add(sixDaysOldBread);
            orderItems.add(threeDaysOldBread);
            Mockito.when(itemRepository.findByName(sixDaysOldBread.getName())).thenReturn(Optional.of(bread));
            Mockito.when(itemRepository.findByName(threeDaysOldBread.getName())).thenReturn(Optional.of(bread));

            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    5 x bread (6 days old): €3.00
                    2 x bread (3 days old): €1.00
                    Total: €4.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }

        @Test
        void givenOrderWithSixDutchBeer200GramVegetableAndThreeBreadThreeDaysOldShouldReturnCorrectTotalPrice() {

            OrderItem threeDaysOldBread = new OrderItem("bread", 3, 3);
            OrderItem twoHundredGmVegetable = new OrderItem("vegetable", 200.0);
            OrderItem dutchBeer = new OrderItem("Dutch beer", 6, "Dutch");
            orderItems.add(threeDaysOldBread);
            orderItems.add(twoHundredGmVegetable);
            orderItems.add(dutchBeer);
            Mockito.when(itemRepository.findByName(threeDaysOldBread.getName())).thenReturn(Optional.of(bread));
            Mockito.when(itemRepository.findByName(twoHundredGmVegetable.getName())).thenReturn(Optional.of(vegetable));
            Mockito.when(itemRepository.findByName(dutchBeer.getName())).thenReturn(Optional.of(dutch_Beer));
            String expectedReceipt = """
                    Order details:
                    3 x bread (3 days old): €2.00
                    200.0 g x vegetable: €1.86
                    6 x Dutch beer: €1.00
                    Total: €4.86
                    """;
            String actualReceipt = orderService.processOrder(order);
            assertEquals(expectedReceipt, actualReceipt);
        }

        @Test
        void givenOrderWithSixGermanBeerAndThreeBreadSixDaysOld() {
            OrderItem breadSixDaysOld = new OrderItem("bread", 3, 6);
            orderItems.add(breadSixDaysOld);
            OrderItem germanBeer = new OrderItem("German beer", 6, "German");
            orderItems.add(germanBeer);
            Mockito.when(itemRepository.findByName("German beer")).thenReturn(Optional.of(german_Beer));
            Mockito.when(itemRepository.findByName("bread")).thenReturn(Optional.of(bread));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    3 x bread (6 days old): €1.00
                    6 x German beer: €2.00
                    Total: €3.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }
        @Test
        void givenOrderWithSevenGermanBeersItemsDiscountShouldApply() {
            OrderItem germanBeer = new OrderItem("German beer", 7, "German");
            orderItems.add(germanBeer);
            Mockito.when(itemRepository.findByName(germanBeer.getName())).thenReturn(Optional.of(german_Beer));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    7 x German beer: €3.00
                    Total: €3.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }
        @Test
        void givenOrderWithTwelveBelgiumBeersItemsDiscountShouldApply() {
            OrderItem belgiumBeer = new OrderItem("Belgium beer", 12, "Belgium");
            orderItems.add(belgiumBeer);
            Mockito.when(itemRepository.findByName(belgiumBeer.getName())).thenReturn(Optional.of(belgium_Beer));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    12 x Belgium beer: €3.00
                    Total: €3.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }
        @Test
        void givenOrderWithSixDutchBeersAsSeparateOrderItemsDiscountShouldApply() {
            Mockito.when(orderItem1.getName()).thenReturn("Dutch beer");
            Mockito.when(orderItem1.getQuantity()).thenReturn(1);
            Mockito.when(orderItem1.getOrigin()).thenReturn("Dutch");
            int i = 0;
            while (i < 6) {
                orderItems.add(orderItem1);
                i++;
            }
            Mockito.when(itemRepository.findByName(orderItem1.getName())).thenReturn(Optional.of(dutch_Beer));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    6 x Dutch beer: €1.00
                    Total: €1.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }

        @Test
        void givenOrderWithMoreThanSixDutchBeersAsSeparateOrderItemsDiscountShouldApply() {
            Mockito.when(orderItem1.getName()).thenReturn("Dutch beer");
            Mockito.when(orderItem1.getQuantity()).thenReturn(1);
            Mockito.when(orderItem1.getOrigin()).thenReturn("Dutch");
            int i = 0;
            while (i < 8) {
                orderItems.add(orderItem1);
                i++;
            }
            Mockito.when(itemRepository.findByName(orderItem1.getName())).thenReturn(Optional.of(dutch_Beer));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    8 x Dutch beer: €2.00
                    Total: €2.00
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }

        @Test
        void givenOrderWithThreeGermanBeersAnd3BelgiumBeersAsSeparateOrderItemsDiscountShouldNotApply() {
            OrderItem germanBeer = new OrderItem("German beer", 3, "German");
            OrderItem belgiumBeer = new OrderItem("Belgium beer", 3, "Belgium");
            orderItems.add(germanBeer);
            orderItems.add(belgiumBeer);
            Mockito.when(itemRepository.findByName(germanBeer.getName())).thenReturn(Optional.of(german_Beer));
            Mockito.when(itemRepository.findByName(belgiumBeer.getName())).thenReturn(Optional.of(belgium_Beer));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    3 x German beer: €3.00
                    3 x Belgium beer: €2.25
                    Total: €5.25
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }
        @Test
        void givenOrderWithThreeBelgiumBeersTwiceAsSeparateOrderItemsDiscountShouldApply() {
            OrderItem belgiumBeer = new OrderItem("Belgium beer", 3, "Belgium");
            OrderItem belgiumBeer_again = new OrderItem("Belgium beer", 3, "Belgium");
            orderItems.add(belgiumBeer_again);
            orderItems.add(belgiumBeer);
            Mockito.when(itemRepository.findByName(belgiumBeer_again.getName())).thenReturn(Optional.of(belgium_Beer));
            Mockito.when(itemRepository.findByName(belgiumBeer.getName())).thenReturn(Optional.of(belgium_Beer));
            String actualReceipt = orderService.processOrder(order);

            String expectedReceipt = """
                    Order details:
                    6 x Belgium beer: €1.50
                    Total: €1.50
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }

        @Test
        void givenOrderWithVegetablesWeightTill500gmShouldApplyDiscountSevenPercent() {
            Mockito.when(orderItem2.getName()).thenReturn("vegetable");
            Mockito.when(orderItem2.getWeight()).thenReturn(300.0);
            Mockito.when(orderItem2.getPrice()).thenReturn(1.0);
            Mockito.when(orderItem3.getName()).thenReturn("vegetable");
            Mockito.when(orderItem3.getWeight()).thenReturn(200.0);
            Mockito.when(orderItem3.getPrice()).thenReturn(1.0);
            orderItems.add(orderItem2);
            orderItems.add(orderItem3);
            Mockito.when(itemRepository.findByName(orderItem2.getName())).thenReturn(Optional.of(vegetable));
            Mockito.when(itemRepository.findByName(orderItem3.getName())).thenReturn(Optional.of(vegetable));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    500.0 g x vegetable: €4.65
                    Total: €4.65
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }
        @Test
        void givenOrderWithVegetablesAsSeparateItemsShouldApplyDiscountOnTotalWeight() {
            Mockito.when(orderItem2.getName()).thenReturn("vegetable");
            Mockito.when(orderItem2.getWeight()).thenReturn(300.0);
            Mockito.when(orderItem2.getPrice()).thenReturn(1.0);
            Mockito.when(orderItem3.getName()).thenReturn("vegetable");
            Mockito.when(orderItem3.getWeight()).thenReturn(300.0);
            Mockito.when(orderItem3.getPrice()).thenReturn(1.0);
            orderItems.add(orderItem2);
            orderItems.add(orderItem3);
            Mockito.when(itemRepository.findByName(orderItem2.getName())).thenReturn(Optional.of(vegetable));
            Mockito.when(itemRepository.findByName(orderItem3.getName())).thenReturn(Optional.of(vegetable));
            String actualReceipt = orderService.processOrder(order);
            String expectedReceipt = """
                    Order details:
                    600.0 g x vegetable: €5.40
                    Total: €5.40
                    """;
            assertEquals(expectedReceipt, actualReceipt);
        }

        @Test
        void givenOrderWithFiveBelgiumBeer90GramVegetableAndSixBreadSixDaysOldShouldReturnCorrectReceipt() {

            OrderItem sixDaysOldBread = new OrderItem("bread", 6, 6);
            OrderItem sevenHundredGmVegetable = new OrderItem("vegetable", 90.0);
            OrderItem belgiumBeer = new OrderItem("Belgium beer", 5, "Belgium");
            orderItems.add(sixDaysOldBread);
            orderItems.add(sevenHundredGmVegetable);
            orderItems.add(belgiumBeer);
            Mockito.when(itemRepository.findByName(sixDaysOldBread.getName())).thenReturn(Optional.of(bread));
            Mockito.when(itemRepository.findByName(sevenHundredGmVegetable.getName())).thenReturn(Optional.of(vegetable));
            Mockito.when(itemRepository.findByName(belgiumBeer.getName())).thenReturn(Optional.of(belgium_Beer));
            String expectedReceipt = """
                    Order details:
                    6 x bread (6 days old): €2.00
                    5 x Belgium beer: €3.75
                    90.0 g x vegetable: €0.86
                    Total: €6.61
                    """;
            String actualReceipt = orderService.processOrder(order);
            assertEquals(expectedReceipt, actualReceipt);
        }


    }
    @Nested
    class TestGetAllDiscountRulesAndPrices{
        @BeforeEach
        void setup(){
           items=new ArrayList<>();
           items.add(new Item("bread", Item.ProductType.BREAD,1.0,"No discount on bread one day old or newer.On breads 3 days old buy 1 take 2."));
           items.add(new Item("vegetable", Item.ProductType.VEGETABLE,1.0,"5% discount if you buy up to 100g in same order.7% discount if you buy 100 to 500g. 10% discount if you buy more than 500g."));
           items.add(new Item("Dutch beer", Item.ProductType.DUTCH_BEER,0.50," € 2,00 for each Dutch beer pack."));
           items.add(new Item("German beer", Item.ProductType.GERMAN_BEER,1.0," € 4,00 for each German beer pack."));
           items.add(new Item("Belgium beer", Item.ProductType.BELGIUM_BEER,0.75," € 3,00 for each Belgium beer pack."));
            itemRepository = Mockito.mock(ItemRepository.class);
            orderService = new OrderService(itemRepository);
            pricePerItem=new HashMap<>();
        }
        @Test
        void shouldReturnEmptyListWhenNoDiscountPresent(){

            Mockito.when(itemRepository.findAll()).thenReturn(Collections.emptyList());
            List<String> discountRules=orderService.getAllDiscountRules();
            assertEquals(0,discountRules.size());
        }
        @Test
        void shouldReturnDiscountsWhenItemsHaveDiscounts(){
            Mockito.when(itemRepository.findAll()).thenReturn(items);
            List<String> discountRules=orderService.getAllDiscountRules();
            assertEquals(items.size(),discountRules.size());
            assertEquals(items.get(2).getDiscountRule(),discountRules.get(2));
            assertEquals(items.get(items.size()-1).getDiscountRule(),discountRules.get(discountRules.size()-1));
        }
        @Test
        void shouldReturnEmptyMapWhenNoItemPresent(){
            Mockito.when(itemRepository.findAll()).thenReturn( Collections.EMPTY_LIST);
            pricePerItem=orderService.getPricesItemWise();
            assertEquals(0,pricePerItem.size());
        }
        @Test
        void shouldReturnPricesWhenItemsPresent(){
            Mockito.when(itemRepository.findAll()).thenReturn(items);
            pricePerItem=orderService.getPricesItemWise();
            assertEquals(items.size(),pricePerItem.size());
            assertEquals(items.get(0).getUnitPrice(),pricePerItem.get("bread"));
        }
    }
}
