package com.github.kodtheseus.forgetemplate;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.ChatComponentText;
import java.util.Arrays;
public class ItemTrackerCommand implements ICommand {
    private static final ItemTracker itemTracker = new ItemTracker();

    @Override
    public String getCommandName() {
        return "itemtracker";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/itemtracker <start|stop|reset>";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.emptyList(); // No aliases for this command
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // Check if user provided a subcommand
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("§cPlease specify a subcommand: start, stop, or reset."));
            return;
        }

        // Switch based on the subcommand provided
        switch (args[0].toLowerCase()) {
            case "start":
                itemTracker.startTracker(); // Start tracking
                sender.addChatMessage(new ChatComponentText("§aItem tracking started!"));
                break;

            case "stop":
                itemTracker.stopTracker(); // Stop tracking
                sender.addChatMessage(new ChatComponentText("§cItem tracking stopped!"));
                break;

            case "reset":
                itemTracker.resetTracker(); // Reset tracking data
                sender.addChatMessage(new ChatComponentText("§eItem tracking has been reset."));
                break;

            default:
                sender.addChatMessage(new ChatComponentText("§cUnknown subcommand. Use start, stop, or reset."));
                break;
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // Everyone can use this command
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        // Auto-complete with possible subcommands: start, stop, reset
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "reset");
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false; // No username is used in this command
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0; // Default comparison
    }
}