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
                double profit = ProfitTrackerModII.getProfit();
                sender.sendMessage(new TextComponentString("§aTotal profit: " + profit + " coins."));
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
        totalProfit += calculateProfit(elapsedTime);
        startTime = 0;
    }

    public static void resumeStopwatch() {
        startTime = System.currentTimeMillis();
    }

    public static double getProfit() {
        return totalProfit;
    }

    private static double calculateProfit(long elapsedTime) {
        // Example profit calculation logic
        return elapsedTime / 1000.0 * 10; // 10 coins per second
    }

    private void startStopwatch(String category) {
        fetchPlayerCollections(category);
        startTime = System.currentTimeMillis();
        Minecraft.getInstance().player.sendMessage(new StringTextComponent("Stopwatch started for " + category + "!"));
    }

    private void calculateProfitFromChat() {
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onChatMessage(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
                String message = event.getMessage().getString();
    
                // Check if the message contains the word "sacks"
                if (message.contains("sacks")) {
                    try {
                        // Example message: "10 Enchanted Diamonds added to your sacks"
                        String[] parts = message.split(" ");
                        int quantity = Integer.parseInt(parts[0]); // Extract quantity
                        String material = parts[1] + " " + parts[2]; // Extract material (e.g., "Enchanted Diamonds")
                        if (parts.length < 3) {
                            Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                                "Unexpected message format: " + message));
                            return;
                        }
                        // Calculate profit based on material and quantity
                        double pricePerUnit = getPricePerCollection(material);
                        if (pricePerUnit == null|| pricePerUnit == 0.0) {
                            Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                                "Material not available in Bazaar: " + material));
                        }
                        double profit = quantity * pricePerUnit;
    
                        // Update total profit
                        totalProfit += profit;
    
                        // Notify the player
                        Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                            "Processed: " + quantity + " " + material + " | Profit: " + profit + " coins"));
                    } catch (Exception e) {
                        // Handle parsing errors
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                            "Error processing message"));
                    }
                    } 
                    else {
                    // Ignore messages that don't contain "sacks"
                    return;
                }
            }
        });
    }
    private void getPricePerCollection(String material) {
        try {
            String apiUrl = "https://api.hypixel.net/skyblock/bazaar";
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();

                JsonObject products = jsonResponse.getAsJsonObject("products");
                if (products.has(material)) {
                    JsonObject materialData = products.getAsJsonObject(material);
                    JsonObject quickStatus = materialData.getAsJsonObject("quick_status");
                    return quickStatus.get("sellPrice").getAsDouble(); // Return the sell price
                } else {
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                        "Material not found in Bazaar: " + material));
                    return 0.0;
                }
            } else {
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                    "Failed to fetch Bazaar data. HTTP Response Code: " + connection.getResponseCode()));
                return 0.0;
            }
        } catch (Exception e) {
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                "Error fetching Bazaar data: " + e.getMessage()));
            e.printStackTrace();
            return 0.0;
        }
}}