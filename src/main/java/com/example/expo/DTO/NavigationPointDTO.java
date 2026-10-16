package com.example.expo.DTO;

import com.example.expo.Enum.NodeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NavigationPointDTO {


    private Long nodeId;
    private String name;
    private Double x;
    private Double y;
    private NodeType nodeType;
    private Long destinationId;
    private String destinationName;

}

