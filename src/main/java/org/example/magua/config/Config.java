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
import java.util.LinkedHashMap;
import java.util.Map;
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

    private String apiKey;//环境变量或真实的key
    private String apiKeyVal;//真实的key
    private String apiUrl;
    private String model;
    private boolean stream=true;
    private boolean streamOptions=true;
    private String thinking="enabled";//如果设为 enabled，则使用思考模式。如果设为 disabled，则使用非思考模式
    private String reasoningEffort="high";//控制思考模式开关与思考强度。none 关闭思考模式；low / high / max 开启思考模式。默认强度为 high。出于兼容考虑，minimal 映射为 low，medium / xhigh 映射为 high。
    private double temperature=1;
    private double topP=1;

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
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("apiKey", apiKey);
            data.put("apiUrl", apiUrl);
            data.put("model", model);
            data.put("stream", stream);
            data.put("streamOptions", streamOptions);
            data.put("thinking", thinking);
            data.put("reasoningEffort", reasoningEffort);
            data.put("temperature", temperature);
            data.put("topP", topP);

            YAML.dump(data, writer);

            Matcher matcher = ENV_PATTERN.matcher(apiKey);
            if (matcher.find()) {
                String apiKeyVal= System.getenv(matcher.group(1));
                this.setApiKeyVal(apiKeyVal);
            }else {
                this.setApiKeyVal(apiKey);
            }


        } catch (IOException e) {
            throw new RuntimeException("保存配置文件失败: " + FILE_PATH, e);
        }
    }

    private static Config loadConfig() {
        try {
            String text = Files.readString(FILE_PATH);
            Config config=YAML.loadAs(text, Config.class);

            String apiKey=config.getApiKey();
            Matcher matcher = ENV_PATTERN.matcher(apiKey);
            if (matcher.find()) {
                String apiKeyVal = System.getenv(matcher.group(1));
                config.setApiKeyVal( apiKeyVal);
            }else {
                config.setApiKeyVal(apiKey);
            }
            return config;
        } catch (IOException e) {
            // 不创建默认配置，不静默失败，直接让程序终止！
            throw new RuntimeException("致命错误：无法读取或解析配置文件 [" + FILE_PATH + "]，程序终止", e);
        } catch (YAMLException e) { // SnakeYAML 解析异常也一并处理
            throw new RuntimeException("致命错误：配置文件格式非法，程序终止", e);
        }
    }


}