# Minecraft DeepSeek AI聊天插件

将 DeepSeek API 接入 Minecraft 服务器的 Spigot 插件  
支持核心: Spigot/Paper/Leaves 等 | 版本: 1.21+ | 需要 Java 21 | 基于 Java + Gradle 构建

[![在 MineBBS 下载](https://img.shields.io/badge/下载-MineBBS-blue)](https://www.minebbs.com/resources/deepseek-deepseek.11808/)  

## 功能特色
- 深度求索 AI 聊天集成
- 完全可定制的提示词和 API 参数
- 可配置的上下文记忆系统
- Vault 经济系统集成
- TrChat 兼容支持
- PlaceholderAPI 支持（开发中）
- 所有设置均可通过配置文件调整

## 命令列表
| 命令 | 描述 | 权限节点 |
|------|------|----------|
| `/deepseek reload` 或 `/ds reload` | 重载插件配置 | `deepseek.admin` |
| `/deepseek reset` 或 `/ds reset` | 清空对话历史 | `deepseek.admin` |
| `/deepseek help` 或 `/ds help` | 显示帮助信息 | `deepseek.admin` |
| `/deepseek revoke` 或 `/ds r` | 撤回请求并阻止记录 | `deepseek.admin` |

## 权限说明
| 权限节点 | 描述 | 默认 |
|----------|------|------|
| `deepseek.use` | 使用AI聊天（需以`@AI`开头） | 仅OP |
| `deepseek.bypass` | 绕过调用次数/token计费 | 仅OP |
| `deepseek.admin` | 使用全部管理命令 | 仅OP |

## 配置选项
通过 `config.yml` 可自定义：
- **API设置**：模型名称、API密钥、温度参数等
- **上下文设置**：最大记忆长度、单次响应限制等
- **消息格式**：所有提示文本均可修改
- **经济系统**：按次收费或按token计费
- **隐私设置**：是否向AI显示玩家ID
- **系统提示词**：内置多套带颜色代码的模板

