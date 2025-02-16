//package com.project.orm.controller;
//
//import com.project.orm.service.RedisService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/redis")
//public class RedisController {
//
//    private final RedisService redisService;
//
//    public RedisController(RedisService redisService) {
//        this.redisService = redisService;
//    }
//
//    @PostMapping("/save")
//    public String saveData(@RequestParam String key, @RequestParam String value) {
//        redisService.saveData(key, value, 10); // Save for 10 minutes
//        return "Data saved in Redis!";
//    }
//
//    @GetMapping("/get")
//    public String getData(@RequestParam String key) {
//        String value = redisService.getData(key);
//        return value != null ? value : "Key not found!";
//    }
//
//    @DeleteMapping("/delete")
//    public String deleteData(@RequestParam String key) {
//        redisService.deleteData(key);
//        return "Key deleted from Redis!";
//    }
//}
