import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.ItemPickupEvent;
import net.minecraftforge.event.server.ServerChatEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import java.util.HashMap;
import java.util.Map;

public class ItemTracker {

    private final Map<String, Integer> itemCounts = new HashMap<>();  // Maps item names to their total count
    private final Map<String, Long> lastMeasuredTime = new HashMap<>();  // Maps item names to their last measurement timestamp
    private final Map<String, Integer> measurementCount = new HashMap<>();  // Maps item names to how many times they have been measured in the last 10 seconds
    private final long MEASUREMENT_WINDOW = 10 * 1000; // 10 seconds in milliseconds

    @SubscribeEvent
public void onItemAdded(ItemPickupEvent event) {
    // Get the item name and count from the event
    String itemName = event.getItem().getItem().getDisplayName().getString();
    int itemCount = event.getItem().getItem().getCount(); // Get the count of items in the stack
    long currentTime = System.currentTimeMillis();

    // Get the last measured time for the item, or 0 if it hasn't been measured yet
    long lastTime = lastMeasuredTime.getOrDefault(itemName, 0L);

    // If the item was measured recently (within the last 10 seconds), count it
    if (currentTime - lastTime <= MEASUREMENT_WINDOW) {
        int currentCount = measurementCount.getOrDefault(itemName, 0);
        measurementCount.put(itemName, currentCount + 1);
    } else {
        // Reset the count if the 10-second window has passed
        measurementCount.put(itemName, 1);
    }

    // Update the last measured time
    lastMeasuredTime.put(itemName, currentTime);

    // If the item has been measured more than once in the last 10 seconds, mark it as repetitive
    if (measurementCount.get(itemName) > 1) {
        System.out.println("DEBUG: Item " + itemName + " is repetitive and will not be measured again.");
        // Do not measure it again
        return;
    }

    // Otherwise, update the count of the item in the inventory
    int currentCount = itemCounts.getOrDefault(itemName, 0);
    int newCount = currentCount + itemCount; // Add the count of items picked up

    itemCounts.put(itemName, newCount);

    // Notify the player of the updated count
    System.out.println("DEBUG: Item added: " + itemName + " | Count added: " + itemCount + " | New total count: " + newCount);
}

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        if (event.getMessage().contains("[Sacks]")) {
            String message = event.getMessage();
            String[] parts = message.split(" ");
            for (int i = 1; i < parts.length; i++) {
                String material = parts[i];
                String quantityStr = parts[i + 1];
                try {
                    int quantity = Integer.parseInt(quantityStr);

                    int currentCount = itemCounts.getOrDefault(material, 0);
                    int newCount = currentCount + quantity;
                    itemCounts.put(material, newCount);

                    System.out.println("Material: " + material + " | Quantity: " + quantity + " | New count: " + newCount);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity for material: " + material);
                }
            }
        }
    }
}