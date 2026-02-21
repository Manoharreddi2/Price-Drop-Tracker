package com.watchdog.pricedrop.scheduler;

import com.watchdog.pricedrop.model.ProductTracker;
import com.watchdog.pricedrop.service.EmailService;
import com.watchdog.pricedrop.service.ScraperService;
import com.watchdog.pricedrop.service.TrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PriceCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PriceCheckScheduler.class);

    @Autowired
    private TrackerService trackerService;

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private EmailService emailService;

    /**
     * Runs every 24 hours to check all tracked product prices.
     * Sends email alert if a product's price drops below the target price.
     * Only sends one email per product (checks notified flag).
     */
    @Scheduled(fixedRate = 86400000) // 24 hours in milliseconds
    public void checkPrices() {
        logger.info("🔍 Price check scheduler started at: {}", LocalDateTime.now());

        try {
            List<ProductTracker> products = trackerService.getAllProducts();

            for (ProductTracker product : products) {
                try {
                    // Skip already notified products
                    if (product.isNotified()) {
                        logger.info("Skipping already notified product: {}", product.getProductUrl());
                        continue;
                    }

                    // Scrape current price
                    double currentPrice = scraperService.scrapePrice(product.getProductUrl());

                    if (currentPrice <= 0) {
                        logger.warn("Could not scrape price for: {}", product.getProductUrl());
                        continue;
                    }

                    // Prepare updates
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("currentPrice", currentPrice);
                    updates.put("lastChecked", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                    // Check if price dropped below target
                    if (currentPrice <= product.getTargetPrice()) {
                        logger.info("💰 Price drop detected for: {} | Current: {} | Target: {}",
                                product.getProductUrl(), currentPrice, product.getTargetPrice());

                        // Send email alert
                        emailService.sendPriceDropAlert(
                                product.getEmail(),
                                product.getProductUrl(),
                                currentPrice,
                                product.getTargetPrice()
                        );

                        // Mark as notified
                        updates.put("notified", true);
                    }

                    // Update product in Firestore
                    trackerService.updateProduct(product.getId(), updates);

                } catch (Exception e) {
                    logger.error("Error checking price for product {}: {}", product.getId(), e.getMessage());
                }
            }

            logger.info("✅ Price check scheduler completed at: {}", LocalDateTime.now());

        } catch (Exception e) {
            logger.error("Error in price check scheduler: {}", e.getMessage());
        }
    }
}
