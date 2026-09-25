package org.example.magua.ui;

/**
 * IDEA Light 风格的工作台色板与常用样式片段。
 * 文字参考标题栏配置按钮：近黑色、加粗、偏大。
 */
public final class UiTheme {

    private UiTheme() {
    }

    public static final String BG = "#FFFFFF";
    public static final String TOOL_BG = "#F2F2F2";
    public static final String PANEL_BG = "#F5F5F5";
    public static final String HOVER = "#E6E6E6";
    public static final String SELECTION = "#CDE0F8";
    public static final String BORDER = "#BDBDBD";
    public static final String BORDER_SOFT = "#D0D0D0";

    /** 与配置按钮同级的深色字 */
    public static final String TEXT = "#2B2B2B";
    public static final String TEXT_SECONDARY = "#2B2B2B";
    public static final String TEXT_MUTED = "#3C3C3C";

    public static final String ACCENT = "#2F6FED";
    public static final String ACCENT_HOVER = "#2459C9";
    public static final String DANGER = "#B83B37";
    public static final String USER_BG = "#E8F0FE";
    public static final String ASSISTANT_BG = "#F7F7F7";
    public static final String TOOL_ROW_BG = "#E8EEF5";

    /** Linux 优先 Noto/文泉驿，字重才吃得住 */
    public static final String FONT =
            "'Noto Sans CJK SC', 'WenQuanYi Micro Hei', 'Microsoft YaHei UI', 'Microsoft YaHei', 'Segoe UI', sans-serif";

    public static String root() {
        return "-fx-background-color: " + BG
                + "; -fx-font-family: " + FONT
                + "; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";";
    }

    public static String toolWindow() {
        return "-fx-background-color: " + TOOL_BG + "; -fx-border-color: " + BORDER_SOFT
                + "; -fx-border-width: 0 1 0 0; -fx-font-family: " + FONT
                + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";";
    }

    public static String titleBar() {
        return "-fx-background-color: " + TOOL_BG + "; -fx-border-color: " + BORDER_SOFT
                + "; -fx-border-width: 0 0 1 0; -fx-padding: 0; -fx-font-family: " + FONT
                + "; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";";
    }

    public static String primaryButton() {
        return "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-background-radius: 4;"
                + "-fx-padding: 8 16; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-font-family: " + FONT + ";";
    }

    public static String dangerButton() {
        return "-fx-background-color: " + DANGER + "; -fx-text-fill: white; -fx-background-radius: 4;"
                + "-fx-padding: 8 16; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-font-family: " + FONT + ";";
    }

    public static String ghostButton() {
        return "-fx-background-color: transparent; -fx-text-fill: " + TEXT
                + "; -fx-cursor: hand; -fx-padding: 4 10; -fx-font-size: 15px; -fx-font-weight: bold;"
                + "-fx-background-radius: 4; -fx-font-family: " + FONT + ";";
    }

    public static String field() {
        return "-fx-background-color: " + BG + "; -fx-background-radius: 3; -fx-border-radius: 3;"
                + "-fx-border-color: " + BORDER + "; -fx-font-size: 15px; -fx-font-weight: bold;"
                + "-fx-text-fill: " + TEXT + "; -fx-padding: 8 10; -fx-font-family: " + FONT + ";";
    }

    public static String label(int sizePx) {
        return "-fx-font-family: " + FONT + "; -fx-font-size: " + sizePx + "px; -fx-font-weight: bold;"
                + " -fx-text-fill: " + TEXT + ";";
    }
}
