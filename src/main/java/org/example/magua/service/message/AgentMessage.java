package org.example.magua.service.message;


import lombok.Data;

/**
 * 所有对话消息的父类；{@code role}、{@code content} 与各 API 对齐。
 */
@Data
public abstract class AgentMessage {

    private String role;
    private String content;

    public AgentMessage() {

    }

    public AgentMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

}
