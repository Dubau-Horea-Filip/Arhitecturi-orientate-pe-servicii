package com.project.orm.service;
import com.project.orm.model.Item;
import com.project.orm.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Transactional
@Service
public class ItemService {
    @Autowired
    public ItemRepository itemRepository;

    public boolean existsById(int itemId) {
        return itemRepository.existsById(itemId);
    }

    public Item insertItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItem(int id)
    {
        itemRepository.deleteById(id);
    }

    public List<Item> getAllItems()
    {
        return itemRepository.findAll();
    }

    public Page<Item> getItems(int page, int pageSize, String sortBy) {
        // Validate sort field (to avoid errors)
        List<String> validFields = Arrays.asList("itemId", "itemName", "itemSellingPrice", "itemEnteredDate");
        if (!validFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(sortBy));
        return itemRepository.findAll(pageRequest);
    }

}