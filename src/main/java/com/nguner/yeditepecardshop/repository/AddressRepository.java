package com.nguner.yeditepecardshop.repository;

import com.nguner.yeditepecardshop.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
}
