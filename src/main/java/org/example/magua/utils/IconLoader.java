package org.example.magua.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从 classpath 静态资源加载图标（{@code /icons/{name}.png}）。
 */
public final class IconLoader {

    private static final String BASE_PATH = "/icons/";
    private static final double DEFAULT_SIZE = 20;
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private IconLoader() {
    }

    /**
     * 加载图标并返回固定尺寸的 {@link ImageView}。
     *
     * @param name 资源名（不含路径和后缀），例如 {@code explorer} 对应 {@code /icons/explorer.png}
     */
    public static ImageView icon(String name) {
        return icon(name, DEFAULT_SIZE);
    }

    public static ImageView icon(String name, double size) {
        Image image = getImage(name);
        ImageView view = new ImageView(image);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setMouseTransparent(true);
        return view;
    }

    public static Image getImage(String name) {
        return CACHE.computeIfAbsent(name, IconLoader::loadImage);
    }

    private static Image loadImage(String name) {
        String path = BASE_PATH + name + ".png";
        InputStream stream = IconLoader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalArgumentException("图标资源不存在: " + path);
        }
        try (stream) {
            return new Image(stream);
        } catch (Exception e) {
            throw new IllegalStateException("加载图标失败: " + path, e);
        }
    }
}
