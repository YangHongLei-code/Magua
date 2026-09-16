package org.example.magua.ui;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.example.magua.dialogue.DialogueManagement;

/**
 * 组装主界面：标题栏 + 历史栏 + 聊天区。
 */
public class MainLayout {

    private BorderPane root = new BorderPane();
    private HistorySidebar sidebar = new HistorySidebar();
    private ChatPanel chatPanel = new ChatPanel();

    public MainLayout(Stage stage) {
        TitleBar titleBar = new TitleBar(stage);
        root.setTop(titleBar.getView());
        titleBar.installResizeSupport(root);

        HBox body = new HBox();
        HBox.setHgrow(chatPanel.getView(), Priority.ALWAYS);
        body.getChildren().addAll(sidebar.getView(), chatPanel.getView());
        root.setCenter(body);
        root.setStyle("-fx-background-color: white;");

        wireEvents();
        chatPanel.newDialogue();
    }

    private void wireEvents() {
        sidebar.setOnCreate(() -> chatPanel.newDialogue());

        sidebar.setOnSelect(id -> chatPanel.openDialogue(id));

        sidebar.setOnDelete(id -> {
            try {
                DialogueManagement.getInstance().deleteDialogue(id);
                sidebar.refresh();
                if (id.equals(chatPanel.getDialogueId())) {
                    chatPanel.newDialogue();
                }
            } catch (Exception e) {
                System.out.println("删除对话失败: " + e.getMessage());
            }
        });

        chatPanel.setOnDialogueCreated(id -> {
            sidebar.upsert(id, "新对话", "");
            sidebar.select(id);
        });

        chatPanel.setOnTitleChanged((id, title) -> sidebar.upsert(id, title, "生成中..."));

        chatPanel.setOnStatusChanged(status -> {
            String id = chatPanel.getDialogueId();
            if (id != null) {
                sidebar.upsert(id, null, status);
            }
        });
    }

    public BorderPane getView() {
        return root;
    }
}
