import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// filepath: C:/Users/goble/OneDrive/Documents/pmodii.java
package com.example.profittrackermodii;
package com.example.profittrackermodii;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Arrays;
import java.util.List;

public class ProfitCommand extends CommandBase {

    @Override
    public String getName() {
        return "profit"; // Command name
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/profit <start|pause|resume|check>"; // Command usage
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pmod"); // Command aliases
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("§cPlease specify a subcommand: start, pause, resume, or check."));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                ProfitTrackerModII.startStopwatch();
                sender.sendMessage(new TextComponentString("§aStopwatch started!"));
                break;

            case "pause":
                ProfitTrackerModII.pauseStopwatch();
                sender.sendMessage(new TextComponentString("§aStopwatch paused!"));
                break;

            case "resume":
                ProfitTrackerModII.resumeStopwatch();
                sender.sendMessage(new TextComponentString("§aStopwatch resumed!"));
                break;

            case "check":
                totalProfit = 
                sender.sendMessage(new TextComponentString("§aTotal profit: " + totalProfit + " coins."));
                sender.sendMessage(new TextComponentString("§aRate: " + (totalProfit / ((System.currentTimeMillis() - startTime) / 1000)) + " coins/second."));
                break;

            default:
                sender.sendMessage(new TextComponentString("§cUnknown subcommand. Use start, pause, resume, or check."));
                break;
        }
    }

    @Override
    public boolean checkPermission(ICommandSender sender) {
        return true; // Allow all players to use the command
    }

    @Override
    public List<String> getTabCompletions(ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "start", "pause", "resume", "check");
        }
        return super.getTabCompletions(sender, args, pos);
    }
}
@Mod("profittrackermodii")
public class ProfitTrackerModII {

    private final ItemTracker itemTracker = new ItemTracker(); // Create an instance of ItemTracker

    public ProfitTrackerModII() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(itemTracker); // Register ItemTracker to the event bus
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ProfitCommand());
    }

    public ProfitTrackerModII() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ProfitCommand());
    }

    // Stopwatch logic
    private static long startTime = 0;
    private static double totalProfit = 0.0;

    public static void startStopwatch() {
        startTime = System.currentTimeMillis();
    }

    public static void pauseStopwatch() {
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Correctly retrieve the item counts from the ItemTracker instance
        Map<String, Integer> itemCounts = itemTracker.getItemCounts();

        // Pass the itemCounts to the calculateProfit method
        totalProfit = calculateTotalProfit(itemCounts);

        startTime = 0;
    }

    public static void resumeStopwatch() {
        startTime = System.currentTimeMillis();
    }
    public class ProfitTracker {
    
        // This map holds the materials and their respective counts in the player's inventory.
        private Map<String, Integer> itemCounts;
    
        public ProfitTracker(Map<String, Integer> itemCounts) {
            this.itemCounts = itemCounts;
        }
    
        private double getPricePerCollection(String material) {
            try {
                // Hypixel API key
                String apiKey = "<KEY>"; // Replace with your actual API key
                
                // Construct the URL to query the Hypixel API
                String apiUrl = "https://api.hypixel.net/skyblock/bazaar?key=" + apiKey;
                
                // Establish the HTTP connection
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET"); // Set the request method to GET
                
                // Check the response code
                if (connection.getResponseCode() == 200) {
                    // Read the response from the Hypixel API
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
                    reader.close();
                    
                    // Check if the response was successful
                    if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                        // Access Bazaar products data
                        JsonObject products = jsonResponse.getAsJsonObject("products");
                        
                        // Check if the material exists in the Bazaar data
                        if (products.has(material)) {
                            JsonObject materialData = products.getAsJsonObject(material);
                            JsonObject quickStatus = materialData.getAsJsonObject("quick_status");
                            
                            // Return the sell price of the material
                            return quickStatus.get("sellPrice").getAsDouble();
                        } else {
                            // Notify the player if the material is not found
                            Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                                "Material not found in Bazaar: " + material));
                            return 0.0;
                        }
                    } else {
                        // Notify the player if the API response indicates failure
                        Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                            "Failed to fetch Bazaar data. API response indicates failure."));
                        return 0.0;
                    }
                } else {
                    // Notify the player if the HTTP response code is not 200
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                        "Failed to fetch Bazaar data. HTTP Response Code: " + connection.getResponseCode()));
                    return 0.0;
                }
            } catch (Exception e) {
                // Handle exceptions and notify the player
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                    "Error fetching Bazaar data: " + e.getMessage()));
                e.printStackTrace();
                return 0.0;
            }
        }
    
        // This method calculates the total profit based on the materials in itemCounts
        public double calculateTotalProfit() {    
            // Iterate through each material in the itemCounts map
            for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                String material = entry.getKey();  // Get the material name
                int quantity = entry.getValue();   // Get the material quantity
    
                // Get the price per item from the Bazaar
                double pricePerItem = getPricePerCollection(material);
    
                // Calculate the total value for this material and add it to the total profit
                totalProfit += pricePerItem * quantity;
            }
    
            // Return the total profit
            return totalProfit;
        }
    }    
}
