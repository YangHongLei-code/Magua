package org.example.magua.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 中央多标签编辑区。标签关闭为真实界面逻辑；编辑内容不做业务处理。
 */
public class EditorArea {

    private final StackPane root = new StackPane();
    private final TabPane tabPane = new TabPane();
    private final VBox welcomePane = new VBox(12);
    private int untitledCounter = 1;

    public EditorArea() {
        welcomePane.setAlignment(Pos.CENTER);
        Label title = new Label("Magua");
        Label hint = new Label("通过菜单「文件 → 新建文件」打开编辑标签页");
        welcomePane.getChildren().addAll(title, hint);

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getTabs().addListener((javafx.collections.ListChangeListener<Tab>) c -> refreshEmptyState());

        root.getChildren().addAll(welcomePane, tabPane);
        refreshEmptyState();
    }

    public void openUntitledTab() {
        String title = "未命名-" + untitledCounter++;
        openTab(title, "// 在此输入内容（仅界面演示）\n");
    }

    public void openTab(String title, String content) {
        TextArea textArea = new TextArea(content);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        Tab tab = new Tab(title, textArea);
        tab.setOnCloseRequest(e -> System.out.println("用户关闭了编辑标签：" + title));
        tab.setOnClosed(e -> System.out.println("编辑标签已关闭：" + title));

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        System.out.println("已打开编辑标签：" + title);
    }

    private void refreshEmptyState() {
        boolean empty = tabPane.getTabs().isEmpty();
        welcomePane.setVisible(empty);
        welcomePane.setManaged(empty);
        tabPane.setVisible(!empty);
        tabPane.setManaged(!empty);
    }

    public StackPane getView() {
        return root;
    }
}
