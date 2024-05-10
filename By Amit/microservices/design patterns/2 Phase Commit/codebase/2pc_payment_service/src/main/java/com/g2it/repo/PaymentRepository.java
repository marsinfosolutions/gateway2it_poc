package com.g2it.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g2it.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Payment findByItem(String item);

}
