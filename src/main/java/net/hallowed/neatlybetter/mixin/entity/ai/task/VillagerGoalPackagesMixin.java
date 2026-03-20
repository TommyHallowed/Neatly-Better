package net.hallowed.neatlybetter.mixin.entity.ai.task;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.entity.ai.task.FarmerReplantTask;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoiList;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerGoalPackages.class)
public abstract class VillagerGoalPackagesMixin {

    @Inject(method = "getWorkPackage", at = @At("HEAD"), cancellable = true)
    private static void neatlybetter$addTillingTask(
            Holder<@NotNull VillagerProfession> profession,
            float speed,
            CallbackInfoReturnable<ImmutableList<@NotNull Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir
    ) {
        if (!NTServerConfig.CONFIG.villagerFarmerReplant.get()) return;
        if (!profession.is(VillagerProfession.FARMER)) return;

        WorkAtPoi farmerWork = new WorkAtComposter();

        RunOne<@NotNull Villager> bundled = new RunOne<>(ImmutableList.of(
                Pair.of(farmerWork, 7),
                Pair.of(StrollAroundPoi.create(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                Pair.of(StrollToPoi.create(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                Pair.of(StrollToPoiList.create(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5),
                Pair.of(new HarvestFarmland(), 2),
                Pair.of(new FarmerReplantTask(), 3),
                Pair.of(new UseBonemeal(), 4)
        ));

        Pair<Integer, BehaviorControl<@NotNull LivingEntity>> busyFollow = VillagerGoalPackages.getMinimalLookBehavior();

        ImmutableList<@NotNull Pair<Integer, ? extends BehaviorControl<? super Villager>>> out =
                ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super Villager>>>builder()
                        .add(busyFollow)
                        .add(Pair.of(5, bundled))
                        .add(Pair.of(10, new ShowTradesToPlayer(400, 1600)))
                        .add(Pair.of(10, SetLookAndInteract.create(EntityType.PLAYER, 4)))
                        .add(Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)))
                        .add(Pair.of(3, new GiveGiftToHero(100)))
                        .add(Pair.of(99, UpdateActivityFromSchedule.create()))
                        .build();

        cir.setReturnValue(out);
    }
}
