package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * 自定义标题栏：标题 + 最小化 / 最大化 / 关闭。
 */
public class TitleBar {

    private double resizeMargin = 6;

    private HBox root = new HBox();
    private Stage stage;
    private Button maximizeButton;

    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingWindow;
    private ResizeSession resizeSession;

    public TitleBar(Stage stage) {
        this.stage = stage;
        root.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Maguan");
        title.setMouseTransparent(true);

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 12, 0, 10));
        enableWindowDrag(titleBox);

        Region dragArea = new Region();
        HBox.setHgrow(dragArea, Priority.ALWAYS);
        enableWindowDrag(dragArea);

        Button configButton = new Button("⚙");
        configButton.setFocusTraversable(false);
        configButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #666; -fx-cursor: hand;"
                        + "-fx-font-size: 16px; -fx-padding: 4 10;"
        );
        configButton.setOnAction(e -> {
            System.out.println("用户点击了配置按钮");
            new ConfigDialog().show(stage);
        });

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

        HBox windowControls = new HBox(configButton, minimizeButton, maximizeButton, closeButton);
        windowControls.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleBox, dragArea, windowControls);

        stage.maximizedProperty().addListener((obs, o, maximized) -> updateMaximizeIcon(maximized));
        updateMaximizeIcon(stage.isMaximized());
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
        boolean left = x < resizeMargin;
        boolean right = x > width - resizeMargin;
        boolean top = y < resizeMargin;
        boolean bottom = y > height - resizeMargin;

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

    private class ResizeSession {
        private Cursor cursor;
        private double startScreenX;
        private double startScreenY;
        private double startX;
        private double startY;
        private double startWidth;
        private double startHeight;

        private ResizeSession(Cursor cursor, double startScreenX, double startScreenY,double startX, double startY, double startWidth, double startHeight) {
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
