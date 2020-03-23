package me.masstrix.eternalnature.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Reference for minecraft versions and there protocol. This also stores the servers
 * current version which is found when the plugin is enabled.
 */
public class MinecraftVersion {

    private final static MinecraftVersion serverVersion;
    private final static Map<Integer, String> VERSIONS = new HashMap<>();

    static {
        // Add all minecraft versions.
        VERSIONS.put(0, "1.7");
        VERSIONS.put(5, "1.7.10");
        VERSIONS.put(47, "1.8");
        VERSIONS.put(107, "1.9");
        VERSIONS.put(110, "1.9.4");
        VERSIONS.put(210, "1.10");
        VERSIONS.put(315, "1.11");
        VERSIONS.put(316, "1.11.2");
        VERSIONS.put(335, "1.12");
        VERSIONS.put(338, "1.12.1");
        VERSIONS.put(340, "1.12.2");
        VERSIONS.put(393, "1.13");
        VERSIONS.put(401, "1.13.1");
        VERSIONS.put(404, "1.13.2");
        VERSIONS.put(477, "1.14");
        VERSIONS.put(480, "1.14.1");
        VERSIONS.put(485, "1.14.2");
        VERSIONS.put(490, "1.14.3");
        VERSIONS.put(498, "1.14.4");
        VERSIONS.put(573, "1.15");
        VERSIONS.put(575, "1.15.1");
        VERSIONS.put(578, "1.15.2");

        // Find and set the servers version.
        byte[] version = ReflectionUtil.getVersionUnsafe();
        StringBuilder s = new StringBuilder();
        for (byte b : version) {
            if (s.length() > 0)
                s.append(".");
            s.append(b);
        }
        serverVersion = new MinecraftVersion(s.toString());
    }

    private enum VersionState {
        OLD, VALID, NEW
    }

    /**
     * @return the current version the server is running on.
     */
    public static MinecraftVersion getServerVersion() {
        return serverVersion;
    }

    private String name;
    private int protocolVersion = -1;
    private boolean valid;
    private byte[] asBytes;

    public MinecraftVersion(String name) {
        this.name = name;
        this.asBytes = getBytes(name);
        for (Map.Entry<Integer, String> entry : VERSIONS.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                this.protocolVersion = entry.getKey();
                break;
            }
        }
        valid = false;
    }

    public MinecraftVersion(String name, byte protocolVersion) {
        this.name = name;
        this.protocolVersion = protocolVersion;
        valid = validate();
    }

    /**
     * @return the versions simple name. For example <i>1.13</i> or <i>1.14</i>.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the protocol version. Each version has it's own protocol.
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * @return if the version is valid and recognized. If the version was not found
     *         or it was not in the list then it will return false.
     */
    public boolean isRecognized() {
        return valid;
    }

    private boolean validate() {
        return VERSIONS.containsKey(protocolVersion);
    }

    /**
     * Compare this version instance to another version.
     *
     * @param compare version to compare against.
     * @return -1 if this version is behind <code>compare</code>.
     *         0 if the two versions as the same.
     *         1 if this version is ahead of <code>compare</code>.
     */
    public byte compareTo(MinecraftVersion compare) {
        return this.compareTo(compare.name);
    }

    /**
     * Compare this version instance to another version.
     *
     * @param compare version to compare against.
     * @return -1 if this version is behind <code>compare</code>.
     *         0 if the two versions as the same.
     *         1 if this version is ahead of <code>compare</code>.
     */
    public byte compareTo(String compare) {
        if (compare.equalsIgnoreCase(this.name)) return 0;
        String[] compareData = compare.split("\\.");

        int len = Math.min(compareData.length, this.asBytes.length);
        for (int i = 0; i < len; i++) {
            byte a = Byte.parseByte(compareData[i]);
            byte b = this.asBytes[i];

            if (b > a) return 1;
            else if (b < a) return -1;
        }
        return 0;
    }

    /**
     * Compares this version and returns if this version is older than
     * the compared one.
     *
     * @param compare version to compare to.
     * @return returns if this version is behind <code>compare</code>.
     */
    public boolean isBehindVersion(MinecraftVersion compare) {
        return this.compareTo(compare) == -1;
    }

    /**
     * Compares this version and returns if this version is newer than
     * the compared one.
     *
     * @param compare version to compare to.
     * @return returns if this version is ahead of <code>compare</code>.
     */
    public boolean isAheadOfVersion(MinecraftVersion compare) {
        return this.compareTo(compare) == 1;
    }

    /**
     * Converts a version into an array of bytes and returns it.
     *
     * @param version version in string format.
     * @return an array of bytes parsed from <code>version</code>.
     */
    private static byte[] getBytes(String version) {
        String[] s = version.split("\\.");
        byte[] data = new byte[s.length];
        for (int i = 0; i < s.length; i++)
            data[i] = Byte.parseByte(s[i]);
        return data;
    }
}
