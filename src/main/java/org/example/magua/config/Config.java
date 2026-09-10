package org.example.magua.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.magua.util.AppHome;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private String apiKey;
    private String apiUrl;
    private String model;

    private static final Path FILE_PATH = AppHome.resolve().resolve("magua.yml");
    private static final Yaml YAML = new Yaml();
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}]+)}");


    private class Holder {
        private static final Config INSTANCE = loadConfig();
    }

    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(FILE_PATH)) {
            YAML.dump(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("保存配置文件失败: " + FILE_PATH, e);
        }
    }

    private static Config loadConfig() {
        try {
            String text = Files.readString(FILE_PATH);
            text = resolveEnv(text);
            return YAML.loadAs(text, Config.class);
        } catch (IOException e) {
            // 不创建默认配置，不静默失败，直接让程序终止！
            throw new RuntimeException("致命错误：无法读取或解析配置文件 [" + FILE_PATH + "]，程序终止", e);
        } catch (YAMLException e) { // SnakeYAML 解析异常也一并处理
            throw new RuntimeException("致命错误：配置文件格式非法，程序终止", e);
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

}