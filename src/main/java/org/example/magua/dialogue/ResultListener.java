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

public class ResultListener extends EventSourceListener {
    private JsonMapper jsonMapper = JsonMapper.builder().build();
    private StringBuilder reasoningSb = new StringBuilder();
    private StringBuilder contentSb = new StringBuilder();
    private ToolCalls toolCalls=new ToolCalls();

    private  DialogueStreamHandler handler;
    private MessageContext messageContext;
    private Runnable streamOneRound;
    public ResultListener(MessageContext messageContext, DialogueStreamHandler handler,Runnable streamOneRound) {
        this.handler = handler;
        this.messageContext=messageContext;
        this.streamOneRound = streamOneRound;
    }



    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {

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
                    streamOneRound.run();
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
        }catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {

    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        handler.onStart();
    }
}
