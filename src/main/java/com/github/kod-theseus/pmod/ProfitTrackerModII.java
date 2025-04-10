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

    // Initializing Bazaar data during preInit

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Remove API key configuration loading.
        Configuration config = new Configuration(new File(event.getModConfigurationDirectory(), "profittrackermodii.cfg"));
        config.load();
        config.save();

        // Refresh Bazaar data at initialization
        bazaarPriceCache.refreshBazaarPrices();

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

}