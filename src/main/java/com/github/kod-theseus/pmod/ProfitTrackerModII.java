package com.github.kodtheseus.forgetemplate;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = "profittrackermodii")
public class ProfitTrackerModII {
    public static final Logger LOGGER = LogManager.getLogger(ProfitTrackerModII.class);
    private final BazaarPriceCache bazaarPriceCache = new BazaarPriceCache();
    private static String apiKey; // Declare the apiKey variable

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load API Key from Configuration
        Configuration config = new Configuration(new File(event.getModConfigurationDirectory(), "profittrackermodii.cfg"));
        config.load();

        // Define the key field
        apiKey = config.getString("API Key", "general", "", "Your Hypixel API Key");

        config.save();

        // Log the API Key during initialization to verify it's loaded
        if (apiKey == null || apiKey.isEmpty()) {
            LOGGER.warn("No API key found in the configuration file. Some features may not work.");
        } else {
            LOGGER.info("API key loaded successfully.");
        }

        // Register commands
        ClientCommandHandler.instance.registerCommand(new ProfitCommand());
        ClientCommandHandler.instance.registerCommand(new ItemTrackerCommand());
        MinecraftForge.EVENT_BUS.register(new ItemTracker());
    }

    public static void sendPlayerChat(String message) {
        // Ensure thePlayer is not null before sending chat messages
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
        } else {
            LOGGER.warn("Attempted to send chat message while the player is not loaded: " + message);
        }
    }

    public static String getApiKey() {
        return apiKey;
    }
}