### [简体中文](./README.md) | English

![License](https://img.shields.io/badge/license-MIT-green)
[![release](https://img.shields.io/github/v/release/kylelin1998/ReleasesMonitorTelegramBot)](https://github.com/kylelin1998/ReleasesMonitorTelegramBot/releases/latest)

## Introduction
Releases Monitor Telegram Bot is roll out. It is a new bot of my coding.

It supports Github and Gitee platforms to fetch the latest version.

You can add a Github project to the bot, and if the project has a new version, the bot will notify you.

## Deploy
The bot's deploy steps based on the Docker, its upgrade feature also based on the Docker, so please use the Docker to deploy it in case appear error.

### Prepare

To start, create a folder named whatever you prefer on your server.

Then create another folder named config and the config folder must contains a json file named config.json in, then transfer releases-monitor-for-telegram-universal.jar, run.sh and Dockerfile to the folder.

### config.json
```
{
  "debug": false,
  "on_proxy": false,
  "proxy_host": "127.0.0.1",
  "proxy_port": 7890,
  "bot_admin_id": "xxxx",
  "bot_name": "xxx",
  "bot_token": "xxx",
  "interval_minute": 10,
  "chatIdArray": [
    "xxxxx"
  ],
  "permission_chat_id_array": [
    "xxxx"
  ]
}
```
```
on proxy -> Whether to open proxy
bot admin id -> Bot's admin, the id is chat id of Telegram.
bot name, 和 bot token -> @BotFather has given bot name,  bot token
permission chat id array -> Allow using the bot.
```

### First step:
Build a docker image for use.
```
docker build -t rssb .
```

### Second step:
Run the docker image of just then build.
```
docker run --name rssb -d -v $(pwd):/app --restart=always rssb
```

## About
My telegram: https://t.me/KyleLin1998

My telegram channel: https://t.me/KyleLin1998Channel

My email: email@kylelin1998.com

## Usage
**Commands:**
```
create - Create plan
list - Plan list
exit - Exit
language - Change language
restart - Restart the bot
upgrade - Upgrade the bot
help - Help
```

template:
Support custom message content
* ${htmlUrl} -> Project release url
* ${tagName} -> Project version
* ${name} -> Project name
* ${body} -> Project description

For example, automatically replace the variable:
```
🥳 ReleasesMonitorTelegramBot ${tagName}

${body}
```

![6a6ae12c04f26fab951ff9433e31f3b7647ba9c3.png](https://i.imgur.com/rhgNVb9.png)
![8d1990b74160c9bd45b20a77c68c179abdbfca8b.png](https://i.imgur.com/Fp4RDXu.png)