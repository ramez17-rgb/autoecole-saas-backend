package com.autoecole.saas.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    Optional<Receipt> findByVerificationCode(UUID verificationCode);

    boolean existsByReceiptNumber(String receiptNumber);
}
