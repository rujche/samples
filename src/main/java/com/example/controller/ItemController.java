package com.example.controller;

import com.example.model.Item;
import com.example.service.ItemService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

@Controller("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Post
    public HttpResponse<Item> createItem(@Body Item item) {
        itemService.saveItem(item);
        return HttpResponse.created(item);
    }

    @Get("/{id}")
    public HttpResponse<Item> getItem(@PathVariable String id) {
        return itemService.getItem(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Put("/{id}")
    public HttpResponse<Item> updateItem(@PathVariable String id, @Body Item item) {
        if (!itemService.existsItem(id)) {
            return HttpResponse.notFound();
        }
        item.setId(id);
        itemService.saveItem(item);
        return HttpResponse.ok(item);
    }

    @Delete("/{id}")
    public HttpResponse<Void> deleteItem(@PathVariable String id) {
        if (!itemService.existsItem(id)) {
            return HttpResponse.notFound();
        }
        itemService.deleteItem(id);
        return HttpResponse.noContent();
    }

    @Get("/health")
    public HttpResponse<String> health() {
        return HttpResponse.ok("Service is running");
    }
}
