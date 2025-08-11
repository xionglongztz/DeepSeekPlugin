package xionglongztz;

import me.arasple.mc.trchat.api.event.TrChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public record DsTrChatMsg(DeepSeek plugin) implements Listener {
    @EventHandler
    private void onTrChatReceive(TrChatEvent e) {
        // 在这里处理 TrChat 事件
        // 可以用反射读取消息
        // TrChat 过滤后的内容
        String message = e.getMessage();
        Player player = e.getPlayer();
        String CallMethod = plugin.config.getString("Call", "@AI");
        // 在权限检查前先判断是否正在处理
        synchronized (plugin.processingLock) {
            if (plugin.isProcessing.get() && message.startsWith(CallMethod)) {
                //player.sendMessage(formatMessage(config.getString("isProcessingMsg", "&c当前响应正在请求中,请耐心等待!")));
                // 如果有处理中的请求，直接忽略所有新请求
                return;
            }
        }// 原子化保证每次只有一条消息被处理
        if (player.hasPermission("deepseek.use") && message.startsWith(CallMethod)) {
            plugin.DeepSeekCall(message, player);
        }
    }// TrChat 消息事件
}
