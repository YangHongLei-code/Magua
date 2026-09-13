package org.example.magua.tool;

import tools.jackson.databind.JsonNode;

public interface AgentTool {
    /** 工具名，和 API function.name 一致 */
    String name();
    /** 发给 DeepSeek 的 function 定义（不含外层 type） */
    JsonNode schema();
    /** 执行工具，返回字符串给 model */
    String execute(JsonNode arguments);
}
