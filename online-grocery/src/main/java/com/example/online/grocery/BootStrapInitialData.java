package com.example.online.grocery;

import com.example.online.grocery.entity.Item;
import com.example.online.grocery.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BootStrapInitialData implements CommandLineRunner {
    private  final ItemRepository itemRepository;

    public BootStrapInitialData(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args)  {
        Item bread=new Item(1,"bread", Item.ProductType.BREAD,1.0,"No discount on bread one day old or newer.On breads 3 days old buy 1 take 2.");
        Item vegetable=new Item(2,"vegetable", Item.ProductType.VEGETABLE,1.0,"5% discount if you buy up to 100g in same order.7% discount if you buy 100 to 500g. 10% discount if you buy more than 500g.");
       // Item beer=new Item(3,"beer", Item.ProductType.BEER,0.50,"€ 3,00 for each Belgium beer pack of 6. € 2,00 for each Dutch beer pack.€ 4,00 for each German beer pack.");
        Item dutchBeer=new Item(4,"Dutch beer", Item.ProductType.DUTCH_BEER,0.50," € 2,00 for each Dutch beer pack.");
        Item germanBeer=new Item(5,"German beer", Item.ProductType.GERMAN_BEER,1.0," € 4,00 for each German beer pack.");
        Item belgiumBeer=new Item(6,"Belgium beer", Item.ProductType.BELGIUM_BEER,0.75," € 3,00 for each Belgium beer pack.");
        itemRepository.save(bread);
        itemRepository.save(vegetable);
        //itemRepository.save(beer);
        itemRepository.save(dutchBeer);
        itemRepository.save(germanBeer);
        itemRepository.save(belgiumBeer);
        System.out.println("****added Items****");
    }
}
