package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.MenuItemRequest;
import com.example.expo.DTO.MenuRequest;
import com.example.expo.Model.Menu;
import com.example.expo.Model.MenuItem;
import com.example.expo.Model.Sellerers;
import com.example.expo.Repository.MenuItemRepository;
import com.example.expo.Repository.MenuRepository;
import com.example.expo.Repository.SellersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final SellersRepository sellersRepository;

    public List<Menu> publicMenus(Integer sellerId) {
        Sellerers seller = seller(sellerId);
        if (!"VERIFIED".equals(seller.getVerification_status()) || !"ACTIVE".equals(seller.getStatus()))
            throw new ApiException("Restaurant is not available");
        return menuRepository.findAllBySeller_Id(sellerId);
    }

    @Transactional
    public Menu createMenu(Integer sellerId, MenuRequest request) {
        Sellerers seller = seller(sellerId);
        requireActiveVerified(seller);
        Menu menu = new Menu(); menu.setName(request.name().trim()); menu.setSeller(seller);
        return menuRepository.save(menu);
    }

    @Transactional
    public MenuItem createItem(Integer sellerId, Integer menuId, MenuItemRequest request) {
        Menu menu = ownedMenu(sellerId, menuId);
        MenuItem item = new MenuItem(); item.setMenu(menu); apply(item, request);
        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem updateItem(Integer sellerId, Integer itemId, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(itemId).orElseThrow(() -> new ApiException("Menu item not found"));
        if (!item.getMenu().getSeller().getId().equals(sellerId)) throw new ApiException("Menu item does not belong to this restaurant");
        apply(item, request); return menuItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Integer sellerId, Integer itemId) {
        MenuItem item = menuItemRepository.findById(itemId).orElseThrow(() -> new ApiException("Menu item not found"));
        if (!item.getMenu().getSeller().getId().equals(sellerId)) throw new ApiException("Menu item does not belong to this restaurant");
        item.setAvailable(false); menuItemRepository.save(item); // soft-disable; preserve order history
    }

    public List<MenuItem> items(Integer menuId) {
        if (!menuRepository.existsById(menuId)) throw new ApiException("Menu not found");
        return menuItemRepository.findAllByMenu_Id(menuId);
    }

    public List<MenuItem> inactiveItems(Integer sellerId, Integer menuId) {
        ownedMenu(sellerId, menuId); // لا نعرض منتجات قائمة تابعة لمؤسسة أخرى.
        return menuItemRepository.findAllByMenu_IdAndAvailableFalse(menuId);
    }

    public List<MenuItem> inactiveSellerItems(Integer sellerId) {
        seller(sellerId); // يشمل كل قوائم المؤسسة، بعد التأكد من وجودها.
        return menuItemRepository.findAllByMenu_Seller_IdAndAvailableFalse(sellerId);
    }

    private void apply(MenuItem item, MenuItemRequest r) {
        item.setName(r.name().trim()); item.setDescription(r.description()); item.setPrice(r.price());
        // نفس شروط اكتمال البيانات في الإضافة والتعديل والاستيراد.
        // الوصف اختياري؛ الاسم المؤقت والسعر صفر يمنعان التفعيل.
        item.setAvailable(!r.name().startsWith("[INCOMPLETE NAME]")
                && r.price().signum() > 0 && (r.available() == null || r.available()));
    }
    private Menu ownedMenu(Integer sellerId, Integer menuId) {
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new ApiException("Menu not found"));
        if (!menu.getSeller().getId().equals(sellerId)) throw new ApiException("Menu does not belong to this restaurant");
        return menu;
    }
    private Sellerers seller(Integer id) { return sellersRepository.findById(id).orElseThrow(() -> new ApiException("Restaurant not found")); }
    private void requireActiveVerified(Sellerers s) { if (!"VERIFIED".equals(s.getVerification_status()) || !"ACTIVE".equals(s.getStatus())) throw new ApiException("Restaurant must be active and verified"); }

    // مسار عرض إضافي؛ لا يغيّر عرض المنيو أو الاستيراد أو غير النشط.
    public List<Menu> getAllMenusForAdmin() { return menuRepository.findAll(); }

}
