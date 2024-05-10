package com.g2it.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.g2it.constant.OrderPreparationStatus;
import com.g2it.model.Order;
import com.g2it.model.TransactionData;
import com.g2it.repo.OrderRepository;

@RestController
public class OrderController {

	@Autowired
	private OrderRepository orderRepository;

	@PostMapping("/prepare_order")
	public ResponseEntity<String> prepareOrder(@RequestBody TransactionData data) {
		try {
			Order order = new Order();
			order.setOrderNumber(data.getOrderNumber());
			order.setItem(data.getItem());
			order.setPreparationStatus(OrderPreparationStatus.PREPARING.name());
			orderRepository.save(order);

			if (shouldFailedDuringPrepare()) {
				throw new RuntimeException("Prepare phase failed for order " + data.getOrderNumber());
			}

			return ResponseEntity.ok("Order prepared successfully");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during oredr preparation");
		}
	}

	@PostMapping("/commit_order")
	public ResponseEntity<String> commitOrder(@RequestBody TransactionData data) {
		Order order = orderRepository.findByItem(data.getItem());

		if (order != null && order.getPreparationStatus().equalsIgnoreCase(OrderPreparationStatus.PREPARING.name())) {
			order.setPreparationStatus(OrderPreparationStatus.COMMITTED.name());
			orderRepository.save(order);

			return ResponseEntity.ok("Order committed successfully");
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Order cannot be committed");
	}

	@PostMapping("/rollback_order")
	public ResponseEntity<String> rollbackOrder(@RequestBody TransactionData data) {
		Order order = orderRepository.findByItem(data.getItem());

		if (order != null) {
			order.setPreparationStatus(OrderPreparationStatus.ROLLBACK.name());
			orderRepository.save(order);

			return ResponseEntity.ok("Order rolled back successfully");
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during order rollback");
	}

	private boolean shouldFailedDuringPrepare() {
		return false;
	}
}
