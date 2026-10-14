package com.example.expo.Controller;
import com.example.expo.DTO.VisitRequest; import com.example.expo.Model.VisitedPlace; import com.example.expo.Service.VisitService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/v1/users/{userId}/visits") @RequiredArgsConstructor public class VisitController {
 private final VisitService service;
 @GetMapping public List<VisitedPlace> getVisits(@PathVariable Integer userId){return service.getVisits(userId);}
 @PostMapping public VisitedPlace record(@PathVariable Integer userId,@Valid @RequestBody VisitRequest request){return service.record(userId,request);}
 @DeleteMapping("/{visitId}") public void delete(@PathVariable Integer userId,@PathVariable Integer visitId){service.delete(userId,visitId);}
}
