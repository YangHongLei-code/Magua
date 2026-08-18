package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.magua.state.AppState;

/**
 * 右侧 AI 聊天面板（仅界面；发送仅打印控制台）。
 */
public class ChatPanel {

    private final BorderPane root = new BorderPane();
    private final AppState appState;
    private final VBox historyBox = new VBox(10);

    public ChatPanel(AppState appState) {
        this.appState = appState;
        root.setMinWidth(240);

        root.setTop(buildHeader());
        root.setCenter(buildHistory());
        root.setBottom(buildInputBar());

        addMessage("User", "你好");
        addMessage("AI", "你好啊，我是界面演示用的助手。");
    }

    private HBox buildHeader() {
        Label title = new Label("AI 聊天");

        Button closeButton = new Button("×");
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(e -> {
            System.out.println("用户点击了关闭聊天面板按钮");
            appState.setChatPanelVisible(false);
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(title, spacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(4, 4, 4, 10));
        return header;
    }

    private ScrollPane buildHistory() {
        historyBox.setPadding(new Insets(12));

        ScrollPane scrollPane = new ScrollPane(historyBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private HBox buildInputBar() {
        TextArea input = new TextArea();
        input.setPromptText("输入消息...");
        input.setPrefRowCount(2);
        input.setWrapText(true);
        HBox.setHgrow(input, Priority.ALWAYS);

        Button sendButton = new Button("发送");
        sendButton.setOnAction(e -> {
            String text = input.getText() == null ? "" : input.getText().trim();
            System.out.println("用户点击了发送按钮" + (text.isEmpty() ? "" : "，内容：" + text));
            if (!text.isEmpty()) {
                addMessage("User", text);
                input.clear();
            }
        });

        HBox bar = new HBox(8, input, sendButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10));
        return bar;
    }

    private void addMessage(String role, String content) {
        Label roleLabel = new Label(role);
        Label body = new Label(content);
        body.setWrapText(true);
        body.setMaxWidth(Double.MAX_VALUE);

        VBox bubble = new VBox(4, roleLabel, body);
        historyBox.getChildren().add(bubble);
    }

    public BorderPane getView() {
        return root;
    }
}
