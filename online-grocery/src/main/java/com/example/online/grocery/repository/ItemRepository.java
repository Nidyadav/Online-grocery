package com.example.online.grocery.repository;

import com.example.online.grocery.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,Integer> {
    Optional<Item> findByName(String name);
}
