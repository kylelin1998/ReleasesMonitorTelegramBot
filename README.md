### 简体中文 | [English](./README_en.md)

![License](https://img.shields.io/badge/license-MIT-green)
[![release](https://img.shields.io/github/v/release/kylelin1998/ReleasesMonitorTelegramBot)](https://github.com/kylelin1998/ReleasesMonitorTelegramBot/releases/latest)

## 简介
Github, Gitee项目抓取release最新版

如果有监控到最新版本会通知到您设置好的Telegram群聊， 频道， 或者个人号上

支持自定义消息通知， 由你掌控内容

## 部署
Youtube：https://youtu.be/CiDxb1ESijQ

哔哩哔哩： https://www.bilibili.com/video/BV1Ts4y1S7bn/

机器人的部署步骤是基于 Docker 的，其机器人升级功能也基于 Docker，因此请使用 Docker 进行部署，以防出现错误

首先，在您的服务器上创建一个文件夹

然后，在其中创建名为 config 的另一个文件夹，config文件夹下必须包含名为 config.json 的JSON文件

接着，将 releases-monitor-for-telegram-universal.jar, run.sh 和 Dockerfile 传输到该文件夹中

### config.json
```json
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
bot admin主要作用是设置成只有你才能触发命令
* on_proxy -> 是否开启代理
* bot_admin_id -> Bot的管理者chat id
* bot_name -> Bot 用户名
* bot_token -> Bot token
* interval_minute -> 监控间隔(分钟)
* chatIdArray -> 需要发送的Chat Id列表
* permission_chat_id_array -> 你只能允许列表下的这些chat id使用机器人， 可以填写个人的，或者是群的chat id

### 第一步:
编译镜像
```
docker build -t rmb .
```

### 第二步:
运行容器镜像
```
docker run --name rmb -d -v $(pwd):/app --restart=always rmb
```

## 关于我
我的TG: https://t.me/KyleLin1998

我的TG频道: https://t.me/KyleLin1998Channel

我的邮箱: email@kylelin1998.com

## 使用说明
**机器人命令:**
```
create - 创建计划
list - 计划列表
exit - 退出
language - 切换语言
restart - 重启机器人
upgrade - 升级机器人
help - 帮助
```

template说明:
支持自定义发送通知消息文本
* ${htmlUrl} -> 项目release地址
* ${tagName} -> 项目版本
* ${name} -> 版本名称
* ${body} -> 版本说明

例子, 会自动替换对应内容:
```
🥳 ReleasesMonitorTelegramBot ${tagName}

${body}
```

![6a6ae12c04f26fab951ff9433e31f3b7647ba9c3.png](https://i.imgur.com/rhgNVb9.png)
![8d1990b74160c9bd45b20a77c68c179abdbfca8b.png](https://i.imgur.com/Fp4RDXu.png)