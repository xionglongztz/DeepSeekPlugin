# DeepSeek Plugin for Minecraft Server
[EN](README.md) | [简中](docs/README_zh-CN.md) 
## Features
- DeepSeek API integration for in-game AI chat
- Fully customizable prompts and API parameters
- Configurable context memory system
- Vault economy support
- TrChat compatibility
- PlaceholderAPI support (In development)
- All settings configurable via YAML

## Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/deepseek reload` | Reload config | `deepseek.admin` |
| `/deepseek reset` | Clear history | `deepseek.admin` |
| `/deepseek help` | Show help | `deepseek.admin` |
| `/deepseek revoke` | Cancel request | `deepseek.admin` |

## Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `deepseek.use` | Access AI chat | OP |
| `deepseek.bypass` | Bypass costs | OP |
| `deepseek.admin` | All commands | OP |

## Configuration
Customize via `config.yml`:
- **API Settings**: Model, API key, temperature
- **Context**: Memory limits, response length
- **Messages**: All response texts
- **Economy**: Cost per call/token
- **Privacy**: Player ID visibility
- **Prompts**: Pre-built templates with formatting

---
