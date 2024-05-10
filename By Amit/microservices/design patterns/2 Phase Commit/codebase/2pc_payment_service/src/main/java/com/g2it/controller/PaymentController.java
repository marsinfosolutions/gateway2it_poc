package com.g2it.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.g2it.constant.PaymentStatus;
import com.g2it.model.Payment;
import com.g2it.model.TransactionData;
import com.g2it.repo.PaymentRepository;

@RestController
public class PaymentController {

	@Autowired
	private PaymentRepository paymentRepository;

	@PostMapping("/prepare_payment")
	public ResponseEntity<String> preparePayment(@RequestBody TransactionData data) {
		try {
			Payment payment = new Payment();
			payment.setOrderNumber(data.getOrderNumber());
			payment.setItem(data.getItem());
			payment.setPreparationStatus(PaymentStatus.PENDING.name());
			payment.setPrice(data.getPrice());
			payment.setPaymentMode(data.getPaymentMode());
			paymentRepository.save(payment);

			if (shouldFailedDuringPrepare()) {
				throw new RuntimeException("Prepare phase failed for payment " + data.getOrderNumber());
			}

			return ResponseEntity.ok("Payment prepared successfully");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during payment preparation");
		}
	}

	private boolean shouldFailedDuringPrepare() {
		return false;
	}

	@PostMapping("/commit_payment")
	public ResponseEntity<String> commitPayment(@RequestBody TransactionData data) {
		Payment payment = paymentRepository.findByItem(data.getItem());

		if (payment != null && payment.getPreparationStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
			payment.setPreparationStatus(PaymentStatus.APPROVED.name());
			paymentRepository.save(payment);

			return ResponseEntity.ok("Payment committed successfully");
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment cannot be committed");
	}

	@PostMapping("/rollback_payment")
	public ResponseEntity<String> rollbackPayment(@RequestBody TransactionData data) {
		Payment payment = paymentRepository.findByItem(data.getItem());

		if (payment != null) {
			payment.setPreparationStatus(PaymentStatus.ROLLBACK.name());
			paymentRepository.save(payment);

			return ResponseEntity.ok("Payment rolled back successfully");
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during Payment rollback");
	}

}