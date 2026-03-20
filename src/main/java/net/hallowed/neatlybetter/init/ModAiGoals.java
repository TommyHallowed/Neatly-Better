package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.entity.ai.goal.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;

public final class ModAiGoals {
    private ModAiGoals() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(ModAiGoals::onEntityLoad);
    }

    private static void onEntityLoad(Entity entity, ServerLevel world) {
        if (!(entity instanceof Mob mob)) return;

        final GoalSelector goals = mob.goalSelector;
        final EntityType<?> type = mob.getType();

        // 1) Run while charging crossbow
        if (type == EntityType.PILLAGER || type == EntityType.PIGLIN) {
            if (NTServerConfig.CONFIG.runWhileCharging.get()
                    && mob instanceof PathfinderMob path
                    && !hasGoal(goals, RunWhileChargingCrossbowGoal.class)) {
                goals.addGoal(2, new RunWhileChargingCrossbowGoal(path, 1.0D));
            }
        }

        // 2) Parkour Goal
        if ((type == EntityType.VINDICATOR ||
                        type == EntityType.PIGLIN_BRUTE ||
                        type == EntityType.ZOMBIFIED_PIGLIN ||
                        type == EntityType.ZOMBIE)) {
            if (NTServerConfig.CONFIG.mobParkour.get()
                    && world.getDifficulty() == Difficulty.HARD
                    && !hasGoal(goals, ParkourGoal.class) ) {
                goals.addGoal(1, new ParkourGoal(mob));
            }
        }

        // 3) Open Fence Gate
        if (type == EntityType.VILLAGER) {
            if (NTServerConfig.CONFIG.villagerOpensFenceGate.get()
                    && !hasGoal(goals, OpenFenceGateGoal.class)) {
                goals.addGoal(2, new OpenFenceGateGoal(mob));
            }
        }

        // 4) Follow emerald block
        if (type == EntityType.VILLAGER) {
            if (NTServerConfig.CONFIG.villagerEmeraldBlockTempt.get()
                    && mob instanceof PathfinderMob path
                    && !hasGoal(goals, FollowEmeraldBlockGoal.class)) {
                goals.addGoal(3, new FollowEmeraldBlockGoal(path, 0.5D, 16.0D, 2.5D));
            }
        }

        // 5) Sheep flee from wolves
        if (type == EntityType.SHEEP) {
            if (NTServerConfig.CONFIG.sheepRunFromWolves.get()
                    && mob instanceof PathfinderMob path
                    && !hasGoal(goals, SheepFleeFromWolvesGoal.class)) {
                goals.addGoal(2, new SheepFleeFromWolvesGoal(path));
            }
        }

        // 6) Ground-item breeding
        if (mob instanceof Animal animal
                && NTServerConfig.CONFIG.groundItemBreeding.get()
                && !hasGoal(goals, GroundItemBreedGoal.class)) {
            goals.addGoal(4, new GroundItemBreedGoal(animal));
        }
    }

    private static boolean hasGoal(GoalSelector goals, Class<? extends Goal> goalClass) {
        for (var entry : goals.getAvailableGoals()) {
            if (goalClass.isInstance(entry.getGoal())) return true;
        }
        return false;
    }
}
