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

package eu.kennytv.maintenance.core.util;

public final class Version implements Comparable<Version> {
    private final int[] parts;
    private final String version;
    private final String tag;

    public Version(final String version) {
        if (version == null || version.isEmpty()) {
            this.version = "";
            tag = "";
            parts = new int[0];
            System.out.println("Unknown Maintenance version detected!");
            return;
        }
        final String[] split = version.split("-", 2)[0].split("\\.");
        parts = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            if (!split[i].matches("[0-9]+")) {
                this.version = "";
                tag = "";
                System.out.println("Unknown Maintenance version detected!");
                return;
            }
            parts[i] = Integer.parseInt(split[i]);
        }

        this.version = version;
        tag = version.contains("-") ? version.split("-", 2)[1] : "";
    }

    /**
     * Compare two versions.
     *
     * @param version version to compare to
     * @return 0 if they are the same, 1 if this instance is newer, -1 if older
     */
    @Override
    public int compareTo(final Version version) {
        if (version == null || version.toString() == null) return 0;
        final int max = Math.max(this.parts.length, version.parts.length);
        for (int i = 0; i < max; i++) {
            final int partA = i < this.parts.length ? this.parts[i] : 0;
            final int partB = i < version.parts.length ? version.parts[i] : 0;
            if (partA < partB) return -1;
            if (partA > partB) return 1;
        }

        if (this.tag.isEmpty() && !version.tag.isEmpty())
            return 1;
        if (!this.tag.isEmpty() && version.tag.isEmpty())
            return -1;
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
        if (o == null) return false;
        return o instanceof Version && o == this || version.equals(o.toString());
    }
}