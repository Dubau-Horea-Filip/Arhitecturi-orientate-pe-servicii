package com.project.orm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.orm.model.Item;
import com.project.orm.repository.ItemRepository;
import com.jayway.jsonpath.JsonPath;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class SpringBootORMTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ItemRepository repository;

    private void clearDB() {
        repository.deleteAll();
    }

    @BeforeEach
    public void setup() throws Exception {
        clearDB();

        List<String> rows = Files.lines(Paths.get(ClassLoader.getSystemResource("testData.txt")
                .toURI())).collect(Collectors.toList());
        for (String row : rows) {
            // convert JSON string to Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Double> map = mapper.readValue(row, Map.class);

            mockMvc.perform(post("/app/item")
                    .contentType("application/json")
                    .content(row))
                    .andDo(print())
                    .andExpect(jsonPath("$.itemId", greaterThan(0)))
                    .andExpect(jsonPath("$.itemEnteredDate", is(notNullValue())))
                    .andExpect(jsonPath("$.itemSellingPrice", is(map.get("itemSellingPrice")))) // selling price
                    .andExpect(jsonPath("$.itemLastModifiedDate", is(notNullValue())))
                    .andExpect(jsonPath("$.itemLastModifiedByUser", is(nullValue())))
                    .andExpect(jsonPath("$.itemStatus", is("AVAILABLE")))
                    .andExpect(jsonPath("$.*", hasSize(9)))
                    .andExpect(status().isCreated());
        }
    }

    @Test // 1
    public void testInsert() throws Exception {
        String item = "{\n" +
                "\t\"itemName\": \"item_x\",\n" +
                "\t\"itemEnteredByUser\": \"user_x\",\n" +
                "\t\"itemBuyingPrice\": 50.0,\n" +
                "\t\"itemSellingPrice\": 55.0\n" +
                "}";

        MvcResult result = mockMvc.perform(post("/app/item")
                .contentType("application/json")
                .content(item))
                .andDo(print())
                .andExpect(jsonPath("$.itemId", greaterThan(0)))
                .andExpect(jsonPath("$.itemEnteredDate", is(notNullValue())))
                .andExpect(jsonPath("$.itemSellingPrice", is(55.0))) // selling price
                .andExpect(jsonPath("$.itemLastModifiedByUser", is(nullValue())))
                .andExpect(jsonPath("$.itemLastModifiedDate", is(notNullValue())))
                .andExpect(jsonPath("$.itemStatus", is("AVAILABLE")))
                .andExpect(jsonPath("$.*", hasSize(9)))
                .andExpect(status().isCreated())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.itemId");

        List<Item> itemsFromDb = repository.findAll();

        assertEquals(itemsFromDb.size(), 5);

        String itemExisting = "{\n" +
                "\t\"itemId\":" + id + ",\n" +
                "\t\"itemName\": \"item_x\",\n" +
                "\t\"itemEnteredByUser\": \"user_x\",\n" +
                "\t\"itemBuyingPrice\": 50.0,\n" +
                "\t\"itemSellingPrice\": 55.0\n" +
                "}";

        mockMvc.perform(post("/app/item")
                .contentType("application/json")
                .content(itemExisting))
                .andDo(print())
                .andExpect(status().isBadRequest());


        itemsFromDb = repository.findAll();

        assertEquals(itemsFromDb.size(), 5);
    }


    @Test // 2
    public void testDeleteById() throws Exception {
        mockMvc.perform(delete("/app/item/" + -1)
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound());

        String item = "{\n" +
                "\t\"itemName\": \"item_x\",\n" +
                "\t\"itemEnteredByUser\": \"user_x\",\n" +
                "\t\"itemBuyingPrice\": 50.0,\n" +
                "\t\"itemSellingPrice\": 55.0\n" +
                "}";

        MvcResult result = mockMvc.perform(post("/app/item")
                .contentType("application/json")
                .content(item))
                .andDo(print())
                .andExpect(jsonPath("$.itemId", greaterThan(0)))
                .andExpect(jsonPath("$.itemEnteredDate", is(notNullValue())))
                .andExpect(jsonPath("$.itemSellingPrice", is(55.0))) // selling price
                .andExpect(jsonPath("$.itemLastModifiedByUser", is(nullValue())))
                .andExpect(jsonPath("$.itemLastModifiedDate", is(notNullValue())))
                .andExpect(jsonPath("$.itemStatus", is("AVAILABLE")))
                .andExpect(jsonPath("$.*", hasSize(9)))
                .andExpect(status().isCreated())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        Integer id = JsonPath.parse(response).read("$.itemId");

        List<Item> itemsFromDb = repository.findAll();
        assertEquals(itemsFromDb.size(), 5);

        mockMvc.perform(delete("/app/item/" + id)
                .contentType("application/json"))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().isNoContent());

        Optional<Item> notToBeFoundItem = repository.findById(id);
        assertEquals(notToBeFoundItem.isEmpty(), Boolean.TRUE);
    }

    @Test // 3
    public void testGetPaginationAndSorting() throws Exception {
        mockMvc.perform(get("/app/item")
                .param("pageSize", "2")
                .param("page", "1")
                .param("sortBy", "itemName"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].itemName", is("item3")))
                .andExpect(jsonPath("$.[1].itemEnteredByUser", is("user2")))
                .andExpect(status().isOk());
    }
}
