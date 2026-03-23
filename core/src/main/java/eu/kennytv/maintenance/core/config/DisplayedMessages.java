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

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DisplayedMessages {

    private static final Random RANDOM = new Random();
    private final Map<String, List<String>> messages;
    private final List<String> defaultMessages;

    public DisplayedMessages(final Map<String, List<String>> messages) {
        this.messages = messages;
        this.defaultMessages = messages.get("default");
    }

    public List<String> getMessages(@Nullable final String key) {
        Preconditions.checkNotNull(defaultMessages, "no default message configured in messages list");
        if (key == null || messages.size() == 1) {
            return defaultMessages;
        }
        return messages.getOrDefault(key, defaultMessages);
    }

    public String getRandomEntry(@Nullable final String key) {
        final List<String> messages = getMessages(key);
        return messages.get(RANDOM.nextInt(messages.size()));
    }

    public List<String> getDefaultMessages() {
        return defaultMessages;
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Set<String> getKeys() {
        return messages.keySet();
    }
}
