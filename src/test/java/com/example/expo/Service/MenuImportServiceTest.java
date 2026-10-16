package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Client.AiMenuClient;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import org.springframework.mock.web.MockMultipartFile;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuImportServiceTest {
    private MenuRepository menus;
    private MenuItemRepository items;
    private PdfUploadService uploader;
    private AiMenuClient ai;
    private MenuImportService service;
    private Menu menu;
    private ValidatorFactory factory;

    @BeforeEach void setup() {
        menus = mock(MenuRepository.class); items = mock(MenuItemRepository.class);
        uploader = mock(PdfUploadService.class); ai = mock(AiMenuClient.class);
        factory = Validation.buildDefaultValidatorFactory();
        service = new MenuImportService(menus, items, uploader, ai, factory.getValidator());
        Sellerers seller = new Sellerers(); seller.setId(7); seller.setStatus("ACTIVE"); seller.setVerification_status("VERIFIED");
        menu = new Menu(); menu.setId(3); menu.setSeller(seller);
        when(menus.findById(3)).thenReturn(Optional.of(menu));
        when(menus.findByIdForImport(3)).thenReturn(Optional.of(menu));
    }
    @AfterEach void close() { factory.close(); }

    @Test void previewKeepsUnknownPriceWithoutSavingAndLimitsRepeatedRequests() {
        byte[] pdf = {1, 2}; var request = new MockMultipartFile("file", "menu.pdf", "application/pdf", pdf); when(uploader.readPdf(request)).thenReturn(pdf);
        when(ai.analyzePdf(pdf)).thenReturn(new MenuImportPreviewResponse(true, "SAR",
                List.of(new MenuImportPreviewResponse.Item("لاتيه", null, null)), List.of()));
        var result = service.preview(7, 3, request);
        assertEquals(BigDecimal.ZERO, result.items().get(0).price()); assertFalse(result.items().get(0).available());
        assertFalse(result.warnings().isEmpty());
        verifyNoInteractions(items);
        assertThrows(ApiException.class, () -> service.preview(7, 3, request));
        verify(ai, times(1)).analyzePdf(pdf);
    }

    @Test void wrongSellerCannotAnalyzeOrSave() {
        assertThrows(ApiException.class, () -> service.preview(99, 3, new MockMultipartFile("file", "menu.pdf", "application/pdf", new byte[]{1})));
        assertThrows(ApiException.class, () -> service.confirmItems(99, 3, request("Tea", "10.00")));
        verifyNoInteractions(items, uploader, ai);
    }

    @Test void inactiveSellerCannotImport() {
        menu.getSeller().setStatus("INACTIVE");
        assertThrows(ApiException.class, () -> service.confirmItems(7, 3, request("Tea", "10.00")));
        verifyNoInteractions(items);
    }

    @Test void confirmAddsNormalItemsAndLeavesExistingOrderReferencesAlone() {
        MenuItem old = new MenuItem(); old.setId(2); old.setName("Coffee"); old.setPrice(new BigDecimal("5.00"));
        OrderItem historical = new OrderItem(); historical.setMenuItem(old); historical.setItemName("Coffee"); historical.setUnitPrice(old.getPrice());
        when(items.findAllByMenu_Id(3)).thenReturn(List.of(old));
        when(items.saveAll(anyList())).thenAnswer(call -> {
            List<MenuItem> saved = call.getArgument(0); saved.get(0).setId(8); return saved;
        });
        var result = service.confirmItems(7, 3, request(" Tea ", "10.00"));
        assertEquals("Tea", result.get(0).name()); assertTrue(result.get(0).available());
        assertSame(old, historical.getMenuItem()); assertEquals(new BigDecimal("5.00"), historical.getUnitPrice());
        verify(items, never()).delete(any()); verify(items, never()).save(any());
    }

    @Test void skipsExistingDuplicateIncludingDisabledItemWithoutChangingIt() {
        MenuItem old = new MenuItem(); old.setName("Tea"); old.setAvailable(false); old.setPrice(BigDecimal.ONE);
        when(items.findAllByMenu_Id(3)).thenReturn(List.of(old));
        doAnswer(call -> call.getArgument(0)).when(items).saveAll(anyList());
        var result = service.confirmItems(7, 3, new MenuImportConfirmRequest(List.of(
                item(" tea ", "10"), item("Coffee", "15"),
                new MenuImportItemRequest("Book", null, null, true))));
        assertEquals(2, result.size()); assertEquals("Coffee", result.get(0).name());
        assertTrue(result.get(0).available()); assertFalse(result.get(1).available());
        assertEquals(BigDecimal.ZERO, result.get(1).price());
        assertEquals(BigDecimal.ONE, old.getPrice()); assertFalse(old.getAvailable());
        verify(items, never()).save(any());
    }

    @Test void keepsFirstOccurrenceAndSkipsDuplicateInsidePreviewBatch() {
        doAnswer(call -> call.getArgument(0)).when(items).saveAll(anyList());
        var request = new MenuImportConfirmRequest(List.of(item("Tea", "10"), item(" tea ", "15"), item("Coffee", "20")));
        var result = service.confirmItems(7, 3, request);
        assertEquals(2, result.size()); assertEquals("Tea", result.get(0).name());
        assertEquals(new BigDecimal("10"), result.get(0).price());
        assertEquals("Coffee", result.get(1).name());
    }

    @Test void allDuplicatesReturnEmptyListAndConsumePreview() {
        preparePreview();
        var existing = java.util.stream.Stream.of(" tea ", "BOOK", "Laptop").map(name -> {
            MenuItem old = new MenuItem(); old.setName(name); return old;
        }).toList();
        when(items.findAllByMenu_Id(3)).thenReturn(existing);
        assertTrue(service.confirm(7, 3).isEmpty());
        verify(items, never()).saveAll(anyList());
        assertThrows(ApiException.class, () -> service.confirm(7, 3));
    }

    @Test void rejectMissingPriceEmptyBatchAndNullElement() {
        assertThrows(ApiException.class, () -> service.confirmItems(7, 3, new MenuImportConfirmRequest(List.of())));
        assertThrows(ApiException.class, () -> service.confirmItems(7, 3, new MenuImportConfirmRequest(Arrays.asList((MenuImportItemRequest) null))));
        verify(items, never()).saveAll(any());
    }

    @Test void rejectOutOfRangeAndOverPrecisePrices() {
        for (String price : List.of("-1", "1.001", "100000000.00")) {
            assertThrows(ApiException.class, () -> service.confirmItems(7, 3, request("Tea", price)));
        }
        verify(items, never()).saveAll(any());
    }

    @Test void confirmsMixedProductCategoriesAndPreservesVariantNames() {
        doAnswer(call -> call.getArgument(0)).when(items).saveAll(anyList());
        var products = new MenuImportConfirmRequest(List.of(
                item("كتاب رحلة - الطبعة الثانية", "49.95"),
                item("Laptop X 16GB / 512GB", "3299"),
                item("Laptop X 32GB / 1TB", "4299"),
                new MenuImportItemRequest("قميص مقاس L", "قطن", new BigDecimal("99"), false)));
        var result = service.confirmItems(7, 3, products);
        assertEquals(4, result.size());
        assertEquals("كتاب رحلة - الطبعة الثانية", result.get(0).name());
        assertEquals("Laptop X 32GB / 1TB", result.get(2).name());
        assertEquals(new BigDecimal("3299"), result.get(1).price());
        assertFalse(result.get(3).available());
    }

    @Test void catalogWithUnclearDevicePriceRemainsPreviewOnly() {
        byte[] pdf = {1, 2}; var file = new MockMultipartFile("file", "devices.pdf", "application/pdf", pdf);
        when(uploader.readPdf(file)).thenReturn(pdf);
        when(ai.analyzePdf(pdf)).thenReturn(new MenuImportPreviewResponse(true, "SAR",
                List.of(new MenuImportPreviewResponse.Item("Laptop X", "قسط شهري 150 ريال؛ السعر الكامل غير واضح", null)), List.of()));
        var result = service.preview(7, 3, file);
        assertTrue(result.isMenu()); assertEquals(BigDecimal.ZERO, result.items().get(0).price());
        assertFalse(result.items().get(0).available());
        assertFalse(result.warnings().isEmpty()); verifyNoInteractions(items);
    }

    @Test void unrelatedDocumentDoesNotProduceImportableItems() {
        byte[] pdf = {1, 2}; var file = new MockMultipartFile("file", "receipt.pdf", "application/pdf", pdf);
        when(uploader.readPdf(file)).thenReturn(pdf);
        when(ai.analyzePdf(pdf)).thenReturn(new MenuImportPreviewResponse(false, null,
                List.of(new MenuImportPreviewResponse.Item("Receipt line", null, BigDecimal.TEN)), List.of("Not a catalog")));
        var result = service.preview(7, 3, file);
        assertFalse(result.isMenu()); assertTrue(result.items().isEmpty()); verifyNoInteractions(items);
    }

    @Test void confirmsZeroPriceAsInactiveEvenWhenActivationWasRequested() {
        doAnswer(call -> call.getArgument(0)).when(items).saveAll(anyList());
        for (Boolean availability : Arrays.asList(true, false, null)) {
            var result = service.confirmItems(7, 3, new MenuImportConfirmRequest(List.of(
                    new MenuImportItemRequest("Laptop awaiting price", null, BigDecimal.ZERO, availability))));
            assertEquals(BigDecimal.ZERO, result.get(0).price()); assertFalse(result.get(0).available());
        }
    }

    @Test void previewNormalizesInvalidPricesAndKeepsValidProductsActive() {
        byte[] pdf = {1, 2}; var file = new MockMultipartFile("file", "products.pdf", "application/pdf", pdf);
        when(uploader.readPdf(file)).thenReturn(pdf);
        when(ai.analyzePdf(pdf)).thenReturn(new MenuImportPreviewResponse(true, "SAR", List.of(
                new MenuImportPreviewResponse.Item("Unknown", null, null),
                new MenuImportPreviewResponse.Item("Negative", null, new BigDecimal("-1")),
                new MenuImportPreviewResponse.Item("Imprecise", null, new BigDecimal("1.001")),
                new MenuImportPreviewResponse.Item("Book", "Book details", new BigDecimal("49.95"))), List.of()));
        var result = service.preview(7, 3, file);
        assertEquals(4, result.items().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(BigDecimal.ZERO, result.items().get(i).price()); assertFalse(result.items().get(i).available());
        }
        assertEquals(new BigDecimal("49.95"), result.items().get(3).price()); assertTrue(result.items().get(3).available());
        verifyNoInteractions(items);
    }

    @Test void savesIncompleteProductsInactiveAndKeepsCompleteProductActive() {
        doAnswer(call -> call.getArgument(0)).when(items).saveAll(anyList());
        var result = service.confirmItems(7, 3, new MenuImportConfirmRequest(List.of(
                new MenuImportItemRequest("Tea", "Tea details", BigDecimal.TEN, true),
                new MenuImportItemRequest("Book", null, BigDecimal.TEN, true),
                new MenuImportItemRequest("Laptop", "Specs", null, true),
                new MenuImportItemRequest(null, "Unknown product", BigDecimal.TEN, true))));
        assertEquals(4, result.size()); assertTrue(result.get(0).available());
        assertTrue(result.get(1).available()); // غياب الوصف وحده لا يمنع التفعيل.
        for (int i = 2; i < 4; i++) assertFalse(result.get(i).available());
        assertEquals(BigDecimal.ZERO, result.get(2).price());
        assertTrue(result.get(3).name().startsWith("[INCOMPLETE NAME]"));
    }

    private MenuImportPreviewResponse preparePreview() {
        byte[] pdf = {1, 2};
        var file = new MockMultipartFile("file", "products.pdf", "application/pdf", pdf);
        when(uploader.readPdf(file)).thenReturn(pdf);
        when(ai.analyzePdf(pdf)).thenReturn(new MenuImportPreviewResponse(true, "SAR", List.of(
                new MenuImportPreviewResponse.Item("Tea", "Tea details", BigDecimal.TEN),
                new MenuImportPreviewResponse.Item("Book", null, BigDecimal.TEN),
                new MenuImportPreviewResponse.Item("Laptop", "Specs", null)), List.of()));
        doAnswer(call -> call.getArgument(0)).when(items).saveAll(anyList());
        return service.preview(7, 3, file);
    }

    @Test void confirmUsesEveryPreviewItemAndCannotBeRepeated() {
        var preview = preparePreview();
        var saved = service.confirm(7, 3);
        assertEquals(preview.items().size(), saved.size());
        assertTrue(saved.get(0).available());
        assertTrue(preview.items().get(1).available());
        assertTrue(saved.get(1).available()); assertFalse(saved.get(2).available());
        assertEquals(BigDecimal.ZERO, saved.get(2).price());
        assertThrows(ApiException.class, () -> service.confirm(7, 3));
        verify(items, times(1)).saveAll(anyList());
    }

    @Test void confirmCannotUseMissingExpiredOrDifferentSellersPreview() {
        assertThrows(ApiException.class, () -> service.confirm(7, 3));
        service.usePreviewClock(java.time.Clock.fixed(java.time.Instant.EPOCH, java.time.ZoneOffset.UTC));
        preparePreview();
        assertThrows(ApiException.class, () -> service.confirm(99, 3));
        assertThrows(ApiException.class, () -> service.confirm(7, 99));
        service.usePreviewClock(java.time.Clock.fixed(java.time.Instant.EPOCH.plusSeconds(1801), java.time.ZoneOffset.UTC));
        assertThrows(ApiException.class, () -> service.confirm(7, 3));
        verify(items, never()).saveAll(anyList());
    }

    @Test void failedSaveKeepsPreviewForRetry() {
        preparePreview();
        when(items.saveAll(anyList())).thenThrow(new IllegalStateException("Database failure"));
        assertThrows(IllegalStateException.class, () -> service.confirm(7, 3));
        doAnswer(call -> call.getArgument(0)).when(items).saveAll(anyList());
        assertEquals(3, service.confirm(7, 3).size());
    }

    @Test void inFlightConfirmationBlocksRepeatedConfirmationAndRollbackAllowsRetry() {
        preparePreview();
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        try {
            service.confirm(7, 3);
            assertThrows(ApiException.class, () -> service.confirm(7, 3));
            var callbacks = org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations();
            callbacks.forEach(c -> c.afterCompletion(org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK));
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
        }
        assertEquals(3, service.confirm(7, 3).size());
    }

    @Test void committedTransactionConsumesPreview() {
        preparePreview();
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        try {
            service.confirm(7, 3);
            var callbacks = org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations();
            callbacks.forEach(c -> c.afterCompletion(org.springframework.transaction.support.TransactionSynchronization.STATUS_COMMITTED));
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
        }
        assertThrows(ApiException.class, () -> service.confirm(7, 3));
    }

    private MenuImportConfirmRequest request(String name, String price) { return new MenuImportConfirmRequest(List.of(item(name, price))); }
    private MenuImportItemRequest item(String name, String price) { return new MenuImportItemRequest(name, "Product details", new BigDecimal(price), null); }
}
