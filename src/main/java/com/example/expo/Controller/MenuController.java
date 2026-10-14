package com.example.expo.Controller;

import com.example.expo.DTO.MenuRequest;
import com.example.expo.DTO.MenuItemRequest;
import com.example.expo.Model.Menu;
import com.example.expo.Model.MenuItem;
import com.example.expo.Service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menus")
public class MenuController {
    private final MenuService menuService;
    //عرض قوائم مطعم معيّن
    @GetMapping("/restaurant/{sellerId}")
    public List<Menu> getRestaurantMenus(@PathVariable Integer sellerId) {
        return menuService.publicMenus(sellerId); }
    //عرض أصناف قائمة معيّنة
    @GetMapping("/{menuId}/items")
    public List<MenuItem> getItems(@PathVariable Integer menuId) {
        return menuService.items(menuId); }
    //إنشاء قائمة جديدة للمطعم
    @PostMapping("/restaurant/{sellerId}")
    public Menu createMenu(@PathVariable Integer sellerId, @Valid @RequestBody MenuRequest request) {
        return menuService.createMenu(sellerId, request); }
   //إضافة صنف إلى قائمة المطعم
    @PostMapping("/{menuId}/restaurant/{sellerId}/items")
    public MenuItem createItem(@PathVariable Integer sellerId, @PathVariable Integer menuId, @Valid @RequestBody MenuItemRequest request) {
        return menuService.createItem(sellerId, menuId, request); }
    //تعديل صنف
    @PutMapping("/items/{itemId}/restaurant/{sellerId}")
    public MenuItem updateItem(@PathVariable Integer sellerId, @PathVariable Integer itemId, @Valid @RequestBody MenuItemRequest request) {
        return menuService.updateItem(sellerId, itemId, request); }
    //تعطيل صنف
    @DeleteMapping("/items/{itemId}/restaurant/{sellerId}")
    public void disableItem(@PathVariable Integer sellerId, @PathVariable Integer itemId) {
        menuService.deleteItem(sellerId, itemId); }
}
