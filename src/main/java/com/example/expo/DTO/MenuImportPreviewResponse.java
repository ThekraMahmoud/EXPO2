package com.example.expo.DTO;

import java.math.BigDecimal;
import java.util.List;

// نتيجة مؤقتة: isMenu تعني قائمة منتجات أو خدمات لأي نشاط، وليست مقصورة على الطعام.
// خدمة المعاينة تحول السعر المجهول إلى صفر مع available=false قبل إرجاعه للبائع.
public record MenuImportPreviewResponse(boolean isMenu, String currency,
                                        List<Item> items, List<String> warnings) {
    public record Item(String name, String description, BigDecimal price, Boolean available) {
        // يفيد نتائج التحليل الداخلية التي لم تحدد التوفر بعد؛ الخدمة تطبق القاعدة النهائية.
        public Item(String name, String description, BigDecimal price) {
            this(name, description, price, price != null && price.signum() > 0);
        }
    }
}
