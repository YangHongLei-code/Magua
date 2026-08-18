package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.example.magua.state.AppState;

/**
 * 可折叠侧边内容区：根据活动栏切换视图，右上角 × 可关闭。
 */
public class SidePanel {

    private final BorderPane root = new BorderPane();
    private final Label titleLabel = new Label();
    private final StackPane contentHost = new StackPane();
    private final AppState appState;

    private final Node explorerView;
    private final Node searchView;
    private final Node bookmarksView;

    public SidePanel(AppState appState) {
        this.appState = appState;
        root.setMinWidth(180);

        explorerView = new FileTreePane().getView();
        searchView = new SearchPane().getView();
        bookmarksView = new BookmarksPane().getView();

        root.setTop(buildHeader());
        root.setCenter(contentHost);
        BorderPane.setAlignment(contentHost, Pos.TOP_LEFT);

        appState.activeActivityProperty().addListener((obs, o, n) -> showActivity(n));
        showActivity(appState.getActiveActivity());
    }

    private HBox buildHeader() {
        Button closeButton = new Button("×");
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(e -> {
            System.out.println("用户点击了关闭侧边栏按钮");
            appState.setSidePanelVisible(false);
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(titleLabel, spacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(4, 4, 4, 10));
        return header;
    }

    private void showActivity(AppState.Activity activity) {
        contentHost.getChildren().clear();
        switch (activity) {
            case SEARCH -> {
                titleLabel.setText("搜索");
                contentHost.getChildren().add(searchView);
            }
            case BOOKMARKS -> {
                titleLabel.setText("书签");
                contentHost.getChildren().add(bookmarksView);
            }
            default -> {
                titleLabel.setText("资源管理器");
                contentHost.getChildren().add(explorerView);
            }
        }
    }

    public BorderPane getView() {
        return root;
    }
}
