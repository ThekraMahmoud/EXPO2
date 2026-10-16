
package com.example.expo.Repository;

import com.example.expo.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findAllByOrder_Id(Integer orderId);

    List<Payment> findAllByStatus(String status);

    boolean existsByTransactionReference(String transactionReference);
}
