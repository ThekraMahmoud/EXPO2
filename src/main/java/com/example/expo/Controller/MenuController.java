package com.example.expo.Controller;

import com.example.expo.DTO.MenuRequest;
import com.example.expo.DTO.MenuItemDisplayResponse;
import com.example.expo.DTO.MenuItemRequest;
import com.example.expo.Model.Menu;
import com.example.expo.Model.MenuItem;
import com.example.expo.Service.MenuService;
import com.example.expo.Service.MenuImportService;
import com.example.expo.DTO.MenuImportPreviewResponse;
import com.example.expo.DTO.MenuImportConfirmRequest;
import com.example.expo.DTO.MenuImportedItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menus")
public class MenuController {
    private final MenuService menuService;
    private final MenuImportService menuImportService;

    // قائمة مراجعة للبائع: منها المنتجات التي لم يستطع AI تحديد أسعارها.
    @GetMapping("/{menuId}/restaurant/{sellerId}/items/inactive")
    public List<MenuItem> getInactiveItems(@PathVariable Integer sellerId, @PathVariable Integer menuId) {
        return menuService.inactiveItems(sellerId, menuId);
    }

    @GetMapping("/restaurant/{sellerId}/items/inactive")
    public List<MenuItem> getSellerInactiveItems(@PathVariable Integer sellerId) {
        return menuService.inactiveSellerItems(sellerId);
    }

    // الخطوة الأولى: رفع PDF من الجهاز وإرجاع المعاينة فقط. اسم حقل الملف هو file.
    @PostMapping(value = "/{menuId}/restaurant/{sellerId}/import/preview", consumes = "multipart/form-data")
    public MenuImportPreviewResponse previewImport(@PathVariable Integer sellerId, @PathVariable Integer menuId,
                                                   @RequestPart("file") MultipartFile file) {
        return menuImportService.preview(sellerId, menuId, file);
    }

    // تأكيد كل منتجات آخر معاينة؛ لا يرسل البائع JSON للمنتجات مرة أخرى.
    @PostMapping("/{menuId}/restaurant/{sellerId}/import/confirm")
    public List<MenuImportedItemResponse> confirmImport(@PathVariable Integer sellerId, @PathVariable Integer menuId,
                                                       @RequestBody(required = false) String body) {
        if (body != null && !body.isBlank()) {
            throw new com.example.expo.Api.ApiException("Confirmation uses the latest preview; send this request without a body");
        }
        return menuImportService.confirm(sellerId, menuId);
    }
    //عرض قوائم مطعم معيّن
    @GetMapping("/restaurant/{sellerId}")
    public List<Menu> getRestaurantMenus(@PathVariable Integer sellerId) {
        return menuService.publicMenus(sellerId); }
    //عرض أصناف قائمة معيّنة
    @GetMapping("/{menuId}/items")
    public List<MenuItemDisplayResponse> getItems(@PathVariable Integer menuId) {
        // صيغة العرض منفصلة عن الكيان؛ available يبقى محفوظًا لتحديد حالة المنتج.
        return menuService.items(menuId).stream().map(item -> new MenuItemDisplayResponse(
                item.getName(), item.getDescription(), item.getPrice(), item.getId())).toList(); }
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

    @GetMapping("/admin/all")
    public List<Menu> getAllMenusForAdmin() { return menuService.getAllMenusForAdmin(); }

}
