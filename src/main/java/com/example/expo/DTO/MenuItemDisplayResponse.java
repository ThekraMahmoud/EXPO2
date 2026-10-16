package com.example.expo.DTO;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;

// ترتيب العرض المطلوب؛ نحتفظ بالمعرّف للتعديل دون إظهار حالة التوفر هنا.
@JsonPropertyOrder({"name", "description", "price", "id"})
public record MenuItemDisplayResponse(String name, String description, BigDecimal price, Integer id) {}
