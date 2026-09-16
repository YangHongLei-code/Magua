package org.example.magua.dialogue;

import org.example.magua.dialogue.entity.DialogueInfo;
import org.example.magua.message.*;
import org.example.magua.util.AppHome;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: yhl
 * @CreateTime: 2026-09-08  15:43
 * @Description: TODO
 */
public class DialogueManagement {
    private Map<String, MessageContext> messageContextMap=new HashMap<String, MessageContext>();
    private JsonMapper jsonMapper = JsonMapper.builder().build();
    public String newDialogue() {
        String dialogueId= UUID.randomUUID().toString();
        messageContextMap.put(dialogueId, new MessageContext(AppHome.resolve().resolve("data/dialogues/"+dialogueId + ".jsonl")));
        return dialogueId;
    }
    public List<DialogueInfo> getDialogueList() {
        List<DialogueInfo> dialogueInfos = new ArrayList<>();
        Path dir = AppHome.resolve().resolve("data/dialogues/");

        try (Stream<Path> stream = Files.walk(dir)) {

            List<File> allFile = stream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            for (File file : allFile) {

                try (BufferedReader br = Files.newBufferedReader(
                        file.toPath(), StandardCharsets.UTF_8)) {

                    DialogueInfo dialogueInfo = null;

                    do {
                        String line = br.readLine();

                        if (line == null) {
                            throw new IllegalStateException("文件 " + file.getName() + " 中未找到 role=user 的行");
                        }

                        if (line.isBlank()) continue;   // 跳过空行

                        JsonNode rootNode;
                        try {
                            rootNode = jsonMapper.readTree(line);
                        } catch (Exception e) {
                            throw new IllegalStateException("文件 " + file.getName() + " JSON 解析失败: " + line, e);
                        }

                        String role = rootNode.path("role").asText();
                        if ("user".equals(role)) {
                            dialogueInfo = new DialogueInfo();

                            String name = file.getName();
                            String baseName = name.substring(0, name.lastIndexOf("."));
                            dialogueInfo.setDialogueId(baseName);

                            String content = rootNode.path("content").asText();
                            dialogueInfo.setDialogueTitle(content);

                            dialogueInfo.setDialogueTime(new Date(file.lastModified()));

                            dialogueInfos.add(dialogueInfo);
                        }

                    } while (dialogueInfo == null);

                } catch (IOException e) {
                    throw new RuntimeException("读取文件失败: " + file.getName(), e);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return dialogueInfos;
    }

    public MessageContext getDialogue(String dialogueId) throws IOException {
        MessageContext messageContext = messageContextMap.get(dialogueId);
        if(messageContext!=null){
            return messageContext;
        }else{
            messageContext = new MessageContext(AppHome.resolve().resolve("data/dialogues/"+dialogueId + ".jsonl"));
            messageContext.loadFile();
            messageContextMap.put(dialogueId, messageContext);
            return messageContext;
        }
    }

    public boolean deleteDialogue(String dialogueId) throws Exception {
        messageContextMap.remove(dialogueId);
        return Files.deleteIfExists(AppHome.resolve().resolve("data/dialogues/"+dialogueId + ".jsonl"));
    }



    private class Holder {
        private static final DialogueManagement INSTANCE = new DialogueManagement();
    }

    private DialogueManagement() {}

    public static DialogueManagement getInstance() {
        return Holder.INSTANCE;
    }

}
