# Server compatibility check (Minecraft 26.3)

Fluidloggable changes block states at startup and adds fluid data to chunk packets.
The client must establish server support before reading world data. Turning off
only the custom fluid-packet receiver cannot make an active client vanilla-compatible.

The configuration phase now exchanges `fluidloggable:compatibility` protocol 1.
The server waits for acknowledgement before world loading. The client cancels
configuration completion if its startup mode does not match the server's support.
State belongs to the configuration listener, so reconnecting and reconfiguration
do not reuse another connection's support result.

| Client startup mode | Server | Result |
| --- | --- | --- |
| Normal | Matching updated Fluidloggable | Join with fluidlogging |
| Normal | No compatibility announcement | Disconnect with compatibility-mode/restart instructions |
| Compatibility mode | No compatibility announcement | Join with gameplay mixins disabled |
| Compatibility mode | Updated Fluidloggable | Disconnect with instructions to disable compatibility mode and restart |
| Normal | Different compatibility protocol | Disconnect with version-mismatch instructions |

Dedicated servers require the updated client handshake. An older Fluidloggable
build does not implement this announcement; update both sides. The handshake
version describes the wire contract, not the displayed mod version, and must be
updated when incompatible changes are made to that contract. It does not compare
every block-mixin configuration or third-party mod between client and server.

Compatibility mode remains off by default. Its setting is captured during startup;
changing the menu toggle does not undo already-applied mixins. It still requires
a full restart and also disables the integrated server's fluidlogging features.
The connection-check mixin remains active in that mode.

## Validation

With Java 25, run `./gradlew build` and `./gradlew runClientGameTest`.
Unit tests cover the support/mode matrix, mismatched protocol values, and isolation
between connections. The existing client game test checks joining an integrated
server and synchronizing stored water and lava. The existing server game tests
cover fluid behavior.

Manual connection tests confirmed by the reporter:

- Server without Fluidloggable, normal mode: explanatory missing-support message.
- Same server, compatibility mode after restart: connection succeeds.
- Local dedicated Fabric 26.3 server with the matching updated build, normal mode:
  connection succeeds.
- Same dedicated server, compatibility mode after restart: explanatory message to
  disable compatibility mode.

The checks below also cover gameplay persistence and configurations beyond that
confirmed connection matrix:

1. In normal mode, join a server without Fluidloggable: expect the explanatory
   disconnect before chunk loading, without a buffer-underflow error.
2. Enable compatibility mode, restart, and join the same server: expect success.
3. With matching updated client/server builds and normal mode, join and verify
   water/lava synchronization. Reconnect and change dimensions.
4. Enable compatibility mode, restart, and join the updated modded server: expect
   the message to turn the mode off and restart.
5. Try a client without the mod against the updated server: expect the server's
   missing-support message.
6. Repeat singleplayer in both startup modes and switch between servers to check
   that support is not carried over from the previous connection.
