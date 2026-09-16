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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    private Config config = Config.getInstance();

    private TextField apiKeyField = new TextField();
    private PasswordField apiKeyValField = new PasswordField();
    private TextField apiUrlField = new TextField();
    private TextField modelField = new TextField();
    private CheckBox streamBox = new CheckBox("流式输出 (stream)");
    private CheckBox streamOptionsBox = new CheckBox("返回 usage (stream_options)");
    private ComboBox<String> thinkingBox = new ComboBox<>();
    private ComboBox<String> reasoningEffortBox = new ComboBox<>();
    private TextField temperatureField = new TextField();
    private TextField topPField = new TextField();

    private Stage stage;

    public void show(Stage owner) {
        loadFromConfig();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        int row = 0;
        grid.add(new Label("API Key:"), 0, row);
        apiKeyField.setPrefWidth(360);
        apiKeyField.setPromptText("${DEEPSEEK_API_KEY} 或直接填密钥");
        grid.add(apiKeyField, 1, row);

        grid.add(new Label("解析后密钥:"), 0, ++row);
        apiKeyValField.setEditable(false);
        apiKeyValField.setDisable(true);
        grid.add(apiKeyValField, 1, row);

        Label apiKeyHint = new Label("支持 ${环境变量名}，或直接填写真实 key");
        apiKeyHint.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        grid.add(apiKeyHint, 1, ++row);

        grid.add(new Label("API URL:"), 0, ++row);
        grid.add(apiUrlField, 1, row);

        grid.add(new Label("模型:"), 0, ++row);
        grid.add(modelField, 1, row);

        grid.add(streamBox, 1, ++row);
        grid.add(streamOptionsBox, 1, ++row);

        grid.add(new Label("思考模式:"), 0, ++row);
        thinkingBox.getItems().setAll("enabled", "disabled");
        thinkingBox.setEditable(true);
        grid.add(thinkingBox, 1, row);

        grid.add(new Label("思考强度:"), 0, ++row);
        reasoningEffortBox.getItems().setAll("none", "low", "high", "max", "minimal", "medium", "xhigh");
        reasoningEffortBox.setEditable(true);
        grid.add(reasoningEffortBox, 1, row);

        grid.add(new Label("temperature:"), 0, ++row);
        grid.add(temperatureField, 1, row);

        grid.add(new Label("top_p:"), 0, ++row);
        grid.add(topPField, 1, row);

        Button saveBtn = new Button("保存");
        saveBtn.setStyle(
                "-fx-background-color: #43a047; -fx-text-fill: white; -fx-background-radius: 6;"
                        + "-fx-padding: 8 20; -fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> {
            if (saveToConfig()) {
                System.out.println("用户保存了配置");
                stage.close();
            }
        });

        Button cancelBtn = new Button("取消");
        cancelBtn.setStyle(
                "-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 6;"
                        + "-fx-padding: 8 20; -fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> {
            System.out.println("用户取消了配置");
            stage.close();
        });

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(0, 16, 16, 16));

        VBox root = new VBox(grid, buttons);
        root.setStyle("-fx-background-color: white;");

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("模型配置");
        stage.setScene(new Scene(root, 560, 520));
        stage.setResizable(false);
        stage.show();
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
            // apiKeyVal 由 Config.save()/加载逻辑解析，弹窗不直接写
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
