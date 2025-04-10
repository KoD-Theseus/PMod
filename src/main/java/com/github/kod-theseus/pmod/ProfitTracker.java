package com.github.kodtheseus.forgetemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.util.ChatComponentText;
import net.minecraft.command.ICommandSender;
import net.minecraft.client.Minecraft;

public class ProfitTracker {
    private Map<String, Integer> itemCounts;

    public ProfitTracker(Map<String, Integer> itemCounts) {
        this.itemCounts = itemCounts;
    }

    // Pass the ICommandSender as a parameter to send messages to the player
    public void refreshBazaarPricesAsync(ICommandSender sender) {
        CompletableFuture.runAsync(() -> {
            try {
                String apiUrl = "https://api.hypixel.net/skyblock/bazaar";
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

                        // Send chat message safely to the main thread
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            sender.addChatMessage(new ChatComponentText("Bazaar prices updated successfully!"));
                        });

                    } else {
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            sender.addChatMessage(new ChatComponentText("Failed to fetch Bazaar data. API response indicates failure."));
                        });
                    }
                } else {
                    int responseCode = connection.getResponseCode();

                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        sender.addChatMessage(new ChatComponentText("Failed to fetch Bazaar data. HTTP Response Code: " + responseCode));
                    });
                }
            } catch (Exception e) {
                ProfitTrackerModII.LOGGER.error("Error fetching Bazaar data", e);

                // Send error message to the player
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    sender.addChatMessage(new ChatComponentText("Error fetching Bazaar data: " + e.getMessage()));
                });

                e.printStackTrace();
            }
        });
    }

    // Calculates the total profit from the item counts and price cache
    public double calculateTotalProfit(BazaarPriceCache bazaarPriceCache) {
        double profit = 0.0;
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            String material = entry.getKey();
            int quantity = entry.getValue();
            double pricePerItem = bazaarPriceCache.getPrice(material);
            profit += pricePerItem * quantity;
        }
        return profit;
    }
}