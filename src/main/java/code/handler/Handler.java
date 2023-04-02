package code.handler;

import code.config.*;
import code.entity.MonitorSentRecordTableEntity;
import code.entity.MonitorTableEntity;
import code.entity.OnEnum;
import code.entity.PlatformEnum;
import code.handler.steps.StepResult;
import code.handler.steps.StepsBuilder;
import code.handler.steps.StepsChatSession;
import code.util.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static code.Main.*;

@Slf4j
public class Handler {

    private static boolean isAdmin(String fromId) {
        return GlobalConfig.getBotAdminId().equals(fromId);
    }

    public static void init() {
        new Thread(() -> {
            while (true) {
                try {
                    for (MonitorTableEntity entity : MonitorTableRepository.selectList()) {
                        releaseMessageHandle(null, entity, false);
                    }

                    TimeUnit.MINUTES.sleep(GlobalConfig.getIntervalMinute());
                } catch (Exception e) {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                }
            }
        }).start();

        // Create
        StepsBuilder
                .create()
                .bindCommand(Command.Create)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .init((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor1), false);

                    return StepResult.ok();
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    Integer count = MonitorTableRepository.selectCount(session.getFromId(), session.getText());
                    if (null == count) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                        return StepResult.end();
                    }
                    if (count > 0) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.MonitorExists), false);
                        return StepResult.reject();
                    }
                    if (session.getText().length() > 8) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateNameTooLong), false);
                        return StepResult.reject();
                    }

                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor2, session.getText()), false);

                    List<InlineKeyboardButton> buttons = new ArrayList<>();
                    InlineKeyboardButton button1 = new InlineKeyboardButton();
                    button1.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Github));
                    button1.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Create, PlatformEnum.Github.getPlatform()));

                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Gitee));
                    button2.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Create, PlatformEnum.Gitee.getPlatform()));

                    buttons.add(button1);
                    buttons.add(button2);
                    MessageHandle.sendInlineKeyboard(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor3), buttons);

                    context.put("name", session.getText());

                    return StepResult.ok();
                }, (StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String text = session.getText();
                    PlatformEnum platformEnum = PlatformEnum.get(text);
                    if (null == platformEnum) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor4), false);
                        return StepResult.reject();
                    }

                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor5, session.getText()), false);

                    context.put("platformEnum", platformEnum);

                    return StepResult.ok();
                }, (StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String text = session.getText();

                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor6, session.getText()), false);

                    context.put("owner", text);

                    return StepResult.ok();
                }, (StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String text = session.getText();

                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor7, session.getText()), false);

                    context.put("repo", text);

                    return StepResult.ok();
                }, (StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String text = session.getText();

                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor8, session.getText()), false);

                    context.put("version", text);

                    return StepResult.ok();
                }, (StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String name = (String) context.get("name");
                    PlatformEnum platformEnum = (PlatformEnum) context.get("platformEnum");
                    String owner = (String) context.get("owner");
                    String repo = (String) context.get("repo");
                    String version = (String) context.get("version");

                    MonitorTableEntity where = new MonitorTableEntity();
                    where.setChatId(session.getFromId());
                    MonitorTableEntity entity = new MonitorTableEntity();
                    String id = session.getFromId() + MonitorTableRepository.selectCount(where) + RandomUtils.nextInt(10000, 99999);
                    entity.setId(id);
                    entity.setChatId(session.getFromId());
                    entity.setCreateTime(System.currentTimeMillis());
                    entity.setMonitorName(name);
                    entity.setPlatform(platformEnum.getPlatform());
                    entity.setMonitorOn(OnEnum.Off.getType());
                    entity.setGitOwner(owner);
                    entity.setGitRepo(repo);
                    entity.setVersion(version);
                    entity.setTemplate(session.getText());
                    entity.setWebPagePreview(OnEnum.Off.getType());

                    boolean insert = MonitorTableRepository.save(entity);
                    if (!insert) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                    }

                    showMonitorHandle(session, id);
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitorFinish), false);
                    releaseMessageHandle(session, entity, true);

                    return StepResult.ok();
                })
                .build();

        // Update
        StepsBuilder
                .create()
                .bindCommand(Command.Update)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .init((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    MonitorTableEntity entity = MonitorTableRepository.selectOne(session.getText(), session.getFromId());
                    if (null == entity) {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.NotFound, session.getText()), false);
                        return StepResult.end();
                    }

                    List<InlineKeyboardButton> buttons = InlineKeyboardButtonBuilder
                            .create()
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayWebPagePreview), StepsCenter.buildCallbackData(false, session, Command.Update, "web_page_preview"))
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayMonitorName), StepsCenter.buildCallbackData(false, session, Command.Update, "name"))
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayPlatform), StepsCenter.buildCallbackData(false, session, Command.Update, "platform"))
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayGitOwner), StepsCenter.buildCallbackData(false, session, Command.Update, "owner"))
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayGitRepo), StepsCenter.buildCallbackData(false, session, Command.Update, "repo"))
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayVersion), StepsCenter.buildCallbackData(false, session, Command.Update, "version"))
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayChatIdArray), StepsCenter.buildCallbackData(false, session, Command.Update, "chat_id_array"))
                            .add(I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayTemplate), StepsCenter.buildCallbackData(false, session, Command.Update, "template"))
                            .build();
                    Message keyboard = MessageHandle.sendInlineKeyboard(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UpdateMonitor1), buttons);

                    context.put("id", entity.getId());
                    context.put("message", keyboard);

                    return StepResult.ok();
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String text = session.getText();
                    if (text.equals("name")) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor1), false);
                    }
                    else if (text.equals("web_page_preview")) {
                        List<InlineKeyboardButton> buttons = InlineKeyboardButtonBuilder
                                .create()
                                .add(I18nHandle.getText(session.getFromId(), I18nEnum.On), StepsCenter.buildCallbackData(false, session, Command.Update, "" + OnEnum.On.getType()))
                                .add(I18nHandle.getText(session.getFromId(), I18nEnum.Off), StepsCenter.buildCallbackData(false, session, Command.Update, "" + OnEnum.Off.getType()))
                                .build();
                        MessageHandle.sendInlineKeyboard(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor10), buttons);
                    }
                    else if (text.equals("platform")) {
                        List<InlineKeyboardButton> buttons = InlineKeyboardButtonBuilder
                                .create()
                                .add(I18nHandle.getText(session.getFromId(), I18nEnum.Github), StepsCenter.buildCallbackData(false, session, Command.Update, PlatformEnum.Github.getPlatform()))
                                .add(I18nHandle.getText(session.getFromId(), I18nEnum.Gitee), StepsCenter.buildCallbackData(false, session, Command.Update, PlatformEnum.Gitee.getPlatform()))
                                .build();
                        MessageHandle.sendInlineKeyboard(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor3), buttons);
                    }
                    else if (text.equals("owner")) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor5), false);
                    }
                    else if (text.equals("repo")) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor6), false);
                    }
                    else if (text.equals("version")) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor7), false);
                    }
                    else if (text.equals("template")) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor8), false);
                    }
                    else if (text.equals("chat_id_array")) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor9), false);
                    }
                    else {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UpdateMonitor2), false);
                        return StepResult.reject();
                    }

                    MessageHandle.deleteMessage((Message) context.get("message"));
                    context.put("field", text);

                    return StepResult.ok();
                }, (StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String id = (String) context.get("id");
                    String field = (String) context.get("field");

                    MonitorTableEntity entity = new MonitorTableEntity();
                    if (field.equals("name")) {
                        Integer count = MonitorTableRepository.selectCount(session.getFromId(), session.getText());
                        if (null == count) {
                            MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                            return StepResult.end();
                        }
                        if (count > 0) {
                            MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.MonitorExists), false);
                            return StepResult.reject();
                        }
                        if (session.getText().length() > 8) {
                            MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateNameTooLong), false);
                            return StepResult.reject();
                        }
                        entity.setMonitorName(session.getText());
                    }
                    else if (field.equals("web_page_preview")) {
                        String text = session.getText();
                        OnEnum onEnum = OnEnum.get(Integer.parseInt(text));
                        if (null == onEnum) {
                            MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UpdateFieldError), false);
                            return StepResult.reject();
                        }
                        entity.setWebPagePreview(onEnum.getType());
                    }
                    else if (field.equals("platform")) {
                        String text = session.getText();
                        PlatformEnum platformEnum = PlatformEnum.get(text);
                        if (null == platformEnum) {
                            MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CreateMonitor4), false);
                            return StepResult.reject();
                        }
                        entity.setPlatform(text);
                    }
                    else if (field.equals("owner")) {
                        entity.setGitOwner(session.getText());
                    }
                    else if (field.equals("repo")) {
                        entity.setGitRepo(session.getText());
                    }
                    else if (field.equals("version")) {
                        entity.setVersion(session.getText());
                    }
                    else if (field.equals("template")) {
                        entity.setTemplate(session.getText());
                    }
                    else if (field.equals("chat_id_array")) {
                        String[] s = StringUtils.split(session.getText(), " ");
                        if (s.length == 0) {
                            MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UpdateFieldError), false);
                            return StepResult.reject();
                        }
                        entity.setChatIdArrayJson(JSON.toJSONString(s));
                    }

                    MonitorTableEntity where = new MonitorTableEntity();
                    where.setId(id);
                    MonitorTableRepository.update(entity, where);

                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UpdateMonitorFinish), false);
                    showMonitorHandle(session, id);

                    return StepResult.end();
                })
                .build();

        // Delete
        StepsBuilder
                .create()
                .bindCommand(Command.Delete)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .init((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Confirm));
                    inlineKeyboardButton.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Delete, "confirm"));

                    InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
                    inlineKeyboardButton2.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Cancel));
                    inlineKeyboardButton2.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Delete, "cancel"));

                    MessageHandle.sendInlineKeyboard(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.DeleteMonitorConfirm), inlineKeyboardButton, inlineKeyboardButton2);

                    return StepResult.ok();
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    if ("confirm".equals(session.getText())) {
                        MonitorTableEntity monitorTableEntity = MonitorTableRepository.selectOne(list.get(0), session.getFromId());
                        if (null != monitorTableEntity) {
                            MonitorTableEntity delete = new MonitorTableEntity();
                            delete.setId(monitorTableEntity.getId());
                            MonitorSentRecordTableEntity sentRecordTableEntity = new MonitorSentRecordTableEntity();
                            sentRecordTableEntity.setMonitorId(monitorTableEntity.getId());
                            MonitorSentRecordTableRepository.delete(sentRecordTableEntity);
                            MonitorTableRepository.delete(delete);
                        }
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.DeleteMonitorFinish), false);

                        showMonitorListHandle(session);
                    } else {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.CancelSucceeded), false);
                    }

                    return StepResult.end();
                })
                .build();

        // Exit
        StepsBuilder
                .create()
                .bindCommand(Command.Exit)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    StepsCenter.exit(session);

                    MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.ExitSucceeded), false);
                    return StepResult.end();
                })
                .build();

        // List
        StepsBuilder
                .create()
                .bindCommand(Command.List)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {

                    showMonitorListHandle(session);

                    return StepResult.end();
                })
                .build();

        // Get
        StepsBuilder
                .create()
                .bindCommand(Command.Get)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {

                    showMonitorHandle(session, session.getText());

                    return StepResult.end();
                })
                .build();

        // On
        StepsBuilder
                .create()
                .bindCommand(Command.On)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {

                    MonitorTableEntity settings = MonitorTableRepository.selectOne(session.getText(), session.getFromId());
                    if (null != settings) {
                        settings.setMonitorOn(OnEnum.On.getType());
                        MonitorTableRepository.update(settings);
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.OnMonitor), false);
                    } else {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.NotFound), false);
                    }

                    return StepResult.end();
                })
                .build();

        // Off
        StepsBuilder
                .create()
                .bindCommand(Command.Off)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {

                    MonitorTableEntity settings = MonitorTableRepository.selectOne(session.getText(), session.getFromId());
                    if (null != settings) {
                        settings.setMonitorOn(OnEnum.Off.getType());
                        MonitorTableRepository.update(settings);
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.OffMonitor), false);
                    } else {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.NotFound), false);
                    }

                    return StepResult.end();
                })
                .build();

        // Test
        StepsBuilder
                .create()
                .bindCommand(Command.Test)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {

                    MonitorTableEntity settings = MonitorTableRepository.selectOne(session.getText(), session.getFromId());
                    if (null == settings) {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.TestMonitor, session.getText()), false);
                        return StepResult.end();
                    }
                    releaseMessageHandle(session, settings, true);

                    return StepResult.end();
                })
                .build();

        // Language
        StepsBuilder
                .create()
                .bindCommand(Command.Language)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .init((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    ArrayList<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
                    for (I18nLocaleEnum value : I18nLocaleEnum.values()) {
                        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                        inlineKeyboardButton.setText(value.getDisplayText());
                        inlineKeyboardButton.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Language, value.getAlias()));

                        inlineKeyboardButtons.add(inlineKeyboardButton);
                    }

                    MessageHandle.sendInlineKeyboard(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.LanguageList), inlineKeyboardButtons);

                    return StepResult.ok();
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    I18nLocaleEnum alias = I18nLocaleEnum.getI18nLocaleEnumByAlias(session.getText());

                    I18nHandle.save(session.getFromId(), alias);

                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.ChangeLanguageFinish), false);

                    return StepResult.end();
                })
                .build();

        // Restart
        StepsBuilder
                .create()
                .bindCommand(Command.Restart)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                })
                .init((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    if (!isAdmin(session.getFromId())) {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.YouAreNotAnAdmin), false);
                        return StepResult.end();
                    }

                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Confirm));
                    inlineKeyboardButton.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Restart, "true"));

                    InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
                    inlineKeyboardButton2.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Cancel));
                    inlineKeyboardButton2.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Restart, "false"));

                    MessageHandle.sendInlineKeyboard(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.AreYouSureToRestartRightNow), inlineKeyboardButton, inlineKeyboardButton2);

                    return StepResult.ok();
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    Boolean of = Boolean.valueOf(session.getText());
                    if (of) {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.Restarting), false);
                        ProgramUtil.restart(Config.MetaData.ProcessName);
                    } else {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.CancelSucceeded), false);
                    }
                    return StepResult.end();
                })
                .build();

        // Upgrade
        StepsBuilder
                .create()
                .bindCommand(Command.Upgrade)
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession stepsChatSession) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    MessageHandle.sendMessage(stepsChatSession.getChatId(), I18nHandle.getText(stepsChatSession.getFromId(), I18nEnum.UnknownError), false);
                })
                .init((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    if (!isAdmin(session.getFromId())) {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.YouAreNotAnAdmin), false);
                        return StepResult.end();
                    }

                    Message message = MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.GettingUpdateData), false);
                    GithubUtil.LatestReleaseResponse release = GithubUtil.getLatestRelease(RequestProxyConfig.create(), Config.MetaData.GitOwner, Config.MetaData.GitRepo);
                    if (release.isOk()) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(I18nHandle.getText(session.getFromId(), I18nEnum.AreYouSureToUpgradeThisBotRightNow));
                        builder.append("\n");
                        builder.append(I18nHandle.getText(session.getFromId(), I18nEnum.TargetVersion) + ": ");
                        builder.append(release.getTagName());
                        builder.append("\n");
                        builder.append(I18nHandle.getText(session.getFromId(), I18nEnum.CurrentVersion) + ": ");
                        builder.append(Config.MetaData.CurrentVersion);
                        builder.append("\n");
                        builder.append(I18nHandle.getText(session.getFromId(), I18nEnum.UpdateLogs) + ": ");
                        builder.append("\n");
                        builder.append(release.getBody());

                        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                        inlineKeyboardButton.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Confirm));
                        inlineKeyboardButton.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Upgrade, "true"));

                        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
                        inlineKeyboardButton2.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Cancel));
                        inlineKeyboardButton2.setCallbackData(StepsCenter.buildCallbackData(false, session, Command.Upgrade, "false"));

                        MessageHandle.sendInlineKeyboard(session.getChatId(), builder.toString(), inlineKeyboardButton, inlineKeyboardButton2);

                        String url = "";
                        for (GithubUtil.LatestReleaseAsset asset : release.getAssets()) {
                            if (Config.MetaData.JarName.equals(asset.getName())) {
                                url = asset.getBrowserDownloadUrl();
                                break;
                            }
                        }

                        context.put("url", url);

                        return StepResult.ok();
                    } else {
                        MessageHandle.editMessage(message, I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError));
                        return StepResult.end();
                    }
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    Boolean of = Boolean.valueOf(session.getText());
                    if (of) {
                        Message message = MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.Updating), false);
                        String url = (String) context.get("url");

                        AtomicInteger count = new AtomicInteger();
                        String temp = System.getProperty("user.dir") + "/temp.jar";
                        log.info("temp: " + temp);
                        boolean b = DownloadUtil.download(
                                RequestProxyConfig.create(),
                                url,
                                temp,
                                (String var1, String var2, Long var3, Long var4) -> {
                                    if ((var4 - var3) > 0) {
                                        count.incrementAndGet();
                                        if (count.get() == 100) {
                                            MessageHandle.editMessage(message, I18nHandle.getText(session.getFromId(), I18nEnum.Downloaded, BytesUtil.toDisplayStr(var3), BytesUtil.toDisplayStr(var4)));
                                            count.set(0);
                                        }
                                    }
                                }
                        );

                        if (b) {
                            System.exit(1);
                        } else {
                            MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.UnknownError), false);
                        }

                    } else {
                        MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.CancelSucceeded), false);
                    }
                    return StepResult.end();
                })
                .build();

    }

    private static void showMonitorHandle(StepsChatSession session, String id) {
        MonitorTableEntity entity = MonitorTableRepository.selectOne(id, session.getFromId());
        if (null != entity) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(I18nHandle.getText(session.getFromId(), I18nEnum.On));
            inlineKeyboardButton.setCallbackData(StepsCenter.buildCallbackData(true, session, Command.On, id));

            InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
            inlineKeyboardButton2.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Off));
            inlineKeyboardButton2.setCallbackData(StepsCenter.buildCallbackData(true, session, Command.Off, id));

            InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
            inlineKeyboardButton3.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Test));
            inlineKeyboardButton3.setCallbackData(StepsCenter.buildCallbackData(true, session, Command.Test, id));

            InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
            inlineKeyboardButton4.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Update));
            inlineKeyboardButton4.setCallbackData(StepsCenter.buildCallbackData(true, session, Command.Update, id));

            InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();
            inlineKeyboardButton5.setText(I18nHandle.getText(session.getFromId(), I18nEnum.Delete));
            inlineKeyboardButton5.setCallbackData(StepsCenter.buildCallbackData(true, session, Command.Delete, id));

            MessageHandle.sendInlineKeyboard(session.getChatId(), getMonitorData(session, entity), inlineKeyboardButton, inlineKeyboardButton2, inlineKeyboardButton3, inlineKeyboardButton4, inlineKeyboardButton5);
        } else {
            MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.NotFound), false);
        }
    }

    private static void showMonitorListHandle(StepsChatSession session) {
        List<MonitorTableEntity> monitorTableEntityList = MonitorTableRepository.selectListByChatId(session.getFromId());
        if (monitorTableEntityList.size() > 0) {
            StringBuilder builder = new StringBuilder();
            ArrayList<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();

            for (MonitorTableEntity entity : monitorTableEntityList) {
                builder.append(I18nHandle.getText(session.getFromId(), I18nEnum.MonitorList, entity.getMonitorName(), OnEnum.get(entity.getMonitorOn())));
                builder.append("\n\n");

                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(entity.getMonitorName());
                button.setCallbackData(StepsCenter.buildCallbackData(true, session, Command.Get, entity.getId()));
                inlineKeyboardButtons.add(button);
            }

            MessageHandle.sendInlineKeyboard(session.getChatId(), builder.toString(), inlineKeyboardButtons);
        } else {
            MessageHandle.sendMessage(session.getChatId(), session.getReplyToMessageId(), I18nHandle.getText(session.getFromId(), I18nEnum.NothingHere), false);
        }
    }

    private static void releaseMessageHandle(StepsChatSession session, MonitorTableEntity monitorTableEntity, boolean isTest) {
        try {
            boolean on = monitorTableEntity.getMonitorOn().equals(OnEnum.On.getType());
            if (on || isTest) {
                PlatformEnum platformEnum = PlatformEnum.get(monitorTableEntity.getPlatform());
                String template = monitorTableEntity.getTemplate();
                String text = null;
                String tagName = null;
                switch (platformEnum) {
                    case Github:
                        GithubUtil.LatestReleaseResponse github = GithubUtil.getLatestRelease(RequestProxyConfig.create(), monitorTableEntity.getGitOwner(), monitorTableEntity.getGitRepo());
                        if (null != github) {
                            text = replaceTemplate(template, github);
                            tagName = github.getTagName();
                        }
                        break;
                    case Gitee:
                        GiteeUtil.LatestReleaseResponse gitee = GiteeUtil.getLatestRelease(RequestProxyConfig.create(), monitorTableEntity.getGitOwner(), monitorTableEntity.getGitRepo());
                        if (null != gitee) {
                            text = replaceTemplate(template, gitee);
                            tagName = gitee.getTagName();
                        }
                        break;
                }

                if (StringUtils.isNotBlank(text) && StringUtils.isNotBlank(tagName)) {
                    if (isTest) {
                        MessageHandle.sendMessage(session.getChatId(), text, monitorTableEntity.getWebPagePreview() == OnEnum.On.getType());
                    } else {
                        List<MonitorSentRecordTableEntity> sentRecordTableEntityList = MonitorSentRecordTableRepository.selectListByMonitorId(monitorTableEntity.getId());
                        List<String> list = sentRecordTableEntityList.stream().map(MonitorSentRecordTableEntity::getVersion).collect(Collectors.toList());
                        for (String s : list) {
                            if (s.equals(tagName)) {
                                return;
                            }
                        }
                        VersionUtil.VersionCompareResult result = VersionUtil.compare(tagName, monitorTableEntity.getVersion());
                        switch (result) {
                            case GT:
                                String[] chatIdArray = null;
                                if (StringUtils.isNotBlank(monitorTableEntity.getChatIdArrayJson())) {
                                    chatIdArray = JSON.parseArray(monitorTableEntity.getChatIdArrayJson(), String.class).toArray(new String[] {});
                                }
                                if (null == chatIdArray || chatIdArray.length == 0) {
                                    chatIdArray = GlobalConfig.getChatIdArray();
                                }
                                for (String s : chatIdArray) {
                                    if (StringUtils.isNotBlank(s)) {
                                        MessageHandle.sendMessage(s, text, monitorTableEntity.getWebPagePreview() == OnEnum.On.getType());
                                    }
                                }

                                break;
                        }
                        MonitorSentRecordTableEntity monitorSentRecordTableEntity = new MonitorSentRecordTableEntity();
                        monitorSentRecordTableEntity.setMonitorId(monitorTableEntity.getId());
                        monitorSentRecordTableEntity.setVersion(tagName);
                        MonitorSentRecordTableRepository.save(monitorSentRecordTableEntity);
                    }
                } else {
                    if (isTest) {
                        MessageHandle.sendMessage(session.getChatId(), I18nHandle.getText(session.getFromId(), I18nEnum.NothingAtAll), monitorTableEntity.getWebPagePreview() == OnEnum.On.getType());
                    }
                }

            }
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
            if (isTest) {
                MessageHandle.sendMessage(GlobalConfig.getBotAdminId(), e.getMessage(), false);
            }
        }
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

    private static String getOnDisplayI18nText(String fromId, OnEnum onEnum) {
        switch (onEnum) {
            case On:
                return I18nHandle.getText(fromId, I18nEnum.On);
            case Off:
                return I18nHandle.getText(fromId, I18nEnum.Off);
            default:
                return "";
        }
    }

    private static String getMonitorData(StepsChatSession session, MonitorTableEntity entity) {
        String chatIdArrayStr = "";
        String chatIdArrayJson = entity.getChatIdArrayJson();
        String[] chatIdArray = null;
        if (StringUtils.isNotBlank(chatIdArrayJson)) {
            chatIdArray = JSON.parseArray(chatIdArrayJson, String.class).toArray(new String[]{});
        }

        if (ArrayUtils.isEmpty(chatIdArray)) {
            chatIdArrayStr = StringUtils.join(GlobalConfig.getChatIdArray(), " ");
        } else {
            chatIdArrayStr = StringUtils.join(chatIdArray, " ");
        }

        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayMonitorName), entity.getMonitorName()));
        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayOn), getOnDisplayI18nText(session.getFromId(), OnEnum.get(entity.getMonitorOn()))));
        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayWebPagePreview), getOnDisplayI18nText(session.getFromId(), OnEnum.get(entity.getWebPagePreview()))));
        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayPlatform), PlatformEnum.get(entity.getPlatform())));
        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayGitOwner), entity.getGitOwner()));
        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayGitRepo), entity.getGitRepo()));
        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayVersion), entity.getVersion()));
        builder.append(String.format("%s: %s\n", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayChatIdArray), chatIdArrayStr));
        builder.append(String.format("%s: \n%s", I18nHandle.getText(session.getFromId(), I18nEnum.ConfigDisplayTemplate), entity.getTemplate()));

        return builder.toString();
    }

}
