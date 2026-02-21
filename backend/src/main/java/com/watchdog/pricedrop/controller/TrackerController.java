package com.watchdog.pricedrop.controller;

import com.watchdog.pricedrop.model.ProductTracker;
import com.watchdog.pricedrop.service.EmailService;
import com.watchdog.pricedrop.service.ScraperService;
import com.watchdog.pricedrop.service.TrackerService;
import com.watchdog.pricedrop.scheduler.PriceCheckScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TrackerController {

    private static final Logger logger = LoggerFactory.getLogger(TrackerController.class);

    @Autowired
    private TrackerService trackerService;

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private PriceCheckScheduler priceCheckScheduler;

    @Autowired
    private EmailService emailService;

    private String getUserId(HttpServletRequest request) {
        return (String) request.getAttribute("userId");
    }

    @PostMapping("/track-product")
    public ResponseEntity<?> trackProduct(@RequestBody ProductTracker request, HttpServletRequest httpRequest) {
        try {
            String userId = getUserId(httpRequest);

            if (request.getProductUrl() == null || request.getProductUrl().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product URL is required"));
            }
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (request.getTargetPrice() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Target price must be greater than 0"));
            }

            double currentPrice = scraperService.scrapePrice(request.getProductUrl());

            ProductTracker product = new ProductTracker();
            product.setProductUrl(request.getProductUrl());
            product.setTargetPrice(request.getTargetPrice());
            product.setCurrentPrice(currentPrice > 0 ? currentPrice : 0);
            product.setEmail(request.getEmail());
            product.setLastChecked(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            product.setNotified(false);
            product.setUserId(userId);

            String docId = trackerService.addProduct(product);
            product.setId(docId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("message", "Product is now being tracked!", "product", product)
            );
        } catch (Exception e) {
            logger.error("Error tracking product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to track product: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(HttpServletRequest httpRequest) {
        try {
            String userId = getUserId(httpRequest);
            List<ProductTracker> products = trackerService.getProductsByUser(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch products: " + e.getMessage()));
        }
    }

    @PostMapping("/check-now")
    public ResponseEntity<?> checkNow() {
        try {
            priceCheckScheduler.checkPrices();
            return ResponseEntity.ok(Map.of("message", "Price check completed!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Price check failed: " + e.getMessage()));
        }
    }

    @PostMapping("/test-price-drop")
    public ResponseEntity<?> testPriceDrop(@RequestParam String id, @RequestParam double price) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("currentPrice", price);
            updates.put("notified", false);
            updates.put("lastChecked", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            trackerService.updateProduct(id, updates);

            List<ProductTracker> allProducts = trackerService.getAllProducts();
            for (ProductTracker p : allProducts) {
                if (p.getId().equals(id)) {
                    if (price <= p.getTargetPrice()) {
                        emailService.sendPriceDropAlert(p.getEmail(), p.getProductUrl(), price, p.getTargetPrice());
                        updates.put("notified", true);
                        trackerService.updateProduct(id, updates);
                        return ResponseEntity.ok(Map.of(
                                "message", "Price drop simulated & email sent to " + p.getEmail(),
                                "currentPrice", price,
                                "targetPrice", p.getTargetPrice()
                        ));
                    }
                    return ResponseEntity.ok(Map.of(
                            "message", "Price updated but no email (price still above target)",
                            "currentPrice", price,
                            "targetPrice", p.getTargetPrice()
                    ));
                }
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Product not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Test failed: " + e.getMessage()));
        }
    }
}
