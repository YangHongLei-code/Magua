package org.example.magua.message;


import lombok.Data;

/**
 * 用户消息；{@code name} 为 DeepSeek 等 API 的可选字段，为 null 时不序列化。
 */
@Data
public class UserMessage extends AgentMessage {
    public UserMessage() {
    }

    public UserMessage(String content) {
        super("user", content);
    }
}
