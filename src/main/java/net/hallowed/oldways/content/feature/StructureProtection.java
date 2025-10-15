package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class StructureProtection {
    private StructureProtection() {}

    public static void register() {
        UseBlockCallback.EVENT.register(StructureProtection::onUseBlock);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(StructureProtection::onKillUnlock);
        ServerTickEvents.END_SERVER_TICK.register(StructureProtection::tick);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUuid();
            PER_PLAYER_STATE.remove(id);
            FORCED_BY_INSIDE.remove(id);
        });
    }

    private static final int BLAZES_TO_UNLOCK_FORTRESS = 10;

    private record StructureKeyInst(RegistryKey<Structure> key, String dimensionId, long startChunkX, long startChunkZ) {}

    private static final class Lock {
        boolean unlocked = false;
        int blazeKills = 0;
    }

    private static final class GlobalLock {
        volatile boolean unlocked = false;
        int mansionRequired = -1;
        int mansionKills = 0;
    }

    private static final Map<UUID, Map<StructureKeyInst, Lock>> PER_PLAYER_STATE = new ConcurrentHashMap<>();
    private static final Set<UUID> FORCED_BY_INSIDE = ConcurrentHashMap.newKeySet();
    private static final Map<StructureKeyInst, GlobalLock> GLOBAL_LOCKS = new ConcurrentHashMap<>();

    /* ===================  EVENTS  =================== */

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (!(world instanceof ServerWorld sw) || !(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
        if (!isSurvivalLike(sp)) return ActionResult.PASS; // allow SURVIVAL or ADVENTURE

        BlockPos pos = hit.getBlockPos();
        BlockState state = sw.getBlockState(pos);

        // Jungle Temple (GLOBAL): chest near repeater unlocks the WHOLE INSTANCE
        if (state.getBlock() instanceof ChestBlock) {
            // first try by chest pos
            var inJungle = structureAt(sw, pos, StructureKeys.JUNGLE_PYRAMID);
            // fallback: use player's pos (some temples have chests on the edge/outside vanilla bounds)
            if (inJungle == null) inJungle = structureAt(sw, sp.getBlockPos(), StructureKeys.JUNGLE_PYRAMID);

            if (inJungle != null && hasRepeaterNearby(sw, pos)) {
                var g = globalLock(inJungle);
                if (!g.unlocked) {
                    g.unlocked = true;
                    playUnlockSoundToPlayersInside(sw, inJungle, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
                }
                return ActionResult.PASS;
            }
        }

        return ActionResult.PASS;
    }

    private static void onKillUnlock(ServerWorld world, net.minecraft.entity.Entity killer, net.minecraft.entity.LivingEntity victim, net.minecraft.entity.damage.DamageSource source) {
        if (!(killer instanceof ServerPlayerEntity sp) || !isSurvivalLike(sp)) return; // allow SURVIVAL or ADVENTURE

        // Woodland Mansion (GLOBAL counting)
        if (victim instanceof EvokerEntity) {
            var inst = structureAt(world, victim.getBlockPos(), StructureKeys.MANSION);
            if (inst != null) {
                ensureMansionRequirement(world, inst, victim.getBlockPos());
                var g = globalLock(inst);
                if (!g.unlocked) {
                    if (g.mansionRequired <= 0) {
                        g.unlocked = true;
                        playUnlockSoundToPlayersInside(world, inst, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
                    } else {
                        g.mansionKills++;
                        if (g.mansionKills >= g.mansionRequired) {
                            g.unlocked = true;
                            playUnlockSoundToPlayersInside(world, inst, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
                        }
                    }
                }
            }
            return;
        }

        // Nether Fortress (per-player counting, SHARE unlock to everyone currently inside)
        if (victim instanceof BlazeEntity) {
            var inst = structureAt(world, victim.getBlockPos(), StructureKeys.FORTRESS);
            if (inst != null) {
                var lock = playerLocks(sp).computeIfAbsent(inst, k -> new Lock());
                if (!lock.unlocked) {
                    lock.blazeKills++;
                    if (lock.blazeKills >= BLAZES_TO_UNLOCK_FORTRESS) {
                        unlockPerPlayerForAllInside(world, inst);
                        playUnlockSoundToPlayersInside(world, inst, SoundEvents.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
                    }
                }
            }
        }
    }

    /* ===================  TICK  =================== */

    private static void tick(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (ServerPlayerEntity sp : world.getPlayers()) {
                if (!isSurvival(sp) && !FORCED_BY_INSIDE.contains(sp.getUuid())) continue;

                var inst = findAnyTargetStructure(world, sp.getBlockPos());

                // On entering a mansion instance, scan it once to set required evoker kills.
                if (inst != null && inst.key() == StructureKeys.MANSION) {
                    ensureMansionRequirement(world, inst, sp.getBlockPos());
                }

                boolean insideLocked = inst != null && !isUnlocked(sp, inst) && isInside(world, sp.getBlockPos(), inst);

                if (insideLocked) {
                    ensureAdventureInside(sp);
                } else if (FORCED_BY_INSIDE.remove(sp.getUuid())) {
                    ensureSurvival(sp);
                }
            }
        }
    }

    /* ===================  PUBLIC API FOR MIXINS  =================== */

    public static boolean isLockedAndPlayerOutside(ServerWorld world, ServerPlayerEntity sp, BlockPos blockPos) {
        StructureKeyInst inst = findAnyTargetStructure(world, blockPos);
        if (inst == null) return false;
        if (isUnlocked(sp, inst)) return false;
        return !isInside(world, sp.getBlockPos(), inst);
    }

    /* ===================  HELPERS  =================== */

    private static boolean isSurvival(ServerPlayerEntity sp) {
        return sp.interactionManager.getGameMode() == GameMode.SURVIVAL;
    }

    private static boolean isSurvivalLike(ServerPlayerEntity sp) {
        GameMode gm = sp.interactionManager.getGameMode();
        return gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE;
    }

    private static void ensureAdventureInside(ServerPlayerEntity sp) {
        var gm = sp.interactionManager.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (gm != GameMode.ADVENTURE) sp.changeGameMode(GameMode.ADVENTURE);
        FORCED_BY_INSIDE.add(sp.getUuid());
    }

    private static void ensureSurvival(ServerPlayerEntity sp) {
        var gm = sp.interactionManager.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (gm != GameMode.SURVIVAL) sp.changeGameMode(GameMode.SURVIVAL);
    }

    private static Map<StructureKeyInst, Lock> playerLocks(ServerPlayerEntity sp) {
        return PER_PLAYER_STATE.computeIfAbsent(sp.getUuid(), id -> new HashMap<>());
    }

    private static GlobalLock globalLock(StructureKeyInst inst) {
        return GLOBAL_LOCKS.computeIfAbsent(inst, k -> new GlobalLock());
    }

    private static boolean isUnlocked(ServerPlayerEntity sp, StructureKeyInst inst) {
        GlobalLock g = GLOBAL_LOCKS.get(inst);
        if (g != null && g.unlocked) return true;
        return playerLocks(sp).getOrDefault(inst, new Lock()).unlocked;
    }

    private static void ensureMansionRequirement(ServerWorld world, StructureKeyInst inst, BlockPos around) {
        GlobalLock g = globalLock(inst);
        if (g.unlocked || g.mansionRequired != -1) return;

        int count = countEvokersForInstance(world, inst, around);
        if (count <= 0) {
            g.mansionRequired = 0;
            g.unlocked = true;
            playUnlockSoundToPlayersInside(world, inst, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
        } else {
            g.mansionRequired = count;
            g.mansionKills = 0;
        }
    }

    private static int countEvokersForInstance(ServerWorld world, StructureKeyInst inst, BlockPos center) {
        Box box = new Box(center).expand(192.0, 64.0, 192.0);
        List<EvokerEntity> list = world.getEntitiesByClass(EvokerEntity.class, box, e -> {
            var eInst = structureAt(world, e.getBlockPos(), StructureKeys.MANSION);
            return eInst != null && eInst.equals(inst);
        });
        return list.size();
    }

    private static void unlockPerPlayerForAllInside(ServerWorld world, StructureKeyInst inst) {
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!isSurvivalLike(p)) continue;
            if (isInside(world, p.getBlockPos(), inst)) {
                var locks = playerLocks(p);
                var l = locks.computeIfAbsent(inst, k -> new Lock());
                if (!l.unlocked) l.unlocked = true;
            }
        }
    }

    private static void playUnlockSoundToPlayersInside(ServerWorld world, StructureKeyInst inst, net.minecraft.sound.SoundEvent se, float vol, float pitch) {
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (isInside(world, p.getBlockPos(), inst)) {
                world.playSound(null, p.getBlockPos(), se, SoundCategory.PLAYERS, vol, pitch);
            }
        }
    }

    private static boolean hasRepeaterNearby(ServerWorld world, BlockPos center) {
        int r = 4;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockState s = world.getBlockState(center.add(dx, dy, dz));
                    if (s.getBlock() instanceof RepeaterBlock) return true;
                }
            }
        }
        return false;
    }

    private static boolean isInside(ServerWorld world, BlockPos playerPos, StructureKeyInst inst) {
        var reg = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        Structure structure = reg.get(inst.key());
        if (structure == null) return false;

        StructureStart atPlayer = world.getStructureAccessor().getStructureContaining(playerPos, structure);
        if (atPlayer == StructureStart.DEFAULT || !atPlayer.hasChildren()) return false;

        ChunkPos cp = atPlayer.getPos();
        return cp.x == inst.startChunkX && cp.z == inst.startChunkZ
                && inst.dimensionId.equals(world.getRegistryKey().getValue().toString());
    }

    private static StructureKeyInst findAnyTargetStructure(ServerWorld world, BlockPos pos) {
        StructureKeyInst s;
        s = structureAt(world, pos, StructureKeys.JUNGLE_PYRAMID); if (s != null) return s;
        s = structureAt(world, pos, StructureKeys.MANSION);        if (s != null) return s;
        s = structureAt(world, pos, StructureKeys.FORTRESS);       return s;
    }

    private static StructureKeyInst structureAt(ServerWorld world, BlockPos pos, RegistryKey<Structure> key) {
        var reg = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        Structure structure = reg.get(key);
        if (structure == null) return null;

        StructureAccessor accessor = world.getStructureAccessor();
        StructureStart start = accessor.getStructureContaining(pos, structure);
        if (start == StructureStart.DEFAULT || !start.hasChildren()) return null;

        ChunkPos startChunk = start.getPos();
        return new StructureKeyInst(
                key,
                world.getRegistryKey().getValue().toString(),
                startChunk.x, startChunk.z
        );
    }
}
