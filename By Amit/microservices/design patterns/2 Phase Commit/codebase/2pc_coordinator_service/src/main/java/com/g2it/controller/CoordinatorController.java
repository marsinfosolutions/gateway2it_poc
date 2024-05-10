package com.g2it.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.g2it.model.TransactionData;

@RestController
public class CoordinatorController {

	private final RestTemplate restTemplate = new RestTemplate();

	@PostMapping("/initiate_2pc")
	public String intiateTwoPhaseCommit(@RequestBody TransactionData data) {
		if (callPreparePhase(data)) {
			if (callCommitPhase(data)) {
				return "Transaction committed successfully.";
			}

			callRollback(data);
			return "Transaction Rollback";
		}

		callRollback(data);
		return "Transaction Rollback";
	}

	private boolean callPreparePhase(TransactionData data) {
		try {
			boolean isOrderSuccess = callServices("http://localhost:8081/prepare_order", data);
			boolean isPaymentSuccess = callServices("http://localhost:8082/prepare_paymnet", data);

			return isOrderSuccess && isPaymentSuccess;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean callCommitPhase(TransactionData data) {
		boolean isOrderSuccess = callServices("http://localhost:8081/commit_order", data);
		boolean isPaymentSuccess = callServices("http://localhost:8082/commit_paymnet", data);

		return isOrderSuccess && isPaymentSuccess;

	}

	private boolean callServices(String url, TransactionData data) {
		ResponseEntity<String> response = restTemplate.postForEntity(url, data, String.class);
		return response.getStatusCode().is2xxSuccessful();
	}

	private void callRollback(TransactionData data) {
		callServiceRollbackPhase("http://localhost:8081/rollback_order", data);
		callServiceRollbackPhase("http://localhost:8082/rollback_paymnet", data);
	}

	private ResponseEntity<String> callServiceRollbackPhase(String serviceUrl, TransactionData data) {
		return restTemplate.postForEntity(serviceUrl, data, String.class);
	}
}
