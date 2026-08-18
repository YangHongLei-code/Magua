package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * 书签侧栏占位界面。
 */
public class BookmarksPane {

    private final VBox root = new VBox(8);

    public BookmarksPane() {
        root.setPadding(new Insets(12, 14, 12, 14));

        Label hint = new Label("暂无书签（仅界面演示）");

        Button addButton = new Button("添加书签");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> System.out.println("用户点击了添加书签按钮"));

        root.getChildren().addAll(hint, addButton);
    }

    public VBox getView() {
        return root;
    }
}
