package xionglongztz;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;// Vault

import static xionglongztz.DsUtils.*;

public class DeepSeek extends JavaPlugin implements Listener {
    // 各种变量
    public FileConfiguration config;
    //public final Map<UUID, String> conversationHistory = new ConcurrentHashMap<>();
    private final Deque<String> conversationHistory = new ArrayDeque<>();// 玩家历史记录
    private static final String PF = "&r[&9DeepSeek&r] ";// 插件前缀
    public static String newPF = "&r[&9DeepSeek&r] ";// 管理员自定义的插件前缀
    // 使用 AtomicBoolean 替代简单的 boolean 保证线程安全
    public final AtomicBoolean isProcessing = new AtomicBoolean(false);
    public final Object processingLock = new Object();
    public static Economy econ = null;// 经济插件实例
    private int tokens;// 上一个请求中token数量
    private Boolean doRevoke = false;// 定义是否撤回
    // 启动和停止相关
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        getServer().getPluginManager().registerEvents(this, this);
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();// 获得控制台消息sender
        console.sendMessage(colorize("&9 DeepSeek AI Chat Plugin   _"));
        console.sendMessage(colorize("&9       ,_______,--''.>    / \\_       __"));
        console.sendMessage(colorize("&9   _,'^             (_    |   ), ,--/ |"));
        console.sendMessage(colorize("&9  /                   \\_  \\_   `'    _/"));
        console.sendMessage(colorize("&9 /  ___                 \\__ \\.   __,'"));
        console.sendMessage(colorize("&9|  |   '---+_       ,-._   `v'  |"));
        console.sendMessage(colorize("&9|  \\         \\_.     O| `\\      /"));
        console.sendMessage(colorize("&9 \\  \\           \\_     `--'    /"));
        console.sendMessage(colorize("&9  \\  \\_      ,.   \\          _/"));
        console.sendMessage(colorize("&9   \\   \\_    \\ \\_. \\.      _/ "));
        console.sendMessage(colorize("&9    `-_  '---'    \\__`-,    '--\\"));
        console.sendMessage(colorize("&9       `-_           _./^-----'"));
        console.sendMessage(colorize("&9           `-------'   "));
        console.sendMessage(colorize(PF + "&9版本 &7- &7v" + this.getDescription().getVersion()));
        console.sendMessage(colorize(PF + "&9作者 &7- &7" + this.getDescription().getAuthors().getFirst()));
        console.sendMessage(colorize(PF + "&9环境 &7- &7" + Bukkit.getName()));
        saveDefaultConfig();
        loadConfig();
        setupEconomy();// 检查并加载 Vault
        setupExpansion();// 检查并加载 PlaceholderAPI
        setupTrChat(); // 检查并加载 TrChat
        getServer().getPluginManager().registerEvents(new DsUtils(this), this);// 文本处理相关
        PluginCommand cmd = getCommand("deepseek");
        if (cmd != null) {
            cmd.setExecutor(new DsCommandHandler(this));
            cmd.setTabCompleter(new DsTabComplete());
        }
        console.sendMessage(colorize(PF + "&aDeepSeek 插件已成功启用"));
        console.sendMessage(colorize(PF + "&a欢迎加入我们的插件交流群:&71058206145"));
    }
    @Override
    public void onDisable() {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();// 获得控制台消息sender
        console.sendMessage(colorize(PF + "正在清空对话历史记录..."));
        conversationHistory.clear();
    }
    // 插件兼容
    private void setupEconomy() {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();// 获得控制台消息sender
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            console.sendMessage(colorize(PF + "&c挂钩 &7- &c未找到 &6Vault"));
            return;
        }// 检查前置是否满足
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            console.sendMessage(colorize(PF + "&c挂钩 &7- &c已找到 &6Vault &c但缺少经济插件"));
            return;
        } else {
            console.sendMessage(colorize(PF + "&a挂钩 &7- &a已找到 &6Vault "));
            console.sendMessage(colorize(PF + "&a挂钩 &7- &a已找到 &6" + rsp.getPlugin().getName()));
        }
        econ = rsp.getProvider();
    }// 检查并加载 Vault
    private void setupExpansion() {
        boolean hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();// 获得控制台消息sender
        if (hasPAPI) {// 检查 PlaceholderAPI
            new DsExpansion(this).register();
            console.sendMessage(colorize(PF + "&a挂钩 &7- &a已找到 &6PlaceholderAPI"));

            // 获取 PlaceholderAPI 的 Logger 实例
            Logger papiLogger = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")).getLogger();
            Filter originalFilter = papiLogger.getFilter(); // 保存原始过滤器

            // 创建一个自定义的日志过滤器
            Filter logFilter = record -> {
                String message = record.getMessage();
                Throwable thrown = record.getThrown();

                boolean isCanceledException = (thrown instanceof java.util.concurrent.CancellationException);
                boolean isFailedDownloadMessage = (message != null && message.contains("Failed to download expansion"));

                // 过滤掉 SEVERE 级别的日志，如果是 CancellationException 或者包含 "Failed to download expansion"
                return !(record.getLevel() == Level.SEVERE && (isCanceledException || isFailedDownloadMessage));
            };
            papiLogger.setFilter(logFilter); // 设置自定义过滤器

            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.dispatchCommand(console, "papi ecloud download player"), 80L);
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.dispatchCommand(console, "papi ecloud download vault"), 90L);
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.dispatchCommand(console, "papi ecloud download Server"), 100L);
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.dispatchCommand(console, "papi ecloud download Statistic"), 110L);
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.dispatchCommand(console, "papi ecloud download Math"), 120L);

            // 在所有下载任务执行完成后，恢复原始过滤器并重载PAPI
            Bukkit.getScheduler().runTaskLater(this, () -> {
                papiLogger.setFilter(originalFilter); // 恢复原始过滤器
                Bukkit.dispatchCommand(console, "papi reload");
            }, 125L); // 确保在所有下载任务之后执行

        } else {
            console.sendMessage(colorize(PF + "&c挂钩 &7- &c未找到 &6PlaceholderAPI"));
        }
    }// 检查并加载 PlaceholderAPI
    private void setupTrChat() {
        boolean hasTrChat = Bukkit.getPluginManager().getPlugin("TrChat") != null;
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();// 获得控制台消息sender
        if (hasTrChat) {// 检查 TrChat
            console.sendMessage(colorize(PF + "&a挂钩 &7- &a已找到 &6TrChat"));
            getServer().getPluginManager().registerEvents(new DsTrChatMsg(this), this);// 注册事件监听器
        } else {
            console.sendMessage(colorize(PF + "&c挂钩 &7- &c未找到 &6TrChat"));
        }
    }// 检查并加载 TrChat
    // 金币相关
    private void vaultWithdraw(Player player, int tokens) {
        if (econ == null) return;
        double perTokenCost = config.getDouble("vaultPerToken");
        double perResponseCost = config.getDouble("vaultPerResponse");
        double allCost = (perTokenCost * tokens) + perResponseCost;// 公式: 总消耗 = token消耗 + 次数消耗
        if (perTokenCost > 0 || perResponseCost > 0) {
            double beforeWithdraw = econ.getBalance(player);
            EconomyResponse withdrawResponse = econ.withdrawPlayer(player, allCost);
            if (withdrawResponse.transactionSuccess()) {
                player.sendMessage(formatMessage("&e成功扣除: " + econ.format(allCost)));
            } else if(beforeWithdraw < allCost) {// 当玩家的金额不足以负担得起本次调用时，就扣掉全部金额
                econ.withdrawPlayer(player, econ.getBalance(player));
                player.sendMessage(formatMessage("&e你只剩下: " + econ.format(beforeWithdraw) +
                                                 " 了,本次调用应扣除: " + econ.format(allCost) +
                                                 " ,已扣除全部金额"));
            } else {
                player.sendMessage(formatMessage("&c扣款失败: " + withdrawResponse.errorMessage));
            }
        }
    }// 为对应的玩家扣款
    private boolean canAfford(Player player) {
        if (econ == null) return true;
        Double tokenCost = config.getDouble("vaultPerToken") * config.getInt("maxResponseTokens");
        Double countCost = config.getDouble("vaultPerResponse");
        return econ.has(player, tokenCost + countCost);
    }// 预测玩家当前金币是否足够调用(按照最高token数算)
    // 消息事件相关
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (Bukkit.getPluginManager().getPlugin("TrChat") != null) {return;}
        String message = event.getMessage();
        Player player = event.getPlayer();
        String CallMethod = config.getString("Call","@AI");
        // 在权限检查前先判断是否正在处理
        synchronized (processingLock) {
            if (isProcessing.get() && message.startsWith(CallMethod)) {
                player.sendMessage(formatMessage(
                        config.getString("isProcessingMsg", "&c当前响应正在请求中,请耐心等待!")));
                // 如果有处理中的请求，直接忽略所有新请求
                return;
            }
        }// 原子化保证每次只有一条消息被处理
        if (player.hasPermission("deepseek.use") && message.startsWith(CallMethod)) {
            DeepSeekCall(message,player);
        }
    }// 原版消息事件
    // 调用Web请求相关
    public void DeepSeekCall(String message, Player player) {
        String CallMethod = config.getString("Call","@AI");
        // 在权限检查前先判断是否正在处理
        synchronized (processingLock) {
            if (isProcessing.get() && message.startsWith(CallMethod)) {
                player.sendMessage(formatMessage(config.getString("isProcessingMsg")));
                // 如果有处理中的请求，直接忽略所有新请求
                return;
            }
        }// 原子化保证每次只有一条消息被处理
        if (!player.hasPermission("deepseek.use")){// 无使用权限
            player.sendMessage(formatMessage(config.getString("noPermission","&c你没有这么做的权限!")));
            return;
        }
        if (!canAfford(player) && !player.hasPermission("deepseek.bypass")) {// 判断玩家是否有足够余额或有绕过权限
            player.sendMessage(formatMessage("&c余额不足!"));
            return;
        }
        if (message.startsWith(CallMethod)) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                //Thread.currentThread().setName("DeepSeek-AsyncTask");// 请求调用的线程名称
                synchronized (processingLock) {
                    if (isProcessing.get()) {
                        return;
                    }
                    isProcessing.set(true);
                }
                Bukkit.broadcastMessage(formatMessage(config.getString("isRequestMsg")));
            }, 2L); // 2tick延迟
            String question = message.substring(Objects.requireNonNull(config.getString("Call")).length()).trim();// 排除前面的几个调用词
            // String history = conversationHistory.getOrDefault(player.getUniqueId(), "");
            String history = getAllHistory();// 获取历史记录
            // 构建完整提示，包含系统预设
            String systemPrompt = config.getString("systemPrompt",
                    "你是一个Minecraft游戏助手。回答要简短(最多200字符)，用中文回答。不要使用任何Markdown格式。");
            String prompt;
            String papiVariables = "\n\n可用PAPI变量表:\n" + DsPapiVariableList.PAPI_VARIABLE_LIST;
            if (config.getBoolean("PlayerNamePrompt")){
                prompt = systemPrompt + papiVariables + "\n\n对话历史:\n" + history + "\n玩家名:" + player.getName() + "问题:" + question;
            } else {
                prompt = systemPrompt + papiVariables + "\n\n对话历史:\n" + history + "\n玩家问题:" + question;
            }
            int maxTokens = config.getInt("tokens", 2000);
            if (prompt.length() > maxTokens) {
                prompt = prompt.substring(prompt.length() - maxTokens);
            }
            String finalPrompt = prompt;
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                //Thread.currentThread().setName("DeepSeek-AsyncTask");// 请求调用的线程名称
                try {
                    if (doRevoke) {
                        Bukkit.broadcastMessage(formatMessage(config.getString("revokeMsg","&e当前会话已被管理员撤回!")));
                        synchronized (processingLock) {
                            isProcessing.set(false);
                        }
                        return;
                    }
                    String response = sendRequestToDeepSeek(finalPrompt);
                    response = cleanResponse(response);
                    // 截断过长的回复
                    int maxResponseLength = config.getInt("maxResponseLength", 200);
                    if (response.length() > maxResponseLength) {
                        response = response.substring(0, maxResponseLength) + "...";
                    }
                    // 防止过长
                    String finalResponse = response;
                    Bukkit.getScheduler().runTask(this, () -> {
                        if (doRevoke) {
                            Bukkit.broadcastMessage(formatMessage(config.getString("revokeMsg","&c当前会话已被管理员撤回!")));
                        } else {
                            if (config.getBoolean("PlayerNamePrompt")){
                                addToHistory("玩家" + player.getName() + ": " + question + "\nAI: " + finalResponse);
                            } else {
                                addToHistory("玩家: " + question + "\nAI: " + finalResponse);
                            }// 向历史记录增加新的条目
                            String parsedResponse = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, finalResponse);
                            broadcastMultiline(parsedResponse);// 广播消息
                            if (!player.hasPermission("deepseek.bypass")) {// 若没有免费权限，则进行扣款操作
                                vaultWithdraw(player, tokens);// 根据tokens扣款
                            }
                        }
                        synchronized (processingLock) {
                            isProcessing.set(false);
                        }
                    });
                } catch (IOException e) {
                    getLogger().severe(e.getMessage());// 将错误内容输出到日志
                    Bukkit.getScheduler().runTask(this, () -> {
                        Bukkit.broadcastMessage(formatMessage(config.getString("isBusyMsg")));// 发生错误时给玩家的消息
                        synchronized (processingLock) {
                            isProcessing.set(false);
                        }
                    });
                }
            });
            doRevoke = false;
        }
    }// 调用请求
    private String sendRequestToDeepSeek(String prompt) throws IOException {
        String apiUrl = config.getString("URL", "https://api.deepseek.com/v1/chat/completions");
        String apiKey = config.getString("APIkey");
        URL url = URI.create(apiUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"}," +
                "{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":%f,\"max_tokens\":%d}",
                config.getString("Model","deepseek-chat"),
                escapeJson(config.getString("systemPrompt", "")),
                escapeJson(prompt),
                config.getDouble("Temperature",0.7),
                config.getInt("maxResponseTokens", 300));

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                int tokenIndex = response.indexOf("\"completion_tokens\":") + 20; // 20 是 "\"completion_tokens\":" 的长度
                int tokenEndIndex = response.indexOf(",", tokenIndex); // 可能是逗号或 } 结尾
                if (tokenEndIndex == -1) {
                    tokenEndIndex = response.indexOf("}", tokenIndex); // 检查是否在末尾
                }
                String tokenStr = response.substring(tokenIndex, tokenEndIndex).trim();
                tokens = Integer.parseInt(tokenStr);// 计算tokens
                int contentIndex = response.indexOf("\"content\":\"") + 11;
                int endIndex = response.indexOf("\"", contentIndex);
                return response.substring(contentIndex, endIndex).replace("\\n", "\n");
            }
        } else {
            throw new IOException("HTTP错误: " + responseCode);
        }
    }// 向DeepSeek服务器发出请求
    public void revoke() {
        doRevoke = true;
    }// 撤回当前回复
    // 配置文件相关
    public void loadConfig() {// 载入配置
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();// 获得控制台消息sender
        // 配置文件
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResource("config.yml")) {
                if (in == null) {
                    throw new IllegalArgumentException("无法找到内置配置文件");
                }
                Files.copy(in, configFile.toPath());
                console.sendMessage(colorize(PF + "&a已创建默认配置文件!"));
            } catch (Exception e) {
                console.sendMessage(colorize(PF + "&c无法创建配置文件!"));
                getLogger().severe(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        newPF = config.getString("Prefix", "&r[&9DeepSeek&r] ");
    }// 初始化配置文件
    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        newPF = config.getString("Prefix", "&r[&9DeepSeek&r] ");
    }// 重载插件
    // 历史记录相关
    public void resetConversationHistory() {
        // 清空历史记录
        conversationHistory.clear();
    }// 清空历史记录
    private void addToHistory(String newEntry) {
        int MAX_HISTORY_SIZE = 100;// 历史记录上限
        // 检查并移除最老的条目
        while (conversationHistory.size() >= MAX_HISTORY_SIZE) {
            conversationHistory.removeFirst(); // 移除最老的提问
        }
        // 添加新条目
        conversationHistory.addLast(newEntry);
    }// 添加消息到历史记录
    private String getAllHistory() {
        // 获取历史记录
        return String.join("\n", conversationHistory);
    } // 获取全部历史记录
}
