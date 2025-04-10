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

    private final ItemTracker itemTracker;

    public ProfitTracker(ItemTracker itemTracker) {
        this.itemTracker = itemTracker;
        this.itemCounts = itemTracker.getItemCounts();
    }

    // Pass the ICommandSender as a parameter to send messages to the player
    public void refreshBazaarPricesAsync(ICommandSender sender) {
        CompletableFuture.runAsync(() -> {
            try {
                ProfitCommand.bazaarPriceCache.refreshBazaarPrices();

                Minecraft.getMinecraft().addScheduledTask(() -> {
                    sender.addChatMessage(new ChatComponentText("Bazaar prices updated successfully!"));
                });
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
            if (pricePerItem == 0.0) {
                ProfitTrackerModII.LOGGER.warn("Price unavailable for material: " + material);
                continue;
            }
            profit += pricePerItem * quantity;
        }
        return profit;
    }
}