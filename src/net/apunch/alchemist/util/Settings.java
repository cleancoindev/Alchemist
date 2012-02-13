package net.apunch.alchemist.util;

import java.io.File;

import net.apunch.alchemist.Alchemist;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.YamlStorage;

public class Settings {
    private final YamlStorage config;

    public Settings(Alchemist plugin) {
        config = new YamlStorage(plugin.getDataFolder() + File.separator + "config.yml", "Alchemist Configuration");
    }

    public void load() {
        DataKey root = config.getKey("");
        for (Setting setting : Setting.values())
            if (!root.keyExists(setting.path))
                root.setRaw(setting.path, setting.get());
            else
                setting.set(root.getRaw(setting.path));

        save();
    }

    public void save() {
        config.save();
    }

    public YamlStorage getConfig() {
        return config;
    }

    public enum Setting {
        INIT_MESSAGE("messages.initialization-message",
                "<e>Hello there, <player>. Give me what I need and I will brew you a potion!");

        private String path;
        private Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        public boolean asBoolean() {
            return (Boolean) value;
        }

        public double asDouble() {
            return (Double) value;
        }

        public int asInt() {
            return (Integer) value;
        }

        public long asLong() {
            return (Long) value;
        }

        public String asString() {
            return value.toString();
        }

        private Object get() {
            return value;
        }

        private void set(Object value) {
            this.value = value;
        }
    }
}