package xionglongztz;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DsExpansion extends PlaceholderExpansion {
    private final DeepSeek plugin;
    public DsExpansion(DeepSeek plugin) {this.plugin = plugin;}
    @Override
    public @NotNull String getIdentifier() {return "deepseek";}
    @Override
    public @NotNull String getAuthor() {return plugin.getDescription().getAuthors().getFirst();}
    @Override
    public boolean persist() {return true;}
    @Override
    public @NotNull String getVersion() {return plugin.getDescription().getVersion();}
    @Override
    public boolean canRegister() {return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");}
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return null;
        }

        switch (identifier) {
            case "player_x":
                return String.valueOf(player.getLocation().getBlockX());
            case "player_y":
                return String.valueOf(player.getLocation().getBlockY());
            case "player_z":
                return String.valueOf(player.getLocation().getBlockZ());
            case "player_biome":
                return player.getLocation().getBlock().getBiome().name();
            case "player_biome_capitalized":
                String biomeName = player.getLocation().getBlock().getBiome().name();
                return biomeName.substring(0, 1).toUpperCase() + biomeName.substring(1).toLowerCase();
            case "player_block_underneath":
                return player.getLocation().subtract(0, 1, 0).getBlock().getType().name();
            case "player_health_rounded":
                return String.valueOf(Math.round(player.getHealth()));
            case "player_exp":
                return String.valueOf(player.getExp());
            case "player_current_exp":
                return String.valueOf(player.getTotalExperience());
            case "player_exp_to_level":
                return String.valueOf(player.getExpToLevel());
            case "player_food_level":
                return String.valueOf(player.getFoodLevel());
            case "player_gamemode":
                return player.getGameMode().name();
            case "player_is_op":
                return player.isOp() ? "yes" : "no";
            case "player_item_in_hand":
                return player.getInventory().getItemInMainHand().getType().name();
            case "player_item_in_hand_name":
                return player.getInventory().getItemInMainHand().hasItemMeta() && player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() ?
                        player.getInventory().getItemInMainHand().getItemMeta().getDisplayName() : "";
            case "player_item_in_hand_durability":
                if (player.getInventory().getItemInMainHand().hasItemMeta() && player.getInventory().getItemInMainHand().getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                    org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) player.getInventory().getItemInMainHand().getItemMeta();
                    return String.valueOf(player.getInventory().getItemInMainHand().getType().getMaxDurability() - damageable.getDamage());
                }
                return "0";
            case "player_name":
                return player.getName();
            case "player_world_type":
                return player.getWorld().getWorldType().name();
            case "vault_eco_balance":
                if (DeepSeek.econ != null) {
                    return String.valueOf(DeepSeek.econ.getBalance(player));
                }
                return null;
            case "server_name":
                return Bukkit.getServer().getName();
            case "server_online":
                return String.valueOf(Bukkit.getOnlinePlayers().size());
            case "server_version":
                return Bukkit.getVersion();
            case "server_ram_free":
                return String.valueOf(Runtime.getRuntime().freeMemory() / 1024 / 1024);
            case "server_ram_max":
                return String.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024);
            case "server_ram_total":
                return String.valueOf(Runtime.getRuntime().totalMemory() / 1024 / 1024);
            case "server_ram_used":
                return String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
            case "server_uptime":
                long uptimeTicks = Bukkit.getServer().getWorlds().get(0).getFullTime();
                long seconds = uptimeTicks / 20;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
            default:
                return null;
        }
    }
}
