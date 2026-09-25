package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * 自定义标题栏：标题 + 最小化 / 最大化 / 关闭。
 */
public class TitleBar {

    private static final Color ICON_COLOR = Color.web(UiTheme.TEXT);
    private static final double ICON_STROKE = 1.8;

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
        root.setPrefHeight(36);
        root.setStyle(UiTheme.titleBar());

        Label title = new Label("Magua");
        title.setStyle(UiTheme.label(15));
        title.setMouseTransparent(true);

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 12, 0, 12));
        enableWindowDrag(titleBox);

        Region dragArea = new Region();
        HBox.setHgrow(dragArea, Priority.ALWAYS);
        enableWindowDrag(dragArea);

        Button configButton = new Button("⚙");
        configButton.setFocusTraversable(false);
        configButton.setStyle(UiTheme.ghostButton());
        configButton.setOnAction(e -> {
            System.out.println("用户点击了配置按钮");
            new ConfigDialog().show(stage);
        });

        maximizeButton = createIconButton(maximizeIcon(false));
        maximizeButton.setOnAction(e -> toggleMaximize());

        Button minimizeButton = createIconButton(minimizeIcon());
        minimizeButton.setOnAction(e -> {
            System.out.println("用户点击了最小化按钮");
            stage.setIconified(true);
        });

        Button closeButton = createIconButton(closeIcon());
        closeButton.setOnAction(e -> {
            System.out.println("用户点击了关闭窗口按钮");
            stage.close();
        });

        HBox windowControls = new HBox(2, configButton, minimizeButton, maximizeButton, closeButton);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.setPadding(new Insets(0, 4, 0, 0));

        root.getChildren().addAll(titleBox, dragArea, windowControls);

        stage.maximizedProperty().addListener((obs, o, maximized) -> updateMaximizeIcon(maximized));
        updateMaximizeIcon(stage.isMaximized());
    }

    private Button createIconButton(Node icon) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setFocusTraversable(false);
        button.setStyle(UiTheme.ghostButton() + "-fx-padding: 6 10;");
        return button;
    }

    private Node minimizeIcon() {
        Line line = new Line(0, 0, 11, 0);
        line.setStroke(ICON_COLOR);
        line.setStrokeWidth(ICON_STROKE);
        line.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        StackPane box = new StackPane(line);
        box.setMinSize(14, 14);
        box.setMaxSize(14, 14);
        box.setMouseTransparent(true);
        return box;
    }

    private Node maximizeIcon(boolean restored) {
        StackPane box = new StackPane();
        box.setMinSize(14, 14);
        box.setMaxSize(14, 14);
        box.setMouseTransparent(true);
        if (restored) {
            Rectangle back = new Rectangle(8, 8);
            back.setFill(Color.TRANSPARENT);
            back.setStroke(ICON_COLOR);
            back.setStrokeWidth(ICON_STROKE);
            StackPane.setAlignment(back, Pos.TOP_RIGHT);
            back.setTranslateX(1);
            back.setTranslateY(-1);

            Rectangle front = new Rectangle(8, 8);
            front.setFill(Color.web(UiTheme.TOOL_BG));
            front.setStroke(ICON_COLOR);
            front.setStrokeWidth(ICON_STROKE);
            StackPane.setAlignment(front, Pos.BOTTOM_LEFT);
            front.setTranslateX(-1);
            front.setTranslateY(1);

            box.getChildren().addAll(back, front);
        } else {
            Rectangle square = new Rectangle(11, 11);
            square.setFill(Color.TRANSPARENT);
            square.setStroke(ICON_COLOR);
            square.setStrokeWidth(ICON_STROKE);
            box.getChildren().add(square);
        }
        return box;
    }

    private Node closeIcon() {
        Line a = new Line(0, 0, 10, 10);
        Line b = new Line(10, 0, 0, 10);
        for (Line line : new Line[]{a, b}) {
            line.setStroke(ICON_COLOR);
            line.setStrokeWidth(ICON_STROKE);
            line.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        }
        StackPane box = new StackPane(a, b);
        box.setMinSize(14, 14);
        box.setMaxSize(14, 14);
        box.setMouseTransparent(true);
        return box;
    }

    private void toggleMaximize() {
        boolean next = !stage.isMaximized();
        System.out.println("用户点击了" + (next ? "最大化" : "还原") + "按钮");
        stage.setMaximized(next);
    }

    private void updateMaximizeIcon(boolean maximized) {
        maximizeButton.setGraphic(maximizeIcon(maximized));
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
