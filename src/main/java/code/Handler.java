package code;

import code.config.Config;
import code.config.MonitorConfigSettings;
import code.config.RequestProxyConfig;
import code.util.ExceptionUtil;
import code.util.GiteeUtil;
import code.util.GithubUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static code.Main.Bot;
import static code.Main.GlobalConfig;

@Slf4j
public class Handler {

    private static Map<String, List<String>> addMonitorMap = new HashMap<>();
    private static Map<String, List<String>> updateMonitorMap = new HashMap<>();

    public static void init() {
        new Thread(() -> {
            while (true) {
                try {
                    for (MonitorConfigSettings configSettings : Config.readMonitorConfigList()) {
                        releaseMessageHandle(RequestProxyConfig.create(), configSettings, false);
                    }

                    Thread.sleep(GlobalConfig.getIntervalMinute() * 60 * 1000);
                } catch (Exception e) {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                }
            }
        }).start();
    }

    public static void showMonitorListHandle(String chatId, Integer replyToMessageId) {
        List<MonitorConfigSettings> list = Config.readMonitorConfigList();
        if (list.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (MonitorConfigSettings settings : list) {
                builder.append(String.format("Name: %s, On: %s \n\n", settings.getFileBasename(), settings.getOn()));
            }
            sendMessageWithTryCatch(chatId, replyToMessageId, builder.toString());
        } else {
            sendMessageWithTryCatch(chatId, replyToMessageId, "Nothing here of monitor file, come to create it.");
        }
    }

    public static void showMonitorHandle(String chatId, Integer replyToMessageId, String text) {
        MonitorConfigSettings settings = Config.readMonitorConfig(text);
        if (null != settings) {
            sendMessageWithTryCatch(chatId, replyToMessageId, settings.toString());
        } else {
            sendMessageWithTryCatch(chatId, replyToMessageId, "Not found.");
        }
    }

    public static void onMonitorHandle(String chatId, Integer replyToMessageId, String text) {
        MonitorConfigSettings settings = Config.readMonitorConfig(text);
        if (null != settings) {
            settings.setOn(true);
            Config.saveMonitorConfig(settings);
            sendMessageWithTryCatch(chatId, replyToMessageId, "Saved success, Monitor changed online status.");
        } else {
            sendMessageWithTryCatch(chatId, replyToMessageId, "Not found.");
        }
    }
    public static void offMonitorHandle(String chatId, Integer replyToMessageId, String text) {
        MonitorConfigSettings settings = Config.readMonitorConfig(text);
        if (null != settings) {
            settings.setOn(false);
            Config.saveMonitorConfig(settings);
            sendMessageWithTryCatch(chatId, replyToMessageId, "Saved success, Monitor changed offline status.");
        } else {
            sendMessageWithTryCatch(chatId, replyToMessageId, "Not found.");
        }
    }

    public static void exitEditModeHandle(String chatId, Integer replyToMessageId) {
        addMonitorMap.remove(chatId);
        updateMonitorMap.remove(chatId);

        sendMessageWithTryCatch(chatId, replyToMessageId, "Exited success.");
    }

    public static void createMonitorHandle(boolean first, String chatId, Integer replyToMessageId, String text) {
        if (!first && !addMonitorMap.containsKey(chatId)) {
            return;
        }

        String key = "create" + chatId;
        synchronized (key.intern()) {
            if (first && !addMonitorMap.containsKey(chatId)) {
                addMonitorMap.put(chatId, new ArrayList<String>());

                sendMessageWithTryCatch(chatId, "Please send me the name of the monitor, and I will create it.");
                return;
            }
            if (addMonitorMap.containsKey(chatId)) {
                List<String> list = addMonitorMap.get(chatId);
                if (list.size() == 0) {
                    list.add(text);
                    sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Monitor named %s, created.", text));
                    sendMessageWithTryCatch(chatId, "Please continue to send me what you want set-up platform, for example, github or gitee");
                    return;
                }
                if (list.size() == 1) {
                    list.add(text);
                    sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Platform name: %s.", text));
                    sendMessageWithTryCatch(chatId, "Please continue to send me your git owner name");
                    return;
                }
                if (list.size() == 2) {
                    list.add(text);
                    sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Your git owner name: %s.", text));
                    sendMessageWithTryCatch(chatId, "Please continue to send me your git repo name");
                    return;
                }
                if (list.size() == 3) {
                    list.add(text);
                    sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Your git repo name: %s.", text));
                    sendMessageWithTryCatch(chatId, "Please continue to send me the repo's latest version");
                    return;
                }
                if (list.size() == 4) {
                    list.add(text);
                    sendMessageWithTryCatch(chatId, replyToMessageId, String.format("version: %s.", text));
                    sendMessageWithTryCatch(chatId, "Please continue to send me to template content");
                    return;
                }
                if (list.size() == 5) {
                    list.add(text);
                    sendMessageWithTryCatch(chatId, replyToMessageId, String.format("template content:\n %s.", text));
                    // save to file
                    MonitorConfigSettings settings = new MonitorConfigSettings();
                    settings.setFileBasename(list.get(0));
                    settings.setFilename(list.get(0) + ".json");
                    settings.setPlatform(list.get(1));
                    settings.setGitOwner(list.get(2));
                    settings.setGitRepo(list.get(3));
                    settings.setVersion(list.get(4));
                    settings.setTemplate(list.get(5));
                    settings.setOn(false);
                    settings.setSentRecordList(new ArrayList<>());
                    Config.saveMonitorConfig(settings);
                    addMonitorMap.remove(chatId);

                    showMonitorHandle(chatId, replyToMessageId, list.get(0));
                    sendMessageWithTryCatch(chatId, "Created finish! Requesting visit release API, please be patient.");
                    releaseMessageHandle(RequestProxyConfig.create(), settings, true);

                    return;
                }
            }

        }
    }

    public static void testMonitorHandle(String chatId, Integer replyToMessageId, String text) {
        MonitorConfigSettings settings = Config.readMonitorConfig(text);
        if (null == settings) {
            sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Monitor named %s, not found, please send me again.", text));
            return;
        }
        releaseMessageHandle(RequestProxyConfig.create(), settings, true);
    }

    public static void updateMonitorHandle(boolean first, String chatId, Integer replyToMessageId, String text) {
        if (!first && !updateMonitorMap.containsKey(chatId)) {
            return;
        }

        String key = "update" + chatId;
        synchronized (key.intern()) {
            if (first && !updateMonitorMap.containsKey(chatId)) {
                MonitorConfigSettings settings = Config.readMonitorConfig(text);
                if (null == settings) {
                    sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Monitor named %s, not found, please send me again.", text));
                    return;
                }

                updateMonitorMap.put(chatId, new ArrayList<String>());
                List<String> list = updateMonitorMap.get(chatId);
                list.add(text);

                sendMessageWithTryCatch(chatId, "Please continue to send me what you want set-up field name");
                return;
            }
            if (updateMonitorMap.containsKey(chatId)) {
                List<String> list = updateMonitorMap.get(chatId);
                if (list.size() == 1) {
                    try {
                        MonitorConfigSettings settings = Config.readMonitorConfig(list.get(0));
                        Field field = settings.getClass().getDeclaredField(text);
                        if (null == field) {
                            sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Field named %s, not found, please send me again.", text));
                            return;
                        }

                        list.add(text);
                        sendMessageWithTryCatch(chatId, replyToMessageId, String.format("Field name: %s", text));
                        sendMessageWithTryCatch(chatId, "Please continue to send me what you want set-up field value");
                    } catch (NoSuchFieldException e) {
                        log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                        sendMessageWithTryCatch(chatId, replyToMessageId, "System unknown error.");
                    }

                    return;
                }
                if (list.size() == 2) {
                    list.add(text);

                    try {
                        MonitorConfigSettings settings = Config.readMonitorConfig(list.get(0));
                        Field field = settings.getClass().getDeclaredField(list.get(1));
                        field.setAccessible(true);
                        if (text.equals("true") || text.equals("false")) {
                            field.set(settings, Boolean.valueOf(text));
                        } else {
                            field.set(settings, text);
                        }

                        Config.saveMonitorConfig(settings);
                        updateMonitorMap.remove(chatId);

                        showMonitorHandle(chatId, replyToMessageId, list.get(0));
                        sendMessageWithTryCatch(chatId, "Updated finish! ");
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                        sendMessageWithTryCatch(chatId, replyToMessageId, "System unknown error.");
                    }

                    return;
                }
            }

        }
    }

    public static void releaseMessageHandle(RequestProxyConfig proxyConfig, MonitorConfigSettings configSettings, boolean isTest) {
        try {
            Boolean on = configSettings.getOn();
            if ((null != on && on) || isTest) {
                String platform = configSettings.getPlatform().toLowerCase();
                String template = configSettings.getTemplate();
                String text = null;
                String tagName = null;
                if (platform.contains("github")) {
                    GithubUtil.LatestReleaseResponse response = GithubUtil.getLatestRelease(proxyConfig, configSettings.getGitOwner(), configSettings.getGitRepo());
                    if (null != response) {
                        text = replaceTemplate(template, response);
                        tagName = response.getTagName();
                    }
                } else if (platform.contains("gitee")) {
                    GiteeUtil.LatestReleaseResponse response = GiteeUtil.getLatestRelease(proxyConfig, configSettings.getGitOwner(), configSettings.getGitRepo());
                    if (null != response) {
                        text = replaceTemplate(template, response);
                        tagName = response.getTagName();
                    }
                }

                if (StringUtils.isNotBlank(text) && StringUtils.isNotBlank(tagName)) {
                    if (isTest) {
                        sendMessageWithTryCatch(GlobalConfig.getBotAdminId(), configSettings.toString());
                        sendMessageWithTryCatch(GlobalConfig.getBotAdminId(), text);
                    } else {
                        List<String> list = configSettings.getSentRecordList();
                        int tagNameInt = getVersionInt(tagName);
                        int versionInt = getVersionInt(configSettings.getVersion());
                        if (tagNameInt > versionInt && !compareVersion(tagNameInt, list)) {
                            for (String s : GlobalConfig.getChatIdArray()) {
                                if (StringUtils.isNotBlank(s)) {
                                    sendMessageWithTryCatch(s, text);
                                    Thread.sleep(500);
                                }
                            }

                            list.add(tagName);
                            Config.saveMonitorConfig(configSettings);
                        }
                    }
                } else {
                    if (isTest) {
                        sendMessageWithTryCatch(GlobalConfig.getBotAdminId(), "Not found up-to-date release");
                    }
                }
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
            if (isTest) {
                sendMessageWithTryCatch(GlobalConfig.getBotAdminId(), e.getMessage());
            }
        }
    }

    private static boolean compareVersion(int tagNameInt, List<String> list) {
        for (String s : list) {
            if (getVersionInt(s) >= tagNameInt) {
                return true;
            }
        }
        return false;
    }

    private static int getVersionInt(String version) {
        String v = version.replaceAll("\\.", "");
        v = v.replaceAll("v", "");
        v = v.replaceAll("version", "");
        v = v.replaceAll(" ", "");
        return Integer.valueOf(v).intValue();
    }

    private static String replaceTemplate(String template, Object jsonObj) {
        try {
            if (StringUtils.isBlank(template) || null == jsonObj) {
                return null;
            }
            String json = JSON.toJSONString(jsonObj);

            String s = template;
            if (template.contains("${htmlUrl}")) {
                s = StringUtils.replace(s, "${htmlUrl}", (String) JSONPath.eval(json, "$.htmlUrl"));
            }
            if (template.contains("${tagName}")) {
                s = StringUtils.replace(s, "${tagName}", (String) JSONPath.eval(json, "$.tagName"));
            }
            if (template.contains("${name}")) {
                s = StringUtils.replace(s, "${name}", (String) JSONPath.eval(json, "$.name"));
            }
            if (template.contains("${body}")) {
                s = StringUtils.replace(s, "${body}", (String) JSONPath.eval(json, "$.body"));
            }

            return s;
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return null;
    }

    public static Message sendMessageWithTryCatch(String chatId, String text) {
        return sendMessageWithTryCatch(chatId, null, text);
    }
    public static Message sendMessageWithTryCatch(String chatId, Integer replyToMessageId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(replyToMessageId);
        sendMessage.setText(text);
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.disableWebPagePreview();
        return sendMessageWithTryCatch(sendMessage);
    }

    public static Message sendMessageWithTryCatch(SendMessage sendMessage) {
        try {
            Message execute = Bot.execute(sendMessage);
            return execute;
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return null;
    }

    public static Boolean deleteMessageWithTryCatch(DeleteMessage deleteMessage) {
        try {
            Boolean execute = Bot.execute(deleteMessage);
            return execute;
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return null;
    }

}
