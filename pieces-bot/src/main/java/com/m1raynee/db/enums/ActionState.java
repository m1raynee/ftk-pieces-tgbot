package com.m1raynee.db.enums;

@lombok.Getter
@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum ActionState {
    STORED("S"),
    TAKEN("T"),
    LOST("L");

    private final String code;

    public static ActionState fromCode(String code) {
        for (ActionState state : ActionState.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown ActionState code: " + code);
    }

}
