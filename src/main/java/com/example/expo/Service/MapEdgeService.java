package com.example.expo.Service;


import com.example.expo.Model.MapEdge;
import com.example.expo.Repository.MapEdgesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapEdgeService {


    private final MapEdgesRepository mapEdgeRepository;

    // Get all map edges
    public List<MapEdge> getAllMapEdges() {
        return mapEdgeRepository.findAll();
    }

    // Get map edge by ID
    public MapEdge getMapEdgeById(Long id) {
        return mapEdgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Map edge not found"));
    }

    // Add map edge
    public MapEdge addMapEdge(MapEdge mapEdge) {
        return mapEdgeRepository.save(mapEdge);
    }

    // Update map edge
    public MapEdge updateMapEdge(Long id, MapEdge mapEdge) {

        MapEdge oldMapEdge = mapEdgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Map edge not found"));

        oldMapEdge.setStartNode(mapEdge.getStartNode());
        oldMapEdge.setEndNode(mapEdge.getEndNode());
        oldMapEdge.setDistance(mapEdge.getDistance());
        oldMapEdge.setPathType(mapEdge.getPathType());

        return mapEdgeRepository.save(oldMapEdge);
    }

    // Delete map edge
    public void deleteMapEdge(Long id) {

        MapEdge mapEdge = mapEdgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Map edge not found"));

        mapEdgeRepository.delete(mapEdge);
    }
}
