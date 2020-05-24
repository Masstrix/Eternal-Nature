package me.masstrix.version;

import java.util.regex.Pattern;

@IVersion(pattern = "\\d*w\\d*[a-z]")
public class MinecraftSnapshot extends Version {

    private static final Pattern STR = Pattern.compile("[a-z]+");

    public MinecraftSnapshot() {}

    public MinecraftSnapshot(String version) {
        super(version);
    }

    public MinecraftSnapshot(byte[] data) {
        super(data);
    }

    public byte getYear() {
        return super.data[0];
    }

    public int getWeek() {
        return super.data[1];
    }

    public byte getIteration() {
        return super.data[2];
    }

    /**
     * @param v version to compare to.
     * @return 0 if the same. 1 if this version is ahead. -1 if this version is behind.
     */
    @Override
    public int compareTo(Version v) {
        if (!(v instanceof MinecraftSnapshot)) return -1;
        if (v.version.equals(version)) return 0;
        for (int i = 0; i < 3; i++) {
            if (data[i] == v.data[i]) continue;
            if (data[i] > v.data[i]) return 1;
            return -1;
        }
        return 0;
    }

    /**
     * Converts an array of bytes into a version string.
     *
     * @param bytes array to convert.
     * @return a version string from the byte array.
     */
    @Override
    public String fromBytes(byte[] bytes) {
        return bytes[0] + "w" + bytes[1] + ((char) ('a' + bytes[2]));
    }

    /**
     * Converts a version string into an array of bytes.
     *
     * Formatted to be [year, week, iteration].
     *
     * @param version a version string.
     * @return a byte array from the version string.
     */
    @Override
    public byte[] toBytes(String version) {
        String[] v = STR.split(version);
        char it = version.charAt(version.length() - 1);
        byte itVal = (byte) (it - 'a');
        return new byte[] {Byte.parseByte(v[0]), Byte.parseByte(v[1]), itVal};
    }
}
