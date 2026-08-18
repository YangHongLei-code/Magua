package org.example.magua.state;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 仅管理界面可见性与当前活动视图，不涉及业务功能。
 */
public class AppState {

    public enum Activity {
        EXPLORER,
        SEARCH,
        BOOKMARKS
    }

    private final ObjectProperty<Activity> activeActivity = new SimpleObjectProperty<>(Activity.EXPLORER);
    private final BooleanProperty sidePanelVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty chatPanelVisible = new SimpleBooleanProperty(true);

    public ObjectProperty<Activity> activeActivityProperty() {
        return activeActivity;
    }

    public Activity getActiveActivity() {
        return activeActivity.get();
    }

    public void setActiveActivity(Activity activity) {
        activeActivity.set(activity);
    }

    public BooleanProperty sidePanelVisibleProperty() {
        return sidePanelVisible;
    }

    public boolean isSidePanelVisible() {
        return sidePanelVisible.get();
    }

    public void setSidePanelVisible(boolean visible) {
        sidePanelVisible.set(visible);
    }

    public void toggleSidePanel() {
        sidePanelVisible.set(!sidePanelVisible.get());
    }

    public BooleanProperty chatPanelVisibleProperty() {
        return chatPanelVisible;
    }

    public boolean isChatPanelVisible() {
        return chatPanelVisible.get();
    }

    public void setChatPanelVisible(boolean visible) {
        chatPanelVisible.set(visible);
    }

    public void toggleChatPanel() {
        chatPanelVisible.set(!chatPanelVisible.get());
    }

    /**
     * 点击活动栏图标：同图标再次点击则折叠侧栏，否则切换视图并展开。
     */
    public void selectActivity(Activity activity) {
        if (activity == getActiveActivity() && isSidePanelVisible()) {
            setSidePanelVisible(false);
        } else {
            setActiveActivity(activity);
            setSidePanelVisible(true);
        }
    }
}
