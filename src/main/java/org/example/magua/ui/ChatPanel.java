package org.example.magua.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.magua.dialogue.DialogueManagement;
import org.example.magua.dialogue.DialogueService;
import org.example.magua.dialogue.DialogueStreamHandler;
import org.example.magua.dialogue.entity.MessageVo;
import org.example.magua.dialogue.entity.Usage;
import org.example.magua.message.AgentMessage;
import org.example.magua.message.AssistantMessage;
import org.example.magua.message.MessageContext;
import org.example.magua.message.UserMessage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 右侧主聊天区：标题、消息流、输入框。
 */
public class ChatPanel {

    private BorderPane root = new BorderPane();
    private VBox historyBox = new VBox(16);
    private ScrollPane scrollPane;
    private TextArea input;
    private Button sendButton;
    private Button stopButton;

    private DialogueService dialogueService = new DialogueService();
    private String dialogueId;

    private AssistantMessagePane currentAssistant;
    private boolean waiting;

    private Consumer<String> onDialogueCreated;
    private BiConsumer<String, String> onTitleChanged;
    private Consumer<String> onStatusChanged;

    public ChatPanel() {
        root.setStyle("-fx-background-color: #ffffff;");
        root.setTop(buildHeader());
        root.setCenter(buildHistory());
        root.setBottom(buildInputBar());
    }

    public void setOnDialogueCreated(Consumer<String> onDialogueCreated) {
        this.onDialogueCreated = onDialogueCreated;
    }

    public void setOnTitleChanged(BiConsumer<String, String> onTitleChanged) {
        this.onTitleChanged = onTitleChanged;
    }

    public void setOnStatusChanged(Consumer<String> onStatusChanged) {
        this.onStatusChanged = onStatusChanged;
    }

    public String getDialogueId() {
        return dialogueId;
    }

    public void newDialogue() {
        if (waiting) {
            dialogueService.stop();
        }
        dialogueId = null;
        historyBox.getChildren().clear();
        currentAssistant = null;
        setWaiting(false);
        notifyStatus("");
    }

    public void openDialogue(String id) {
        if (id == null || id.isBlank() || id.equals(dialogueId)) {
            return;
        }
        if (waiting) {
            dialogueService.stop();
            setWaiting(false);
        }
        dialogueId = id;
        historyBox.getChildren().clear();
        currentAssistant = null;
        try {
            MessageContext ctx = DialogueManagement.getInstance().getDialogue(id);
            for (AgentMessage message : ctx.getMessages()) {
                renderHistoryMessage(message);
            }
            scrollToBottom();
        } catch (Exception e) {
            addSystemTip("加载对话失败: " + e.getMessage());
        }
        notifyStatus("");
    }

    private void renderHistoryMessage(AgentMessage message) {
        if (message instanceof UserMessage) {
            addUserBubble(message.getContent());
        } else if (message instanceof AssistantMessage assistant) {
            AssistantMessagePane pane = new AssistantMessagePane();
            if (assistant.getReasoningContent() != null && !assistant.getReasoningContent().isBlank()) {
                pane.appendReasoning(assistant.getReasoningContent());
            }
            if (assistant.getToolCalls() != null && !assistant.getToolCalls().isEmpty()) {
                assistant.getToolCalls().forEach(tc -> {
                    String name = tc.path("function").path("name").asText("tool");
                    pane.toolStart(name);
                    pane.toolResult(name);
                });
            }
            if (assistant.getContent() != null && !assistant.getContent().isBlank()) {
                pane.appendContent(assistant.getContent());
            }
            wrapAssistant(pane);
        }
    }

    private VBox buildHeader() {
        Label title = new Label("Magua");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        Label subtitle = new Label("Agent 对话");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        VBox header = new VBox(2, title, subtitle);
        header.setPadding(new Insets(16, 20, 12, 20));
        header.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
        return header;
    }

    private ScrollPane buildHistory() {
        historyBox.setPadding(new Insets(16, 20, 16, 20));
        historyBox.setFillWidth(true);

        scrollPane = new ScrollPane(historyBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }

    private VBox buildInputBar() {
        input = new TextArea();
        input.setPromptText("输入消息, Enter 发送, Shift+Enter 换行");
        input.setPrefRowCount(3);
        input.setWrapText(true);
        input.setStyle(
                "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ddd;"
                        + "-fx-font-size: 14px;"
        );
        input.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                sendMessage();
            }
        });

        sendButton = new Button("发送");
        sendButton.setStyle(
                "-fx-background-color: #64b5f6; -fx-text-fill: white; -fx-background-radius: 8;"
                        + "-fx-padding: 8 18; -fx-cursor: hand; -fx-font-size: 13px;"
        );
        sendButton.setOnAction(e -> sendMessage());

        stopButton = new Button("停止");
        stopButton.setDisable(true);
        stopButton.setStyle(
                "-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 8;"
                        + "-fx-padding: 8 18; -fx-cursor: hand; -fx-font-size: 13px;"
        );
        stopButton.setOnAction(e -> {
            dialogueService.stop();
            setWaiting(false);
            notifyStatus("");
        });

        HBox actions = new HBox(8, stopButton, sendButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox bar = new VBox(8, input, actions);
        bar.setPadding(new Insets(12, 20, 16, 20));
        bar.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 1 0 0 0; -fx-background-color: white;");
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
        if (dialogueId == null) {
            dialogueId = DialogueManagement.getInstance().newDialogue();
            if (onDialogueCreated != null) {
                onDialogueCreated.accept(dialogueId);
            }
        }

        addUserBubble(text);
        input.clear();
        if (onTitleChanged != null) {
            onTitleChanged.accept(dialogueId, text.length() > 40 ? text.substring(0, 40) + "…" : text);
        }

        currentAssistant = new AssistantMessagePane();
        wrapAssistant(currentAssistant);
        setWaiting(true);
        notifyStatus("生成中...");

        dialogueService.streamAsk(dialogueId, text, new DialogueStreamHandler() {
            @Override
            public void onChunk(MessageVo m) {
                Platform.runLater(() -> handleChunk(m));
            }

            @Override
            public void onComplete() {
                Platform.runLater(() -> {
                    if (currentAssistant != null) {
                        currentAssistant.complete();
                    }
                    setWaiting(false);
                    notifyStatus("");
                });
            }

            @Override
            public void onError(String s, Throwable t) {
                Platform.runLater(() -> {
                    if (currentAssistant != null) {
                        currentAssistant.appendContent("\n[错误] " + s);
                    } else {
                        addSystemTip("[错误] " + s);
                    }
                    setWaiting(false);
                    notifyStatus("");
                });
            }
        });
    }

    private void handleChunk(MessageVo m) {
        if (m == null || m.getType() == null || currentAssistant == null) {
            return;
        }
        switch (m.getType()) {
            case "reasoning" -> {
                if (m.getData() != null) {
                    currentAssistant.appendReasoning(String.valueOf(m.getData()));
                }
            }
            case "content" -> {
                if (m.getData() != null) {
                    currentAssistant.appendContent(String.valueOf(m.getData()));
                }
            }
            case "tool_start" -> currentAssistant.toolStart(String.valueOf(m.getData()));
            case "tool_result" -> currentAssistant.toolResult(String.valueOf(m.getData()));
            case "usage" -> {
                if (m.getData() != null) {
                    currentAssistant.updateUsage(m.getData());
                }
            }
            case "done" -> {
                // 功能侧表示本轮工具结束、将进入下一轮
                currentAssistant.beginNextRound();
            }
            default -> {
            }
        }
        scrollToBottom();
    }

    private void addUserBubble(String text) {
        Label body = new Label(text);
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        body.setMaxWidth(520);

        VBox bubble = new VBox(body);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle("-fx-background-color: #2e7d32; -fx-background-radius: 14;");

        Circle avatar = new Circle(14);
        avatar.setStyle("-fx-fill: #cfd8dc;");

        HBox row = new HBox(10, bubble, avatar);
        row.setAlignment(Pos.TOP_RIGHT);
        historyBox.getChildren().add(row);
        scrollToBottom();
    }

    private void wrapAssistant(AssistantMessagePane pane) {
        HBox row = new HBox(pane.getView());
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(pane.getView(), Priority.ALWAYS);
        historyBox.getChildren().add(row);
        scrollToBottom();
    }

    private void addSystemTip(String text) {
        Label tip = new Label(text);
        tip.setWrapText(true);
        tip.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
        historyBox.getChildren().add(tip);
        scrollToBottom();
    }

    private void setWaiting(boolean waiting) {
        this.waiting = waiting;
        sendButton.setDisable(waiting);
        input.setDisable(waiting);
        stopButton.setDisable(!waiting);
        sendButton.setText(waiting ? "发送中..." : "发送");
        sendButton.setStyle(waiting
                ? "-fx-background-color: #90caf9; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 18; -fx-font-size: 13px;"
                : "-fx-background-color: #64b5f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand; -fx-font-size: 13px;"
        );
    }

    private void notifyStatus(String status) {
        if (onStatusChanged != null && dialogueId != null) {
            onStatusChanged.accept(status);
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    public BorderPane getView() {
        return root;
    }
}
