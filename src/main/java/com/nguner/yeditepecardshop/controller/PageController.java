package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.repository.ProductRepository;
import com.nguner.yeditepecardshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {
    private final ProductRepository productRepository;
    private final ProductService productService;

    public PageController(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        List<Product> allProducts = productRepository.findAll();
        Map<String, Product> uniqueProducts = new LinkedHashMap<>();
        // Prefer Packs, then Boxes, then Cases for each title
        for (Product p : allProducts) {
            String title = p.getTitle();
            if (!uniqueProducts.containsKey(title) || "Packs".equalsIgnoreCase(p.getCategoryName())) {
                uniqueProducts.put(title, p);
            }
        }
        List<Product> products = new java.util.ArrayList<>(uniqueProducts.values());
        Collections.shuffle(products);
        products = products.stream().limit(10).toList();
        model.addAttribute("homepageProducts", products);
        model.addAttribute("currentUrl", request.getRequestURI());
        return "index";
    }

    @GetMapping("/packs")
    public String packs(Model model, HttpServletRequest request) {
        List<Product> packs = productRepository.findAll()
            .stream()
            .filter(p -> "Packs".equalsIgnoreCase(p.getCategoryName()))
            .toList();
        model.addAttribute("packs", packs);
        model.addAttribute("currentUrl", request.getRequestURI());
        return "packs";
    }

    @GetMapping("/boxes")
    public String boxes(Model model, HttpServletRequest request) {
        List<Product> boxes = productRepository.findAll()
            .stream()
            .filter(p -> "Boxes".equalsIgnoreCase(p.getCategoryName()))
            .toList();
        model.addAttribute("boxes", boxes);
        model.addAttribute("currentUrl", request.getRequestURI());
        return "boxes";
    }

    @GetMapping("/cases")
    public String cases(Model model, HttpServletRequest request) {
        List<Product> cases = productRepository.findAll()
            .stream()
            .filter(p -> "Cases".equalsIgnoreCase(p.getCategoryName()))
            .toList();
        model.addAttribute("cases", cases);
        model.addAttribute("currentUrl", request.getRequestURI());
        return "cases";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    @GetMapping("/shipping")
    public String shipping() {
        return "shipping";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "cancel";
    }

    @GetMapping("/commercial-transaction")
    public String commercialTransaction() {
        return "commercial-transaction";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query, Model model, HttpServletRequest request) {
        if (query != null && !query.trim().isEmpty()) {
            List<Product> searchResults = productService.searchProducts(query.trim());
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("searchQuery", query.trim());
            model.addAttribute("resultCount", searchResults.size());
        } else {
            model.addAttribute("searchResults", List.of());
            model.addAttribute("searchQuery", "");
            model.addAttribute("resultCount", 0);
        }
        model.addAttribute("currentUrl", request.getRequestURI());
        return "search-results";
    }
}
