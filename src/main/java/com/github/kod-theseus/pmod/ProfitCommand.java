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
    private static final String apiKey = "2631dcaa-5b2a-402e-8cf7-e3f1594c7225";

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
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("§cPlease specify a subcommand: start, pause, resume, or check."));
            return;
        }
        Stopwatch stopwatch = new Stopwatch();
        ProfitTracker profitTracker = new ProfitTracker(new HashMap<>());
        BazaarPriceCache bazaarPriceCache = new BazaarPriceCache();
        switch (args[0].toLowerCase()) {
            case "start":
                stopwatch.start();
                sender.addChatMessage(new ChatComponentText("§aStopwatch started!"));
                break;
            case "pause":
                stopwatch.stop();
                sender.addChatMessage(new ChatComponentText("§aStopwatch paused!"));
                if (bazaarPriceCache.isCacheExpired()) {
                    bazaarPriceCache.refreshBazaarPrices("<KEY>"); // Replace with an actual API key
                }
                double totalProfitPause = profitTracker.calculateTotalProfit(bazaarPriceCache);
                sender.addChatMessage(new ChatComponentText("§aTotal profit: " + totalProfitPause + " coins."));
                break;
            case "resume":
                stopwatch.start();
                sender.addChatMessage(new ChatComponentText("§aStopwatch resumed!"));
                break;
            case "check":
                if (bazaarPriceCache.isCacheExpired()) {
                    bazaarPriceCache.refreshBazaarPrices("<KEY>");
                }
                double totalProfitCheck = profitTracker.calculateTotalProfit(bazaarPriceCache);
                double elapsedTime = stopwatch.getElapsedTimeInSeconds();
                sender.addChatMessage(new ChatComponentText("§aTotal profit: " + totalProfitCheck + " coins."));
                sender.addChatMessage(new ChatComponentText("§aRate: " + (elapsedTime > 0 ? (totalProfitCheck / elapsedTime) : 0) + " coins/second."));
                break;
            default:
                sender.addChatMessage(new ChatComponentText("§aUnknown subcommand. Use start, pause, resume, or check."));
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
