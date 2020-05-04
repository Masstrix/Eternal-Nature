package me.masstrix.version;

import java.util.regex.Pattern;

@IVersion(pattern = "^(\\d*\\.)*\\d*")
public class Version implements Comparable<Version> {

    public static final String UNKNOWN = "unknown";
    public static final int AHEAD = 1;
    public static final int CURRENT = 0;
    public static final int BEHIND = -1;

    protected final Pattern VALID;
    protected String title;
    protected String version;
    protected byte[] data;

    {
        IVersion data = this.getClass().getAnnotation(IVersion.class);
        VALID = Pattern.compile(data.pattern());
    }

    public Version() {}

    public Version(String version) {
        if (version.equals(UNKNOWN)) {
            this.version = UNKNOWN;
            this.data = new byte[] {-1};
            return;
        }
        validate(version);

        this.version = version;
        this.data = toBytes(version);
    }

    public Version(byte[] data) {
        this.data = data;
        this.version = fromBytes(data);
    }

    /**
     * Sets the name for the version.
     *
     * @param name name to set for this version.
     * @return this version instance.
     */
    public Version setTitle(String name) {
        this.title = name;
        return this;
    }

    public String getName() {
        return version;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAhead(Version v) {
        return compareTo(v) == AHEAD;
    }

    public boolean isCurrent(Version v) {
        return compareTo(v) == CURRENT;
    }

    public boolean isBehind(Version v) {
        return compareTo(v) == BEHIND;
    }

    /**
     * @param v version to compare to.
     * @return 0 if the same. 1 if this version is ahead. -1 if this version is behind.
     *         If either version is unknown -2 will be returned.
     */
    @Override
    public int compareTo(Version v) {
        if (v.isUnknown() || this.isUnknown()) return -2;
        if (v.version.equals(version)) return 0;
        int max = Math.max(data.length, v.data.length);
        int min = Math.min(data.length, v.data.length);

        boolean isThisLongest = max == data.length;
        byte[] longest  = isThisLongest ? data : v.data;
        byte[] shortest = !isThisLongest ? data : v.data;

        for (int i = 0; i < max; i++) {
            byte val = longest[i];

            // Just check longest version
            if (i >= min) {
                if (val > 0) return isThisLongest ? 1 : -1;
                continue;
            }

            byte othr = shortest[i];

            if (val == othr) continue;
            if (val > othr) return isThisLongest ? 1 : -1;
            return isThisLongest ? -1 : 1;
        }
        return 0;
    }

    /**
     * Converts an array of bytes into a version string.
     *
     * @param bytes array to convert.
     * @return a version string from the byte array.
     */
    public String fromBytes(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            if (builder.length() > 0)
                builder.append(".");
            builder.append(b);
        }
        return builder.toString();
    }

    /**
     * Converts a version string into an array of bytes.
     *
     * @param version a version string.
     * @return a byte array from the version string.
     */
    public byte[] toBytes(String version) {
        String[] s = version.split("\\.");
        byte[] bytes = new byte[s.length];
        for (int i = 0; i < s.length; i++)
            bytes[i] = Byte.parseByte(s[i]);
        return bytes;
    }

    public boolean isValid(String version) {
        return VALID.matcher(version).matches();
    }

    public boolean isUnknown() {
        return data[0] == -1 || version.equals(UNKNOWN);
    }

    @Override
    public String toString() {
        return version;
    }

    /**
     * Validates if a string is correctly formatted.
     *
     * @param version version string to check.
     */
    private void validate(String version) {
        if (!isValid(version))
            throw new IllegalArgumentException("Invalid version match");
    }
}
