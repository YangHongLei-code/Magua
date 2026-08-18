package org.example.magua.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.example.magua.state.AppState;
import org.example.magua.utils.IconLoader;

/**
 * 左侧固定宽度活动栏。
 */
public class ActivityBar {

    private static final double WIDTH = 48;

    private final VBox root = new VBox();
    private final AppState appState;
    private final Button explorerBtn;
    private final Button searchBtn;
    private final Button bookmarksBtn;

    public ActivityBar(AppState appState) {
        this.appState = appState;
        root.setAlignment(Pos.TOP_CENTER);
        root.setPrefWidth(WIDTH);
        root.setMinWidth(WIDTH);
        root.setMaxWidth(WIDTH);

        explorerBtn = createActivityButton("explorer", "资源管理器", AppState.Activity.EXPLORER);
        searchBtn = createActivityButton("search", "搜索", AppState.Activity.SEARCH);
        bookmarksBtn = createActivityButton("bookmarks", "书签", AppState.Activity.BOOKMARKS);

        root.getChildren().addAll(explorerBtn, searchBtn, bookmarksBtn);
        refreshActiveState();

        appState.activeActivityProperty().addListener((obs, o, n) -> refreshActiveState());
        appState.sidePanelVisibleProperty().addListener((obs, o, n) -> refreshActiveState());
    }

    private Button createActivityButton(String iconName, String tooltip, AppState.Activity activity) {
        Button button = new Button();
        button.setGraphic(IconLoader.icon(iconName));
        button.setTooltip(new Tooltip(tooltip));
        button.setFocusTraversable(false);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            System.out.println("用户点击了活动栏按钮：" + tooltip);
            appState.selectActivity(activity);
        });
        return button;
    }

    private void refreshActiveState() {
        setActive(explorerBtn, AppState.Activity.EXPLORER);
        setActive(searchBtn, AppState.Activity.SEARCH);
        setActive(bookmarksBtn, AppState.Activity.BOOKMARKS);
    }

    private void setActive(Button button, AppState.Activity activity) {
        boolean active = appState.isSidePanelVisible() && appState.getActiveActivity() == activity;
        button.setDefaultButton(active);
    }

    public VBox getView() {
        return root;
    }
}
