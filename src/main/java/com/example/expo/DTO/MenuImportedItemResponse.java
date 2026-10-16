package com.example.expo.DTO;

import java.math.BigDecimal;

// استجابة محددة تمنع إرجاع علاقات البائع والسلة والطلبات داخل نتيجة الاستيراد.
public record MenuImportedItemResponse(Integer id, String name, String description,
                                       BigDecimal price, Boolean available) {}
