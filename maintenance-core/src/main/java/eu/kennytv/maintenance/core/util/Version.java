/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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
package eu.kennytv.maintenance.core.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

public final class Version implements Comparable<Version> {
    private final int[] parts;
    private final String version;
    private final String tag;

    /**
     * Creates a new version object to be compared.
     * The version format are numbers separated by '.', possibly followed by a '-' to include an extra tag after it.
     *
     * @param version string representation of the version
     * @throws IllegalArgumentException if the given version string is null or empty
     * @throws NumberFormatException    if there are non-number characters in the version (before the tag)
     */
    public Version(final String version) {
        Preconditions.checkArgument(version != null && !version.isEmpty());

        final int index = version.indexOf('-');
        final String[] split = (index == -1 ? version : version.substring(0, index)).split("\\.");
        parts = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            // Let this throw an exception if parsing fails
            parts[i] = Integer.parseInt(split[i]);
        }

        this.version = version;
        tag = index != -1 ? version.substring(index + 1) : "";
    }

    /**
     * Compare two versions.
     *
     * @param version version to compare to
     * @return 0 if they are the same, 1 if this instance is newer, -1 if older
     */
    @Override
    public int compareTo(@Nullable final Version version) {
        if (version == null) return 0;

        final int max = Math.max(this.parts.length, version.parts.length);
        for (int i = 0; i < max; i++) {
            final int partA = i < this.parts.length ? this.parts[i] : 0;
            final int partB = i < version.parts.length ? version.parts[i] : 0;
            if (partA < partB) {
                return -1;
            } else if (partA > partB) {
                return 1;
            }
        }

        if (this.tag.isEmpty() && !version.tag.isEmpty()) {
            return 1;
        } else if (!this.tag.isEmpty() && version.tag.isEmpty()) {
            return -1;
        }
        return 0;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Version)) return false;
        final Version other = (Version) o;
        return version.equals(other.version);
    }
}