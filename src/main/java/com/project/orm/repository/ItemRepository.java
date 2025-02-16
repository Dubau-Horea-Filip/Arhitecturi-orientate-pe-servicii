package com.project.orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.orm.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
}