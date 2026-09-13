package org.example.magua;

import javafx.application.Application;
import org.example.magua.dialogue.DialogueManagement;
import org.example.magua.dialogue.DialogueService;
import org.example.magua.dialogue.DialogueStreamHandler;
import org.example.magua.dialogue.entity.MessageVo;

import java.util.concurrent.CountDownLatch;

/**
 * classpath 启动入口。不要直接运行继承 Application 的类，否则会报缺少 JavaFX 运行时。
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Application.launch(App.class, args);
    }
}
