package com.nguner.yeditepecardshop.config.populator;

import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class ProductPopulator {
    private static final Logger log = LogManager.getLogger(ProductPopulator.class);
    private final ProductRepository productRepo;

    public ProductPopulator(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @PostConstruct
    public void init() {
        log.info("[ProductPopulator] Clearing and re-populating products collection");
        productRepo.deleteAll();
        List<Product> products = new ArrayList<>();
        // OP-01
        products.add(createProduct("Romance Dawn – OP-01", "Packs", "OP01", 35, 8));
        products.add(createProduct("Romance Dawn – OP-01", "Boxes", "OP01", 0, 185));
        products.add(createProduct("Romance Dawn – OP-01", "Cases", "OP01", 25, 2220));
        // OP-02
        products.add(createProduct("Paramount War – OP-02", "Packs", "OP02", 40, 5.75));
        products.add(createProduct("Paramount War – OP-02", "Boxes", "OP02", 5, 120));
        products.add(createProduct("Paramount War – OP-02", "Cases", "OP02", 15, 1450));
        // OP-03
        products.add(createProduct("Pillars of Strength – OP-03", "Packs", "OP03", 30, 4.5));
        products.add(createProduct("Pillars of Strength – OP-03", "Boxes", "OP03", 10, 95));
        products.add(createProduct("Pillars of Strength – OP-03", "Cases", "OP03", 20, 1140));
        // OP-04
        products.add(createProduct("Kingdoms of Intrigue – OP-04", "Packs", "OP04", 45, 4.5));
        products.add(createProduct("Kingdoms of Intrigue – OP-04", "Boxes", "OP04", 8, 102));
        products.add(createProduct("Kingdoms of Intrigue – OP-04", "Cases", "OP04", 12, 1224));
        // OP-05
        products.add(createProduct("Awakening of the New Era – OP-05", "Packs", "OP05", 50, 8));
        products.add(createProduct("Awakening of the New Era – OP-05", "Boxes", "OP05", 6, 172));
        products.add(createProduct("Awakening of the New Era – OP-05", "Cases", "OP05", 18, 2076));
        // OP-06
        products.add(createProduct("Wings of the Captain – OP-06", "Packs", "OP06", 42, 4.5));
        products.add(createProduct("Wings of the Captain – OP-06", "Boxes", "OP06", 4, 96));
        products.add(createProduct("Wings of the Captain – OP-06", "Cases", "OP06", 10, 1152));
        // OP-07
        products.add(createProduct("500 Years In The Future – OP-07", "Packs", "OP07", 38, 4.5));
        products.add(createProduct("500 Years In The Future – OP-07", "Boxes", "OP07", 7, 83));
        products.add(createProduct("500 Years In The Future – OP-07", "Cases", "OP07", 14, 996));
        // OP-08
        products.add(createProduct("Two Legends – OP-08", "Packs", "OP08", 50, 3.2));
        products.add(createProduct("Two Legends – OP-08", "Boxes", "OP08", 9, 64));
        products.add(createProduct("Two Legends – OP-08", "Cases", "OP08", 12, 768));
        // OP-09
        products.add(createProduct("Emperors of the New World – OP-09", "Packs", "OP09", 45, 3.8));
        products.add(createProduct("Emperors of the New World – OP-09", "Boxes", "OP09", 6, 70));
        products.add(createProduct("Emperors of the New World – OP-09", "Cases", "OP09", 15, 840));
        // OP-10
        products.add(createProduct("Royal Blood – OP-10", "Packs", "OP10", 48, 3.2));
        products.add(createProduct("Royal Blood – OP-10", "Boxes", "OP10", 7, 57.5));
        products.add(createProduct("Royal Blood – OP-10", "Cases", "OP10", 13, 690));
        // OP-11
        products.add(createProduct("A Fist of Divine Speed – OP-11", "Packs", "OP11", 43, 3.8));
        products.add(createProduct("A Fist of Divine Speed – OP-11", "Boxes", "OP11", 5, 70));
        products.add(createProduct("A Fist of Divine Speed – OP-11", "Cases", "OP11", 16, 840));
        // EB-01
        products.add(createProduct("EXTRA BOOSTER -MEMORIAL COLLECTION- [EB-01]", "Packs", "EB01", 50, 4.5));
        products.add(createProduct("EXTRA BOOSTER -MEMORIAL COLLECTION- [EB-01]", "Boxes", "EB01", 8, 100));
        products.add(createProduct("EXTRA BOOSTER -MEMORIAL COLLECTION- [EB-01]", "Cases", "EB01", 12, 1200));
        // EB-02
        products.add(createProduct("EXTRA BOOSTER -Anime 25th collection- [EB-02]", "Packs", "EB02", 50, 3.8));
        products.add(createProduct("EXTRA BOOSTER -Anime 25th collection- [EB-02]", "Boxes", "EB02", 8, 64));
        products.add(createProduct("EXTRA BOOSTER -Anime 25th collection- [EB-02]", "Cases", "EB02", 12, 768));
        // PRB-01
        products.add(createProduct("PREMIUM BOOSTER -ONE PIECE CARD THE BEST- [PRB-01]", "Packs", "PRB01", 44, 7));
        products.add(createProduct("PREMIUM BOOSTER -ONE PIECE CARD THE BEST- [PRB-01]", "Boxes", "PRB01", 6, 43));
        products.add(createProduct("PREMIUM BOOSTER -ONE PIECE CARD THE BEST- [PRB-01]", "Cases", "PRB01", 14, 516));
        productRepo.saveAll(products);
        log.info("[ProductPopulator] Inserted {} products", products.size());
    }

    private Product createProduct(String title, String category, String model, int stock, double price) {
        return Product.builder()
                .title(title)
                .categoryName(category)
                .model(model)
                .stockQuantity(stock)
                .basePrice(price)
                .imagePath(buildImagePath(model, category))
                .createdDate(LocalDateTime.now())
                .description("One Piece Trading Card Game - " + title)
                .weight(0.0)
                .length(0.0)
                .width(0.0)
                .height(0.0)
                .cardsPerPack(category.equals("Packs") ? 12 : 0)
                .packsPerBox(category.equals("Boxes") ? 24 : 0)
                .build();
    }

    private String buildImagePath(String model, String category) {
        String suffix = switch (category.toLowerCase()) {
            case "packs" -> "pack";
            case "boxes" -> "box";
            case "cases" -> "case";
            default      -> category.toLowerCase();
        };
        return String.format("%s-%s.jpg", model.toLowerCase(), suffix);
    }
}
