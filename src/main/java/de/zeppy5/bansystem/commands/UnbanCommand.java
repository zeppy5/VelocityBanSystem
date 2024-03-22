package de.zeppy5.bansystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.zeppy5.bansystem.BanSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UnbanCommand implements SimpleCommand {

    private final BanSystem instance;

    public UnbanCommand(BanSystem instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        new Thread(() -> {
            CommandSource sender = invocation.source();
            String[] args = invocation.arguments();

            if (invocation.arguments().length < 1) {
                syntax(sender);
                return;
            }

            Optional<Player> player = instance.getProxyServer().getPlayer(args[0]);
            String uuid = null;

            if (player.isPresent()) {
                uuid = String.valueOf(player.get().getUniqueId());
            }

            int n = 0;
            while (uuid == null && n < 3) {
                uuid = instance.getMojangAPI().getUUID(args[0]);
                n++;
                if (uuid == null) {
                    sender.sendMessage(Component.text("Failed to fetch player info from Mojang API. Trying again", NamedTextColor.YELLOW));
                }
            }

            if (uuid == null) {
                sender.sendMessage(Component.text("Failed to fetch player info from Mojang API three times. Giving up", NamedTextColor.RED));
                return;
            }

            instance.getBanManager().unbanPlayer(uuid);

            sender.sendMessage(Component.text("Unbanned player: ", NamedTextColor.GREEN)
                    .append(Component.text(args[0] + " (UUID: " + uuid + ")", NamedTextColor.BLUE))
            );
        }).start();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();

        List<String> list = new ArrayList<>();

        if (args.length == 0) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (args.length == 1) {
            instance.getProxyServer().getAllPlayers().forEach(player -> list.add(player.getUsername()));
        }

        List<String> completerList = new ArrayList<>();
        String currentArg = args[args.length - 1].toLowerCase();
        for (String s : list) {
            String s1 = s.toLowerCase();
            if (s1.startsWith(currentArg)) {
                completerList.add(s);
            }
        }

        return CompletableFuture.completedFuture(completerList);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("ban.unban");
    }

    private void syntax(CommandSource sender) {
        sender.sendMessage(Component.text("Usage: /unban <Player>", NamedTextColor.RED));
    }
}
