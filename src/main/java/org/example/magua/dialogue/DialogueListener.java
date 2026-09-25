package org.example.magua.dialogue;

import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.example.magua.dialogue.entity.MessageVo;
import org.example.magua.dialogue.entity.Usage;
import org.example.magua.message.AssistantMessage;
import org.example.magua.message.MessageContext;
import org.example.magua.message.ToolMessage;
import org.example.magua.tool.ToolCalls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;

public class DialogueListener extends EventSourceListener {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final StringBuilder reasoningSb = new StringBuilder();
    private final StringBuilder contentSb = new StringBuilder();
    private final ToolCalls toolCalls = new ToolCalls();

    private final DialogueStreamHandler handler;
    private final MessageContext messageContext;
    private final Runnable streamOneRound;
    private final BooleanSupplier stopped;

    public DialogueListener(MessageContext messageContext,
                            DialogueStreamHandler handler,
                            Runnable streamOneRound,
                            BooleanSupplier stopped) {
        this.handler = handler;
        this.messageContext = messageContext;
        this.streamOneRound = streamOneRound;
        this.stopped = stopped;
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
        if (stopped.getAsBoolean()) {
            return;
        }
        try {
            if ("[DONE]".equals(data)) {
                if (toolCalls.hasTools()) {
                    messageContext.addMessage(new AssistantMessage(toolCalls.toToolCallsJson(), reasoningSb.toString(), contentSb.toString()));
                    List<ToolMessage> toolMessages = toolCalls.executeAll(handler);
                    for (ToolMessage toolMessage : toolMessages) {
                        messageContext.addMessage(toolMessage);
                    }
                    eventSource.cancel();
                    handler.onChunk(new MessageVo("done", "工具调用完毕。"));
                    if (!stopped.getAsBoolean()) {
                        streamOneRound.run();
                    }
                } else {
                    messageContext.addMessage(new AssistantMessage(contentSb.toString()));
                    handler.onComplete();
                }
                return;
            }

            JsonNode root = jsonMapper.readTree(data);
            JsonNode delta = root.path("choices").path(0).path("delta");
            JsonNode reasoningContent = delta.path("reasoning_content");
            if (!reasoningContent.isMissingNode() && !reasoningContent.isNull() && !reasoningContent.asText().isEmpty()) {
                String reasoning = reasoningContent.asText();
                reasoningSb.append(reasoning);
                handler.onChunk(new MessageVo("reasoning", reasoning));
            }
            JsonNode content = delta.path("content");
            if (!content.isMissingNode() && !content.isNull() && !content.asText().isEmpty()) {
                String contentStr = content.asText();
                contentSb.append(contentStr);
                handler.onChunk(new MessageVo("content", contentStr));
            }
            JsonNode tcs = delta.path("tool_calls");
            if (!tcs.isMissingNode() && !tcs.isNull()) {
                for (JsonNode tc : tcs) {
                    toolCalls.add(tc);
                }
            }

            JsonNode usage = root.path("usage");
            if (!usage.isMissingNode() && !usage.isNull() && !usage.isEmpty()) {
                Usage usageData = jsonMapper.treeToValue(usage, Usage.class);
                handler.onChunk(new MessageVo("usage", usageData));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        // 用户停止 / 主动 cancel：不写半截、不回调；UI 停止按钮已自行收尾
        if (stopped.getAsBoolean() || isCanceled(t)) {
            return;
        }
        String detail = t != null ? t.getMessage() : "unknown";
        if (response != null) {
            detail = "HTTP " + response.code() + ": " + detail;
            try {
                if (response.body() != null) {
                    detail += " body=" + response.body().string();
                }
            } catch (IOException ignored) {
            }
        }
        handler.onError("访问api发生错误！" + detail, t);
    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        handler.onStart();
    }

    private static boolean isCanceled(Throwable t) {
        if (t == null) {
            return false;
        }
        String msg = t.getMessage();
        return t instanceof IOException && msg != null && msg.toLowerCase().contains("cancel");
    }
}
