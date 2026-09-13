package org.example.magua.dialogue;

import org.example.magua.dialogue.entity.MessageVo;

public interface DialogueStreamHandler {
    /** 流开始，连接建立时调用一次 */
    default void onStart() {}

    /** 每收到一块文本增量时调用（打字机效果的核心） */
    default void onChunk(MessageVo m) {}

    /** 流正常结束，返回完整文本 */
    default void onComplete() {}

    /** 发生错误 */
    default void onError(String s,Throwable t) {}
}
