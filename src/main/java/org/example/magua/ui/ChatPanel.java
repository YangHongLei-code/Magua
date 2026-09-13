package org.example.magua.ui;

import javafx.application.Platform;
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
import org.example.magua.dialogue.DialogueManagement;
import org.example.magua.dialogue.DialogueService;
import org.example.magua.dialogue.DialogueStreamHandler;
import org.example.magua.dialogue.entity.MessageVo;

/**
 * AI 聊天主界面：发送消息并流式显示回复。
 */
public class ChatPanel {

    private final BorderPane root = new BorderPane();
    private final VBox historyBox = new VBox(10);
    private final DialogueService dialogueService = new DialogueService();
    private final String dialogueId = DialogueManagement.getInstance().newDialogue();

    private TextArea input;
    private Button sendButton;
    private ScrollPane scrollPane;
    private Label currentAssistantBody;
    private boolean waiting;

    public ChatPanel() {
        root.setCenter(buildHistory());
        root.setBottom(buildInputBar());
    }

    private ScrollPane buildHistory() {
        historyBox.setPadding(new Insets(12));

        scrollPane = new ScrollPane(historyBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private HBox buildInputBar() {
        input = new TextArea();
        input.setPromptText("输入消息...");
        input.setPrefRowCount(2);
        input.setWrapText(true);
        HBox.setHgrow(input, Priority.ALWAYS);

        sendButton = new Button("发送");
        sendButton.setOnAction(e -> sendMessage());

        HBox bar = new HBox(8, input, sendButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10));
        return bar;
    }

    private void sendMessage() {
        if (waiting) {
            return;
        }
        String text = input.getText() == null ? "" : input.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        addMessage("User", text);
        input.clear();
        setWaiting(true);
        currentAssistantBody = addMessage("Assistant", "");

        dialogueService.streamAsk(dialogueId, text, new DialogueStreamHandler() {
            @Override
            public void onChunk(MessageVo m) {
                Platform.runLater(() -> handleChunk(m));
            }

            @Override
            public void onComplete() {
                Platform.runLater(() -> setWaiting(false));
            }

            @Override
            public void onError(String s, Throwable t) {
                Platform.runLater(() -> {
                    if (currentAssistantBody != null) {
                        String existing = currentAssistantBody.getText();
                        currentAssistantBody.setText(
                                (existing == null || existing.isEmpty() ? "" : existing + "\n") + "[错误] " + s
                        );
                    } else {
                        addMessage("Error", s);
                    }
                    setWaiting(false);
                });
            }
        });
    }

    private void handleChunk(MessageVo m) {
        if (m == null || m.getType() == null) {
            return;
        }
        switch (m.getType()) {
            case "content" -> {
                if (currentAssistantBody != null && m.getData() != null) {
                    currentAssistantBody.setText(currentAssistantBody.getText() + m.getData());
                    scrollToBottom();
                }
            }
            case "done" -> setWaiting(false);
            default -> {
                // reasoning / usage / tool_* 等先不展示
            }
        }
    }

    private void setWaiting(boolean waiting) {
        this.waiting = waiting;
        sendButton.setDisable(waiting);
        input.setDisable(waiting);
    }

    private Label addMessage(String role, String content) {
        Label roleLabel = new Label(role);
        Label body = new Label(content);
        body.setWrapText(true);
        body.setMaxWidth(Double.MAX_VALUE);

        VBox bubble = new VBox(4, roleLabel, body);
        historyBox.getChildren().add(bubble);
        scrollToBottom();
        return body;
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    public BorderPane getView() {
        return root;
    }
}
