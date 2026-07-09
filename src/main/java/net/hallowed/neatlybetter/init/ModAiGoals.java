package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.entity.ai.goal.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.spider.Spider;

import java.util.ArrayList;

public final class ModAiGoals {
    private ModAiGoals() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(ModAiGoals::onEntityLoad);
    }

    private static void onEntityLoad(Entity entity, ServerLevel world) {
        if (!(entity instanceof Mob mob)) return;

        final GoalSelector goals = mob.goalSelector;
        final EntityType<?> type = mob.getType();

        // 1) Responsive spider attacks + Leap goal
        if (type == EntityTypes.SPIDER) {
            if (NTServerConfig.CONFIG.oldSpiderAttacks.get()) {
                removeExactGoal(goals, MeleeAttackGoal.class);
                removeExactGoal(goals, LeapAtTargetGoal.class);

                if (hasNoGoal(goals, OldSpiderAttackGoal.class)) {
                    goals.addGoal(3, new OldSpiderAttackGoal((Spider) mob));
                }
            }
        }

        // 2) Run while charging crossbow
        if (type == EntityTypes.PILLAGER && mob instanceof Pillager pillager) {
            if (NTServerConfig.CONFIG.runWhileCharging.isTrue()
                    && hasNoGoal(goals, RetreatingCrossbowAttackGoal.class)) {
                removeExactGoal(goals, RangedCrossbowAttackGoal.class);
                goals.addGoal(3, new RetreatingCrossbowAttackGoal<>(pillager, 1.0D, 8.0F, 5.0F));
            }
        }

        // 3) Parkour Goal
        if ((type == EntityTypes.VINDICATOR ||
                type == EntityTypes.PIGLIN_BRUTE ||
                type == EntityTypes.ZOMBIFIED_PIGLIN ||
                type == EntityTypes.HUSK ||
                type == EntityTypes.ZOMBIE)) {
            if (NTServerConfig.CONFIG.mobParkour.get()
                    && world.getDifficulty() == Difficulty.HARD
                    && hasNoGoal(goals, ParkourGoal.class)) {
                goals.addGoal(1, new ParkourGoal(mob));
            }
        }

        // 4) Follow emerald block
        if (type == EntityTypes.VILLAGER) {
            if (NTServerConfig.CONFIG.villagerEmeraldBlockTempt.get()
                    && mob instanceof PathfinderMob path
                    && hasNoGoal(goals, FollowEmeraldBlockGoal.class)) {
                goals.addGoal(3, new FollowEmeraldBlockGoal(path, 0.5D, 16.0D, 2.5D));
            }
        }

        // 5) Sheep flee from wolves
        if (type == EntityTypes.SHEEP) {
            if (NTServerConfig.CONFIG.sheepRunFromWolves.get()
                    && mob instanceof PathfinderMob path
                    && hasNoGoal(goals, SheepFleeFromWolvesGoal.class)) {
                goals.addGoal(2, new SheepFleeFromWolvesGoal(path));
            }
        }

        // 6) Ground-item breeding
        if (mob instanceof Animal animal
                && NTServerConfig.CONFIG.groundItemBreeding.get()
                && hasNoGoal(goals, GroundItemBreedGoal.class)) {
            goals.addGoal(4, new GroundItemBreedGoal(animal));
        }

        // 7) Tamed wolf improvements
        if (type == EntityTypes.WOLF && mob instanceof Wolf wolf) {
            if (NTServerConfig.CONFIG.wolfImprovements.get()) {
                if (hasNoGoal(goals, TamedWolfMeleeAttackGoal.class)) {
                    removeExactGoal(goals, MeleeAttackGoal.class);
                    goals.addGoal(5, new TamedWolfMeleeAttackGoal(wolf, 1.3, true));
                }

                if (hasNoGoal(goals, FastFollowOwnerGoal.class)) {
                    removeExactGoal(goals, FollowOwnerGoal.class);
                    goals.addGoal(6, new FastFollowOwnerGoal(wolf, 1.3, 20.0F, 4.0F));
                }

                final GoalSelector targets = wolf.targetSelector;
                if (hasNoGoal(targets, DefendOwnerGoal.class)) {
                    targets.addGoal(3, new DefendOwnerGoal(wolf));
                }
            }
        }
    }

    private static boolean hasNoGoal(GoalSelector goals, Class<? extends Goal> goalClass) {
        for (var entry : goals.getAvailableGoals()) {
            if (goalClass.isInstance(entry.getGoal())) return false;
        }
        return true;
    }

    private static void removeExactGoal(GoalSelector goals, Class<? extends Goal> goalClass) {
        for (var entry : new ArrayList<>(goals.getAvailableGoals())) {
            if (entry.getGoal().getClass() == goalClass) {
                goals.removeGoal(entry.getGoal());
            }
        }
    }
}
