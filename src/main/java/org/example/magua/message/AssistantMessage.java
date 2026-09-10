package org.example.magua.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.JsonNode;


/**
 * 助手回复；可扩展 tool_calls、reasoning_content（与 DeepSeek 思考模式 / 工具调用对齐）。
 */
@Data
public class AssistantMessage extends AgentMessage {
    @JsonProperty("tool_calls")
    private JsonNode toolCalls;
    @JsonProperty("reasoning_content")
    private String reasoningContent;

    public AssistantMessage() {

    }

    public AssistantMessage(String content) {
        super("assistant", content);
    }
    public AssistantMessage(String reasoningContent,String content) {
        super("assistant", content);
        this.reasoningContent=reasoningContent;
    }
    public AssistantMessage(JsonNode toolCalls,String reasoningContent,String content) {
        super("assistant", content);
        this.toolCalls=toolCalls;
        this.reasoningContent=reasoningContent;
    }

}
