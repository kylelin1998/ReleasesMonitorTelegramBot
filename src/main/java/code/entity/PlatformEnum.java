package code.entity;

import lombok.Getter;

@Getter
public enum PlatformEnum {

    Github("github"),
    Gitee("gitee"),

    ;

    private String platform;

    PlatformEnum(String platfrom) {
        this.platform = platfrom;
    }

    public static PlatformEnum get(String platform) {
        for (PlatformEnum value : PlatformEnum.values()) {
            if (value.getPlatform().equals(platform)) {
                return value;
            }
        }
        return null;
    }

}
