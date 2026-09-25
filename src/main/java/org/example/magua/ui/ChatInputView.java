package org.example.magua.ui;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * 基于 WebView 的输入框。
 * Linux 上 JavaFX TextArea / SwingNode 对 IME、焦点都不稳；WebKit 输入法更可靠。
 */
public class ChatInputView {

    public interface SendHandler {
        void onSend();
    }

    private final WebView webView = new WebView();
    private final WebEngine engine = webView.getEngine();
    private final JsBridge bridge = new JsBridge();
    private volatile boolean ready;
    private volatile boolean enabled = true;
    private SendHandler sendHandler;

    public ChatInputView() {
        webView.setPrefHeight(96);
        webView.setMinHeight(80);
        webView.setMaxHeight(160);
        webView.setContextMenuEnabled(false);
        webView.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(webView, Priority.NEVER);

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                ready = true;
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("magua", bridge);
                engine.executeScript(bindScript());
                applyEnabled();
            }
        });

        engine.loadContent(pageHtml());
    }

    public WebView getView() {
        return webView;
    }

    public void setOnSend(SendHandler sendHandler) {
        this.sendHandler = sendHandler;
    }

    public String getText() {
        if (!ready) {
            return "";
        }
        Object value = engine.executeScript(
                "document.getElementById('t') ? document.getElementById('t').value : ''"
        );
        return value == null ? "" : String.valueOf(value);
    }

    public void clear() {
        if (!ready) {
            return;
        }
        engine.executeScript(
                "var t=document.getElementById('t'); if(t){t.value=''; t.focus();}"
        );
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        applyEnabled();
    }

    public void requestFocusInput() {
        if (!ready) {
            return;
        }
        engine.executeScript("var t=document.getElementById('t'); if(t){t.focus();}");
        webView.requestFocus();
    }

    private void applyEnabled() {
        if (!ready) {
            return;
        }
        engine.executeScript(
                "var t=document.getElementById('t'); if(t){t.disabled=" + (!enabled) + ";}"
        );
    }

    private String pageHtml() {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><style>"
                + "html,body{margin:0;padding:0;height:100%;background:#F5F5F5;}"
                + "textarea{box-sizing:border-box;width:100%;height:100%;resize:none;"
                + "border:1px solid #BDBDBD;border-radius:3px;padding:8px 10px;"
                + "font-family:'Noto Sans CJK SC','WenQuanYi Micro Hei','Microsoft YaHei UI',"
                + "'Segoe UI',sans-serif;font-size:15px;font-weight:700;color:#2B2B2B;"
                + "background:#FFFFFF;outline:none;line-height:1.45;}"
                + "textarea:focus{border-color:#2F6FED;}"
                + "textarea:disabled{background:#F2F2F2;color:#3C3C3C;}"
                + "textarea::placeholder{color:#8A8A8A;font-weight:600;}"
                + "</style></head><body>"
                + "<textarea id='t' spellcheck='false' "
                + "placeholder='输入消息 · Enter 发送 · Shift+Enter 换行'></textarea>"
                + "</body></html>";
    }

    private String bindScript() {
        return ""
                + "var t=document.getElementById('t');"
                + "t.addEventListener('keydown', function(e) {"
                + "  if (e.key === 'Enter' && !e.shiftKey && !e.isComposing && e.keyCode !== 229) {"
                + "    e.preventDefault();"
                + "    if (window.magua) { window.magua.send(); }"
                + "  }"
                + "});"
                + "t.focus();";
    }

    public class JsBridge {
        public void send() {
            Platform.runLater(() -> {
                if (sendHandler != null) {
                    sendHandler.onSend();
                }
            });
        }
    }
}
