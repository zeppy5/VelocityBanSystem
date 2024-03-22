package de.zeppy5.bansystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.zeppy5.bansystem.BanSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ListBansCommand implements SimpleCommand {

    private final int itemsPerPage = 5;

    private final BanSystem instance;

    public ListBansCommand(BanSystem instance) {
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

            try {
                ResultSet rs = instance.getBanManager().getBans(uuid);
                int size = 0;
                if (rs != null) {
                    rs.last();
                    size = rs.getRow();
                    rs.beforeFirst();
                }

                if (size == 0) {
                    sender.sendMessage(Component.text("Player has no bans", NamedTextColor.GREEN));
                    return;
                }

                int page = 0;
                if (args.length == 2) {
                    try {
                        page = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException e) {
                        syntax(sender);
                        return;
                    }
                }

                if ((page + 1) > (int) Math.ceil((double)size / itemsPerPage)) {
                    sender.sendMessage(Component.text("Invalid page number, there are ", NamedTextColor.RED)
                            .append(Component.text((int) Math.ceil((double)size / itemsPerPage), NamedTextColor.BLUE))
                            .append(Component.text(" pages", NamedTextColor.RED))
                    );
                    return;
                }

                if (page < 0) {
                    sender.sendMessage(Component.text("Invalid page number, there are ", NamedTextColor.RED)
                            .append(Component.text((int) Math.ceil((double)size / itemsPerPage), NamedTextColor.BLUE))
                            .append(Component.text(" pages", NamedTextColor.RED))
                    );
                    return;
                }

                printBans(sender, rs, size, page);


            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage(Component.text("An internal Error occurred", NamedTextColor.RED));
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
        return invocation.source().hasPermission("ban.list");
    }

    private void syntax(CommandSource sender) {
        sender.sendMessage(Component.text("Usage: /listbans <Player> [<Page>]", NamedTextColor.RED));
    }

    private void printBans(CommandSource sender, ResultSet rs, int size, int page) throws SQLException {
        sender.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Page " + (page + 1) + " / " + (int) Math.ceil((double)size / itemsPerPage), NamedTextColor.GREEN));

        rs.beforeFirst();

        for (int i = 0; i < (page * itemsPerPage); i++) {
            rs.next();
        }

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        int s = Math.min((size - (page * itemsPerPage)), itemsPerPage);

        for (int i = 0; i < s; i++) {
            rs.next();
            sender.sendMessage(Component.text("⌈¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯⌉", NamedTextColor.DARK_PURPLE));

            String id = rs.getString("ID");
            String reason = rs.getString("REASON");
            String bannedBy = rs.getString("BANNED_BY");

            String bannedOn = dateFormat.format(Date.from(Instant.ofEpochSecond(rs.getLong("DATE"))));

            long expires = rs.getLong("EXPIRES");
            String date;
            if (expires == -1) {
                date = "PERMANENT";
            } else {
                date = dateFormat.format(Date.from(Instant.ofEpochSecond(expires)));
            }

            String status = instance.getBanManager().getStatusFromID(rs.getInt("STATUS"));

            sender.sendMessage(LegacyComponentSerializer.legacy('&').deserialize(
                    "&bBan-ID: &a" + id
                            + "&b\nReason: &a" + reason
                            + "&b\nBan Date: &a" + bannedOn
                            + "&b\nExpire Date: &a" + date
                            + "&b\nBanned by: &a" + bannedBy
                            + "&b\nStatus: &a" + status));

            sender.sendMessage(Component.text("⌊_________________________⌋", NamedTextColor.DARK_PURPLE));
        }

        sender.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.YELLOW));
    }
}
