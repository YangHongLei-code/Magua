package org.example.magua.ui;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.concurrent.Worker;
import javafx.scene.Parent;
import javafx.scene.web.WebView;

/**
 * 用 WebView + Flexmark 渲染 Markdown，高度随内容自适应。
 */
public class MarkdownView {

    private static Parser parser;
    private static HtmlRenderer renderer;

    private WebView webView = new WebView();
    private StringBuilder buffer = new StringBuilder();

    public MarkdownView() {
        webView.setContextMenuEnabled(false);
        webView.setPrefHeight(24);
        webView.setMinHeight(0);
        webView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // 页面加载完成后，根据实际内容高度调整 WebView 高度
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Object h = webView.getEngine().executeScript(
                        "document.body.scrollHeight"
                );
                if (h instanceof Number n) {
                    double height = n.doubleValue();
                    if (height < 1) {
                        height = 24;
                    }
                    webView.setPrefHeight(height);
                    webView.setMinHeight(height);
                    webView.setMaxHeight(height);
                }
            }
        });
    }

    public void append(String text) {
        buffer.append(text);
        render();
    }

    public void setText(String text) {
        buffer = new StringBuilder(text == null ? "" : text);
        render();
    }

    public String getText() {
        return buffer.toString();
    }

    private void render() {
        String html = renderMarkdown(buffer.toString());
        webView.getEngine().loadContent(wrap(html));
    }

    private String wrap(String bodyHtml) {
        return "<html><head><meta charset='utf-8'><style>"
                + "html,body{margin:0;padding:0;background:transparent;}"
                + "body{font-family:'Noto Sans CJK SC','WenQuanYi Micro Hei','Microsoft YaHei UI','Segoe UI',sans-serif;"
                + "font-size:15px;font-weight:700;color:#2B2B2B;line-height:1.65;padding:2px;}"
                + "code,pre{font-family:Consolas,'Cascadia Mono','JetBrains Mono',monospace;background:#F2F2F2;border-radius:2px;}"
                + "code{padding:1px 4px;font-size:14px;font-weight:700;}"
                + "pre{padding:8px;overflow:auto;border:1px solid #D0D0D0;}"
                + "pre code{padding:0;background:transparent;}"
                + "table{border-collapse:collapse;margin:8px 0;}"
                + "th,td{border:1px solid #BDBDBD;padding:4px 8px;text-align:left;}"
                + "th{background:#F2F2F2;font-weight:700;}"
                + "blockquote{border-left:3px solid #BDBDBD;margin:8px 0;padding:4px 10px;color:#2B2B2B;}"
                + "a{color:#2F6FED;font-weight:700;}"
                + "strong,b{font-weight:800;}"
                + "h1,h2,h3,h4{font-weight:800;color:#2B2B2B;}"
                + "</style></head><body>"
                + bodyHtml
                + "</body></html>";
    }

    private String renderMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        return getRenderer().render(getParser().parse(markdown));
    }

    private Parser getParser() {
        if (parser == null) {
            MutableDataSet options = new MutableDataSet();
            parser = Parser.builder(options).build();
        }
        return parser;
    }

    private HtmlRenderer getRenderer() {
        if (renderer == null) {
            MutableDataSet options = new MutableDataSet();
            renderer = HtmlRenderer.builder(options).build();
        }
        return renderer;
    }

    public Parent getView() {
        return webView;
    }
}
