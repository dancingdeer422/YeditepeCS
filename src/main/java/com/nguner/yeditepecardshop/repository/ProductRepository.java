package com.nguner.yeditepecardshop.repository;

import com.nguner.yeditepecardshop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Paginated search by title or model containing the search term, ignoring case
    Page<Product> findByTitleContainingIgnoreCaseOrModelContainingIgnoreCase(String title, String model, Pageable pageable);

    // Non-paginated search for simple use cases
    java.util.List<Product> findByTitleContainingIgnoreCaseOrModelContainingIgnoreCase(String title, String model);
}
