package org.example.magua.message;

import lombok.Data;

/**
 * 系统提示消息；{@code name} 为 DeepSeek 等 API 的可选字段，为 null 时不序列化。
 */
@Data
public class SystemMessage extends AgentMessage {
    public SystemMessage() {
    }

    public SystemMessage(String content) {
        super("system", content);
    }
}
