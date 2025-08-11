package xionglongztz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import static xionglongztz.DeepSeek.newPF;

public record DsUtils(DeepSeek plugin) implements Listener {
    public static String cleanResponse(String response) {
        // 移除Markdown符号
        //response = MARKDOWN_PATTERN.matcher(response).replaceAll("");
        // 替换多个换行为单个换行
        response = response.replaceAll("\n+", "\n");
        response = unescapeUnicode(response);
        return response.trim();
    }// 清除markdown格式
    public static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }// 整理换行格式
    public static String formatMessage(String content) {
        return ChatColor.translateAlternateColorCodes('&',
                newPF + content);
    }// 消息加前缀
    public static String unescapeUnicode(String input) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                // 处理 \\uXXXX 序列
                try {
                    String hex = input.substring(i + 2, i + 6);
                    char c = (char) Integer.parseInt(hex, 16);
                    builder.append(c);
                    i += 6;
                } catch (Exception e) {
                    // 如果格式不正确，保持原样
                    builder.append(input.charAt(i));
                    i++;
                }
            } else {
                builder.append(input.charAt(i));
                i++;
            }
        }
        return builder.toString();
    }// Unicode转换
    public static void broadcastMultiline(String message) {
        String[] lines = message.split("\n"); // 按换行符拆分
        for (String line : lines) {
            if (!line.trim().isEmpty()) { // 忽略空行
                Bukkit.broadcastMessage(formatMessage(line));
            }
        }
    }// 多行广播消息
    public static String colorize(String message) {
        // 转换颜色字符
        return ChatColor.translateAlternateColorCodes('&',message);
    }// 颜色代码转换
}
