package xionglongztz;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public record DsCommandHandler(DeepSeek plugin) implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&r[&9DeepSeek&r] &7使用/ds help查看帮助"));
            return true;
        }
        switch (args[ 0 ].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("deepseek.admin")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes(
                            '&', "&r[&9DeepSeek&r] &c你没有权限执行此命令!"));
                    return true;
                }
                plugin.reloadPluginConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&r[&9DeepSeek&r] &a配置已重载!"));
                break;
            case "help":
                if (!sender.hasPermission("deepseek.admin")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes(
                            '&', "&r[&9DeepSeek&r] &c你没有权限执行此命令!"));
                    return true;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9===== DeepSeek Chat ====="));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&9版本:&7v%s &9作者:&7%s", this.plugin.getDescription().getVersion(), this.plugin.getDescription().getAuthors().getFirst())));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/ds help &7- 显示帮助信息"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/ds reload &7- 重载配置文件"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/ds reset &7- 清空上下文历史"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/ds < revoke | r > &7- 撤回当前会话"));
                break;
            case "reset":
                if (!sender.hasPermission("deepseek.admin")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes(
                            '&', "&r[&9DeepSeek&r] &c你没有权限执行此命令!"));
                    return true;
                }
                plugin.resetConversationHistory();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&r[&9DeepSeek&r] &a对话历史已重置!"));
                break;
            case "revoke","r":
                if (!sender.hasPermission("deepseek.admin")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes(
                            '&', "&r[&9DeepSeek&r] &c你没有权限执行此命令!"));
                    return true;
                }
                if (plugin.isProcessing.get()) {
                    plugin.revoke();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&r[&9DeepSeek&r] &a正在尝试撤回当前会话!"));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&r[&9DeepSeek&r] &e当前无任何进行中的请求!"));
                }
                break;
            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&r[&9DeepSeek&r] &c未知命令,使用/ds help查看帮助"));
                break;
        }
        return true;
    }
}
