package org.example.magua.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.magua.config.Config;

/**
 * 模型配置弹窗：展示并修改 Config 中的可配置项。
 * apiKey 可填环境变量占位（如 ${DEEPSEEK_API_KEY}）或真实 key；
 * apiKeyVal 为解析后的真实值，只读展示。
 */
public class ConfigDialog {

    private final Config config = Config.getInstance();

    private final TextField apiKeyField = new TextField();
    private final PasswordField apiKeyValField = new PasswordField();
    private final TextField apiUrlField = new TextField();
    private final TextField modelField = new TextField();
    private final CheckBox streamBox = new CheckBox("流式输出 (stream)");
    private final CheckBox streamOptionsBox = new CheckBox("返回 usage (stream_options)");
    private final ComboBox<String> thinkingBox = new ComboBox<>();
    private final ComboBox<String> reasoningEffortBox = new ComboBox<>();
    private final TextField temperatureField = new TextField();
    private final TextField topPField = new TextField();

    private Stage stage;

    public void show(Stage owner) {
        loadFromConfig();

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 20, 8, 20));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(88);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        int row = 0;
        grid.add(formLabel("API Key"), 0, row);
        apiKeyField.setMaxWidth(Double.MAX_VALUE);
        apiKeyField.setPromptText("${DEEPSEEK_API_KEY} 或直接填密钥");
        grid.add(apiKeyField, 1, row);

        grid.add(formLabel("解析后密钥"), 0, ++row);
        apiKeyValField.setEditable(false);
        apiKeyValField.setDisable(true);
        apiKeyValField.setMaxWidth(Double.MAX_VALUE);
        grid.add(apiKeyValField, 1, row);

        Label apiKeyHint = new Label("支持 ${环境变量名}，或直接填写真实 key");
        apiKeyHint.setStyle(UiTheme.label(13));
        grid.add(apiKeyHint, 1, ++row);

        grid.add(formLabel("API URL"), 0, ++row);
        apiUrlField.setMaxWidth(Double.MAX_VALUE);
        grid.add(apiUrlField, 1, row);

        grid.add(formLabel("模型"), 0, ++row);
        modelField.setMaxWidth(Double.MAX_VALUE);
        grid.add(modelField, 1, row);

        grid.add(streamBox, 1, ++row);
        grid.add(streamOptionsBox, 1, ++row);

        grid.add(formLabel("思考模式"), 0, ++row);
        thinkingBox.getItems().setAll("enabled", "disabled");
        thinkingBox.setEditable(true);
        thinkingBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(thinkingBox, Priority.ALWAYS);
        grid.add(thinkingBox, 1, row);

        grid.add(formLabel("思考强度"), 0, ++row);
        reasoningEffortBox.getItems().setAll("none", "low", "high", "max", "minimal", "medium", "xhigh");
        reasoningEffortBox.setEditable(true);
        reasoningEffortBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(reasoningEffortBox, Priority.ALWAYS);
        grid.add(reasoningEffortBox, 1, row);

        grid.add(formLabel("temperature"), 0, ++row);
        temperatureField.setMaxWidth(Double.MAX_VALUE);
        grid.add(temperatureField, 1, row);

        grid.add(formLabel("top_p"), 0, ++row);
        topPField.setMaxWidth(Double.MAX_VALUE);
        grid.add(topPField, 1, row);

        Button saveBtn = new Button("保存");
        saveBtn.setStyle(UiTheme.primaryButton());
        saveBtn.setOnAction(e -> {
            if (saveToConfig()) {
                System.out.println("用户保存了配置");
                stage.close();
            }
        });

        Button cancelBtn = new Button("取消");
        cancelBtn.setStyle(
                "-fx-background-color: " + UiTheme.HOVER + "; -fx-text-fill: " + UiTheme.TEXT
                        + "; -fx-background-radius: 4; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 12px;"
                        + "-fx-border-color: " + UiTheme.BORDER + "; -fx-border-radius: 4;"
        );
        cancelBtn.setOnAction(e -> {
            System.out.println("用户取消了配置");
            stage.close();
        });

        HBox buttons = new HBox(8, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(12, 20, 16, 20));

        VBox root = new VBox(grid, buttons);
        root.setStyle("-fx-background-color: " + UiTheme.BG + ";");

        Scene scene = new Scene(root, 560, 520);
        var css = getClass().getResource("/org/example/magua/ui/workbench.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("模型配置");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private Label formLabel(String text) {
        Label label = new Label(text);
        label.setStyle(UiTheme.label(14));
        return label;
    }

    private void loadFromConfig() {
        apiKeyField.setText(nullToEmpty(config.getApiKey()));
        apiKeyValField.setText(nullToEmpty(config.getApiKeyVal()));
        apiUrlField.setText(nullToEmpty(config.getApiUrl()));
        modelField.setText(nullToEmpty(config.getModel()));
        streamBox.setSelected(config.isStream());
        streamOptionsBox.setSelected(config.isStreamOptions());
        thinkingBox.setValue(config.getThinking());
        reasoningEffortBox.setValue(config.getReasoningEffort());
        temperatureField.setText(String.valueOf(config.getTemperature()));
        topPField.setText(String.valueOf(config.getTopP()));
    }

    private boolean saveToConfig() {
        try {
            config.setApiKey(apiKeyField.getText() == null ? "" : apiKeyField.getText().trim());
            config.setApiUrl(apiUrlField.getText());
            config.setModel(modelField.getText());
            config.setStream(streamBox.isSelected());
            config.setStreamOptions(streamOptionsBox.isSelected());
            config.setThinking(thinkingBox.getValue());
            config.setReasoningEffort(reasoningEffortBox.getValue());
            config.setTemperature(Double.parseDouble(temperatureField.getText().trim()));
            config.setTopP(Double.parseDouble(topPField.getText().trim()));
            config.save();
            return true;
        } catch (NumberFormatException e) {
            System.out.println("保存失败：temperature / top_p 必须是数字");
            return false;
        } catch (Exception e) {
            System.out.println("保存失败：" + e.getMessage());
            return false;
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
