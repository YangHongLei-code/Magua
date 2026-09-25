package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.magua.dialogue.DialogueManagement;
import org.example.magua.dialogue.entity.DialogueInfo;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Consumer;

/**
 * 左侧历史栏：新对话 + 会话列表。
 */
public class HistorySidebar {

    private SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BorderPane root = new BorderPane();
    private ListView<DialogueItem> listView = new ListView<>();
    private Consumer<String> onSelect;
    private Runnable onCreate;
    private Consumer<String> onDelete;

    public HistorySidebar() {
        root.setPrefWidth(250);
        root.setMinWidth(200);
        root.setStyle(UiTheme.toolWindow());

        Button newChatBtn = new Button("＋ 新对话");
        newChatBtn.setMaxWidth(Double.MAX_VALUE);
        newChatBtn.setStyle(UiTheme.primaryButton());
        newChatBtn.setOnAction(e -> {
            if (onCreate != null) {
                onCreate.run();
            }
        });

        VBox top = new VBox(newChatBtn);
        top.setPadding(new Insets(10, 10, 8, 10));
        root.setTop(top);

        listView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-control-inner-background: " + UiTheme.TOOL_BG + ";");
        listView.setFixedCellSize(52);
        listView.setCellFactory(lv -> new DialogueCell());
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, item) -> {
            if (item != null && onSelect != null) {
                onSelect.accept(item.id);
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.setCenter(listView);

        refresh();
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }

    public void setOnSelect(Consumer<String> onSelect) {
        this.onSelect = onSelect;
    }

    public void setOnDelete(Consumer<String> onDelete) {
        this.onDelete = onDelete;
    }

    public void select(String dialogueId) {
        for (DialogueItem item : listView.getItems()) {
            if (item.id.equals(dialogueId)) {
                listView.getSelectionModel().select(item);
                return;
            }
        }
    }

    public void refresh() {
        String selected = listView.getSelectionModel().getSelectedItem() == null
                ? null
                : listView.getSelectionModel().getSelectedItem().id;
        listView.getItems().setAll(loadItems());
        if (selected != null) {
            select(selected);
        }
    }

    public void upsert(String dialogueId, String title, String status) {
        DialogueItem found = null;
        for (DialogueItem item : listView.getItems()) {
            if (item.id.equals(dialogueId)) {
                found = item;
                break;
            }
        }
        if (found == null) {
            found = new DialogueItem();
            found.id = dialogueId;
            found.timeText = TIME_FMT.format(new Date());
            listView.getItems().add(0, found);
        }
        if (title != null && !title.isBlank()) {
            found.title = title;
        }
        found.status = status;
        listView.refresh();
        listView.getSelectionModel().select(found);
    }

    private java.util.List<DialogueItem> loadItems() {
        try {
            return DialogueManagement.getInstance().getDialogueList().stream()
                    .sorted(Comparator.comparing(DialogueInfo::getDialogueTime,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(this::toItem)
                    .toList();
        } catch (Exception e) {
            System.out.println("加载对话列表失败: " + e.getMessage());
            return java.util.List.of();
        }
    }

    private DialogueItem toItem(DialogueInfo info) {
        DialogueItem item = new DialogueItem();
        item.id = info.getDialogueId();
        item.title = info.getDialogueTitle() == null || info.getDialogueTitle().isBlank()
                ? "新对话"
                : info.getDialogueTitle();
        item.timeText = info.getDialogueTime() == null ? "" : TIME_FMT.format(info.getDialogueTime());
        return item;
    }

    private class DialogueCell extends ListCell<DialogueItem> {
        @Override
        protected void updateItem(DialogueItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            Label title = new Label(item.title == null ? "新对话" : item.title);
            title.setStyle(UiTheme.label(14));
            title.setTextOverrun(OverrunStyle.ELLIPSIS);
            title.setMaxWidth(170);

            Label time = new Label(item.timeText == null ? "" : item.timeText);
            time.setStyle(UiTheme.label(12));

            VBox texts = new VBox(2, title, time);
            if (item.status != null && !item.status.isBlank()) {
                Label status = new Label(item.status);
                status.setStyle(UiTheme.label(12) + "-fx-text-fill: " + UiTheme.ACCENT + ";");
                texts.getChildren().add(status);
            }

            Button deleteBtn = new Button("×");
            deleteBtn.setStyle(UiTheme.ghostButton() + "-fx-text-fill: " + UiTheme.TEXT_MUTED + "; -fx-padding: 0 4;");
            deleteBtn.setOnAction(e -> {
                if (onDelete != null) {
                    onDelete.accept(item.id);
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(6, texts, spacer, deleteBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 10, 8, 10));
            row.setStyle(isSelected()
                    ? "-fx-background-color: " + UiTheme.SELECTION + "; -fx-background-radius: 0;"
                    : "-fx-background-color: transparent;");

            setGraphic(row);
            setText(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 0 0;");
        }
    }

    public static class DialogueItem {
        public String id;
        public String title;
        public String timeText;
        public String status;
    }

    public BorderPane getView() {
        return root;
    }
}
