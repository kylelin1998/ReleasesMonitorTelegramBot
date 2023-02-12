### [简体中文](./README.md) | English

![License](https://img.shields.io/badge/license-MIT-green)
[![release](https://img.shields.io/github/v/release/kylelin1998/ReleasesMonitorTelegramBot)](https://github.com/kylelin1998/ReleasesMonitorTelegramBot/releases/latest)

## Introduction
GitHub and Gitee project monitor releases up-to-date.
Send messages of up-to-date releases to your set-up Telegram channel,  group, or personal if have up-to-date.

Support custom message content to your decision.

## Install & Deploy
Dockerfile and Jar file to save the same directory for building docker image.
```
docker build -t rmb .
```
You need to build logs, config directory on your personal server.

Then, Need to create a file named config.json in config directory.
```
docker run --name rmb -d -v /var/project/ReleasesMonitorBot/logs:/logs -v /var/project/ReleasesMonitorBot/releases-monitor-for-telegram-universal.jar:/app.jar -v /var/project/ReleasesMonitorBot/config:/config  --restart=always rmb
```
## About
My Telegram: <https://t.me/KyleLin1998>

My Telegram Channel(Software, if have a new version, will be in this channel to notify everyone. Welcome to subscribe to it.): <https://t.me/KyleLin1998Channel>

My email: email@kylelin1998.com

## Usage
config.json for example:
```json
{
  "on_proxy": false,
  "proxy_host": "127.0.0.1",
  "proxy_port": 7890,
  "bot_admin_username": "xxxx",
  "bot_admin_id": "xxxx",
  "bot_name": "xxx",
  "bot_token": "xxx",
  "interval_minute": 10,
  "chatIdArray": ["xxxxx"]
}
```
Bot Admin mainly means only you can trigger command to manage monitor plans
* bot_admin_username -> Bot admins username
* bot_admin_id -> Bot admins chat id
* bot_name -> Bot username
* bot_token -> Bot Token
* interval_minute -> Monitor interval(Minute)
* chatIdArray -> Send to chat id list

You need to send commands in the chat interface of the bot to manage monitor plans, command for example:
* /cmd create \<monitor name>
* /cmd list
* /cmd get \<monitor name>
* /cmd update \<monitor name>
* /cmd on \<monitor name>
* /cmd off \<monitor name>
* /cmd test \<monitor name>
* /cmd exit

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