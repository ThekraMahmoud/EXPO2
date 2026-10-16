package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Client.AiMenuClient;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Clock;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuImportService {
    private final MenuRepository menus;
    private final MenuItemRepository items;
    private final PdfUploadService uploader;
    private final AiMenuClient ai;
    private final Validator validator;
    private final Map<Integer, Long> lastAnalysis = new HashMap<>();
    private final Map<PreviewKey, PendingPreview> pending = new HashMap<>();
    private Clock previewClock = Clock.systemUTC();
    private static final long PREVIEW_TTL_MS = 30 * 60_000L;
    private record PreviewKey(Integer sellerId, Integer menuId) {}
    private static final class PendingPreview {
        final List<MenuImportItemRequest> products;
        final long expiresAt;
        boolean confirming;
        PendingPreview(List<MenuImportItemRequest> products, long expiresAt) {
            this.products = List.copyOf(products); this.expiresAt = expiresAt;
        }
    }

    // للاختبار فقط: الوقت قابل للتحكم دون انتظار انتهاء المعاينة فعليًا.
    void usePreviewClock(Clock clock) { this.previewClock = clock; }

    // المعاينة لا تحفظ شيئًا، ولا تفتح معاملة قاعدة بيانات أثناء انتظار الشبكة.
    public MenuImportPreviewResponse preview(Integer sellerId, Integer menuId, MultipartFile file) {
        requireOwnedActiveMenu(sellerId, menuId, false);
        // بدء معاينة جديدة يلغي السابقة حتى لا يؤكَّد ملف قديم بعد فشل تحليل الملف الجديد.
        discardPreview(sellerId, menuId);
        ai.requireConfigured();
        byte[] pdf = uploader.readPdf(file);
        reserveAnalysis(sellerId);
        MenuImportPreviewResponse result = ai.analyzePdf(pdf);
        if (result == null || result.items() == null || result.warnings() == null || result.items().size() > 100) {
            throw new ApiException("Invalid AI menu result");
        }
        // isMenu هو الاسم القديم لقائمة منتجات/خدمات؛ لا نرفض الكتب والأجهزة بسبب نوع النشاط.
        if (!result.isMenu()) return new MenuImportPreviewResponse(false, result.currency(), List.of(), result.warnings());
        List<String> warnings = new ArrayList<>(result.warnings());
        if (result.currency() == null || !"SAR".equalsIgnoreCase(result.currency())) {
            warnings.add("Confirm currency and enter prices in SAR before saving; no conversion was performed.");
        }
        List<MenuImportPreviewResponse.Item> previewItems = new ArrayList<>();
        for (MenuImportPreviewResponse.Item item : result.items()) {
            if (item == null || (item.name() != null && item.name().length() > 100)
                    || (item.description() != null && item.description().length() > 500)) {
                throw new ApiException("AI returned invalid item fields; try a clearer PDF");
            }
            // المجهول أو غير الصالح يبقى ضمن القائمة بصفر وغير نشط؛ لا نسقط المنتج.
            boolean priced = validPrice(item.price()) && item.price().signum() > 0;
            boolean complete = priced && present(item.name());
            if (!complete) warnings.add("Saved as inactive on confirmation; complete name and price before activation: " + item.name());
            previewItems.add(new MenuImportPreviewResponse.Item(item.name(), item.description(),
                    priced ? item.price() : BigDecimal.ZERO, complete));
        }
        if (!previewItems.isEmpty()) rememberPreview(sellerId, menuId, previewItems);
        return new MenuImportPreviewResponse(true, result.currency(), previewItems, warnings);
    }

    // التأكيد يعتمد على النسخة المحفوظة في السيرفر؛ لا يستقبل منتجات يكتبها العميل.
    @Transactional
    public List<MenuImportedItemResponse> confirm(Integer sellerId, Integer menuId) {
        PreviewKey key = new PreviewKey(sellerId, menuId);
        PendingPreview draft = claimPreview(key);
        boolean managed = TransactionSynchronizationManager.isSynchronizationActive();
        try {
            if (managed) {
                // لا نحذف المعاينة قبل نجاح COMMIT؛ الفشل يسمح بإعادة المحاولة.
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override public void afterCompletion(int status) {
                        finishPreview(key, draft, status == STATUS_COMMITTED);
                    }
                });
            }
            var saved = confirmItems(sellerId, menuId, new MenuImportConfirmRequest(draft.products));
            if (!managed) finishPreview(key, draft, true);
            return saved;
        } catch (RuntimeException failure) {
            if (!managed) finishPreview(key, draft, false);
            throw failure;
        }
    }

    // منطق الحفظ الداخلي مشترك مع الاختبارات؛ لا يوجد له مسار HTTP مستقل.
    List<MenuImportedItemResponse> confirmItems(Integer sellerId, Integer menuId, MenuImportConfirmRequest request) {
        Menu menu = requireOwnedActiveMenu(sellerId, menuId, true);
        // نتحقق من جميع الأصناف قبل الحفظ، وأي فشل يعيد المعاملة كاملة.
        if (!validator.validate(request).isEmpty()) throw new ApiException("Review item names, descriptions and prices before saving");
        Set<String> names = new HashSet<>();
        items.findAllByMenu_Id(menuId).forEach(item -> names.add(normalize(item.getName())));
        List<MenuItem> additions = new ArrayList<>();
        for (MenuImportItemRequest input : request.items()) {
            if (input.price() != null && !validPrice(input.price())) throw new ApiException("Price must be 0 to 99999999.99 with at most two decimal places");
            // نتجاوز الاسم المكرر فقط، داخل المعاينة أو الموجود مسبقًا؛ لا نعدّل المنتج القديم.
            if (present(input.name()) && !names.add(normalize(input.name()))) continue;
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setName(present(input.name()) ? input.name().trim() : "[INCOMPLETE NAME] " + UUID.randomUUID());
            item.setDescription(input.description());
            item.setPrice(input.price() == null ? BigDecimal.ZERO : input.price());
            // لا يتفعّل المنتج الناقص حتى لو أرسل العميل available=true.
            item.setAvailable(present(input.name())
                    && item.getPrice().signum() > 0 && (input.available() == null || input.available()));
            additions.add(item);
        }
        // إن كانت كلها مكررة، ينجح التأكيد بقائمة فارغة دون تنفيذ حفظ.
        if (additions.isEmpty()) return List.of();
        return items.saveAll(additions).stream().map(item -> new MenuImportedItemResponse(
                item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getAvailable())).toList();
    }

    private synchronized void discardPreview(Integer sellerId, Integer menuId) {
        PreviewKey key = new PreviewKey(sellerId, menuId);
        PendingPreview current = pending.get(key);
        if (current != null && current.confirming) throw new ApiException("Confirmation in progress; wait before uploading another PDF");
        pending.remove(key);
    }

    private synchronized void rememberPreview(Integer sellerId, Integer menuId, List<MenuImportPreviewResponse.Item> products) {
        long now = previewClock.millis();
        pending.entrySet().removeIf(e -> !e.getValue().confirming && e.getValue().expiresAt <= now);
        if (pending.size() >= 1000) throw new ApiException("Preview storage is busy; try again later");
        var inputs = products.stream().map(i -> new MenuImportItemRequest(
                i.name(), i.description(), i.price(), i.available())).toList();
        pending.put(new PreviewKey(sellerId, menuId), new PendingPreview(inputs, now + PREVIEW_TTL_MS));
    }

    private synchronized PendingPreview claimPreview(PreviewKey key) {
        PendingPreview draft = pending.get(key);
        if (draft == null || draft.expiresAt <= previewClock.millis()) {
            if (draft != null && !draft.confirming) pending.remove(key);
            throw new ApiException("No current preview; upload the PDF again before confirming");
        }
        if (draft.confirming) throw new ApiException("This preview is already being confirmed");
        draft.confirming = true;
        return draft;
    }

    private synchronized void finishPreview(PreviewKey key, PendingPreview draft, boolean committed) {
        if (committed) pending.remove(key, draft);
        else draft.confirming = false;
    }

    private Menu requireOwnedActiveMenu(Integer sellerId, Integer menuId, boolean lock) {
        Menu menu = (lock ? menus.findByIdForImport(menuId) : menus.findById(menuId))
                .orElseThrow(() -> new ApiException("Menu not found"));
        Sellerers seller = menu.getSeller();
        if (!seller.getId().equals(sellerId)) throw new ApiException("Catalog does not belong to this seller");
        if (!"ACTIVE".equals(seller.getStatus()) || !"VERIFIED".equals(seller.getVerification_status())) {
            throw new ApiException("Seller must be active and verified");
        }
        return menu;
    }

    private boolean validPrice(BigDecimal price) {
        return price != null && price.signum() >= 0
                && price.compareTo(new BigDecimal("99999999.99")) <= 0 && price.stripTrailingZeros().scale() <= 2;
    }

    private boolean present(String value) { return value != null && !value.isBlank() && !value.startsWith("[INCOMPLETE NAME]"); }

    private String normalize(String name) { return name.trim().toLowerCase(Locale.ROOT); }

    // حد بسيط لكل بائع لحماية الحصة المجانية؛ نمسح المدخلات القديمة لتجنب تراكم الذاكرة.
    private synchronized void reserveAnalysis(Integer sellerId) {
        long now = System.currentTimeMillis();
        lastAnalysis.entrySet().removeIf(entry -> now - entry.getValue() >= 60_000);
        if (lastAnalysis.containsKey(sellerId)) throw new ApiException("Wait one minute before analyzing another PDF");
        if (lastAnalysis.size() >= 1000) throw new ApiException("Menu analysis is busy; try again later");
        lastAnalysis.put(sellerId, now);
    }
}
