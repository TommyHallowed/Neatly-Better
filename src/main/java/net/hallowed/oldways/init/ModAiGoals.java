package net.hallowed.oldways.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.hallowed.oldways.content.entity.ai.goal.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;

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
            if (mob instanceof PathfinderMob path && !hasGoal(goals, RunWhileChargingCrossbowGoal.class)) {
                goals.addGoal(2, new RunWhileChargingCrossbowGoal(path, 1.0D));
            }
        }

        // 2) Parkour Goal (only on Hard difficulty)
        if (world.getDifficulty() == Difficulty.HARD &&
                (type == EntityType.VINDICATOR ||
                        type == EntityType.PILLAGER ||
                        type == EntityType.PIGLIN ||
                        type == EntityType.PIGLIN_BRUTE ||
                        type == EntityType.WITCH)) {
            if (!hasGoal(goals, ParkourGoal.class)) {
                goals.addGoal(1, new ParkourGoal(mob));
            }
        }

        // 3) Open Fence Gate Goal
        if (type == EntityType.VILLAGER && !hasGoal(goals, OpenFenceGateGoal.class)) {
            goals.addGoal(2, new OpenFenceGateGoal(mob));
        }

        // 4) Follow emerald block goal
        if (type == EntityType.VILLAGER) {
            if (mob instanceof PathfinderMob path && !hasGoal(goals, FollowEmeraldBlockGoal.class)) {
                goals.addGoal(3, new FollowEmeraldBlockGoal(path, 0.5D, 16.0D, 2.5D));
            }
        }

        // 5) Sheep flee from wolves
        if (type == EntityType.SHEEP) {
            if (mob instanceof PathfinderMob path && !hasGoal(goals, SheepFleeFromWolvesGoal.class)) {
                goals.addGoal(2, new SheepFleeFromWolvesGoal(path));
            }
        }
    }

    private static boolean hasGoal(GoalSelector goals, Class<? extends Goal> goalClass) {
        for (var entry : goals.getAvailableGoals()) {
            if (goalClass.isInstance(entry.getGoal())) return true;
        }
        return false;
    }
}
