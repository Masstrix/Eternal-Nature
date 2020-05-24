package me.masstrix.version;

@IVersion(pattern = "^(\\d*\\.)*\\d*")
public class MinecraftVersion extends Version {

    private int protocol;

    public MinecraftVersion() {}

    public MinecraftVersion(String version) {
        super(version);
        setProtocol();
    }

    public MinecraftVersion(byte[] data) {
        super(data);
        setProtocol();
    }

    public int getProtocol() {
        return protocol;
    }

    private void setProtocol() {
        protocol = MinecraftRelease.getProtocol(super.version);
    }
}
