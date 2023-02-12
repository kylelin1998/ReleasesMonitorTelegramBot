package code.config;

import lombok.Data;

import java.util.List;

@Data
public class MonitorConfigSettings {

    private Boolean on;

    private String filename;
    private String fileBasename;

    private String platform;

    private String gitOwner;
    private String gitRepo;

    private String template;

    private String version;

    private List<String> sentRecordList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("on: %s\n", on));
        builder.append(String.format("platform: %s\n", platform));
        builder.append(String.format("gitOwner: %s\n", gitOwner));
        builder.append(String.format("gitRepo: %s\n", gitRepo));
        builder.append(String.format("version: %s\n", version));
        builder.append(String.format("template: \n%s", template));
        return builder.toString();
    }

}
