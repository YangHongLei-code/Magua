package org.example.magua.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
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
 * @CreateTime: 2026-08-20  14:49
 * @Description: TODO
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE) // 阻止外部 new，强制走单例
public class Config {

    // ========== 配置属性（Bean 的数据） ==========
    private String apiKey;
    private String apiUrl;
    private String model;

    // ========== 持久化相关的私有工具（外界完全看不见） ==========
    private static final Path FILE_PATH = Paths.get(System.getProperty("user.home"), ".myapp", "config.yml");
    private static final Yaml YAML = new Yaml();
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    // ========== 单例 Holder（JVM 类加载机制保证线程安全） ==========
    private static class Holder {
        private static final Config INSTANCE = loadConfig();
    }

    // ========== 对外暴露的唯一入口 ==========
    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    // ========== 对外暴露的核心能力 ==========
    /**
     * 保存当前配置到文件（实例方法，操作的是 this 自身）
     */
    public void save() {
        try (Writer writer = Files.newBufferedWriter(FILE_PATH)) {
            YAML.dump(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("保存配置文件失败: " + FILE_PATH, e);
        }
    }

    // ========== 私有工具方法（仅类内部使用） ==========
    private static Config loadConfig() {
        try {
            String text = Files.readString(FILE_PATH);
            text = resolveEnv(text);
            return YAML.loadAs(text, Config.class);
        } catch (IOException e) {
            // 首次启动文件不存在，返回一个默认的 Config 实例（私有构造器在类内部可访问）
            return createDefaultConfig();
        }
    }

    private static String resolveEnv(String text) {
        Matcher matcher = ENV_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = System.getenv(name);
            if (value == null) {
                throw new IllegalStateException("缺失必需的环境变量: " + name);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Config createDefaultConfig() {
        Config config = new Config();
        config.setApiKey("your-default-key");
        config.setApiUrl("https://api.default.com");
        config.setModel("default-model");
        return config;
    }
}