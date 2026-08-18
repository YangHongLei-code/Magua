package org.example.magua.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 资源管理器文件树（仅展示占位数据，点击输出到控制台）。
 */
public class FileTreePane {

    private final VBox root = new VBox();

    public FileTreePane() {
        VBox.setVgrow(root, Priority.ALWAYS);

        TreeItem<String> project = new TreeItem<>("magua");
        project.setExpanded(true);

        TreeItem<String> src = new TreeItem<>("src");
        src.setExpanded(true);
        TreeItem<String> main = new TreeItem<>("main");
        main.setExpanded(true);
        TreeItem<String> java = new TreeItem<>("java");
        java.setExpanded(true);
        TreeItem<String> app = new TreeItem<>("App.java");
        TreeItem<String> mainLayout = new TreeItem<>("MainLayout.java");
        java.getChildren().addAll(app, mainLayout);
        main.getChildren().add(java);
        src.getChildren().add(main);

        TreeItem<String> resources = new TreeItem<>("resources");
        TreeItem<String> icons = new TreeItem<>("icons");
        icons.getChildren().addAll(
                new TreeItem<>("explorer.png"),
                new TreeItem<>("search.png"),
                new TreeItem<>("bookmarks.png")
        );
        resources.getChildren().add(icons);

        TreeItem<String> pom = new TreeItem<>("pom.xml");
        TreeItem<String> readme = new TreeItem<>("README.md");

        project.getChildren().addAll(src, resources, pom, readme);

        TreeView<String> treeView = new TreeView<>(project);
        treeView.setShowRoot(true);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                System.out.println("用户点击了文件树节点：" + newItem.getValue());
            }
        });

        root.getChildren().add(treeView);
    }

    public VBox getView() {
        return root;
    }
}
