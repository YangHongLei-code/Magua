package org.example.magua.tool;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ToolRegistry {

    private static final String LOCAL_TOOL_PACKAGE = "org.example.magua.tool.local";

    private final Map<String, AgentTool> tools = new HashMap<>();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private ToolRegistry() {
        try {
            String path = LOCAL_TOOL_PACKAGE.replace('.', '/');
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = cl.getResources(path);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("file".equals(url.getProtocol())) {
                    File[] files = new File(url.toURI()).listFiles();
                    if (files == null) {
                        continue;
                    }
                    for (File file : files) {
                        String name = file.getName();
                        if (!file.isFile() || !name.endsWith(".class")) {
                            continue;
                        }
                        String simpleName = name.substring(0, name.length() - ".class".length());
                        loadAndRegister(LOCAL_TOOL_PACKAGE + "." + simpleName);
                    }
                } else if ("jar".equals(url.getProtocol())) {
                    String raw = url.toString();
                    int sep = raw.indexOf("!/");
                    String jarPath = raw.substring(4, sep); // 去掉 "jar:"
                    String prefix = path + "/";
                    try (JarFile jar = new JarFile(new File(URI.create(jarPath)))) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            String name = entries.nextElement().getName();
                            if (!name.startsWith(prefix) || !name.endsWith(".class")) {
                                continue;
                            }
                            // 只要 local 下一层，不要子目录
                            String rest = name.substring(prefix.length());
                            if (rest.contains("/")) {
                                continue;
                            }
                            loadAndRegister(name.replace('/', '.').replace(".class", ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("加载工具失败: " + LOCAL_TOOL_PACKAGE, e);
        }
    }

    private void loadAndRegister(String className) throws Exception {
        if (className.contains("$")) {
            return;
        }
        Class<?> clazz = Class.forName(className);
        if (!AgentTool.class.isAssignableFrom(clazz)) {
            return;
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return;
        }
        AgentTool tool = (AgentTool) clazz.getDeclaredConstructor().newInstance();
        register(tool);
    }

    public void register(AgentTool tool) {
        if (tool == null || tool.name() == null || tool.name().isBlank()) {
            return;
        }
        tools.put(tool.name(), tool);
    }

    public JsonNode allToolSchemas() {
        ArrayNode array = jsonMapper.createArrayNode();
        for (AgentTool tool : tools.values()) {
            ObjectNode wrapper = jsonMapper.createObjectNode();
            wrapper.put("type", "function");
            wrapper.set("function", tool.schema());
            array.add(wrapper);
        }
        return array;
    }

    public String execute(String name, JsonNode args) {
        AgentTool tool = tools.get(name);
        if (tool == null) {
            return "未知工具: " + name;
        }
        return tool.execute(args);
    }

    private static class Holder {
        private static final ToolRegistry INSTANCE = new ToolRegistry();
    }

    public static ToolRegistry getInstance() {
        return Holder.INSTANCE;
    }
}
