package com.watchdog.pricedrop.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    /**
     * Scrapes the product price from the given URL.
     * Attempts multiple common CSS selectors used by e-commerce sites.
     */
    public double scrapePrice(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .get();

            // Common price selectors used by various e-commerce sites
            String[] selectors = {
                    // Amazon
                    ".a-price-whole",
                    "#priceblock_ourprice",
                    "#priceblock_dealprice",
                    ".a-offscreen",
                    // Flipkart
                    "._30jeq3",
                    ".Nx9bqj",
                    // Generic selectors
                    "[class*='price']",
                    "[id*='price']",
                    ".product-price",
                    ".sale-price",
                    ".current-price",
                    "span[itemprop='price']",
                    "meta[itemprop='price']"
            };

            for (String selector : selectors) {
                Element element = document.selectFirst(selector);
                if (element != null) {
                    String priceText;

                    // Handle meta tags differently
                    if (element.tagName().equals("meta")) {
                        priceText = element.attr("content");
                    } else {
                        priceText = element.text();
                    }

                    double price = parsePrice(priceText);
                    if (price > 0) {
                        logger.info("Successfully scraped price: {} from URL: {}", price, url);
                        return price;
                    }
                }
            }

            logger.warn("Could not find price on page: {}", url);
            return -1;

        } catch (Exception e) {
            logger.error("Error scraping price from URL: {} - {}", url, e.getMessage());
            return -1;
        }
    }

    /**
     * Parses a price string by removing currency symbols and non-numeric characters.
     */
    private double parsePrice(String priceText) {
        try {
            if (priceText == null || priceText.isEmpty()) {
                return -1;
            }
            // Remove currency symbols, commas, spaces, and other non-numeric chars (keep dots)
            String cleaned = priceText.replaceAll("[^\\d.]", "");

            // Handle cases with multiple dots (e.g., "1.299.00")
            int lastDot = cleaned.lastIndexOf('.');
            if (lastDot != -1) {
                String beforeDot = cleaned.substring(0, lastDot).replace(".", "");
                String afterDot = cleaned.substring(lastDot);
                cleaned = beforeDot + afterDot;
            }

            if (cleaned.isEmpty()) {
                return -1;
            }

            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse price from text: {}", priceText);
            return -1;
        }
    }
}
