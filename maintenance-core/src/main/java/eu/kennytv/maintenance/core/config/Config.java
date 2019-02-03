/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Further modified version of the SimpleConfig project of PSandro (https://github.com/PSandro/SimpleConfig).
 *
 * @author PSandro on 26.01.19
 * @author KennyTV
 */
public final class Config extends ConfigSection {

    private static final String AWESOME_HEADER =
            "#######################################################################################################################\n" +
                    "#   __  __       _       _                                    _             _  __                     _______     __  #\n" +
                    "#  |  \\/  | __ _(_)_ __ | |_ ___ _ __   __ _ _ __   ___ ___  | |__  _   _  | |/ /___ _ __  _ __  _   |_   _\\ \\   / /  #\n" +
                    "#  | |\\/| |/ _` | | '_ \\| __/ _ \\ '_ \\ / _` | '_ \\ / __/ _ \\ | '_ \\| | | | | ' // _ \\ '_ \\| '_ \\| | | || |  \\ \\ / /   #\n" +
                    "#  | |  | | (_| | | | | | ||  __/ | | | (_| | | | | (_|  __/ | |_) | |_| | | . \\  __/ | | | | | | |_| || |   \\ V /    #\n" +
                    "#  |_|  |_|\\__,_|_|_| |_|\\__\\___|_| |_|\\__,_|_| |_|\\___\\___| |_.__/ \\__, | |_|\\_\\___|_| |_|_| |_|\\__, ||_|    \\_/     #\n" +
                    "#                                                                   |___/                        |___/                #\n" +
                    "#######################################################################################################################\n" +
                    "# You can report bugs here: https://github.com/KennyTV/Maintenance/issues\n" +
                    "# If you need any other help/support, you can also join my Discord server: https://discord.gg/vGCUzHq\n";
    private final Yaml yaml = createYaml();
    private final File file;
    private final Set<String> unsupportedFields;
    private Map<String, String[]> comments = new HashMap<>();
    private String header;

    public Config(final File file, final String... unsupportedFields) {
        this.file = file;
        this.unsupportedFields = new HashSet<>(Arrays.asList(unsupportedFields));
    }

    public void load() throws IOException {
        final String data = new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
        final Map<String, Object> map = yaml.load(data);
        this.values = map != null ? map : new LinkedHashMap<>();
        this.comments = ConfigSerializer.deserializeComments(data);
        if (comments.containsKey(".header"))
            this.header = String.join("\n", comments.remove(".header"));

        final boolean removedFields = values.keySet().removeIf(key -> {
            final String[] split = key.split("\\.");
            String splitKey = "";
            boolean remove = false;
            for (final String s : split) {
                splitKey += s;
                if (!unsupportedFields.contains(splitKey)) {
                    splitKey += ".";
                    continue;
                }
                remove = true;
                break;
            }

            if (remove)
                comments.remove(key);
            return remove;
        });
        if (removedFields) {
            save();
        }
    }

    public void save() throws IOException {
        final byte[] bytes = this.saveConfigToString().getBytes(StandardCharsets.UTF_8);
        this.file.getParentFile().mkdirs();
        this.file.createNewFile();
        Files.write(this.file.toPath(), bytes);
    }

    @Override
    public void set(final String key, final Object value, final String... comments) {
        if (value == null) {
            remove(key);
        } else {
            this.values.put(key, value);
            this.comments.put(key, comments);
        }
    }

    @Override
    public void remove(final String key) {
        this.values.remove(key);
        this.comments.remove(key);
    }

    /**
     * Convenience method, not furthe established as currently not necessary.
     *
     * @see #getSection(String)
     * @deprecated this config is only made for a quite simple use, only goes one level deeper
     */
    @Deprecated
    public Object getDeep(final String key) {
        final String[] split = key.split("\\.", 2);
        if (split.length != 2) return get(key);

        final Object o = getObject(split[0]);
        if (!(o instanceof Map)) return null;

        final Map<String, Object> map = (Map<String, Object>) o;
        return map.get(split[1]);
    }

    public ConfigSection getSection(final String key) {
        final Object o = getObject(key);
        return o instanceof Map ? new ConfigSection((Map<String, Object>) o) : null;
    }

    private static Yaml createYaml() {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setIndent(2);
        return new Yaml(options);
    }

    public String saveConfigToString() {
        return ConfigSerializer.serialize(this.header, this.values, this.comments, this.yaml);
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

    public String getHeader() {
        return header;
    }

    public void resetAwesomeHeader() {
        this.header = AWESOME_HEADER;
    }


}
