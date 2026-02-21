package com.watchdog.pricedrop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTracker {

    private String id;
    private String productUrl;
    private double targetPrice;
    private double currentPrice;
    private String email;
    private String lastChecked;
    private boolean notified;
    private String userId;
}
