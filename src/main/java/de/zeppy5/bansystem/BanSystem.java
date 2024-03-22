package de.zeppy5.bansystem;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.zeppy5.bansystem.ban.BanListener;
import de.zeppy5.bansystem.ban.BanManager;
import de.zeppy5.bansystem.commands.BanCommand;
import de.zeppy5.bansystem.commands.ListBansCommand;
import de.zeppy5.bansystem.commands.RemoveBanCommand;
import de.zeppy5.bansystem.commands.UnbanCommand;
import de.zeppy5.bansystem.configuration.BanReasons;
import de.zeppy5.bansystem.configuration.Config;
import de.zeppy5.bansystem.util.MojangAPI;
import de.zeppy5.bansystem.util.MySQL;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "bansystem",
        name = "VelocityBanSystem",
        version = "1.1"
)
public class BanSystem {

    private final MySQL mySQL;
    private final MojangAPI mojangAPI;
    private final BanManager banManager;
    private final BanReasons banReasonsManager;
    private final Config configManager;

    private final YamlDocument config;

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public BanSystem(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;

        mojangAPI = new MojangAPI(logger);

        banReasonsManager = new BanReasons(dataDirectory, logger);

        configManager = new Config(dataDirectory, logger);
        config = configManager.getConfig();

        String host = config.getString(Route.from("host"));
        String database = config.getString(Route.from("database"));
        String user = config.getString(Route.from("user"));
        String password = config.getString(Route.from("user"));
        int port = config.getInt(Route.from("port"));

        mySQL = new MySQL(host, database, user, password, port);

        banManager = new BanManager(this);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        EventManager eventManager = server.getEventManager();
        CommandManager commandManager = server.getCommandManager();

        CommandMeta ban = commandManager.metaBuilder("ban").plugin(this).build();
        commandManager.register(ban, new BanCommand(this));

        CommandMeta unban = commandManager.metaBuilder("unban").plugin(this).build();
        commandManager.register(unban, new UnbanCommand(this));

        CommandMeta removeBan = commandManager.metaBuilder("removeban").plugin(this).build();
        commandManager.register(removeBan, new RemoveBanCommand(this));

        CommandMeta listBans = commandManager.metaBuilder("listbans").plugin(this).build();
        commandManager.register(listBans, new ListBansCommand(this));

        eventManager.register(this, new BanListener(this));
    }

    public ProxyServer getProxyServer() {
        return server;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public MojangAPI getMojangAPI() {
        return mojangAPI;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public BanReasons getBanReasonsManager() {
        return banReasonsManager;
    }

    public Logger getLogger() {
        return logger;
    }
}
