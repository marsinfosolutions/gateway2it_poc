package com.g2it.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g2it.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{
	Order findByItem(String item);
}
