package com.project.orm.controller;

import com.project.orm.Authentification.JwtUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.orm.model.Item;
import com.project.orm.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.*;
import java.util.List;

@RestController
@Tag(name = "Item Controller", description = "API for fetching item details")
public class ItemController {
    @Autowired
    private ItemService itemService;



//    private final JwtUtil jwtUtil;
//
//    // Constructor Injection of JwtUtil
//    public ItemController(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }


//    @PostMapping("/item")
//    public ResponseEntity<?> insertItem(@RequestBody Item item, @RequestHeader("Authorization") String authHeader) {
//        String token = authHeader.substring(7); // Remove "Bearer " prefix
//        if (!jwtUtil.validateToken(token)) {
//            return ResponseEntity.status(401).body("Unauthorized");
//        }
//
//        if (itemService.existsById(item.getItemId())) {
//            return ResponseEntity.status(400).body("Item already exists");
//        }
//
//        Item savedItem = itemService.insertItem(item);
//        return ResponseEntity.status(201).body(savedItem);
//    }


    // 1. insert POST
    @PostMapping("/app/item")
    @Operation(summary = "insert item", description = "")
    public ResponseEntity<?> insertItem(@RequestBody Item item) {
        //should accept POST requests at /app/item and item data as a JSON body
        //if the itemId exists in the database, then it should return status code 400
        //If the itemId doesn't exist in the database, then it should insert the data and return the inserted item as
        // a response with status code 201
        {
            if (itemService.existsById(item.getItemId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item ID already exists.");
            }
            Item savedItem = itemService.insertItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        }
    }

    @Hidden
    // 2. delete by itemId DELETE
    //should accept DELETE requests at /app/item/{itemId} where itemId is a path variable
    //if the itemId exists in the database, then it should delete the item with status code 204
    //if the itemId doesn't exist in the database, it should return status code 404
    @DeleteMapping("/app/item/{itemId}")
    public ResponseEntity<?> deleteItemById(@PathVariable int itemId) {
        if (itemService.existsById(itemId)) {
            itemService.deleteItem(itemId);
            return ResponseEntity.noContent().build(); // Returns 204 No Content
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
    }

    // New DELETE endpoint for deleting an item by its ID
    @Operation(summary = "delete item, require authentification", description = "auth")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItemByIdAuth(@PathVariable int itemId) {
        if (itemService.existsById(itemId)) {
            itemService.deleteItem(itemId);
            return ResponseEntity.noContent().build(); // 204 No Content (successful delete, no return body)
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found (item doesn't exist)
        }
    }

    @Operation(summary = "get all item", description = "")
    @GetMapping("/app/item/all")
    public ResponseEntity<?> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    // 3. select all with sorting and pagination ?pageSize={pageSize}&page={page}&sortBy={sortBy} GET
    //should accept GET requests at /app/item?pageSize={pageSize}&page={page}&sortBy={sortByField}
    //should return the requested page by paginating with pageSize and sorting by the sortBy field
    @GetMapping("/app/item")
    public ResponseEntity<List<Item>> getItems(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam String sortBy) {

        Page<Item> pagedItems = itemService.getItems(page, pageSize, sortBy);
        List<Item> items = pagedItems.getContent(); // Extract only the content list

        return ResponseEntity.ok(items);
    }


}