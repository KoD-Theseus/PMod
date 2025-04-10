package com.github.kodtheseus.forgetemplate;

public class SackChange {
    private final String internalName;
    private final int delta;
    private final String[] sacks;

    public SackChange(String internalName, int delta, String[] sacks) {
        this.internalName = internalName;
        this.delta = delta;
        this.sacks = sacks;
    }

    public String getInternalName() {
        return internalName;
    }

    public int getDelta() {
        return delta;
    }

    public String[] getSacks() {
        return sacks;
    }
}
import java.util.List;

public class SackChangeEvent {
    private final List<SackChange> sackChanges;
    private final boolean otherItemsAdded;
    private final boolean otherItemsRemoved;

    public SackChangeEvent(List<SackChange> sackChanges, boolean otherItemsAdded, boolean otherItemsRemoved) {
        this.sackChanges = sackChanges;
        this.otherItemsAdded = otherItemsAdded;
        this.otherItemsRemoved = otherItemsRemoved;
    }

    public List<SackChange> getSackChanges() {
        return sackChanges;
    }

    public boolean hasOtherItemsAdded() {
        return otherItemsAdded;
    }

    public boolean hasOtherItemsRemoved() {
        return otherItemsRemoved;
    }
}
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
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§a[ItemTracker] Tracking started.")); // Green-colored message
        LOGGER.info("Item tracking started.");
    }

    // Stop the tracker
    public void stopTracker() {
        trackingActive = false;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§c[ItemTracker] Tracking stopped.")); // Red-colored message
        LOGGER.info("Item tracking stopped.");
    }

    // Check if tracking is active
    public boolean isTrackingActive() {
        return trackingActive;
    }

    // Reset tracking data
    public void resetTracker() {
        // Clear the itemCounts map
        itemCounts.clear();

        // Notify the player and log the reset
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§e[ItemTracker] Tracking data has been reset.")); // Yellow-colored message
        LOGGER.info("Item tracking data has been cleared. The itemCounts map is now empty.");
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

            if (hoverText == null || hoverText.isEmpty()) {
                LOGGER.warn("Hover text is null or empty. Skipping parsing.");
                return;
            }

            // Log hover text for debugging
            LOGGER.info("Hover Text Detected: " + hoverText);

            // If "[Sacks]" is detected in hover text, parse it
            if (hoverText.contains("[Sacks]")) {
                parseSackChanges(hoverText);
            }
        } else {
            // Log if there's no hover event found for debugging
            LOGGER.debug("No HoverEvent or invalid hover text detected in the message.");
        }
    }

    /**
     * Parses hover text to extract and handle sack changes directly.
     *
     * @param hoverText The text to parse.
     */
    private void parseSackChanges(String hoverText) {
        // Ensure hoverText is not null before proceeding
        if (hoverText == null || hoverText.isEmpty()) {
            LOGGER.warn("Cannot parse sack changes: hoverText is null or empty.");
            return;
        }

        final String PATTERN = "([a-zA-Z_]+) -\\s*(-?\\d+)"; // Matches "MaterialName - Amount"
        List<SackChange> changes = new ArrayList<>();

        Pattern regex = Pattern.compile(PATTERN);
        Matcher matcher = regex.matcher(hoverText);

        LOGGER.info("Starting to parse sack changes from hoverText.");
        while (matcher.find()) {
            try {
                String internalName = matcher.group(1);
                int delta = Integer.parseInt(matcher.group(2));
                String[] sacks = hoverText.contains("[Sacks]") ? new String[]{"Main Sack"} : new String[0];

                SackChange sackChange = new SackChange(internalName, delta, sacks);
                changes.add(sackChange);
            } catch (Exception ex) {
                LOGGER.error("Error parsing sack change: " + matcher.group(), ex);
            }
        }

        if (changes.isEmpty()) {
            LOGGER.warn("No sack changes found in hoverText.");
            return;
        }

        handleSackChangeEvent(new SackChangeEvent(changes));
    }

    /**
     * Parses hover text to extract sack changes and returns a SackChangeEvent object.
     *
     * @param hoverText The text to parse.
     * @return The generated SackChangeEvent, or null if parsing fails.
     */
    private SackChangeEvent parseSackChanges(String hoverText) {
        // Ensure hoverText is not null before proceeding
        if (hoverText == null || hoverText.isEmpty()) {
            LOGGER.warn("Cannot parse sack changes: hoverText is null or empty.");
            return null;
        }

        // Example regex pattern (adjust to fit actual hover text format)
        final String PATTERN = "([a-zA-Z_]+) -\\s*(-?\\d+)"; // Matches "MaterialName - Amount"
        List<SackChange> changes = new ArrayList<>();

        Pattern regex = Pattern.compile(PATTERN);
        Matcher matcher = regex.matcher(hoverText);

        LOGGER.info("Starting to parse sack changes from hoverText.");
        while (matcher.find()) {
            try {
                String internalName = matcher.group(1);
                int delta = Integer.parseInt(matcher.group(2));
                String[] sacks = hoverText.contains("[Sacks]") ? new String[]{"Main Sack"} : new String[0];


                SackChange sackChange = new SackChange(internalName, delta, sacks);
                changes.add(sackChange);
            } catch (Exception ex) {
                LOGGER.error("Error parsing sack change: " + matcher.group(), ex);
            }
        }

        if (changes.isEmpty()) {
            LOGGER.warn("No sack changes found in hoverText.");
        }

        return changes.isEmpty() ? null : new SackChangeEvent(changes);
        private void handleSackChangeEvent (SackChangeEvent event){
            LOGGER.info("SackChangeEvent received: " + event.getSackChanges());

            for (SackChange change : event.getSackChanges()) {
                String material = change.getInternalName();
                int count = change.getDelta();
                itemCounts.put(material, itemCounts.getOrDefault(material, 0) + count);

                LOGGER.info("Material: " + material + ", Adjusted Count: " + count);
            }

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

}
