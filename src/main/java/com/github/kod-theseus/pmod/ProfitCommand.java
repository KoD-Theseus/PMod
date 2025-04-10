package com.github.kodtheseus.forgetemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ChatComponentText;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

public class ProfitCommand implements ICommand {

    // Shared instances to prevent desynchronization
    private static final Stopwatch stopwatch = new Stopwatch();
    private static final ProfitTracker profitTracker = new ProfitTracker();
    private static final BazaarPriceCache bazaarPriceCache = new BazaarPriceCache();

    @Override
    public String getCommandName() {
        return "profit";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/profit <start|pause|resume|check|stop>";
    }
    @Override
    public List<String> getCommandAliases() {
        return Collections.emptyList();
    }
    @Override
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("§cPlease specify a subcommand: start, pause, resume, or check."));
            return;
        }
        // Using shared singleton instances
        switch (args[0].toLowerCase()) {
            case "start":
                ProfitCommand.stopwatch.start();
                itemTracker.startTracker();
                sender.addChatMessage(new ChatComponentText("§aStopwatch and item tracking started!"));
                break;
            case "pause":
                ProfitCommand.stopwatch.stop();
                sender.addChatMessage(new ChatComponentText("§aStopwatch paused!"));
                if (bazaarPriceCache.isCacheExpired()) {
                    bazaarPriceCache.refreshBazaarPrices(); // No API key needed
                }
                if (bazaarPriceCache.isCacheExpired()) {
                    bazaarPriceCache.refreshBazaarPrices(); // No API key needed
                }
                double totalProfitPause = ProfitCommand.profitTracker.calculateTotalProfit(ProfitCommand.bazaarPriceCache);
                sender.addChatMessage(new ChatComponentText("§aTotal profit: " + totalProfitPause + " coins."));
                break;
            case "resume":
                ProfitCommand.stopwatch.start();
                sender.addChatMessage(new ChatComponentText("§aStopwatch resumed!"));
                break;
            case "check":
                if (ProfitCommand.bazaarPriceCache.isCacheExpired()) {
                    ProfitCommand.bazaarPriceCache.refreshBazaarPrices(); // No API key needed
                }
                double totalProfitCheck = ProfitCommand.profitTracker.calculateTotalProfit(ProfitCommand.bazaarPriceCache);
                double elapsedTime = ProfitCommand.stopwatch.getElapsedTimeInSeconds();
                sender.addChatMessage(new ChatComponentText("§aTotal profit: " + totalProfitCheck + " coins."));
                sender.addChatMessage(new ChatComponentText("§aRate: " + (elapsedTime > 0 ? (totalProfitCheck / elapsedTime) : 0) + " coins/second."));
                break;
            case "stop":
                stopwatch.stop();
                itemTracker.stopTracker();
                sender.addChatMessage(new ChatComponentText("§cItem tracking and stopwatch stopped!"));
                break;
            case "reset":
                stopwatch.stop();
                profitTracker.reset();
                itemTracker.resetTracker();
                sender.addChatMessage(new ChatComponentText("§eStopwatch, profit tracking, and item tracking data reset."));
                break;
            default:
                sender.addChatMessage(new ChatComponentText("§aUnknown subcommand. Use start, pause, resume, check, stop, or reset."));
                break;
        }
    }


    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }
}
