package org.example.magua.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.magua.dialogue.DialogueManagement;
import org.example.magua.dialogue.DialogueService;
import org.example.magua.dialogue.DialogueStreamHandler;
import org.example.magua.dialogue.entity.MessageVo;
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
    private VBox historyBox = new VBox(12);
    private ScrollPane scrollPane;
    private ChatInputView inputView;
    private Button actionButton;

    private DialogueService dialogueService = new DialogueService();
    private String dialogueId;

    private AssistantMessagePane currentAssistant;
    private boolean waiting;

    private Consumer<String> onDialogueCreated;
    private BiConsumer<String, String> onTitleChanged;
    private Consumer<String> onStatusChanged;

    public ChatPanel() {
        root.setStyle(UiTheme.root());
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
        Label title = new Label("对话");
        title.setStyle(UiTheme.label(18));

        Label subtitle = new Label("Agent Workbench");
        subtitle.setStyle(UiTheme.label(13));

        VBox header = new VBox(2, title, subtitle);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color: " + UiTheme.BG + "; -fx-border-color: " + UiTheme.BORDER_SOFT
                + "; -fx-border-width: 0 0 1 0;");
        return header;
    }

    private ScrollPane buildHistory() {
        historyBox.setPadding(new Insets(12, 14, 12, 14));
        historyBox.setFillWidth(true);
        historyBox.setSpacing(12);

        scrollPane = new ScrollPane(historyBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: " + UiTheme.BG + "; -fx-background: " + UiTheme.BG + ";");
        return scrollPane;
    }

    private VBox buildInputBar() {
        inputView = new ChatInputView();
        inputView.setOnSend(this::sendMessage);

        actionButton = new Button("发送");
        styleActionButton(false);
        actionButton.setOnAction(e -> {
            if (waiting) {
                dialogueService.stop();
                setWaiting(false);
                notifyStatus("");
            } else {
                sendMessage();
            }
        });

        HBox actions = new HBox(actionButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox bar = new VBox(8, inputView.getView(), actions);
        bar.setPadding(new Insets(10, 14, 12, 14));
        bar.setStyle("-fx-background-color: " + UiTheme.PANEL_BG + "; -fx-border-color: " + UiTheme.BORDER_SOFT
                + "; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    private void sendMessage() {
        if (waiting) {
            return;
        }
        String text = inputView.getText() == null ? "" : inputView.getText().trim();
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
        inputView.clear();
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
                currentAssistant.beginNextRound();
            }
            default -> {
            }
        }
        scrollToBottom();
    }

    private void addUserBubble(String text) {
        Label role = new Label("你");
        role.setStyle(UiTheme.label(13));

        Label body = new Label(text);
        body.setWrapText(true);
        body.setStyle(UiTheme.label(15));
        body.setMaxWidth(640);

        VBox bubble = new VBox(4, role, body);
        bubble.setPadding(new Insets(8, 10, 8, 10));
        bubble.setStyle("-fx-background-color: " + UiTheme.USER_BG + "; -fx-background-radius: 4;"
                + "-fx-border-color: " + UiTheme.BORDER_SOFT + "; -fx-border-radius: 4; -fx-border-width: 1;");

        HBox row = new HBox(bubble);
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
        tip.setStyle(UiTheme.label(14));
        historyBox.getChildren().add(tip);
        scrollToBottom();
    }

    private void setWaiting(boolean waiting) {
        this.waiting = waiting;
        inputView.setEnabled(!waiting);
        actionButton.setText(waiting ? "停止" : "发送");
        styleActionButton(waiting);
        if (!waiting) {
            inputView.requestFocusInput();
        }
    }

    private void styleActionButton(boolean stopping) {
        actionButton.setStyle(stopping ? UiTheme.dangerButton() : UiTheme.primaryButton());
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
