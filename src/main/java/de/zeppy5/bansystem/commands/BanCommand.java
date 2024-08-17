package de.zeppy5.bansystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.zeppy5.bansystem.BanSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BanCommand implements SimpleCommand {

    private final BanSystem instance;

    public BanCommand(BanSystem instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        new Thread(() -> {

            CommandSource sender = invocation.source();
            String[] args = invocation.arguments();

            if (invocation.arguments().length < 2) {
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

            String bannedBy;
            if (sender instanceof Player) {
                bannedBy = String.valueOf(((Player) sender).getUniqueId());
            } else {
                bannedBy = "CONSOLE";
            }

            if (args.length >= 3) {

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < args.length - 2; i++) {
                    stringBuilder.append(args[i+2]);
                    stringBuilder.append(' ');
                }

                String reason = stringBuilder.toString();

                if (reason.length() > 200) {
                    sender.sendMessage(Component.text("Reason can only be 200 characters long!", NamedTextColor.RED));
                    return;
                }

                long length;

                try {
                    if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("s")) {
                        length = Long.parseLong(args[1].substring(0, args[1].length() - 1));
                    } else if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("m")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(args[1].substring(0, args[1].length() - 1)), TimeUnit.MINUTES);
                    } else if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("h")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(args[1].substring(0, args[1].length() - 1)), TimeUnit.HOURS);
                    } else if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("d")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(args[1].substring(0, args[1].length() - 1)), TimeUnit.DAYS);
                    } else if (args[1].equalsIgnoreCase("-1")) {
                        length = -1;
                    } else {
                        syntax(sender);
                        return;
                    }
                } catch (NumberFormatException e) {
                    syntax(sender);
                    return;
                }

                instance.getBanManager().banPlayer(uuid, length, reason, bannedBy);

                player.ifPresent(p -> p.disconnect(instance.getBanManager().reason(reason)));

                sender.sendMessage(Component.text("Banned player: " + args[0] + " (UUID: " + uuid + ")"
                                + " for " + (args[1].equals("-1") ? "PERMANENT" : args[1]) + ". Reason: " + reason
                        , NamedTextColor.GREEN));

            } else if (args.length == 2) {

                int id;

                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    syntax(sender);
                    return;
                }

                String reason = instance.getBanReasonsManager().getReason(id);
                String lengthString = instance.getBanReasonsManager().getLength(id);

                if (reason == null || lengthString == null) {
                    syntax(sender);
                    return;
                }


                if (reason.length() > 200) {
                    sender.sendMessage(Component.text("Reason can only be 200 characters long!", NamedTextColor.RED));
                    return;
                }

                long length;

                try {
                    if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("s")) {
                        length = Long.parseLong(lengthString.substring(0, lengthString.length() - 1));
                    } else if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("m")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(lengthString.substring(0, lengthString.length() - 1)), TimeUnit.MINUTES);
                    } else if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("h")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(lengthString.substring(0, lengthString.length() - 1)), TimeUnit.HOURS);
                    } else if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("d")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(lengthString.substring(0, lengthString.length() - 1)), TimeUnit.DAYS);
                    } else if (lengthString.equalsIgnoreCase("-1")) {
                        length = -1;
                    } else {
                        syntax(sender);
                        return;
                    }
                } catch (NumberFormatException e) {
                    syntax(sender);
                    return;
                }

                instance.getBanManager().banPlayer(uuid, length, reason, bannedBy);

                player.ifPresent(p -> p.disconnect(instance.getBanManager().reason(reason)));

                sender.sendMessage(Component.text("Banned player: " + args[0] + " (UUID: " + uuid + ")"
                                + " for " + (lengthString.equals("-1") ? "PERMANENT" : args[1]) + ". Reason: " + reason
                        , NamedTextColor.GREEN)
                );

            } else {
                syntax(sender);
            }
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
        return invocation.source().hasPermission("ban.ban");
    }

    private void syntax(CommandSource sender) {
        List<Map<?, ?>> reasonList = instance.getBanReasonsManager().getList();

        reasonList.forEach(map -> {
            String id = String.valueOf(map.get("id"));
            String reason = String.valueOf(map.get("reason"));
            String length = String.valueOf(map.get("length")).equals("-1") ? "PERMANENT" : String.valueOf(map.get("length"));

            sender.sendMessage(Component.text("ID: ", NamedTextColor.RED)
                    .append(Component.text(id, NamedTextColor.DARK_RED))
                    .append(Component.text(" Length: ", NamedTextColor.RED))
                    .append(Component.text(length, NamedTextColor.DARK_RED))
                    .append(Component.text(" Reason: ", NamedTextColor.RED))
                    .append(Component.text(reason, NamedTextColor.DARK_RED))
            );
        });

        sender.sendMessage(Component.text("Usage: ", NamedTextColor.RED));
        sender.sendMessage(Component.text("/ban <Player> <Reason-ID>", NamedTextColor.DARK_RED));
        sender.sendMessage(Component.text("/ban <Player> <Duration> <Reason>", NamedTextColor.DARK_RED));
        sender.sendMessage(Component.text("Duration:", NamedTextColor.RED));
        sender.sendMessage(Component.text("Use s for Seconds", NamedTextColor.RED));
        sender.sendMessage(Component.text("Use m for Minutes", NamedTextColor.RED));
        sender.sendMessage(Component.text("Use h for Hours", NamedTextColor.RED));
        sender.sendMessage(Component.text("Use d for Days", NamedTextColor.RED));
        sender.sendMessage(Component.text("Use -1 for Permanent", NamedTextColor.RED));
    }
}
