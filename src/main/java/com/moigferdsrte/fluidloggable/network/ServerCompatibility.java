package com.moigferdsrte.fluidloggable.network;

/** The support advertised by one configuration connection, never shared between servers. */
public final class ServerCompatibility {
    public static final int PROTOCOL = 1;
    private Integer serverProtocol;

    public void advertise(int protocol) {
        serverProtocol = protocol;
    }

    public Problem problem(boolean compatibilityMode) {
        if (serverProtocol == null) {
            return compatibilityMode ? null : Problem.MISSING_SUPPORT;
        }
        if (compatibilityMode) {
            return Problem.COMPATIBILITY_MODE;
        }
        return serverProtocol == PROTOCOL ? null : Problem.PROTOCOL_MISMATCH;
    }

    public enum Problem {
        MISSING_SUPPORT("fluidloggable.disconnect.missing_support"),
        COMPATIBILITY_MODE("fluidloggable.disconnect.compatibility_mode"),
        PROTOCOL_MISMATCH("fluidloggable.disconnect.protocol_mismatch");

        public final String translationKey;

        Problem(String translationKey) {
            this.translationKey = translationKey;
        }
    }
}
