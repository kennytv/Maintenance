/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.kennytv.maintenance.core.config;

import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Further modified version of the <a href="https://github.com/PSandro/SimpleConfig">SimpleConfig</a>SimpleConfig project of PSandro.
 *
 * @author PSandro on 26.01.19
 * @author kennytv
 */
public final class Config extends ConfigSection {

    private static final String AWESOME_HEADER =
            """
                    ###################################################################################################################
                    #   __  __       _       _                                    _             _                          _          #
                    #  |  \\/  | __ _(_)_ __ | |_ ___ _ __   __ _ _ __   ___ ___  | |__  _   _  | | _____ _ __  _ __  _   _| |___   __ #
                    #  | |\\/| |/ _` | | '_ \\| __/ _ \\ '_ \\ / _` | '_ \\ / __/ _ \\ | '_ \\| | | | | |/ / _ \\ '_ \\| '_ \\| | | | __\\ \\ / / #
                    #  | |  | | (_| | | | | | ||  __/ | | | (_| | | | | (_|  __/ | |_) | |_| | |   <  __/ | | | | | | |_| | |_ \\ V /  #
                    #  |_|  |_|\\__,_|_|_| |_|\\__\\___|_| |_|\\__,_|_| |_|\\___\\___| |_.__/ \\__, | |_|\\_\\___|_| |_|_| |_|\\__, |\\__| \\_/   #
                    #                                                                  |___/                        |___/             #
                    ###################################################################################################################
                    # You can report bugs here: https://github.com/kennytv/Maintenance/issues
                    # If you need any other help/support, you can also join my Discord server: https://discord.gg/vGCUzHq
                    # The config and language files use MiniMessage, NOT legacy text for input. Use https://webui.adventure.kyori.net/ to edit and preview the formatted text.
                    # For a full list of formats and fancy examples of MiniMessage, see https://docs.adventure.kyori.net/minimessage/format.html
                    """;
    private final Yaml yaml = createYaml();
    private final File file;
    private final Set<String> unsupportedFields;
    private Map<String, String[]> comments = new HashMap<>();
    private String header;

    public Config(final File file, final String... unsupportedFields) {
        super(null, "");
        this.file = file;
        this.unsupportedFields = unsupportedFields.length == 0 ? Collections.emptySet() : Sets.newHashSet(unsupportedFields);
    }

    public void load() throws IOException {
        final String data = Files.readString(this.file.toPath());
        final Map<String, Object> map = yaml.load(data);
        this.values = map != null ? map : new LinkedHashMap<>();
        this.comments = ConfigSerializer.deserializeComments(data);

        final String[] header = comments.remove(".header");
        if (header != null) {
            this.header = String.join("\n", header);
        }

        final boolean removedFields = values.keySet().removeIf(key -> {
            final String[] split = key.split("\\.");
            String splitKey = "";
            for (final String s : split) {
                splitKey += s;
                if (!unsupportedFields.contains(splitKey)) {
                    splitKey += ".";
                    continue;
                }

                // Unsupported field
                comments.remove(key);
                return true;
            }
            return false;
        });
        if (removedFields) {
            save();
        }
    }

    public void save() throws IOException {
        saveTo(file);
    }

    public void saveTo(final File file) throws IOException {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        Files.writeString(file.toPath(), toString());
    }

    public void replaceComments(final Config fromConfig) {
        this.comments = new HashMap<>(fromConfig.comments);
    }

    private static Yaml createYaml() {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setIndent(2);
        options.setWidth(10_000); // be sneaky because autobreak on saving looks disgusting
        return new Yaml(options);
    }

    public void clear() {
        this.values.clear();
        this.comments.clear();
        this.header = null;
    }

    public Map<String, String[]> getComments() {
        return comments;
    }

    public Set<String> getUnsupportedFields() {
        return unsupportedFields;
    }

    @Override
    public Config getRoot() {
        return this;
    }

    @Nullable
    public String getHeader() {
        return header;
    }

    public void resetAwesomeHeader() {
        this.header = AWESOME_HEADER;
    }

    @Override
    public String toString() {
        return ConfigSerializer.serialize(this.header, this.values, this.comments, this.yaml);
    }
}
