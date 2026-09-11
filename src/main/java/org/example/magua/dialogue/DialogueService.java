package org.example.magua.dialogue;

import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.example.magua.config.Config;
import org.example.magua.message.MessageContext;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.StreamHandler;

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

    private Request buildRequest(MessageContext messageContext) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModel());
        body.put("messages", messageContext.getMessages());
//        body.put("tools", toolRegistry.allToolSchemas());
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
        MessageContext messageContext=null;
        try {
            messageContext=dialogueManagement.getDialogue(dialogueId);
        } catch (IOException e) {
            handler.onError("未找到对话！",e);
            return;
        }

        Request request = buildRequest(messageContext);


        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource es, @NotNull Response response) {
                handler.onStart();
            }

            @Override
            public void onEvent(@NotNull EventSource es, String id, String type, @NotNull String data) {
                if ("[DONE]".equals(data)) {
                    es.cancel();
                    handler.onComplete();
                }

            }

            @Override
            public void onFailure(@NotNull EventSource es, Throwable t, Response response) {
                handler.onError("访问api发生错误！",t);
            }
        };

        EventSources.createFactory(client).newEventSource(request, listener);
    }



}
