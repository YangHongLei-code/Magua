package org.example.magua.tool.local;

import org.example.magua.tool.AgentTool;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 返回当前本地时间。
 */
public class GetCurrentTimeTool implements AgentTool {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public String name() {
        return "get_current_time";
    }

    @Override
    public JsonNode schema() {
        ObjectNode schema = jsonMapper.createObjectNode();
        schema.put("name", name());
        schema.put("description", "获取当前本地日期时间");

        ObjectNode parameters = jsonMapper.createObjectNode();
        parameters.put("type", "object");
        parameters.set("properties", jsonMapper.createObjectNode());
        parameters.set("required", jsonMapper.createArrayNode());
        schema.set("parameters", parameters);
        return schema;
    }

    @Override
    public String execute(JsonNode arguments) {
        return LocalDateTime.now().format(FORMATTER);
    }
}
