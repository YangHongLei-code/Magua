package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.magua.dialogue.entity.Usage;

/**
 * 助手消息卡片：多轮展示。每轮可能含 深度思考 / 正文 / 工具调用 / token，
 * 有则显示，无则隐藏。最后一轮（无工具调用）的正文即「最终回答」。
 * 深度思考和正文都用 Markdown 渲染。
 */
public class AssistantMessagePane {

    private VBox root = new VBox(10);
    private VBox roundsBox = new VBox(10);
    private RoundBlock currentRound;
    private int roundIndex = 0;

    public AssistantMessagePane() {
        root.setFillWidth(true);
        root.setMaxWidth(Double.MAX_VALUE);
        root.setPadding(new Insets(10));
        root.setStyle(
                "-fx-background-color: " + UiTheme.ASSISTANT_BG + "; -fx-background-radius: 3;"
                        + "-fx-border-color: " + UiTheme.BORDER_SOFT + "; -fx-border-radius: 3; -fx-border-width: 1;"
        );

        Label role = new Label("助手");
        role.setStyle(UiTheme.label(13)
                + "-fx-background-color: " + UiTheme.HOVER + "; -fx-background-radius: 2; -fx-padding: 2 8;");

        root.getChildren().addAll(role, roundsBox);
        ensureRound();
    }

    public void appendReasoning(String text) {
        ensureRound();
        currentRound.appendReasoning(text);
    }

    public void appendContent(String text) {
        ensureRound();
        currentRound.appendContent(text);
    }

    public void toolStart(String name) {
        ensureRound();
        currentRound.toolStart(name);
    }

    public void toolResult(String name) {
        ensureRound();
        currentRound.toolResult(name);
    }

    public void updateUsage(Object data) {
        if (data == null) {
            return;
        }
        ensureRound();
        currentRound.updateUsage(toUsage(data));
    }

    /** 本轮工具结束，开启下一轮。 */
    public void beginNextRound() {
        currentRound.markRoundFinished();
        currentRound = null;
        ensureRound();
    }

    /** 整次回答结束（onComplete）。 */
    public void complete() {
        if (currentRound != null) {
            currentRound.markRoundFinished();
        }
    }

    private void ensureRound() {
        if (currentRound != null) {
            return;
        }
        roundIndex++;
        currentRound = new RoundBlock(roundIndex);
        roundsBox.getChildren().add(currentRound.getView());
    }

    private Usage toUsage(Object data) {
        if (data instanceof Usage usage) {
            return usage;
        }
        if (data instanceof java.util.Map<?, ?> map) {
            Usage usage = new Usage();
            usage.setTotalTokens(toInt(map.get("total_tokens")));
            usage.setPromptTokens(toInt(map.get("prompt_tokens")));
            usage.setCompletionTokens(toInt(map.get("completion_tokens")));
            usage.setPromptCacheHitTokens(toInt(map.get("prompt_cache_hit_tokens")));
            usage.setPromptCacheMissTokens(toInt(map.get("prompt_cache_miss_tokens")));
            return usage;
        }
        return null;
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    public VBox getView() {
        return root;
    }

    private static class RoundBlock {
        private VBox root = new VBox(8);
        private Label roundTitle;

        private TitledPane thinkingPane;
        private MarkdownView reasoningView = new MarkdownView();

        private VBox contentBox = new VBox(4);
        private Label contentTitle = new Label();
        private MarkdownView contentView = new MarkdownView();

        private Label toolTitle = new Label("调用工具");
        private VBox toolBox = new VBox(6);

        private Label usageLabel = new Label();

        private RoundBlock(int index) {
            roundTitle = new Label("第 " + index + " 轮");
            roundTitle.setStyle(UiTheme.label(13));

            // 深度思考
            VBox thinkingContent = new VBox(reasoningView.getView());
            thinkingContent.setPadding(new Insets(6, 0, 0, 0));

            thinkingPane = new TitledPane("深度思考", thinkingContent);
            thinkingPane.setExpanded(false);
            thinkingPane.setAnimated(true);
            thinkingPane.setStyle(UiTheme.label(14));
            thinkingPane.setVisible(false);
            thinkingPane.setManaged(false);

            // 正文（最终回答）
            contentTitle.setText("最终回答");
            contentTitle.setStyle(UiTheme.label(14));
            contentTitle.setVisible(false);
            contentTitle.setManaged(false);

            contentBox.getChildren().addAll(contentTitle, contentView.getView());
            contentBox.setVisible(false);
            contentBox.setManaged(false);

            // 工具
            toolTitle.setStyle(UiTheme.label(13));
            toolTitle.setVisible(false);
            toolTitle.setManaged(false);

            // token
            usageLabel.setStyle(UiTheme.label(13));
            usageLabel.setVisible(false);
            usageLabel.setManaged(false);

            root.setPadding(new Insets(8));
            root.setStyle(
                    "-fx-background-color: " + UiTheme.BG + "; -fx-background-radius: 2;"
                            + "-fx-border-color: " + UiTheme.BORDER_SOFT + "; -fx-border-radius: 2;"
            );
            root.getChildren().addAll(roundTitle, thinkingPane, contentBox, toolTitle, toolBox, usageLabel);
        }

        private void appendReasoning(String text) {
            reasoningView.append(text);
            if (!thinkingPane.isVisible()) {
                thinkingPane.setVisible(true);
                thinkingPane.setManaged(true);
            }
            if (!thinkingPane.isExpanded() && reasoningView.getText().length() < 80) {
                thinkingPane.setExpanded(true);
            }
        }

        private void appendContent(String text) {
            contentView.append(text);
            if (!contentBox.isVisible()) {
                contentBox.setVisible(true);
                contentBox.setManaged(true);
                contentTitle.setVisible(true);
                contentTitle.setManaged(true);
                // 出现正文即视为最终回答轮，隐藏「第 N 轮」标题
                roundTitle.setVisible(false);
                roundTitle.setManaged(false);
            }
        }

        private void toolStart(String name) {
            if (!toolTitle.isVisible()) {
                toolTitle.setVisible(true);
                toolTitle.setManaged(true);
            }
            HBox row = findOrCreateToolRow(name);
            Label status = (Label) row.getChildren().get(3);
            status.setText("执行中...");
            status.setStyle(UiTheme.label(13) + "-fx-text-fill: " + UiTheme.ACCENT + ";");
        }

        private void toolResult(String name) {
            HBox row = findOrCreateToolRow(name);
            Label status = (Label) row.getChildren().get(3);
            status.setText("已结束");
            status.setStyle(UiTheme.label(13));
        }

        private HBox findOrCreateToolRow(String name) {
            for (var node : toolBox.getChildren()) {
                if (node instanceof HBox row && name.equals(row.getUserData())) {
                    return row;
                }
            }
            Label check = new Label("·");
            check.setStyle(UiTheme.label(16) + "-fx-text-fill: " + UiTheme.ACCENT + ";");

            Label nameLabel = new Label(name == null ? "tool" : name);
            nameLabel.setStyle(UiTheme.label(14));
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Label status = new Label("执行中...");
            status.setStyle(UiTheme.label(13) + "-fx-text-fill: " + UiTheme.ACCENT + ";");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(8, check, nameLabel, spacer, status);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setUserData(name);
            row.setPadding(new Insets(6, 8, 6, 8));
            row.setStyle(
                    "-fx-background-color: " + UiTheme.TOOL_ROW_BG + "; -fx-background-radius: 2;"
                            + "-fx-border-color: " + UiTheme.BORDER_SOFT + "; -fx-border-radius: 2;"
            );
            toolBox.getChildren().add(row);
            return row;
        }

        private void updateUsage(Usage usage) {
            if (usage == null) {
                return;
            }
            usageLabel.setText(
                    "总消耗: " + usage.getTotalTokens()
                            + " · 输入: " + usage.getPromptTokens()
                            + " · 输出: " + usage.getCompletionTokens()
                            + " · 缓存命中: " + usage.getPromptCacheHitTokens()
            );
            usageLabel.setVisible(true);
            usageLabel.setManaged(true);
        }

        /** 本轮收尾：若本轮没有任何实质内容，则隐藏整轮容器。 */
        private void markRoundFinished() {
            boolean hasReasoning = reasoningView.getText() != null && !reasoningView.getText().isBlank();
            boolean hasContent = contentView.getText() != null && !contentView.getText().isBlank();
            boolean hasTool = !toolBox.getChildren().isEmpty();
            boolean hasUsage = usageLabel.isVisible();

            if (!hasReasoning) {
                thinkingPane.setVisible(false);
                thinkingPane.setManaged(false);
            } else {
                thinkingPane.setExpanded(false);
            }

            if (!hasContent) {
                contentBox.setVisible(false);
                contentBox.setManaged(false);
            }

            if (!hasTool) {
                toolTitle.setVisible(false);
                toolTitle.setManaged(false);
            }

            if (!hasUsage) {
                usageLabel.setVisible(false);
                usageLabel.setManaged(false);
            }

            // 整轮啥都没有：隐藏轮标题和容器
            if (!hasReasoning && !hasContent && !hasTool && !hasUsage) {
                roundTitle.setVisible(false);
                roundTitle.setManaged(false);
                root.setVisible(false);
                root.setManaged(false);
            }
        }

        private VBox getView() {
            return root;
        }
    }
}
