package xionglongztz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public record DsTabComplete() implements TabCompleter{
    private static final List<String> SUB_COMMANDS = Arrays.asList("reload", "reset", "help", "revoke");// 子命令
    // 命令相关
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("deepseek") &&
            !command.getName().equalsIgnoreCase("ds")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String partial = args[ 0 ].toLowerCase();
            return SUB_COMMANDS.stream()
                    .filter(sub -> sub.startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[ 0 ].toLowerCase();
            if (sub.equals("reload") || sub.equals("reset") || sub.equals("help") || sub.equals("revoke") || sub.equals("r")) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }// 修改TAB自动补全的候选词
}
