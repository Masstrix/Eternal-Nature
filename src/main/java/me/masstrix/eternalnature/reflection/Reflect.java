package me.masstrix.eternalnature.reflection;

public class Reflect {

    public ReflectMinecraft minecraft() {
        return new ReflectMinecraft();
    }

    public ReflectMinecraft craft() {
        return new ReflectMinecraft();
    }

    private static class ReflectBase {
        String path = "";
        private ReflectBase(String path) {
            this.path = path;
        }

        public String path(String className) {
            return path + "." + className;
        }
    }

    public class ReflectMinecraft extends ReflectBase {

        private ReflectMinecraft() {
            super("net.minecraft");
        }

        public ReflectProtocol protocol() {
            return new ReflectProtocol(path);
        }

        public class ReflectProtocol extends ReflectBase {

            private ReflectProtocol(String path) {
                super(path + ".network.protocol.game");
            }
        }
    }
}
