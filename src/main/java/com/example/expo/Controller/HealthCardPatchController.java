package com.example.expo.Controller;
import com.example.expo.Api.ApiResponse;
import com.example.expo.Service.HealthCardPatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/health-cards")
public class HealthCardPatchController {
    private final HealthCardPatchService service;
    // PUT القديم يبقى كما هو؛ PATCH الجديد يحدّث المفاتيح المرسلة فقط.
    @PatchMapping("/updateHealthCard/{userId}/{healthCardId}")
    public ApiResponse patch(@PathVariable Integer userId, @PathVariable Integer healthCardId,
                             @RequestBody Map<String, Object> fields) {
        service.patch(userId, healthCardId, fields); return new ApiResponse("Health card updated successfully");
    }
}
