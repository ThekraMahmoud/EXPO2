package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuServiceTest {
    private final MenuRepository menus = mock(MenuRepository.class);
    private final MenuItemRepository items = mock(MenuItemRepository.class);
    private final SellersRepository sellers = mock(SellersRepository.class);
    private final MenuService service = new MenuService(menus, items, sellers);
    private Menu menu;
    private MenuItem product;

    @BeforeEach void setup() {
        Sellerers seller = new Sellerers(); seller.setId(7); seller.setStatus("ACTIVE"); seller.setVerification_status("VERIFIED");
        menu = new Menu(); menu.setId(3); menu.setSeller(seller);
        product = new MenuItem(); product.setId(8); product.setName("Laptop X"); product.setMenu(menu);
        product.setPrice(BigDecimal.ZERO); product.setAvailable(false);
        when(menus.findById(3)).thenReturn(Optional.of(menu));
        when(items.findById(8)).thenReturn(Optional.of(product));
        when(items.save(any())).thenAnswer(call -> call.getArgument(0));
        when(sellers.findById(7)).thenReturn(Optional.of(seller));
    }

    @Test void sellerCanListInactiveProductsAcrossAllCatalogsOrOneCatalog() {
        when(items.findAllByMenu_IdAndAvailableFalse(3)).thenReturn(List.of(product));
        when(items.findAllByMenu_Seller_IdAndAvailableFalse(7)).thenReturn(List.of(product));
        assertEquals(List.of(product), service.inactiveItems(7, 3));
        assertEquals(List.of(product), service.inactiveSellerItems(7));
    }

    @Test void wrongSellerCannotListOrEditAnotherCatalog() {
        assertThrows(ApiException.class, () -> service.inactiveItems(99, 3));
        assertThrows(ApiException.class, () -> service.updateItem(99, 8, input("100", true)));
        verify(items, never()).findAllByMenu_IdAndAvailableFalse(any());
        verify(items, never()).save(any());
    }

    @Test void editingPositivePriceCanActivateOrKeepProductInactive() {
        assertTrue(service.updateItem(7, 8, input("3299.00", true)).getAvailable());
        assertEquals(new BigDecimal("3299.00"), product.getPrice());
        assertFalse(service.updateItem(7, 8, input("3299.00", false)).getAvailable());
    }

    @Test void zeroPriceCannotBeActivatedDuringCreationOrUpdate() {
        assertFalse(service.updateItem(7, 8, input("0", true)).getAvailable());
        assertFalse(service.createItem(7, 3, input("0", true)).getAvailable());
    }

    @Test void inactiveProductCannotBeAddedToCart() {
        UserRepository users = mock(UserRepository.class); User user = new User(); user.setId(1);
        when(users.findById(1)).thenReturn(Optional.of(user));
        CartRepository carts = mock(CartRepository.class);
        var cartService = new CartService(carts, mock(CartItemRepository.class), users, sellers, items);
        assertThrows(ApiException.class, () -> cartService.addItem(1, 7, new CartItemRequest(8, 1)));
        verifyNoInteractions(carts);
    }

    @Test void optionalDescriptionAllowsActivationButTemporaryNameDoesNot() {
        assertTrue(service.updateItem(7, 8, new MenuItemRequest("Laptop", "  ", BigDecimal.TEN, true)).getAvailable());
        assertTrue(service.createItem(7, 3, new MenuItemRequest("Laptop", null, BigDecimal.TEN, true)).getAvailable());
        assertFalse(service.updateItem(7, 8, new MenuItemRequest("[INCOMPLETE NAME] test", "Specs", BigDecimal.TEN, true)).getAvailable());
        assertTrue(service.updateItem(7, 8, new MenuItemRequest("Laptop", "Specs", BigDecimal.TEN, true)).getAvailable());
    }

    private MenuItemRequest input(String price, Boolean available) {
        return new MenuItemRequest("Laptop X", "16GB RAM", new BigDecimal(price), available);
    }
}
