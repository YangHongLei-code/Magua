package org.example.magua.config;

import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: yhl
 * @CreateTime: 2026-08-20  15:30
 * @Description: TODO
 */
public class ConfigManager {
    private static JsonMapper MAPPER = JsonMapper.builder().build();
    private static Config config;
    private static File file=new File("/org/example/magua/config.json");
    private static void load() {
        config = MAPPER.readValue(file, Config.class);
    }

    public static Config get() {
        if(config==null){
            load();
        }
        return config;
    }

    public static boolean save(){
        if (config == null) return false;
        MAPPER.writeValue(file, config);
        return true;
    }



}
