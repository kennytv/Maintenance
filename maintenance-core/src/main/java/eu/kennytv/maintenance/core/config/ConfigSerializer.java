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

import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Further modified version of the SimpleConfig project of PSandro (https://github.com/PSandro/SimpleConfig).
 *
 * @author PSandro on 26.01.19
 * @author KennyTV
 */
public final class ConfigSerializer {

    private static final String[] EMPTY = new String[0];
    private static final int INDENT_UNIT = 2;
    private static final String PATH_SEPARATOR_STRING = ".";
    private static final String PATH_SEPERATOR_QUOTED = Pattern.quote(PATH_SEPARATOR_STRING);

    public static String serialize(final String header, final Map<String, Object> data, final Map<String, String[]> comments, final Yaml yaml) {
        if (data.isEmpty()) return yaml.dump(null);

        final String rawYaml = header != null && !header.isEmpty() ? header + yaml.dump(data) : yaml.dump(data);
        final StringBuilder fileData = new StringBuilder();
        int currentIndents = 0;
        String key = "";
        for (final String line : rawYaml.split("\n")) {
            if (line.isEmpty()) continue;

            final int indent = getIndents(line);
            final int indents = indent / INDENT_UNIT;

            final String indentText = indent > 0 ? line.substring(0, indent) : "";
            if (indents <= currentIndents) {
                final String[] array = key.split(PATH_SEPERATOR_QUOTED);
                final int backspace = currentIndents - indents + 1;
                key = join(array, array.length - backspace);
            }

            final String separator = key.isEmpty() ? "" : PATH_SEPARATOR_STRING;
            final String lineKey = line.contains(":") ? line.split(Pattern.quote(":"))[0] : line;
            key += separator + lineKey.substring(indent);

            currentIndents = indents;

            final String[] strings = comments.get(key);
            if (strings != null) {
                for (final String comment : strings) {
                    fileData.append(indentText).append(comment).append('\n');
                }
            }

            fileData.append(line).append('\n');
        }
        return fileData.toString();
    }

    public static Map<String, String[]> deserializeComments(final String data) {
        final Map<String, String[]> comments = new HashMap<>();
        final List<String> currentComments = new ArrayList<>();
        final String[] split = data.split("\n");

        boolean header = true;
        int currentIndents = 0;
        String key = "";
        for (int i = 0; i < split.length; i++) {
            final String line = split[i];
            final String s = line.trim();
            if (s.startsWith("#")) {
                currentComments.add(s);
            } else {
                if (!s.contains(":")) {
                    if (header) {
                        if (!currentComments.isEmpty()) {
                            currentComments.add("\n");
                            comments.put(".header", currentComments.toArray(EMPTY));
                        }
                        header = false;
                    }
                    currentComments.clear();
                    continue;
                }

                header = false;
                final int back = i - currentComments.size() - 1;
                if (back >= 0 && split[back].trim().isEmpty()) {
                    currentComments.add(0, "");
                }

                final int indent = getIndents(line);
                final int indents = indent / INDENT_UNIT;
                if (indents <= currentIndents) {
                    final String[] array = key.split(PATH_SEPERATOR_QUOTED);
                    final int backspace = currentIndents - indents + 1;
                    key = join(array, array.length - backspace);
                }

                final String separator = key.isEmpty() ? "" : PATH_SEPARATOR_STRING;
                final String lineKey = line.contains(":") ? line.split(Pattern.quote(":"))[0] : line;
                key += separator + lineKey.substring(indent);
                currentIndents = indents;

                if (!currentComments.isEmpty()) {
                    comments.put(key, currentComments.toArray(EMPTY));
                    currentComments.clear();
                }
            }
        }
        return comments;
    }

    private static int getIndents(final String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') count++;
            else break;
        }
        return count;
    }

    private static String join(final String[] array, final int length) {
        final String[] copy = new String[length];
        System.arraycopy(array, 0, copy, 0, length);
        return String.join(PATH_SEPARATOR_STRING, copy);
    }
}
