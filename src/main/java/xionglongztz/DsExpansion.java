package xionglongztz;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;// PAPI
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;// 玩家实体
import org.jetbrains.annotations.NotNull;

public class DsExpansion extends PlaceholderExpansion {// 占位符扩展类
    private final DeepSeek plugin;
    public DsExpansion(DeepSeek plugin) {this.plugin = plugin;}
    @Override
    public @NotNull String getIdentifier() {return "deepseek";}// 获取标识符
    @Override
    public @NotNull String getAuthor() {return plugin.getDescription().getAuthors().toString();}// 获取作者
    @Override
    public boolean persist() {return true;}// 必须为true，否则重载后失效
    @Override
    public @NotNull String getVersion() {return plugin.getDescription().getVersion();}// 获取版本号
    @Override
    public boolean canRegister() {return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");}
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        return switch (identifier) {
            case "1" -> "1";// 当前问题
            case "2" -> String.valueOf(2);// 是否已被回答
            default -> null;
        };
    }
}