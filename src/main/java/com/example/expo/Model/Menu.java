        package com.example.expo.Model;

import com.example.expo.Model.MenuItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Menu name is required")
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Sellerers seller;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<MenuItem> menuItems;
}
