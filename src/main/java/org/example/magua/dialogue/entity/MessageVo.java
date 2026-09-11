package org.example.magua.dialogue.entity;

import lombok.Data;

/**
 * @Author: yhl
 * @CreateTime: 2026-05-29  14:18
 * @Description: TODO
 */
@Data
public class MessageVo {
    private String type;
    private Object data;
    public MessageVo(String type, Object data) {
        this.type = type;
        this.data = data;
    }
}
