package com.watchdog.pricedrop.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScraperService {

    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    /**
     * Scrapes the product price from the given URL.
     * Uses multiple strategies: CSS selectors, JSON-LD structured data, and regex fallback.
     */
    public double scrapePrice(String url) {
        try {
            // Clean URL for better compatibility
            String cleanUrl = cleanUrl(url);

            Document document = Jsoup.connect(cleanUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-IN,en-GB;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Connection", "keep-alive")
                    .header("Cache-Control", "max-age=0")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .referrer("https://www.google.com/")
                    .followRedirects(true)
                    .timeout(20000)
                    .method(Connection.Method.GET)
                    .execute()
                    .parse();

            // Strategy 1: Try JSON-LD structured data (most reliable)
            double price = extractFromJsonLd(document);
            if (price > 0) {
                logger.info("✅ [JSON-LD] Price: {} from: {}", price, cleanUrl);
                return price;
            }

            // Strategy 2: Site-specific CSS selectors
            price = extractWithSiteSelectors(document, cleanUrl);
            if (price > 0) {
                logger.info("✅ [CSS] Price: {} from: {}", price, cleanUrl);
                return price;
            }

            // Strategy 3: Generic CSS selectors
            price = extractWithGenericSelectors(document);
            if (price > 0) {
                logger.info("✅ [Generic] Price: {} from: {}", price, cleanUrl);
                return price;
            }

            // Strategy 4: Regex fallback on page source
            price = extractWithRegex(document.html());
            if (price > 0) {
                logger.info("✅ [Regex] Price: {} from: {}", price, cleanUrl);
                return price;
            }

            logger.warn("Could not find price on page: {}", cleanUrl);
            return -1;

        } catch (Exception e) {
            logger.error("Error scraping price from URL: {} - {}", url, e.getMessage());
            return -1;
        }
    }

    /**
     * Cleans the URL by removing unnecessary tracking parameters.
     */
    private String cleanUrl(String url) {
        // For Amazon, strip to the core product URL
        if (url.contains("amazon.in") || url.contains("amazon.com")) {
            Pattern pattern = Pattern.compile("(https?://www\\.amazon\\.[a-z.]+/[^/]+/dp/[A-Z0-9]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        // For Flipkart short links, use as-is
        // For other URLs, return as-is
        return url;
    }

    /**
     * Extracts price from JSON-LD structured data embedded in script tags.
     */
    private double extractFromJsonLd(Document document) {
        Elements scripts = document.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            String json = script.data();
            // Look for "price" field in JSON-LD
            double price = extractPriceFromJson(json, "\"price\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)");
            if (price > 0) return price;

            // Also try "lowPrice"
            price = extractPriceFromJson(json, "\"lowPrice\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)");
            if (price > 0) return price;

            // Try "offer" price
            price = extractPriceFromJson(json, "\"offerPrice\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)");
            if (price > 0) return price;
        }
        return -1;
    }

    private double extractPriceFromJson(String json, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return parsePrice(matcher.group(1));
        }
        return -1;
    }

    /**
     * Extracts price using site-specific CSS selectors.
     */
    private double extractWithSiteSelectors(Document document, String url) {
        String[] selectors;

        if (url.contains("amazon")) {
            selectors = new String[]{
                    ".a-price .a-offscreen",
                    ".a-price-whole",
                    "#corePriceDisplay_desktop_feature_div .a-offscreen",
                    "#corePrice_desktop .a-offscreen",
                    "#tp_price_block_total_price_wc .a-offscreen",
                    "#priceblock_ourprice",
                    "#priceblock_dealprice",
                    "#Price",
                    ".priceToPay .a-offscreen",
                    "span.a-price span.a-offscreen"
            };
        } else if (url.contains("flipkart")) {
            selectors = new String[]{
                    ".Nx9bqj._4b5DiR",
                    ".Nx9bqj",
                    "._30jeq3._16Jk6d",
                    "._30jeq3",
                    ".CEmiEU div._30jeq3",
                    "div._25b18c div._30jeq3",
                    "[class*='SellingPrice']",
                    "[class*='selling-price']"
            };
        } else if (url.contains("myntra")) {
            selectors = new String[]{
                    ".pdp-price strong",
                    ".pdp-discount-container .pdp-price",
                    ".pdp-mrp .pdp-price",
                    "[class*='discountedPrice']",
                    "[class*='sellingPrice']"
            };
        } else {
            return -1; // Let generic selectors handle it
        }

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null) {
                String priceText = element.tagName().equals("meta") ? element.attr("content") : element.text();
                double price = parsePrice(priceText);
                if (price > 0) return price;
            }
        }
        return -1;
    }

    /**
     * Extracts price using generic CSS selectors.
     */
    private double extractWithGenericSelectors(Document document) {
        String[] selectors = {
                "span[itemprop='price']",
                "meta[itemprop='price']",
                "[class*='product-price']",
                "[class*='sale-price']",
                "[class*='current-price']",
                "[class*='selling-price']",
                "[class*='offer-price']",
                "[class*='special-price']",
                "[data-price]"
        };

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null) {
                String priceText;
                if (element.tagName().equals("meta")) {
                    priceText = element.attr("content");
                } else if (element.hasAttr("data-price")) {
                    priceText = element.attr("data-price");
                } else if (element.hasAttr("content")) {
                    priceText = element.attr("content");
                } else {
                    priceText = element.text();
                }
                double price = parsePrice(priceText);
                if (price > 0) return price;
            }
        }
        return -1;
    }

    /**
     * Last resort: Extract price using regex patterns from page source.
     * Looks for common price patterns in script tags and HTML.
     */
    private double extractWithRegex(String html) {
        // Look for price in JavaScript variables or data attributes
        String[] patterns = {
                "\"selling_price\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)",
                "\"price\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)",
                "\"finalPrice\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)",
                "\"offer_price\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)",
                "\"discounted_price\"\\s*:\\s*[\"']?([\\d,]+\\.?\\d*)",
                "₹\\s*([\\d,]+\\.?\\d*)"
        };

        for (String regex : patterns) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                double price = parsePrice(matcher.group(1));
                if (price > 10) { // Filter out tiny values that are likely not prices
                    return price;
                }
            }
        }
        return -1;
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
