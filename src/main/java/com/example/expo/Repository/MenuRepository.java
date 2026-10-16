package com.example.expo.Repository;

import com.example.expo.Model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {
    java.util.List<Menu> findAllBySeller_Id(Integer sellerId);

    // يقفل صف المنيو حتى انتهاء معاملة الاستيراد؛ طلب حفظ ثان ينتظر ثم يرى الأصناف الجديدة.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Menu m where m.id = :id")
    Optional<Menu> findByIdForImport(@Param("id") Integer id);
}
