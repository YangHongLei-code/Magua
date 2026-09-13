package org.example.magua.tool;

import org.example.magua.dialogue.DialogueStreamHandler;
import org.example.magua.dialogue.entity.MessageVo;
import org.example.magua.message.ToolMessage;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolCalls {
    private class Part {
        String id = "";
        String name = "";
        StringBuilder arguments = new StringBuilder();
    }

    private Map<Integer, Part> parts = new HashMap<>();
    private JsonMapper jsonMapper = JsonMapper.builder().build();
    private ToolRegistry registry = ToolRegistry.getInstance();


    public boolean hasTools(){
        return !parts.isEmpty();
    }
    public void add(JsonNode toolCalls){
        int index = toolCalls.path("index").asInt(0);
        Part part = parts.computeIfAbsent(index, k -> new Part());
        if (toolCalls.has("id") && !toolCalls.get("id").isNull()) {
            part.id = toolCalls.get("id").asText();
        }
        JsonNode fn = toolCalls.path("function");
        if (fn.has("name") && !fn.get("name").isNull()) {
            part.name = fn.get("name").asText();
        }
        if (fn.has("arguments") && !fn.get("arguments").isNull()) {
            part.arguments.append(fn.get("arguments").asText());
        }
    }
    public JsonNode toToolCallsJson() {

        ArrayNode array = jsonMapper.createArrayNode();
        for (Part part : parts.values()) {
            ObjectNode toolCall = jsonMapper.createObjectNode();
            toolCall.put("id", part.id);
            toolCall.put("type", "function");
            ObjectNode function = jsonMapper.createObjectNode();
            function.put("name", part.name);
            function.put("arguments", part.arguments.toString());
            toolCall.set("function", function);
            array.add(toolCall);
        }
        return array;
    }
    public List<ToolMessage> executeAll(DialogueStreamHandler handler) {
        List<ToolMessage> results = new ArrayList<>();
        for (Part part : parts.values()) {

                JsonNode args = jsonMapper.readTree(part.arguments.toString());

                handler.onChunk(new MessageVo("tool_start", part.name));
                String result = registry.execute(part.name, args);
                handler.onChunk(new MessageVo("tool_result",  part.name));

                results.add(new ToolMessage(part.id, result));

        }
        return results;
    }


}
