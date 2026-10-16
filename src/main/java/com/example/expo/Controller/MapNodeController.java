package com.example.expo.Controller;


import com.example.expo.Model.MapNode;
import com.example.expo.Service.MapNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/map-nodes")
@RequiredArgsConstructor
public class MapNodeController {

    private final MapNodeService mapNodeService;


    // Get all map nodes
    @GetMapping
    public ResponseEntity<?> getAllMapNodes() {
        return ResponseEntity.status(200)
                .body(mapNodeService.getAllMapNodes());
    }

    // Get map node by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMapNodeById(@PathVariable Long id) {
        return ResponseEntity.status(200)
                .body(mapNodeService.getMapNodeById(id));
    }

    // Add map node
    @PostMapping
    public ResponseEntity<?> addMapNode(@RequestBody MapNode mapNode) {
        return ResponseEntity.status(201)
                .body(mapNodeService.addMapNode(mapNode));
    }

    // Update map node
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMapNode(
            @PathVariable Long id,
            @RequestBody MapNode mapNode) {
        return ResponseEntity.status(200)
                .body(mapNodeService.updateMapNode(id, mapNode));
    }

    // Delete map node
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMapNode(@PathVariable Long id) {
        mapNodeService.deleteMapNode(id);
        return ResponseEntity.status(200)
                .body("Map node deleted successfully");
    }
}
