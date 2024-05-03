package com.example.online.grocery.repository;

import com.example.online.grocery.entity.Item;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;
    @Test
    void shouldReturnItemByName(){
        Item item=new Item("vegetable",1.0, Item.ProductType.VEGETABLE);
        itemRepository.save(item);
        Optional<Item> actualItemOptional =itemRepository.findByName(item.getName());
        if(actualItemOptional.isPresent()) {
            Item actualItem = actualItemOptional.get();
            Assertions.assertEquals(item.getType(), actualItem.getType());
            Assertions.assertEquals(item.getUnitPrice(), actualItem.getUnitPrice(), 0.001);
            Assertions.assertEquals(item.getName(), actualItem.getName());
        }
    }
    @Test
    void shouldReturnEmptyOptionalIfItemWithGivenNameNotPresent(){
        Item item=new Item("vegetable",1.0, Item.ProductType.VEGETABLE);
        itemRepository.save(item);
        Optional<Item> actualItemOptional =itemRepository.findByName("carrot");
        Assertions.assertFalse(actualItemOptional.isPresent());
    }
}
