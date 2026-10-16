package com.example.expo.DTO;

import com.example.expo.DTO.NavigationPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NavigationRouteDTO {

    private List<NavigationPointDTO> points;
    private Double totalDistance;


}

