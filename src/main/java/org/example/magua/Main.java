package org.example.magua;

import org.example.magua.dialogue.DialogueManagement;

/**
 * classpath 启动入口。不要直接运行继承 Application 的类，否则会报缺少 JavaFX 运行时。
 */
public class Main {

    public static void main(String[] args) {
//        Application.launch(App.class, args);
        DialogueManagement dialogueManagement=DialogueManagement.getInstance();
        String dialogueId=dialogueManagement.newDialogue();






    }
}
