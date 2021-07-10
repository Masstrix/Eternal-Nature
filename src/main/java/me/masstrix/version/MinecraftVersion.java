package me.masstrix.version;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinecraftVersion version)) return false;
        return protocol == version.protocol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol);
    }
}
