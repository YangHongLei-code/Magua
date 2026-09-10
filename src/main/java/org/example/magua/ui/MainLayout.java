package org.example.magua.ui;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 组装主界面：标题栏 + AI 聊天。
 */
public class MainLayout {

    private BorderPane root = new BorderPane();

    public MainLayout() {
        ChatPanel chatPanel = new ChatPanel();
        root.setCenter(chatPanel.getView());
    }

    public BorderPane getView() {
        return root;
    }
}
