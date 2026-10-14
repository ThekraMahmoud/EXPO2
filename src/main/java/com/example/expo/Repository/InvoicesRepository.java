package com.example.expo.Repository;

import com.example.expo.Model.Invoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvoicesRepository extends JpaRepository<Invoices, Integer> {
    Invoices findInvoiceById(Integer id);
    boolean existsBySeller_Id(Integer sellerId);
    Optional<Invoices> findByOrder_Id(Integer orderId);
}
