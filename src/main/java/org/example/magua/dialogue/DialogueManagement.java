package org.example.magua.dialogue;

import org.example.magua.message.MessageContext;
import org.example.magua.util.AppHome;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: yhl
 * @CreateTime: 2026-09-08  15:43
 * @Description: TODO
 */
public class DialogueManagement {
    private Map<String, MessageContext> messageContextMap;

    public String newDialogue() {
        String dialogueId= UUID.randomUUID().toString();
        messageContextMap.put(dialogueId, new MessageContext(AppHome.resolve().resolve("data/dialogues/"+dialogueId + ".jsonl")));
        return dialogueId;
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
