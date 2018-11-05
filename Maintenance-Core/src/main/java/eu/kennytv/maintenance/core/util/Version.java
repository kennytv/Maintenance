package eu.kennytv.maintenance.core.util;

public final class Version implements Comparable<Version> {
    private final int[] parts = new int[3];
    private final String version;
    private final String tag;

    public Version(final String version) {
        final String[] split = version.split("-", 2)[0].split("\\.");
        for (int i = 0; i < split.length; i++) {
            if (!isNumeric(split[i])) {
                this.version = null;
                tag = null;
                System.out.println("Unknown Maintenance version detected!");
                return;
            }
            parts[i] = Integer.parseInt(split[i]);
        }

        this.version = version;
        tag = version.contains("-") ? version.split("-", 2)[1] : "";
    }

    /**
     * Compare two versions
     *
     * @param version version to compare to
     * @return 0 if they are the same, 1 if this version (not the one to compare to) is newer, -1 if older
     */
    @Override
    public int compareTo(final Version version) {
        final int max = Math.max(this.parts.length, version.parts.length);
        for (int i = 0; i < max; i += 1) {
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
        return o instanceof Version && o == this || version.equals(o.toString());
    }

    private boolean isNumeric(final String string) {
        try {
            Integer.parseInt(string);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}