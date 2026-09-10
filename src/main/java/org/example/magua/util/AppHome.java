package org.example.magua.util;

import org.example.magua.Main;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author: yhl
 * @CreateTime: 2026-09-08  15:46
 * @Description: 程序安装根目录：jar 所在目录；开发期回退到当前工作目录。
 */
public class AppHome {

    private AppHome() {
    }

    public static Path resolve() {
        try {
            URI uri = Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            Path location = Paths.get(uri);
            if (Files.isRegularFile(location) && location.getFileName().toString().endsWith(".jar")) {
                return location.getParent().toAbsolutePath().normalize();
            }
        } catch (Exception ignored) {
            // 走 fallback
        }
        return Paths.get("").toAbsolutePath().normalize();
    }
    /**
     * 只有以 ~ 开头的参数才做路径替换（将 ~ 替换为 home 目录），其余参数原样返回。
     */
    public static Path resolvePath(Path home, String raw) {
        String resolvedRaw = raw;
        if (raw.startsWith("~")) {
            resolvedRaw = raw.replace("~", home.toString());
        }
        Path p = Path.of(resolvedRaw);
        return p.normalize();
    }

    /**
     * args 约定：只有以 ~ 开头的参数才做路径替换（将 ~ 替换为 home 目录），其余参数原样返回。
     */
    public static String resolveArg(Path home, String arg) {
        if (arg == null || arg.isBlank()) {
            return arg;
        }
        if (arg.startsWith("~")) {
            // 约定：用户必须写成 ~/xxx 或 ~\xxx
            // 将 ~ 替换为 home 的字符串表示
            String replaced = arg.replace("~", home.toString());
            // 使用 Path 规范化，处理多余的斜杠等
            return Path.of(replaced).normalize().toString();
        }
        return arg;
    }
}
