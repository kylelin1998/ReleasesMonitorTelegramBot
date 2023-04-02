package code.entity;

import lombok.Getter;

@Getter
public enum OnEnum {

    On(1),
    Off(2),

    ;

    private int type;

    OnEnum(int type) {
        this.type = type;
    }

    public static OnEnum get(int type) {
        for (OnEnum value : OnEnum.values()) {
            if (value.getType() == type) {
                return value;
            }
        }
        return null;
    }

}
