package org.example.magua;

import org.example.magua.dialogue.DialogueManagement;
import org.example.magua.message.MessageContext;
import org.example.magua.message.UserMessage;

import java.io.IOException;

/**
 * classpath 启动入口。不要直接运行继承 Application 的类，否则会报缺少 JavaFX 运行时。
 */
public class Main {

    public static void main(String[] args) throws Exception {
//        Application.launch(App.class, args);
        DialogueManagement dialogueManagement=DialogueManagement.getInstance();
        String dialogueId=dialogueManagement.newDialogue();
        MessageContext messageContext= dialogueManagement.getDialogue(dialogueId);
        messageContext.addMessage(new UserMessage("AAA"));




    }
}
