/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
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
    private static final String PATH_SEPARATOR_STRING = ".";
    private static final String PATH_SEPARATOR_QUOTED = Pattern.quote(PATH_SEPARATOR_STRING);
    private static final int INDENT_UNIT = 2;

    public static String serialize(final String header, final Map<String, Object> data, final Map<String, String[]> comments, final Yaml yaml) {
        if (data.isEmpty()) {
            return yaml.dump(null);
        }

        final String rawYaml = yaml.dump(data);
        final StringBuilder fileData = new StringBuilder();
        int currentKeyIndents = 0;
        String key = "";
        for (final String line : rawYaml.split("\n")) {
            if (line.isEmpty()) continue;

            final int indent = getIndents(line);
            final int indents = indent / INDENT_UNIT;
            final boolean keyLine;
            final String substring = line.substring(indent);
            if (substring.trim().isEmpty() || substring.charAt(0) == '-') {
                keyLine = false;
            } else if (indents <= currentKeyIndents) {
                final String[] array = key.split(PATH_SEPARATOR_QUOTED);
                final int backspace = currentKeyIndents - indents + 1;
                key = join(array, array.length - backspace);
                keyLine = true;
            } else {
                keyLine = line.indexOf(':') != -1;
            }

            if (!keyLine) {
                // Nothing to do, go to next line
                fileData.append(line).append('\n');
                continue;
            }

            final String newKey = substring.split(Pattern.quote(":"))[0]; // Not sure about the quote thing, so I'll just keep it :aaa:
            if (!key.isEmpty()) {
                key += PATH_SEPARATOR_STRING;
            }
            key += newKey;

            // Add comments if present
            if (comments != null) {
                final String[] strings = comments.get(key);
                if (strings != null) {
                    final String indentText = indent > 0 ? line.substring(0, indent) : "";
                    for (final String comment : strings) {
                        if (comment.isEmpty()) {
                            fileData.append('\n');
                        } else {
                            fileData.append(indentText).append(comment).append('\n');
                        }
                    }
                }
            }

            currentKeyIndents = indents;
            fileData.append(line).append('\n');
        }
        return header != null && !header.isEmpty() ? header + fileData : fileData.toString();
    }

    public static Map<String, String[]> deserializeComments(final String data) {
        //TODO go through known yaml keys and figure out comments from there instead of from line to line?
        // ... tho everything here would benefit from a cleanup
        final Map<String, String[]> comments = new HashMap<>();
        final List<String> currentComments = new ArrayList<>();
        boolean header = true;
        boolean multiLineValue = false;
        int currentIndents = 0;
        String key = "";
        for (final String line : data.split("\n")) {
            final String s = line.trim();
            // It's a comment!
            if (s.startsWith("#")) {
                currentComments.add(s);
                continue;
            }

            // Header is over - save it!
            if (header) {
                if (!currentComments.isEmpty()) {
                    currentComments.add("");
                    comments.put(".header", currentComments.toArray(EMPTY));
                    currentComments.clear();
                }
                header = false;
            }

            // Save empty lines as well
            if (s.isEmpty()) {
                currentComments.add(s);
                continue;
            }

            // Multi line values?
            if (s.startsWith("- |")) {
                multiLineValue = true;
                continue;
            }

            final int indent = getIndents(line);
            final int indents = indent / INDENT_UNIT;
            // Check if the multi line value is over
            if (multiLineValue) {
                if (indents > currentIndents) continue;

                multiLineValue = false;
            }

            // Check if this is a level lower
            if (indents <= currentIndents) {
                final String[] array = key.split(PATH_SEPARATOR_QUOTED);
                final int backspace = currentIndents - indents + 1;
                final int delta = array.length - backspace;
                key = delta >= 0 ? join(array, delta) : key;
            }

            // Finish current key
            final String separator = key.isEmpty() ? "" : PATH_SEPARATOR_STRING;
            final String lineKey = line.indexOf(':') != -1 ? line.split(Pattern.quote(":"))[0] : line;
            key += separator + lineKey.substring(indent);
            currentIndents = indents;

            if (!currentComments.isEmpty()) {
                comments.put(key, currentComments.toArray(EMPTY));
                currentComments.clear();
            }
        }
        return comments;
    }

    private static int getIndents(final String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') {
                break;
            }

            count++;
        }
        return count;
    }

    private static String join(final String[] array, final int length) {
        final String[] copy = new String[length];
        System.arraycopy(array, 0, copy, 0, length);
        return String.join(PATH_SEPARATOR_STRING, copy);
    }
}
