package de.zeppy5.bansystem.ban;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import de.zeppy5.bansystem.BanSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class BanListener {

    private final BanSystem instance;

    public BanListener(BanSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        ResultSet rs = instance.getBanManager().checkBanned(String.valueOf(event.getPlayer().getUniqueId()));

        if (rs != null) {
            try {
                while (rs.next()) {
                    if (((System.currentTimeMillis() / 1000) > rs.getLong("EXPIRES")) && rs.getLong("EXPIRES") != -1) {
                        instance.getBanManager().expireBan(rs.getString("ID"));
                    } else {
                        event.setResult(ResultedEvent.ComponentResult.denied(reason(rs)));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                instance.getLogger().error("An internal Error occurred");
                event.getPlayer().disconnect(Component.text("An internal Error occurred"));
            }
        }
    }

    public TextComponent reason(ResultSet rs) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            long expires = rs.getLong("EXPIRES");
            String date;
            if (expires == -1) {
                date = "PERMANENT";
            } else {
                date = dateFormat.format(Date.from(Instant.ofEpochSecond(expires)));
            }

            String reason = rs.getString("REASON");

            String banID = rs.getString("ID");
            return Component.text("You are banned from this server!\n", NamedTextColor.RED)
                    .append(Component.text("Reason: ", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))
                    .append(Component.text(reason, NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
                    .append(Component.text("\nExpires: ", NamedTextColor.RED))
                    .append(Component.text(date, NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true))
                    .append(Component.text("\nBan-ID: ", NamedTextColor.GRAY))
                    .append(Component.text(banID, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
        } catch (SQLException e) {
            e.printStackTrace();
            return Component.text("");
        }
    }

}
