package com.example.expo.Controller;

import com.example.expo.DTO.ParamedicRequestInput;
import com.example.expo.Model.*;
import com.example.expo.Service.ParamedicRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Actor IDs follow the application's existing account contract. */
@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/paramedic-requests")
public class ParamedicRequestController {
    private final ParamedicRequestService service;
    public record Assignment(Integer caregiverId) {}
    @PostMapping("/user/{userId}")
    public ParamedicRequest create(@PathVariable Integer userId, @Valid @RequestBody ParamedicRequestInput body) {
        return service.create(userId, body);
    }
    @GetMapping("/user/{userId}")
    public List<ParamedicRequest> history(@PathVariable Integer userId) { return service.history(userId); }
    @PostMapping("/{id}/cancel")
    public ParamedicRequest cancel(@PathVariable Integer id, @RequestParam Integer userId) { return service.cancel(userId, id); }
    @GetMapping("/clinic")
    public ClinicBranch clinic(@RequestParam Integer caregiverId) { return service.clinic(caregiverId); }
    @GetMapping("/clinic/inbox")
    public List<ParamedicRequest> inbox(@RequestParam Integer caregiverId) { return service.inbox(caregiverId); }
    @PostMapping("/{id}/accept")
    public ParamedicRequest accept(@PathVariable Integer id, @RequestParam Integer caregiverId) { return service.accept(caregiverId, id); }
    @PostMapping("/{id}/complete")
    public ParamedicRequest complete(@PathVariable Integer id, @RequestParam Integer caregiverId) { return service.complete(caregiverId, id); }
    @PutMapping("/clinics/{clinicId}/caregiver")
    public ClinicBranch assign(@PathVariable Integer clinicId, @RequestParam Integer adminId, @RequestBody Assignment body) {
        return service.assign(adminId, clinicId, body.caregiverId());
    }
}
