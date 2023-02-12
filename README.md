### 简体中文 | [English](./README_en.md)

![License](https://img.shields.io/badge/license-MIT-green)
[![release](https://img.shields.io/github/v/release/kylelin1998/ReleasesMonitorTelegramBot)](https://github.com/kylelin1998/ReleasesMonitorTelegramBot/releases/latest)

## 简介
Github, Gitee项目监控release最新版， 
如果有监控到最新版本会通知到您设置好的Telegram群聊， 频道， 或者个人号上

## 安装 & 部署
Dockerfile 与 jar文件放置到同一目录中进行打包镜像
```
docker build -t rmb .
```
需要在服务器上建立好对应目录， logs, config

需要先在config目录下创建一个config.json文件
```
docker run --name rmb -d -v /var/project/ReleasesMonitorBot/logs:/logs -v /var/project/ReleasesMonitorBot/releases-monitor-for-telegram-universal.jar:/app.jar -v /var/project/ReleasesMonitorBot/config:/config  --restart=always rmb
```

## 关于
我的Telegram: <https://t.me/KyleLin1998>

我的Telegram频道(软件最新通知会在此频道通知， 欢迎关注): <https://t.me/KyleLin1998Channel>

我的邮箱: email@kylelin1998.com

## 使用说明
config.json示例:
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
bot admin主要作用是设置成只有你才能触发命令
* on_proxy -> 是否开启代理 
* bot_admin_username -> Bot的管理者用户名
* bot_admin_id -> Bot的管理者Chat Id
* bot_name -> Bot用户名
* bot_token -> Bot Token
* interval_minute -> 监控间隔(分钟)
* chatIdArray -> 需要发送的Chat Id列表

你需要在机器人聊天界面发送对应命令去执行管理监控计划， 命令如下：
* /cmd create \<monitor name>
* /cmd list
* /cmd get \<monitor name>
* /cmd update \<monitor name>
* /cmd on \<monitor name>
* /cmd off \<monitor name>
* /cmd test \<monitor name>
* /cmd exit

![8d1990b74160c9bd45b20a77c68c179abdbfca8b.png](https://i.imgur.com/Fp4RDXu.png)