
package com.example.expo.Repository;

import com.example.expo.Model.ClinicBranch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicBranchRepository
        extends JpaRepository<ClinicBranch, Integer> {
    @org.springframework.data.jpa.repository.Query("select (count(r) > 0) from ParamedicRequest r where r.clinicId = :id and r.status = 'ACCEPTED'")
    boolean hasAcceptedRequests(@org.springframework.data.repository.query.Param("id") Integer id);
    java.util.Optional<ClinicBranch> findByCaregiverId(Integer caregiverId);
}

