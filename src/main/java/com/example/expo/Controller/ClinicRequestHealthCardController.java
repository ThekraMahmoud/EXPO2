package com.example.expo.Controller;

import com.example.expo.DTO.ClinicRequestHealthCardResponse;
import com.example.expo.Service.ClinicRequestHealthCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/paramedic-requests")
public class ClinicRequestHealthCardController {
    private final ClinicRequestHealthCardService service;
    @GetMapping("/{requestId}/health-card")
    public ResponseEntity<ClinicRequestHealthCardResponse> healthCard(@PathVariable Integer requestId,
            @RequestParam Integer caregiverId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .header("Referrer-Policy", "no-referrer").body(service.get(caregiverId, requestId));
    }
}
