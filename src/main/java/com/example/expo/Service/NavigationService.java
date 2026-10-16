package com.example.expo.Service;


import com.example.expo.DTO.NavigationPointDTO;
import com.example.expo.DTO.NavigationRouteDTO;
import com.example.expo.Model.MapEdge;
import com.example.expo.Model.MapNode;
import com.example.expo.Repository.MapEdgesRepository;
import com.example.expo.Repository.MapNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NavigationService {


    private final MapNodeRepository mapNodeRepository;
    private final MapEdgesRepository mapEdgeRepository;

    public NavigationRouteDTO findShortestPath(
            Long startNodeId, Long endNodeId) {

        MapNode startNode = mapNodeRepository.findById(startNodeId)
                .orElseThrow(() -> new RuntimeException(
                        "Start node not found with id: " + startNodeId));

        MapNode endNode = mapNodeRepository.findById(endNodeId)
                .orElseThrow(() -> new RuntimeException(
                        "Destination node not found with id: " + endNodeId));

        List<MapEdge> edges = mapEdgeRepository.findAll();

        Map<Long, List<MapEdge>> graph = new HashMap<>();

        for (MapEdge edge : edges) {
            Long startId = edge.getStartNode().getId();
            Long endId = edge.getEndNode().getId();

            graph.computeIfAbsent(startId, key -> new ArrayList<>()).add(edge);

            graph.computeIfAbsent(endId, key -> new ArrayList<>()).add(edge);
        }

        Map<Long, Double> distances = new HashMap<>();
        Map<Long, Long> previousNodes = new HashMap<>();

        PriorityQueue<Map.Entry<Long, Double>> queue =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (MapNode node : mapNodeRepository.findAll()) {
            distances.put(node.getId(), Double.POSITIVE_INFINITY);
        }

        distances.put(startNodeId, 0.0);
        queue.offer(new AbstractMap.SimpleEntry<>(startNodeId, 0.0));

        while (!queue.isEmpty()) {
            Map.Entry<Long, Double> current = queue.poll();

            Long currentNodeId = current.getKey();
            double currentDistance = current.getValue();

            if (currentDistance > distances.get(currentNodeId)) {
                continue;
            }

            if (currentNodeId.equals(endNodeId)) {
                break;
            }

            for (MapEdge edge : graph.getOrDefault(
                    currentNodeId, Collections.emptyList())) {

                Long neighborId;

                if (edge.getStartNode().getId().equals(currentNodeId)) {
                    neighborId = edge.getEndNode().getId();
                } else {
                    neighborId = edge.getStartNode().getId();
                }

                double newDistance =
                        currentDistance + edge.getDistance();

                if (newDistance < distances.getOrDefault(
                        neighborId, Double.POSITIVE_INFINITY)) {

                    distances.put(neighborId, newDistance);
                    previousNodes.put(neighborId, currentNodeId);

                    queue.offer(new AbstractMap.SimpleEntry<>(
                            neighborId, newDistance));
                }
            }
        }

        if (distances.get(endNodeId) == Double.POSITIVE_INFINITY) {
            throw new RuntimeException(
                    "No route found between the selected nodes");
        }

        List<MapNode> path = new ArrayList<>();
        Long currentNodeId = endNodeId;

        while (currentNodeId != null) {
            Long nodeId = currentNodeId;

            MapNode node = mapNodeRepository.findById(nodeId).orElseThrow(() -> new RuntimeException("Node not found with id: " + nodeId));

            path.add(node);
            currentNodeId = previousNodes.get(currentNodeId);
        }

        Collections.reverse(path);

        List<NavigationPointDTO> points = path.stream()
                .map(node -> new NavigationPointDTO(
                        node.getId(),
                        node.getName(),
                        node.getX(),
                        node.getY(),
                        node.getNodeType(),
                        node.getDestination() != null
                                ? node.getDestination().getId()
                                : null,
                        node.getDestination() != null
                                ? node.getDestination().getName()
                                : null
                ))
                .toList();

        return new NavigationRouteDTO(
                points,
                distances.get(endNodeId)
        );
    }
}
