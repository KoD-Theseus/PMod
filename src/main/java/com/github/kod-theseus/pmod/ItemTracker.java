package com.github.kodtheseus.forgetemplate;

import net.minecraft.client.Minecraft;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ItemTracker {

    // Maps items to their total count
    public final Map<String, Integer> itemCounts = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(ItemTracker.class);

    // Tracks if tracking is active
    private boolean trackingActive = false;

    // Start the tracker
    public void startTracker() {
        trackingActive = true;
        sendPlayerMessage("§a[ItemTracker] Tracking started."); // Green-colored message
        LOGGER.info("Item tracking started.");
    }

    // Stop the tracker
    public void stopTracker() {
        trackingActive = false;
        sendPlayerMessage("§c[ItemTracker] Tracking stopped."); // Red-colored message
        LOGGER.info("Item tracking stopped.");
    }

    // Check if tracking is active
    public boolean isTrackingActive() {
        return trackingActive;
    }

    // Reset tracking data
    public void resetTracker() {
        itemCounts.clear();
        sendPlayerMessage("§e[ItemTracker] Tracking data has been reset."); // Yellow-colored message
        LOGGER.info("Tracking data has been reset.");
    }

    /**
     * Event: Intercept chat messages from the client.
     */
    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        // Check if the tracker is active
        if (!trackingActive) return;

        // Attempt to extract a hover event from the message
        IChatComponent message = event.message;
        HoverEvent hoverEvent = message.getChatStyle().getChatHoverEvent();

        if (hoverEvent != null && hoverEvent.getValue() != null) {
            // Extract the hover text content
            String hoverText = hoverEvent.getValue().getUnformattedText();

            // Log hover text for debugging
            LOGGER.info("Hover Text Detected: " + hoverText);

            // If "[Sacks]" is detected in hover text, parse it
            if (hoverText.contains("[Sacks]")) {
                parseSacksMessage(hoverText);
            }
        } else {
            // Log if there's no hover event found for debugging
            LOGGER.debug("No HoverEvent or invalid hover text detected in the message.");
        }
    }

    /**
     * Parse and extract the [Sacks] components (materials and quantities) from the hover text.
     * @param hoverText The full hover text containing "[Sacks]".
     */
    private void parseSacksMessage(String hoverText) {
        // Example parsing logic (Adjust according to your hover text structure)
        LOGGER.info("Parsing Sacks Message: " + hoverText);

        // Example: Extract each line from the hover text
        String[] lines = hoverText.split("\n");
        for (String line : lines) {
            // Example: "Material - Count" format
            if (line.contains(" - ")) {
                String[] parts = line.split(" - ");
                String material = parts[0].trim();
                int count;

                try {
                    count = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    LOGGER.error("Failed to parse material count from line: " + line, e);
                    continue;
                }

                // Update itemCounts map
                itemCounts.put(material, itemCounts.getOrDefault(material, 0) + count);

                // Log parsed data for debugging
                LOGGER.info("Material: " + material + ", Count: " + count);
            }
        }
    }

    /**
     * Sends a chat message to the player.
     * @param message The message to send.
     */
    private void sendPlayerMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }
}
