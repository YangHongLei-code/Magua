package org.example.magua.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: yhl
 * @CreateTime: 2026-08-20  15:30
 * @Description: TODO
 */
public class ConfigManager {
    private static Path filePath = Paths.get("magua.yml");
    private static Yaml yaml = new Yaml();
    private static Pattern envPattern = Pattern.compile("\\$\\{([^}]+)}");
    private static Config config;

    public static Config get() {
        if (config == null) {
            try {
                String text = Files.readString(filePath);
                text = resolveEnv(text);
                config = yaml.loadAs(text, Config.class);
            } catch (IOException e) {
                throw new RuntimeException("无法加载配置文件: " + filePath, e);
            }
        }
        return config;
    }

    public static void save(Config config) {
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            yaml.dump(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("无法保存配置文件: " + filePath, e);
        }
    }

    private static String resolveEnv(String text) {
        Matcher matcher = envPattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = System.getenv(name);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value == null ? "" : value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
