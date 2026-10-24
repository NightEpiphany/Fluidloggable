package com.moigferdsrte.fluidloggable.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerCompatibilityTest {
    @Test
    void unsupportedServerRequiresCompatibilityMode() {
        var connection = new ServerCompatibility();
        assertEquals(ServerCompatibility.Problem.MISSING_SUPPORT, connection.problem(false));
        assertNull(connection.problem(true));
    }

    @Test
    void supportedServerRequiresActiveMod() {
        var connection = new ServerCompatibility();
        connection.advertise(ServerCompatibility.PROTOCOL);
        assertNull(connection.problem(false));
        assertEquals(ServerCompatibility.Problem.COMPATIBILITY_MODE, connection.problem(true));
    }

    @Test
    void rejectsDifferentAndInvalidProtocols() {
        for (int protocol : new int[]{-1, 0, ServerCompatibility.PROTOCOL + 1, Integer.MAX_VALUE}) {
            var connection = new ServerCompatibility();
            connection.advertise(protocol);
            assertEquals(ServerCompatibility.Problem.PROTOCOL_MISMATCH, connection.problem(false));
        }
    }

    @Test
    void supportDoesNotLeakToNextConnectionOrReconfiguration() {
        var previous = new ServerCompatibility();
        previous.advertise(ServerCompatibility.PROTOCOL);
        assertNull(previous.problem(false));
        assertEquals(ServerCompatibility.Problem.MISSING_SUPPORT, new ServerCompatibility().problem(false));
    }
}
