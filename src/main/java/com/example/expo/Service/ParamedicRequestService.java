package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.ParamedicRequestInput;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service @RequiredArgsConstructor
public class ParamedicRequestService {
    private final ParamedicRequestRepository requests;
    private final UserRepository users;
    private final ClinicBranchRepository clinics;

    private User active(Integer id, String role) {
        User user = users.findById(id).orElseThrow(() -> new ApiException("الحساب غير موجود"));
        if (!role.equals(user.getRole()) || !"ACTIVE".equals(user.getStatus()))
            throw new ApiException("الحساب غير مخول لهذا الإجراء");
        return user;
    }
    public ClinicBranch clinic(Integer caregiverId) {
        active(caregiverId, "CAREGIVER");
        return clinics.findByCaregiverId(caregiverId)
            .orElseThrow(() -> new ApiException("لم يربط الأدمن حسابك بفرع عيادة بعد"));
    }
    @Transactional
    public ParamedicRequest create(Integer userId, ParamedicRequestInput input) {
        // Lock only this visitor while checking for an existing open request.
        User user = users.findForParamedicRequest(userId)
            .orElseThrow(() -> new ApiException("الحساب غير موجود"));
        if (!"USER".equals(user.getRole()) || !"ACTIVE".equals(user.getStatus()))
            throw new ApiException("طلب المساعدة متاح لحساب الزائر النشط فقط");
        if (input.locationDescription() == null || input.locationDescription().isBlank())
            throw new ApiException("يرجى كتابة موقعك بالتفصيل");
        if (requests.existsByUserIdAndStatusIn(userId, List.of("PENDING", "ACCEPTED")))
            throw new ApiException("لديك طلب مساعدة مفتوح بالفعل؛ تابعي حالته أدناه");
        ParamedicRequest request = new ParamedicRequest();
        request.setUserId(userId);
        request.setVisitorName(user.getUsername());
        request.setLocationDescription(input.locationDescription().trim());
        request.setNote(input.note() == null ? null : input.note().trim());
        return requests.save(request);
    }
    public List<ParamedicRequest> history(Integer userId) {
        active(userId, "USER");
        return requests.findTop100ByUserIdOrderByCreatedAtDesc(userId);
    }
    public List<ParamedicRequest> inbox(Integer caregiverId) {
        ClinicBranch branch = clinic(caregiverId);
        return requests.findTop100ByStatusOrClinicIdOrderByCreatedAtDesc("PENDING", branch.getId());
    }
    @Transactional
    public ParamedicRequest accept(Integer caregiverId, Integer id) {
        ClinicBranch branch = clinic(caregiverId);
        if (requests.claim(id, branch.getId(), branch.getName(), LocalDateTime.now()) != 1)
            throw new ApiException("لم يعد الطلب متاحًا؛ ربما قبلته عيادة أخرى أو ألغاه الزائر");
        return requests.findById(id).orElseThrow();
    }
    @Transactional
    public ParamedicRequest cancel(Integer userId, Integer id) {
        active(userId, "USER");
        if (requests.cancel(id, userId, LocalDateTime.now()) != 1)
            throw new ApiException("يمكن إلغاء طلبك فقط قبل قبول العيادة له");
        return requests.findById(id).orElseThrow();
    }
    @Transactional
    public ParamedicRequest complete(Integer caregiverId, Integer id) {
        ClinicBranch branch = clinic(caregiverId);
        if (requests.complete(id, branch.getId(), LocalDateTime.now()) != 1)
            throw new ApiException("يمكن للعيادة التي قبلت الطلب فقط إنهاؤه");
        return requests.findById(id).orElseThrow();
    }
    @Transactional
    public ClinicBranch assign(Integer adminId, Integer clinicId, Integer caregiverId) {
        active(adminId, "ADMIN");
        ClinicBranch branch = clinics.findById(clinicId).orElseThrow(() -> new ApiException("العيادة غير موجودة"));
        if (caregiverId != null) {
            active(caregiverId, "CAREGIVER");
            clinics.findByCaregiverId(caregiverId).ifPresent(existing -> {
                if (!Objects.equals(existing.getId(), clinicId))
                    throw new ApiException("مقدم الرعاية مرتبط بعيادة أخرى بالفعل");
            });
        }
        if (!Objects.equals(branch.getCaregiverId(), caregiverId)
            && requests.existsByClinicIdAndStatus(clinicId, "ACCEPTED"))
            throw new ApiException("أكملي طلبات العيادة المقبولة قبل تغيير مقدم الرعاية");
        branch.setCaregiverId(caregiverId);
        return clinics.save(branch);
    }
}
