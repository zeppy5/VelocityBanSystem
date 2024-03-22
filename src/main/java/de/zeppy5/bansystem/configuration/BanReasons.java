package de.zeppy5.bansystem.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BanReasons {

    private final YamlDocument config;

    public BanReasons(Path dir, Logger logger) {

        try {
            config = YamlDocument.create(new File(dir.toFile(), "reasons.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/reasons.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Could not load reasons.yml");
            throw new RuntimeException();
        }

    }

    public List<Map<?, ?>> getList() {
        return config.getMapList(Route.from("reasons"));
    }

    public String getReason(int id) {
        return String.valueOf(Objects.requireNonNull(getList().stream()
                .filter(map -> map.get("id").equals(id))
                .findFirst().orElse(new HashMap<>())).get("reason"));
    }

    public String getLength(int id) {
        return String.valueOf(Objects.requireNonNull(getList().stream()
                .filter(map -> map.get("id").equals(id))
                .findFirst().orElse(new HashMap<>())).get("length"));
    }
}
