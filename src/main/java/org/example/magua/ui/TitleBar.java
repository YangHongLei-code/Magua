package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.example.magua.state.AppState;

/**
 * 自定义标题栏：左侧品牌、中间拖拽区、右上角应用菜单 + 窗口控制按钮。
 */
public class TitleBar {

    private static final double RESIZE_MARGIN = 6;

    private final HBox root = new HBox();
    private final Stage stage;
    private final Button maximizeButton;

    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingWindow;
    private ResizeSession resizeSession;

    public TitleBar(Stage stage, AppState appState, EditorArea editorArea) {
        this.stage = stage;
        root.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("Magua");
        brand.setMouseTransparent(true);

        HBox brandBox = new HBox(brand);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(0, 12, 0, 10));
        enableWindowDrag(brandBox);

        Region dragArea = new Region();
        HBox.setHgrow(dragArea, Priority.ALWAYS);
        enableWindowDrag(dragArea);

        HBox menus = buildMenus(appState, editorArea);

        maximizeButton = createWindowButton("□");
        maximizeButton.setOnAction(e -> toggleMaximize());

        Button minimizeButton = createWindowButton("─");
        minimizeButton.setOnAction(e -> {
            System.out.println("用户点击了最小化按钮");
            stage.setIconified(true);
        });

        Button closeButton = createWindowButton("×");
        closeButton.setOnAction(e -> {
            System.out.println("用户点击了关闭窗口按钮");
            stage.close();
        });

        HBox windowControls = new HBox(minimizeButton, maximizeButton, closeButton);
        windowControls.setAlignment(Pos.CENTER_RIGHT);

        // 菜单放在右上角，紧挨窗口控制按钮
        root.getChildren().addAll(brandBox, dragArea, menus, windowControls);

        stage.maximizedProperty().addListener((obs, o, maximized) -> updateMaximizeIcon(maximized));
        updateMaximizeIcon(stage.isMaximized());
    }

    private HBox buildMenus(AppState appState, EditorArea editorArea) {
        HBox menus = new HBox(
                menuButton("文件", buildFileItems(editorArea)),
                menuButton("编辑", buildEditItems()),
                menuButton("选择", buildSelectionItems()),
                menuButton("查看", buildViewItems(appState)),
                menuButton("帮助", buildHelpItems())
        );
        menus.setAlignment(Pos.CENTER_RIGHT);
        return menus;
    }

    private MenuButton menuButton(String text, MenuItem... items) {
        MenuButton button = new MenuButton(text);
        button.setFocusTraversable(false);
        button.getItems().addAll(items);
        return button;
    }

    private MenuItem[] buildFileItems(EditorArea editorArea) {
        Menu newMenu = new Menu("新建");
        newMenu.getItems().addAll(
                actionItem("新建文件", () -> {
                    System.out.println("用户点击了菜单：文件 → 新建 → 新建文件");
                    editorArea.openUntitledTab();
                }),
                actionItem("新建窗口", () -> System.out.println("用户点击了菜单：文件 → 新建 → 新建窗口"))
        );

        return new MenuItem[]{
                newMenu,
                actionItem("打开文件...", () -> System.out.println("用户点击了菜单：文件 → 打开文件...")),
                actionItem("打开文件夹...", () -> System.out.println("用户点击了菜单：文件 → 打开文件夹...")),
                new SeparatorMenuItem(),
                actionItem("保存", () -> System.out.println("用户点击了菜单：文件 → 保存")),
                actionItem("另存为...", () -> System.out.println("用户点击了菜单：文件 → 另存为...")),
                new SeparatorMenuItem(),
                actionItem("退出", () -> {
                    System.out.println("用户点击了菜单：文件 → 退出");
                    stage.close();
                })
        };
    }

    private MenuItem[] buildEditItems() {
        return new MenuItem[]{
                actionItem("撤销", () -> System.out.println("用户点击了菜单：编辑 → 撤销")),
                actionItem("重做", () -> System.out.println("用户点击了菜单：编辑 → 重做")),
                new SeparatorMenuItem(),
                actionItem("剪切", () -> System.out.println("用户点击了菜单：编辑 → 剪切")),
                actionItem("复制", () -> System.out.println("用户点击了菜单：编辑 → 复制")),
                actionItem("粘贴", () -> System.out.println("用户点击了菜单：编辑 → 粘贴")),
                new SeparatorMenuItem(),
                actionItem("查找", () -> System.out.println("用户点击了菜单：编辑 → 查找")),
                actionItem("替换", () -> System.out.println("用户点击了菜单：编辑 → 替换"))
        };
    }

    private MenuItem[] buildSelectionItems() {
        return new MenuItem[]{
                actionItem("全选", () -> System.out.println("用户点击了菜单：选择 → 全选")),
                actionItem("扩大选区", () -> System.out.println("用户点击了菜单：选择 → 扩大选区")),
                actionItem("缩小选区", () -> System.out.println("用户点击了菜单：选择 → 缩小选区")),
                new SeparatorMenuItem(),
                actionItem("向上复制行", () -> System.out.println("用户点击了菜单：选择 → 向上复制行")),
                actionItem("向下复制行", () -> System.out.println("用户点击了菜单：选择 → 向下复制行"))
        };
    }

    private MenuItem[] buildViewItems(AppState appState) {
        CheckMenuItem sidePanelItem = new CheckMenuItem("显示侧边栏");
        sidePanelItem.setSelected(appState.isSidePanelVisible());
        sidePanelItem.setOnAction(e -> {
            System.out.println("用户点击了菜单：查看 → 显示侧边栏 = " + sidePanelItem.isSelected());
            appState.setSidePanelVisible(sidePanelItem.isSelected());
        });
        appState.sidePanelVisibleProperty().addListener((obs, o, visible) ->
                sidePanelItem.setSelected(visible));

        CheckMenuItem chatPanelItem = new CheckMenuItem("显示聊天面板");
        chatPanelItem.setSelected(appState.isChatPanelVisible());
        chatPanelItem.setOnAction(e -> {
            System.out.println("用户点击了菜单：查看 → 显示聊天面板 = " + chatPanelItem.isSelected());
            appState.setChatPanelVisible(chatPanelItem.isSelected());
        });
        appState.chatPanelVisibleProperty().addListener((obs, o, visible) ->
                chatPanelItem.setSelected(visible));

        Menu appearance = new Menu("外观");
        appearance.getItems().addAll(
                actionItem("放大", () -> System.out.println("用户点击了菜单：查看 → 外观 → 放大")),
                actionItem("缩小", () -> System.out.println("用户点击了菜单：查看 → 外观 → 缩小")),
                actionItem("重置缩放", () -> System.out.println("用户点击了菜单：查看 → 外观 → 重置缩放"))
        );

        return new MenuItem[]{
                sidePanelItem,
                chatPanelItem,
                new SeparatorMenuItem(),
                appearance,
                actionItem("切换全屏", () -> {
                    System.out.println("用户点击了菜单：查看 → 切换全屏");
                    stage.setFullScreen(!stage.isFullScreen());
                })
        };
    }

    private MenuItem[] buildHelpItems() {
        return new MenuItem[]{
                actionItem("文档", () -> System.out.println("用户点击了菜单：帮助 → 文档")),
                actionItem("检查更新", () -> System.out.println("用户点击了菜单：帮助 → 检查更新")),
                new SeparatorMenuItem(),
                actionItem("关于 Magua", () -> System.out.println("用户点击了菜单：帮助 → 关于 Magua"))
        };
    }

    private MenuItem actionItem(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        return item;
    }

    private Button createWindowButton(String text) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        return button;
    }

    private void toggleMaximize() {
        boolean next = !stage.isMaximized();
        System.out.println("用户点击了" + (next ? "最大化" : "还原") + "按钮");
        stage.setMaximized(next);
    }

    private void updateMaximizeIcon(boolean maximized) {
        maximizeButton.setText(maximized ? "❐" : "□");
    }

    private void enableWindowDrag(Region region) {
        region.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.PRIMARY || stage.isMaximized()) {
                return;
            }
            draggingWindow = true;
            dragOffsetX = e.getScreenX() - stage.getX();
            dragOffsetY = e.getScreenY() - stage.getY();
            e.consume();
        });

        region.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!draggingWindow || e.getButton() != MouseButton.PRIMARY || stage.isMaximized()) {
                return;
            }
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
            e.consume();
        });

        region.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> draggingWindow = false);

        region.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                toggleMaximize();
            }
        });
    }

    /**
     * 为无边框窗口安装边缘缩放。
     */
    public void installResizeSupport(Region contentRoot) {
        contentRoot.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (stage.isMaximized()) {
                contentRoot.setCursor(Cursor.DEFAULT);
                return;
            }
            contentRoot.setCursor(cursorFor(e.getX(), e.getY(), contentRoot.getWidth(), contentRoot.getHeight()));
        });

        contentRoot.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (stage.isMaximized() || e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            Cursor cursor = contentRoot.getCursor();
            if (cursor == null || cursor == Cursor.DEFAULT) {
                return;
            }
            resizeSession = new ResizeSession(
                    cursor,
                    e.getScreenX(),
                    e.getScreenY(),
                    stage.getX(),
                    stage.getY(),
                    stage.getWidth(),
                    stage.getHeight()
            );
            e.consume();
        });

        contentRoot.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (resizeSession == null || stage.isMaximized()) {
                return;
            }
            resizeSession.apply(stage, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        contentRoot.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> resizeSession = null);
    }

    private Cursor cursorFor(double x, double y, double width, double height) {
        boolean left = x < RESIZE_MARGIN;
        boolean right = x > width - RESIZE_MARGIN;
        boolean top = y < RESIZE_MARGIN;
        boolean bottom = y > height - RESIZE_MARGIN;

        if (top && left) {
            return Cursor.NW_RESIZE;
        }
        if (top && right) {
            return Cursor.NE_RESIZE;
        }
        if (bottom && left) {
            return Cursor.SW_RESIZE;
        }
        if (bottom && right) {
            return Cursor.SE_RESIZE;
        }
        if (left) {
            return Cursor.W_RESIZE;
        }
        if (right) {
            return Cursor.E_RESIZE;
        }
        if (top) {
            return Cursor.N_RESIZE;
        }
        if (bottom) {
            return Cursor.S_RESIZE;
        }
        return Cursor.DEFAULT;
    }

    public HBox getView() {
        return root;
    }

    private static final class ResizeSession {
        private final Cursor cursor;
        private final double startScreenX;
        private final double startScreenY;
        private final double startX;
        private final double startY;
        private final double startWidth;
        private final double startHeight;

        private ResizeSession(Cursor cursor, double startScreenX, double startScreenY,
                              double startX, double startY, double startWidth, double startHeight) {
            this.cursor = cursor;
            this.startScreenX = startScreenX;
            this.startScreenY = startScreenY;
            this.startX = startX;
            this.startY = startY;
            this.startWidth = startWidth;
            this.startHeight = startHeight;
        }

        private void apply(Stage stage, double screenX, double screenY) {
            double dx = screenX - startScreenX;
            double dy = screenY - startScreenY;

            double minW = stage.getMinWidth() > 0 ? stage.getMinWidth() : 400;
            double minH = stage.getMinHeight() > 0 ? stage.getMinHeight() : 300;

            double x = startX;
            double y = startY;
            double w = startWidth;
            double h = startHeight;

            if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
                w = Math.max(minW, startWidth + dx);
            }
            if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
                h = Math.max(minH, startHeight + dy);
            }
            if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
                w = Math.max(minW, startWidth - dx);
                x = startX + startWidth - w;
            }
            if (cursor == Cursor.N_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.NE_RESIZE) {
                h = Math.max(minH, startHeight - dy);
                y = startY + startHeight - h;
            }

            stage.setX(x);
            stage.setY(y);
            stage.setWidth(w);
            stage.setHeight(h);
        }
    }
}
