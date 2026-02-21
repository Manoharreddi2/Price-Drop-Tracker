package com.watchdog.pricedrop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a price drop alert email to the user.
     */
    public void sendPriceDropAlert(String to, String productUrl, double currentPrice, double targetPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("🔔 Price Drop Alert - Price Drop Watchdog");
            message.setText(
                    "Great news! A product you're tracking has dropped in price.\n\n" +
                    "📦 Product URL: " + productUrl + "\n\n" +
                    "💰 Current Price: ₹" + String.format("%.2f", currentPrice) + "\n" +
                    "🎯 Your Target Price: ₹" + String.format("%.2f", targetPrice) + "\n\n" +
                    "The current price is below your target! Hurry and grab the deal!\n\n" +
                    "---\n" +
                    "Price Drop Watchdog 🐕"
            );

            mailSender.send(message);
            logger.info("Price drop alert email sent to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send email to: {} - {}", to, e.getMessage());
        }
    }
}
