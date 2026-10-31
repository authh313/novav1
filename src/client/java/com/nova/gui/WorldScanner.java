package com.nova.gui;

import com.nova.NovaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Scans already-loaded client chunks; it never asks the server to load additional chunks. */
public final class WorldScanner {
    private static final int CHUNK_RADIUS = 5;
    private static final int UPDATE_TICKS = 12;
    private static final int MAX_STORAGE_RESULTS = 12;
    private static final int MAX_AMETHYST_RESULTS = 8;
    private static int ticks;
    private static Object lastDimension;
    private static List<StorageHit> storage = List.of();
    private static List<AmethystHit> amethyst = List.of();

    private WorldScanner() {}

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            storage = List.of();
            amethyst = List.of();
            lastDimension = null;
            return;
        }
        Object dimension = mc.level.dimension();
        if (!Objects.equals(lastDimension, dimension)) {
            lastDimension = dimension;
            storage = List.of();
            amethyst = List.of();
            ticks = UPDATE_TICKS;
        }
        if (++ticks < UPDATE_TICKS) return;
        ticks = 0;

        boolean findStorage = moduleEnabled("Storage ESP");
        boolean findAmethyst = moduleEnabled("Amethyst Finder");
        if (!findStorage && !findAmethyst) {
            storage = List.of();
            amethyst = List.of();
            return;
        }

        int originX = mc.player.blockPosition().getX() >> 4;
        int originZ = mc.player.blockPosition().getZ() >> 4;
        double playerX = mc.player.getX();
        double playerY = mc.player.getY();
        double playerZ = mc.player.getZ();
        List<StorageHit> containers = new ArrayList<>();
        List<AmethystHit> geodeChunks = new ArrayList<>();

        for (int cx = originX - CHUNK_RADIUS; cx <= originX + CHUNK_RADIUS; cx++) {
            for (int cz = originZ - CHUNK_RADIUS; cz <= originZ + CHUNK_RADIUS; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                if (findStorage) {
                    for (BlockEntity entity : chunk.getBlockEntities().values()) {
                        BlockPos pos = entity.getBlockPos();
                        BlockState state = chunk.getBlockState(pos);
                        if (!isStorage(state)) continue;
                        double dx = pos.getX() + 0.5 - playerX;
                        double dy = pos.getY() + 0.5 - playerY;
                        double dz = pos.getZ() + 0.5 - playerZ;
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        containers.add(new StorageHit(storageName(state), pos.immutable(), distance));
                    }
                }

                if (findAmethyst && containsAmethyst(chunk)) {
                    double dx = (cx << 4) + 8.0 - playerX;
                    double dz = (cz << 4) + 8.0 - playerZ;
                    geodeChunks.add(new AmethystHit(cx, cz, Math.sqrt(dx * dx + dz * dz)));
                }
            }
        }

        containers.sort(Comparator.comparingDouble(StorageHit::distance));
        geodeChunks.sort(Comparator.comparingDouble(AmethystHit::distance));
        storage = List.copyOf(containers.subList(0, Math.min(MAX_STORAGE_RESULTS, containers.size())));
        amethyst = List.copyOf(geodeChunks.subList(0, Math.min(MAX_AMETHYST_RESULTS, geodeChunks.size())));
    }

    private static boolean moduleEnabled(String name) {
        return NovaClient.modules() != null && NovaClient.modules().all().stream()
                .anyMatch(module -> module.name.equals(name) && module.isEnabled());
    }

    private static boolean isStorage(BlockState state) {
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.ENDER_CHEST)
                || state.is(Blocks.BARREL) || state.getBlock() instanceof ShulkerBoxBlock;
    }

    private static String storageName(BlockState state) {
        if (state.is(Blocks.ENDER_CHEST)) return "Ender chest";
        if (state.is(Blocks.BARREL)) return "Barrel";
        if (state.getBlock() instanceof ShulkerBoxBlock) return "Shulker box";
        if (state.is(Blocks.TRAPPED_CHEST)) return "Trapped chest";
        return "Chest";
    }

    private static boolean containsAmethyst(LevelChunk chunk) {
        for (LevelChunkSection section : chunk.getSections()) {
            if (section != null && section.maybeHas(WorldScanner::isAmethyst)) return true;
        }
        return false;
    }

    private static boolean isAmethyst(BlockState state) {
        return state.is(Blocks.BUDDING_AMETHYST) || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.AMETHYST_CLUSTER) || state.is(Blocks.LARGE_AMETHYST_BUD)
                || state.is(Blocks.MEDIUM_AMETHYST_BUD) || state.is(Blocks.SMALL_AMETHYST_BUD);
    }

    public static List<StorageHit> storage() { return storage; }
    public static List<AmethystHit> amethyst() { return amethyst; }

    public record StorageHit(String kind, BlockPos pos, double distance) {}
    public record AmethystHit(int chunkX, int chunkZ, double distance) {}
}
