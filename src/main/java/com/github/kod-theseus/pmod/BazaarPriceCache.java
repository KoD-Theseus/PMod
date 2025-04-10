package com.github.kodtheseus.forgetemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BazaarPriceCache {
    private final Map<String, Double> bazaarPrices = new HashMap<>();
    private long lastUpdateTime = 0;
    private final long CACHE_EXPIRATION_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds

    public void refreshBazaarPrices(String apiKey) {
        CompletableFuture.runAsync(() -> {
            try {
                String apiUrl = "https://api.hypixel.net/skyblock/bazaar?key=" + ProfitTrackerModII.getApiKey()
                        ;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonParser jsonParser = new JsonParser();
                    JsonObject jsonResponse = jsonParser.parse(reader).getAsJsonObject();
                    reader.close();

                    if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                        JsonObject products = jsonResponse.getAsJsonObject("products");
                        products.entrySet().forEach(entry -> {
                            String material = entry.getKey();
                            JsonObject quickStatus = entry.getValue().getAsJsonObject().getAsJsonObject("quick_status");
                            double sellPrice = quickStatus.get("sellPrice").getAsDouble();
                            bazaarPrices.put(material, sellPrice);
                        });

                        lastUpdateTime = System.currentTimeMillis();
                        ProfitTrackerModII.LOGGER.info("Bazaar prices updated successfully!");
                    } else {
                        ProfitTrackerModII.LOGGER.error("Failed to fetch Bazaar data. API response indicates failure.");
                    }
                } else {
                    ProfitTrackerModII.LOGGER.error("Failed to fetch Bazaar data. HTTP Response Code: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                ProfitTrackerModII.LOGGER.error("Error fetching Bazaar data", e);
            }
        });
    }

    public double getPrice(String material) {
        return bazaarPrices.getOrDefault(material, 0.0);
    }

    public boolean isCacheExpired() {
        return System.currentTimeMillis() - lastUpdateTime > CACHE_EXPIRATION_TIME;
    }
}