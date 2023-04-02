package code.config;

import lombok.Getter;

@Getter
public enum I18nEnum {

    BotStartSucceed("bot_start_succeed"),
    HelpText("help_text"),

    InvalidCommand("invalid_command"),
    MonitorList("monitor_list"),
    NothingHere("nothing_here"),

    On("on"),
    Off("off"),
    Test("test"),
    Update("update"),
    NotFound("not_found"),
    UnknownError("unknown_error"),
    NothingAtAll("nothing_at_all"),
    CancelSucceeded("cancel_succeeded"),
    Confirm("confirm"),
    Cancel("cancel"),
    Delete("delete"),
    Finish("finish"),
    ExitSucceeded("exit_succeeded"),
    Getting("getting"),
    Downloading("downloading"),
    ForceRecord("force_record"),

    Github("github"),
    Gitee("gitee"),

    OnMonitor("on_monitor"),
    OffMonitor("off_monitor"),

    LanguageList("language_list"),
    ChangeLanguageFinish("change_language_finish"),

    MonitorExists("monitor_exists"),

    CreateNameTooLong("create_name_too_long"),
    CreateMonitor1("create_monitor_1"),
    CreateMonitor2("create_monitor_2"),
    CreateMonitor3("create_monitor_3"),
    CreateMonitor4("create_monitor_4"),
    CreateMonitor5("create_monitor_5"),
    CreateMonitor6("create_monitor_6"),
    CreateMonitor7("create_monitor_7"),
    CreateMonitor8("create_monitor_8"),
    CreateMonitor9("create_monitor_9"),
    CreateMonitor10("create_monitor_10"),
    CreateMonitorFinish("create_monitor_finish"),


    TestMonitor("test_monitor"),
    ForceRecordSucceeded("force_record_succeeded"),


    UpdateMonitor1("update_monitor_1"),
    UpdateMonitor2("update_monitor_2"),
    UpdateMonitor3("update_monitor_3"),
    UpdateMonitor4("update_monitor_4"),
    UpdateFieldError("update_field_error"),
    UpdateMonitorFinish("update_monitor_finish"),

    DeleteMonitorConfirm("delete_monitor_confirm"),
    DeleteMonitorFinish("delete_monitor_finish"),


    ConfigDisplayOn("config_display_on"),

    ConfigDisplayWebPagePreview("config_display_web_page_preview"),
    ConfigDisplayPlatform("config_display_platform"),
    ConfigDisplayGitOwner("config_display_git_owner"),
    ConfigDisplayGitRepo("config_display_git_repo"),
    ConfigDisplayVersion("config_display_version"),
    ConfigDisplayTemplate("config_display_template"),
    ConfigDisplayChatIdArray("config_display_chat_id_array"),
    ConfigDisplayMonitorName("config_display_monitor_name"),

    YouAreNotAnAdmin("you_are_not_an_admin"),
    AreYouSureToRestartRightNow("are_you_sure_to_restart_right_now"),
    Restarting("restarting"),
    GettingUpdateData("getting_update_data"),
    AreYouSureToUpgradeThisBotRightNow("are_you_sure_to_upgrade_this_bot_right_now"),
    TargetVersion("target_version"),
    CurrentVersion("current_version"),
    UpdateLogs("update_logs"),
    Updating("updating"),
    Downloaded("downloaded"),

    ;

    private String key;

    I18nEnum(String key) {
        this.key = key;
    }

}
