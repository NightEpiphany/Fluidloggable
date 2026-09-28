# Fluid synchronisation regression tests

Run with Java 21:

```text
./gradlew :neoforge:test
./gradlew :neoforge:build :fabric:build
```

The NeoForge tests load the actual mod and its mixins. A test-only source and
flowing fluid are registered during the normal registry event, after vanilla's
fluid-state table is populated. No test fluids are included in the release JAR.

The six tests cover:

- Mapping modded source/flowing/falling states while preserving vanilla IDs.
- Individual fluid-update packet round trips, including empty fluid.
- Grouped section updates using the same state IDs in both directions.
- Initial chunk-section synchronisation and its advertised byte size.
- Stable IDs when the table is rebuilt.
- Rejection of invalid incoming state IDs.

The individual-update test fails with the original packet implementation with
`Can't find id for 'fluidlogged:sync_test[...]'`, matching the reported failure
for Slice & Dice fertiliser.

## Manual modpack check

Use the patched NeoForge build on both client and server. Its network protocol
version is 2; the old build uses 1 and is intentionally rejected at connection.
Singleplayer already uses the same build for both sides.

In a test world with Slice & Dice:

1. Place stairs beside flowing liquid fertiliser, and into existing fertiliser.
2. Fill and empty eligible blocks with fertiliser; check that it remains
   fertiliser and keeps the expected source/flow state.
3. Leave and return to the chunk, then leave and rejoin the world. Check that
   fertiliser inside the blocks is still present and visible.
4. Repeat basic placement/removal with water and lava.

Disk storage still uses the existing per-chunk palette and FluidState codec;
this change affects network IDs, not the world-save format.

## Sable chunk construction (Minecraft 1.21.1)

Four additional parameterized cases cover both LevelChunkSection constructors.
Sable 2.0.5 uses the palette constructor in LevelPlot.newEmptyChunk. The tests
exercise that same vanilla constructor with the real Fluidloggable mixins;
they do not load Sable or Aeronautics themselves.

Coverage includes empty-section inspection and packet size, sending sections
between both constructor paths, clearing stale data on read, and adding,
synchronising, and removing vanilla and modded source/flowing/falling fluids.
Before the constructor fix, all four cases fail because the palette-created
section has a null fluid map; the empty palette-section size check reproduces
the reported getSerializedSize crash.

Manual check with Sable 2.0.5 / Aeronautics on NeoForge 1.21.1:

1. Assemble a simple dry structure with the physics assembler.
2. Disassemble and reassemble it, then leave and rejoin the world.
3. Repeat with fluidlogged blocks and check their fluid before and after
   assembly, disassembly, and reloading. Fluid transfer and Sable's own save
   path require in-game verification beyond this constructor regression.

This local fix includes the earlier modded-fluid ID fix. No further network
protocol or save-format changes are introduced by the constructor fix.
