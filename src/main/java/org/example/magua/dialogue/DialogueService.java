package org.example.magua.dialogue;

import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.example.magua.config.Config;
import org.example.magua.message.MessageContext;
import org.example.magua.message.UserMessage;
import org.example.magua.tool.ToolRegistry;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yhl
 * @CreateTime: 2026-09-11  16:30
 * @Description: TODO
 */
public class DialogueService {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)  // 流式必须设 0
            .build();
    private Config config = Config.getInstance();
    private DialogueManagement dialogueManagement = DialogueManagement.getInstance();
    private JsonMapper jsonMapper = JsonMapper.builder().build();
    private ToolRegistry toolRegistry = ToolRegistry.getInstance();
    private EventSource current;
    private volatile boolean stopped;

    private Request buildRequest(MessageContext messageContext) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModel());
        body.put("messages", messageContext.getMessages());
        body.put("tools", toolRegistry.allToolSchemas());
        body.put("thinking", Map.of("type", config.getThinking()));
        body.put("stream", config.isStream());
        body.put("stream_options", Map.of("include_usage", config.isStreamOptions()));
        body.put("reasoning_effort", config.getReasoningEffort());
        body.put("temperature", config.getTemperature());
        body.put("top_p", config.getTopP());

        RequestBody requestBody = RequestBody.create(jsonMapper.writeValueAsString(body), MediaType.parse("application/json; charset=utf-8"));
        return new Request.Builder()
                .url(config.getApiUrl())
                .post(requestBody)
                .header("Authorization", "Bearer " + config.getApiKeyVal())
                .header("Accept", "text/event-stream")
                .build();
    }

    public void streamAsk(String dialogueId, String userMessage, DialogueStreamHandler handler) {
        stopped = false;
        MessageContext messageContext;
        try {
            messageContext = dialogueManagement.getDialogue(dialogueId);
            messageContext.addMessage(new UserMessage(userMessage));
        } catch (IOException e) {
            handler.onError("未找到对话！", e);
            return;
        }
        streamOneRound(messageContext, handler);
    }

    private void streamOneRound(MessageContext messageContext, DialogueStreamHandler handler) {
        if (stopped) {
            return;
        }
        EventSourceListener listener = new DialogueListener(
                messageContext,
                handler,
                () -> streamOneRound(messageContext, handler),
                () -> stopped
        );
        current = EventSources.createFactory(client).newEventSource(buildRequest(messageContext), listener);
    }
    public void stop() {
        stopped = true;
        if (current != null) {
            current.cancel();
            current = null;
        }
    }

}
