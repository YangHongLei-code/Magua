package org.example.magua.service.message;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @Author: yhl
 * @CreateTime: 2026-05-29  16:29
 * @Description: TODO
 */
public class MessageContext {

    private List<AgentMessage> messages = new ArrayList<>();
    private Path filePath;
    private JsonMapper jsonMapper = JsonMapper.builder().build();
    public MessageContext(Path filePath) {
        this.filePath=filePath;
    }

    public void addMessage(AgentMessage message)throws Exception {
        messages.add(message);
        appendLine(message);
    }

    public List<AgentMessage> getMessages() {
        return messages;
    }

    private void appendLine(AgentMessage message)throws Exception  {
            String line =jsonMapper.writeValueAsString(message);
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(filePath, line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
    public void loadFile() throws Exception {
        if (!Files.exists(filePath)) {
            return;
        }
        Stream<String> lines = Files.lines(filePath);
                lines.map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(line -> {
                    JsonNode rootNode = jsonMapper.readTree(line);
                    String role = rootNode.path("role").asText();
                    AgentMessage message;
                    switch (role){
                        case "user" -> message = jsonMapper.treeToValue(rootNode, UserMessage.class);
                        case "assistant" -> message = jsonMapper.treeToValue(rootNode, AssistantMessage.class);
                        case "system" -> message = jsonMapper.treeToValue(rootNode, SystemMessage.class);
                        case "tool" -> message = jsonMapper.treeToValue(rootNode, ToolMessage.class);
                        default -> throw new IllegalArgumentException("未知 role: " + role);
                    }
                    messages.add(message);
                });
    }
}