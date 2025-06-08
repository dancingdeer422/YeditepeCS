// com/nguner/yeditepecardshop/controller/ProductController.java
package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping("/api/products")
//@AllArgsConstructor
//public class ProductController {
//
//    private final ProductService productService;
//
//    /**
//     * GET /api/products
//     * Optional query‑params:
//     *  - page (default 0)
//     *  - size (default 10)
//     *  - sortBy: "price" or "date" (default: none)
//     *  - sortDir: "asc" or "desc" (default "asc")
//     */
//    @GetMapping
//    public ResponseEntity<Page<Product>> list(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDir
//    ) {
//        Page<Product> result;
//        if ("price".equalsIgnoreCase(sortBy)) {
//            result = productService.getByPrice(page, size, sortDir);
//        } else if ("date".equalsIgnoreCase(sortBy)) {
//            result = productService.getByDate(page, size, sortDir);
//        } else {
//            result = productService.getPaginated(page, size);
//        }
//        return ResponseEntity.ok(result);
//    }
//
//    /** GET /api/products/{id} */
//    @GetMapping("/{id}")
//    public ResponseEntity<Product> getOne(@PathVariable String id) {
//        return ResponseEntity.ok(productService.getProductById(id));
//    }
//
//    /** POST /api/products */
//    @Secured({"ROLE_ADMIN"})
//    @PostMapping
//    public ResponseEntity<Product> create(@RequestBody Product payload) {
//        Product created = productService.createProduct(payload);
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(created);
//    }
//
//    /** PUT /api/products/{id} */
//    @Secured({"ROLE_ADMIN"})
//    @PutMapping("/{id}")
//    public ResponseEntity<Product> update(
//            @PathVariable String id,
//            @RequestBody Product payload
//    ) {
//        return ResponseEntity.ok(productService.updateProduct(id, payload));
//    }
//
//    /** DELETE /api/products/{id} */
//    @Secured({"ROLE_ADMIN"})
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable String id) {
//        productService.deleteProduct(id);
//        return ResponseEntity.noContent().build();
//    }
//}

import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /* LIST --------------------------------------------------------------- */
    @GetMapping
    public ResponseEntity<Page<Product>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Product> result = productService.getPaginated(page, size);
        return ResponseEntity.ok(result);
    }

    /* GET ONE ------------------------------------------------------------ */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /* CREATE ------------------------------------------------------------- */
    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(payload));
    }

    /* UPDATE ------------------------------------------------------------- */
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable String id,
            @RequestBody Product payload
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, payload));
    }

    /* DELETE ------------------------------------------------------------- */
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /* SEARCH ------------------------------------------------------------- */
    // Paginated search endpoint
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.searchProducts(query, page, size);
        return ResponseEntity.ok(products);
    }
}

