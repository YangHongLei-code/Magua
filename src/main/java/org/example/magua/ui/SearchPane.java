package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * 搜索侧栏占位界面。
 */
public class SearchPane {

    private final VBox root = new VBox(8);

    public SearchPane() {
        root.setPadding(new Insets(12, 14, 12, 14));

        TextField searchField = new TextField();
        searchField.setPromptText("在文件中搜索...");

        Button searchButton = new Button("搜索");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setOnAction(e ->
                System.out.println("用户点击了搜索按钮，关键字：" + searchField.getText()));

        Label hint = new Label("输入关键字后点击搜索（仅界面演示）");
        hint.setWrapText(true);

        root.getChildren().addAll(searchField, searchButton, hint);
    }

    public VBox getView() {
        return root;
    }
}
