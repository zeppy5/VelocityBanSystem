package de.zeppy5.bansystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.zeppy5.bansystem.BanSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RemoveBanCommand implements SimpleCommand {

    private final BanSystem instance;

    public RemoveBanCommand(BanSystem instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        new Thread(() -> {
            CommandSource sender = invocation.source();
            String[] args = invocation.arguments();

            if (args.length < 1) {
                syntax(sender);
                return;
            }

            instance.getBanManager().removeBan(args[0]);

            sender.sendMessage(Component.text("Tried to remove ban with ID: ", NamedTextColor.GREEN)
                    .append(Component.text(args[0], NamedTextColor.BLUE))
            );
        }).start();

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("ban.remove");
    }

    private void syntax(CommandSource sender) {
        sender.sendMessage(Component.text("Usage: /removeban <ban-id>", NamedTextColor.RED));
    }
}
