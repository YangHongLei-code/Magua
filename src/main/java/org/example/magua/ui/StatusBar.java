package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * 底部状态栏（仅展示占位信息）。
 */
public class StatusBar {

    private final HBox root = new HBox(16);

    public StatusBar() {
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(4, 10, 4, 10));

        Label position = new Label("行: 1, 列: 1");
        Label encoding = new Label("UTF-8");
        Label language = new Label("语言: Java");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        root.getChildren().addAll(position, spacer, encoding, language);
    }

    public HBox getView() {
        return root;
    }
}
