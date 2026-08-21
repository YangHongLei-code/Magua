package org.example.magua;

import javafx.application.Application;

/**
 * classpath 启动入口。不要直接运行继承 Application 的类，否则会报缺少 JavaFX 运行时。
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
