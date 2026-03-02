package net.hallowed.oldways.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.hallowed.oldways.content.entity.ai.goal.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;

public final class ModAiGoals {
    private ModAiGoals() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(ModAiGoals::onEntityLoad);
    }

    private static void onEntityLoad(Entity entity, ServerWorld world) {
        if (!(entity instanceof MobEntity mob)) return;

        final GoalSelector goals = mob.goalSelector;
        final EntityType<?> type = mob.getType();

        // 1) Run while charging crossbow
        if (type == EntityType.PILLAGER || type == EntityType.PIGLIN) {
            if (mob instanceof PathAwareEntity path && !hasGoal(goals, RunWhileChargingCrossbowGoal.class)) {
                goals.add(2, new RunWhileChargingCrossbowGoal(path, 1.0D));
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
                goals.add(1, new ParkourGoal(mob));
            }
        }

        // 3) Open Fence Gate Goal
        if (type == EntityType.VILLAGER && !hasGoal(goals, OpenFenceGateGoal.class)) {
            goals.add(2, new OpenFenceGateGoal(mob));
        }

        // 4) Follow emerald block goal
        if (type == EntityType.VILLAGER) {
            if (mob instanceof PathAwareEntity path && !hasGoal(goals, FollowEmeraldBlockGoal.class)) {
                goals.add(3, new FollowEmeraldBlockGoal(path, 0.5D, 16.0D, 2.5D));
            }
        }

        // 5) Sheep flee from wolves
        if (type == EntityType.SHEEP) {
            if (mob instanceof PathAwareEntity path && !hasGoal(goals, SheepFleeFromWolvesGoal.class)) {
                goals.add(2, new SheepFleeFromWolvesGoal(path));
            }
        }
    }

    private static boolean hasGoal(GoalSelector goals, Class<? extends Goal> goalClass) {
        for (var entry : goals.getGoals()) {
            if (goalClass.isInstance(entry.getGoal())) return true;
        }
        return false;
    }
}
