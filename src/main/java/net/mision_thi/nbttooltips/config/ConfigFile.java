package net.mision_thi.nbttooltips.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;

public class ConfigFile extends ConfigSection {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private final File file;

    public ConfigFile(String name) {
        this(new File(FabricLoader.getInstance().getConfigDir().toFile(), String.format("%s.json", name)));
    }

    public ConfigFile(File file) {
        super(read(file));
        this.file = file.getAbsoluteFile();
    }

    private static JsonElement read(File file) {
        if (!file.exists()) return null;
        try {
            return gson.fromJson(new FileReader(file), JsonElement.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            gson.toJson((element == null) ? new JsonObject() : element, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
