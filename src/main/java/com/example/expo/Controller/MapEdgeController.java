package com.example.expo.Controller;


import com.example.expo.Model.MapEdge;
import com.example.expo.Service.MapEdgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/map-edges")
@RequiredArgsConstructor
public class MapEdgeController {


    private final MapEdgeService mapEdgeService;

    // Get all map edges
    @GetMapping
    public ResponseEntity<?> getAllMapEdges() {
        return ResponseEntity.status(200)
                .body(mapEdgeService.getAllMapEdges());
    }

    // Get map edge by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMapEdgeById(@PathVariable Long id) {
        return ResponseEntity.status(200)
                .body(mapEdgeService.getMapEdgeById(id));
    }

    // Add map edge
    @PostMapping
    public ResponseEntity<?> addMapEdge(@RequestBody MapEdge mapEdge) {
        return ResponseEntity.status(201)
                .body(mapEdgeService.addMapEdge(mapEdge));
    }

    // Update map edge
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMapEdge(
            @PathVariable Long id,
            @RequestBody MapEdge mapEdge) {
        return ResponseEntity.status(200)
                .body(mapEdgeService.updateMapEdge(id, mapEdge));
    }

    // Delete map edge
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMapEdge(@PathVariable Long id) {
        mapEdgeService.deleteMapEdge(id);
        return ResponseEntity.status(200)
                .body("Map edge deleted successfully");
    }
}

