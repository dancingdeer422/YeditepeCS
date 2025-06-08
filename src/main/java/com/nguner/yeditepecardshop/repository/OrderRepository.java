package com.nguner.yeditepecardshop.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.nguner.yeditepecardshop.model.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
}
