package com.example.expo.Controller;

import com.example.expo.DTO.*;
import com.example.expo.Service.PublicParamedicRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/health-cards/public/{token}/paramedic-request")
public class PublicParamedicRequestController {
    private final PublicParamedicRequestService service;
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicParamedicRequestStatus> create(@PathVariable String token,
            @Valid @RequestBody PublicParamedicRequestInput input) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.create(token, input));
    }
    @GetMapping
    public ResponseEntity<PublicParamedicRequestStatus> status(@PathVariable String token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.status(token));
    }
}
