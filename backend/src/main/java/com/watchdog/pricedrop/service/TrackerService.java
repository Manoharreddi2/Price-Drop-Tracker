package com.watchdog.pricedrop.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.watchdog.pricedrop.model.ProductTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrackerService {

    private static final Logger logger = LoggerFactory.getLogger(TrackerService.class);
    private static final String COLLECTION_NAME = "tracked_products";

    @Autowired
    private Firestore firestore;

    /**
     * Adds a new product to track in Firestore with userId.
     */
    public String addProduct(ProductTracker product) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("productUrl", product.getProductUrl());
        data.put("targetPrice", product.getTargetPrice());
        data.put("currentPrice", product.getCurrentPrice());
        data.put("email", product.getEmail());
        data.put("lastChecked", product.getLastChecked());
        data.put("notified", product.isNotified());
        data.put("userId", product.getUserId());

        ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME).add(data);
        String docId = future.get().getId();
        logger.info("Product added with ID: {}", docId);
        return docId;
    }

    /**
     * Retrieves products for a specific user from Firestore.
     */
    public List<ProductTracker> getProductsByUser(String userId) throws Exception {
        List<ProductTracker> products = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            products.add(docToProduct(doc));
        }

        logger.info("Fetched {} products for user: {}", products.size(), userId);
        return products;
    }

    /**
     * Retrieves all tracked products from Firestore (for scheduler).
     */
    public List<ProductTracker> getAllProducts() throws Exception {
        List<ProductTracker> products = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            products.add(docToProduct(doc));
        }

        logger.info("Fetched {} total tracked products", products.size());
        return products;
    }

    /**
     * Updates a tracked product in Firestore.
     */
    public void updateProduct(String id, Map<String, Object> updates) throws Exception {
        firestore.collection(COLLECTION_NAME).document(id).update(updates).get();
        logger.info("Product updated with ID: {}", id);
    }

    private ProductTracker docToProduct(QueryDocumentSnapshot doc) {
        ProductTracker product = new ProductTracker();
        product.setId(doc.getId());
        product.setProductUrl(doc.getString("productUrl"));
        product.setTargetPrice(doc.getDouble("targetPrice") != null ? doc.getDouble("targetPrice") : 0);
        product.setCurrentPrice(doc.getDouble("currentPrice") != null ? doc.getDouble("currentPrice") : 0);
        product.setEmail(doc.getString("email"));
        product.setLastChecked(doc.getString("lastChecked"));
        product.setNotified(doc.getBoolean("notified") != null && doc.getBoolean("notified"));
        product.setUserId(doc.getString("userId"));
        return product;
    }
}
