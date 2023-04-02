package code.config;

import code.util.ExceptionUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
@Slf4j
public class Config {

    public final static String CurrentDir = System.getProperty("user.dir") + "/config";

    public final static String SettingsPath = CurrentDir + "/config.json";

    public final static String DBPath = CurrentDir + File.separator + "db.db";

    static {
        File file = new File(CurrentDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public final static class MetaData {
        public final static String CurrentVersion = "1.0.10";
        public final static String GitOwner = "kylelin1998";
        public final static String GitRepo = "ReleasesMonitorTelegramBot";
        public final static String ProcessName = "releases-monitor-for-telegram-universal.jar";
        public final static String JarName = "releases-monitor-for-telegram-universal.jar";
    }

    public static ConfigSettings readConfig() {
        try {
            File file = new File(SettingsPath);
            boolean exists = file.exists();
            if (exists) {
                String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                ConfigSettings configSettings = JSON.parseObject(text, ConfigSettings.class, JSONReader.Feature.SupportSmartMatch);
                return configSettings;
            }
        } catch (IOException e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return new ConfigSettings();
    }

}
