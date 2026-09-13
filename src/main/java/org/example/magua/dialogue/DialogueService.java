package org.example.magua.dialogue;

import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.example.magua.config.Config;
import org.example.magua.dialogue.entity.MessageVo;
import org.example.magua.dialogue.entity.Usage;
import org.example.magua.message.MessageContext;
import org.example.magua.message.UserMessage;
import org.example.magua.tool.ToolRegistry;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.tools.DiagnosticListener;
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
    private Config config=Config.getInstance();
    private DialogueManagement dialogueManagement=DialogueManagement.getInstance();
    private JsonMapper jsonMapper = JsonMapper.builder().build();
    private ToolRegistry toolRegistry=ToolRegistry.getInstance();
    private Request buildRequest(MessageContext messageContext) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModel());
        body.put("messages", messageContext.getMessages());
        body.put("tools", toolRegistry.allToolSchemas());
        body.put("thinking", Map.of("type", config.getThinking()));
        body.put("stream", config.isStream());
        body.put("stream_options", Map.of("include_usage", true));
        body.put("reasoning_effort", config.getReasoningEffort());
        RequestBody requestBody = RequestBody.create(jsonMapper.writeValueAsString(body), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(config.getApiUrl())
                .post(requestBody)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Accept", "text/event-stream")
                .build();
        return request;
    }


    public void streamAsk(String dialogueId, String userMessage, DialogueStreamHandler handler) {
        MessageContext messageContext;
        try {
            messageContext = dialogueManagement.getDialogue(dialogueId);
            messageContext.addMessage(new UserMessage(userMessage));
        } catch (IOException e) {
            handler.onError("未找到对话！", e);
            return;
        }
        streamOneRound(messageContext,handler);
    }

    private void streamOneRound(MessageContext messageContext, DialogueStreamHandler handler) {
        EventSourceListener listener = new ResultListener(messageContext,handler,() -> streamOneRound(messageContext,handler));
        EventSources.createFactory(client).newEventSource(buildRequest(messageContext), listener);
    }



}
