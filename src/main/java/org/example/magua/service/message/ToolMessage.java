package org.example.magua.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * 工具执行结果，对应 role = tool。
 */
@Data
public class ToolMessage extends AgentMessage {
    @JsonProperty("tool_call_id")
    private String toolCallId;

    public ToolMessage() {
    }

    public ToolMessage(String toolCallId, String content) {
        super("tool", content);
        this.toolCallId = toolCallId;
    }

}
