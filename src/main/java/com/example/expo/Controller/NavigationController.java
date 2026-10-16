package com.example.expo.Controller;

import com.example.expo.Service.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/navigation")
@RequiredArgsConstructor
public class NavigationController {


    private final NavigationService navigationService;

    @GetMapping("/route")
    public ResponseEntity<?> findShortestPath(@RequestParam Long startNodeId, @RequestParam Long endNodeId) {
        return ResponseEntity.status(200).body(navigationService.findShortestPath(startNodeId, endNodeId));
    }
}

