package szef.raf.santabackend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LetterType {
    ADULT("standard"),
    KID("child");

    private final String value;

    LetterType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LetterType fromString(String text) {
        if (text == null) {
            return ADULT;
        }
        String normalized = text.trim().toLowerCase();
        if ("kid".equals(normalized) || "child".equals(normalized)) {
            return KID;
        }
        return ADULT;
    }
}
