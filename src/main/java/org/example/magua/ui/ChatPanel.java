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

/**
 * AI 聊天主界面（仅界面；发送仅打印控制台）。
 */
public class ChatPanel {

    private BorderPane root = new BorderPane();
    private VBox historyBox = new VBox(10);

    public ChatPanel() {
        root.setCenter(buildHistory());
        root.setBottom(buildInputBar());
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
