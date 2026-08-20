package org.example.magua.ui;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 组装主界面：标题栏 + AI 聊天。
 */
public class MainLayout {

    private BorderPane root = new BorderPane();

    public MainLayout(Stage stage) {
        TitleBar titleBar = new TitleBar(stage);
        ChatPanel chatPanel = new ChatPanel();

        root.setTop(titleBar.getView());
        root.setCenter(chatPanel.getView());
        titleBar.installResizeSupport(root);
    }

    public BorderPane getView() {
        return root;
    }
}
