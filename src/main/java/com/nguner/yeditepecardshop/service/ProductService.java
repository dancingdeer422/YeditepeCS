package com.nguner.yeditepecardshop.service;

import com.nguner.yeditepecardshop.exception.ProductNotFoundException;
import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

//@Service
//@AllArgsConstructor
//public class ProductService {
//
//    private final ProductRepository productRepo;
//
//    /** CREATE */
//    public Product createProduct(Product payload) {
//        payload.setCreatedDate(LocalDateTime.now());
//        return productRepo.save(payload);
//    }
//
//    /** READ ONE */
//    public Product getProductById(String id) {
//        return productRepo.findById(id)
//                .orElseThrow(() -> new ProductNotFoundException(id));
//    }
//
//    /** UPDATE */
//    public Product updateProduct(String id, Product payload) {
//        Product existing = getProductById(id);
//        existing.setTitle(payload.getTitle());
//        existing.setCategoryName(payload.getCategoryName());
//        existing.setModel(payload.getModel());
//        existing.setQuantityInStock(payload.getQuantityInStock());
//        existing.setBasePrice(payload.getBasePrice());
//        //existing.setImage(payload.getImage());
//        // keep existing.getCreatedDate()
//        return productRepo.save(existing);
//    }
//
//    /** DELETE */
//    public void deleteProduct(String id) {
//        Product existing = getProductById(id);
//        productRepo.delete(existing);
//    }
//
//    /** PAGINATED & UNSORTED */
//    public Page<Product> getPaginated(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return productRepo.findAll(pageable);
//    }
//
//    /** SORTED BY PRICE */
//    public Page<Product> getByPrice(int page, int size, String direction) {
//        Sort sort = direction.equalsIgnoreCase("desc")
//                ? Sort.by("basePrice").descending()
//                : Sort.by("basePrice").ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//        return productRepo.findAll(pageable);
//    }
//
//    /** SORTED BY CREATED DATE */
//    public Page<Product> getByDate(int page, int size, String direction) {
//        Sort sort = direction.equalsIgnoreCase("desc")
//                ? Sort.by("createdDate").descending()
//                : Sort.by("createdDate").ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//        return productRepo.findAll(pageable);
//    }
//}

import com.nguner.yeditepecardshop.exception.ProductNotFoundException;
import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;

    /* CREATE -------------------------------------------------------------- */
    public Product createProduct(Product payload) {
        payload.setCreatedDate(LocalDateTime.now());
        // Normalize category name
        payload.setCategoryName(normalizeCategoryName(payload.getCategoryName()));
        return productRepo.save(payload);
    }

    /* READ ONE ------------------------------------------------------------ */
    public Product getProductById(String id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /* UPDATE -------------------------------------------------------------- */
    public Product updateProduct(String id, Product payload) {
        Product existing = getProductById(id);

        existing.setTitle(payload.getTitle());
        // Normalize category names to ensure consistency
        existing.setCategoryName(normalizeCategoryName(payload.getCategoryName()));
        existing.setModel(payload.getModel());

        // Update stock quantity
        existing.setStockQuantity(payload.getStockQuantity());

        existing.setBasePrice(payload.getBasePrice());
        existing.setImagePath(payload.getImagePath());

        // Update additional fields if provided
        if (payload.getDescription() != null) {
            existing.setDescription(payload.getDescription());
        }
        if (payload.getWeight() > 0) {
            existing.setWeight(payload.getWeight());
        }
        if (payload.getLength() > 0) {
            existing.setLength(payload.getLength());
        }
        if (payload.getWidth() > 0) {
            existing.setWidth(payload.getWidth());
        }
        if (payload.getHeight() > 0) {
            existing.setHeight(payload.getHeight());
        }
        if (payload.getCardsPerPack() > 0) {
            existing.setCardsPerPack(payload.getCardsPerPack());
        }
        if (payload.getPacksPerBox() > 0) {
            existing.setPacksPerBox(payload.getPacksPerBox());
        }

        // keep existing.getCreatedDate()
        return productRepo.save(existing);
    }

    /* DELETE -------------------------------------------------------------- */
    public void deleteProduct(String id) {
        productRepo.delete(getProductById(id));
    }

    /* PAGINATION HELPERS ------------------------------------------------- */
    public Page<Product> getPaginated(int page, int size) {
        return productRepo.findAll(PageRequest.of(page, size));
    }

    /* SEARCH -------------------------------------------------------------- */
    // Method to handle PAGINATED search
    public Page<Product> searchProducts(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepo.findByTitleContainingIgnoreCaseOrModelContainingIgnoreCase(query, query, pageable);
    }

    // Method to handle simple search without pagination
    public java.util.List<Product> searchProducts(String query) {
        return productRepo.findByTitleContainingIgnoreCaseOrModelContainingIgnoreCase(query, query);
    }

    /* HELPER METHODS ------------------------------------------------------ */
    private String normalizeCategoryName(String categoryName) {
        if (categoryName == null) return null;

        return switch (categoryName.trim()) {
            case "Booster Pack" -> "Packs";
            case "Booster Box" -> "Boxes";
            case "Case" -> "Cases";
            default -> categoryName;
        };
    }
}


