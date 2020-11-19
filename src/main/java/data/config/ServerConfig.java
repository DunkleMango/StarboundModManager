package data.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerConfig {
    private String name;
    private List<Long> modIds;

    public ServerConfig(String name) {
        this.name = name;
        this.modIds = new ArrayList<>();
    }

    public ServerConfig(String name, List<Long> modIds) {
        this.name = name;
        if (modIds == null) {
            this.modIds = new ArrayList<>();
        } else {
            this.modIds = modIds;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void addMod(Long modId) {
        this.modIds.add(modId);
    }

    public void rename(String name) {
        this.name = name;
    }

    public void exportData(File file) {
        if (file != null && file.exists() && file.isFile()) {
            // TODO - export config
        }
    }

    public static ServerConfig importData(File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) {
            // TODO - import config
            return new ServerConfig("imported server config");
        }
        throw new IOException("Unable to load server config.");
    }
}
