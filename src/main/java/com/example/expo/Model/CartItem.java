package com.example.expo.Model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_menu_item",
                        columnNames = {"cart_id", "menu_item_id"}
                )
        }
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;


    /////////Relation

    @ManyToOne
    @JsonIgnore // لا نعيد السلة داخل كل عنصر؛ علاقة قاعدة البيانات محفوظة كما هي.
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Transient
    public BigDecimal getSubtotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}