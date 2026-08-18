package org.example.magua.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.example.magua.state.AppState;

/**
 * 组装主界面：自定义标题栏 +（活动栏 | 可调 SplitPane）+ 状态栏。
 */
public class MainLayout {

    private final BorderPane root = new BorderPane();
    private final AppState appState = new AppState();

    private final SidePanel sidePanel;
    private final ChatPanel chatPanel;
    private final EditorArea editorArea;
    private final SplitPane centerSplit = new SplitPane();
    private final TitleBar titleBar;

    private double sidePanelDivider = 0.22;
    private double chatPanelDividerFromRight = 0.28;

    public MainLayout(Stage stage) {
        editorArea = new EditorArea();
        sidePanel = new SidePanel(appState);
        chatPanel = new ChatPanel(appState);
        ActivityBar activityBar = new ActivityBar(appState);
        StatusBar statusBar = new StatusBar();
        titleBar = new TitleBar(stage, appState, editorArea);

        root.setTop(titleBar.getView());
        root.setBottom(statusBar.getView());

        HBox workspace = new HBox();
        HBox.setHgrow(centerSplit, Priority.ALWAYS);
        rebuildCenterSplit();

        workspace.getChildren().addAll(activityBar.getView(), centerSplit);
        root.setCenter(workspace);

        titleBar.installResizeSupport(root);

        appState.sidePanelVisibleProperty().addListener((obs, o, visible) -> rebuildCenterSplit());
        appState.chatPanelVisibleProperty().addListener((obs, o, visible) -> rebuildCenterSplit());
    }

    private void rebuildCenterSplit() {
        rememberCurrentDividers();

        ObservableList<Node> items = centerSplit.getItems();
        items.clear();

        boolean showSide = appState.isSidePanelVisible();
        boolean showChat = appState.isChatPanelVisible();

        if (showSide) {
            items.add(sidePanel.getView());
        }
        items.add(editorArea.getView());
        if (showChat) {
            items.add(chatPanel.getView());
        }

        centerSplit.setDividerPositions(computeDividerPositions(showSide, showChat));
    }

    private void rememberCurrentDividers() {
        ObservableList<Node> items = centerSplit.getItems();
        double[] positions = centerSplit.getDividerPositions();
        if (items.isEmpty() || positions.length == 0) {
            return;
        }

        boolean hadSide = items.get(0) == sidePanel.getView();
        boolean hadChat = items.get(items.size() - 1) == chatPanel.getView();

        if (hadSide && positions.length >= 1) {
            sidePanelDivider = positions[0];
        }
        if (hadChat) {
            double leftOfChat = positions[positions.length - 1];
            chatPanelDividerFromRight = 1.0 - leftOfChat;
        }
    }

    private double[] computeDividerPositions(boolean showSide, boolean showChat) {
        if (showSide && showChat) {
            double left = clamp(sidePanelDivider, 0.12, 0.45);
            double right = clamp(1.0 - chatPanelDividerFromRight, left + 0.2, 0.88);
            return new double[]{left, right};
        }
        if (showSide) {
            return new double[]{clamp(sidePanelDivider, 0.12, 0.5)};
        }
        if (showChat) {
            return new double[]{clamp(1.0 - chatPanelDividerFromRight, 0.5, 0.88)};
        }
        return new double[0];
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public BorderPane getView() {
        return root;
    }

    public AppState getAppState() {
        return appState;
    }
}
