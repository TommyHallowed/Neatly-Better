package net.hallowed.oldways.mixin.entity.ai.task;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.hallowed.oldways.content.entity.ai.task.FarmerReplantTask;
import net.hallowed.oldways.mixin.accessor.VillagerTaskListProviderAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTaskListProvider.class)
public abstract class VillagerTaskListProviderMixin {

    @Inject(method = "createWorkTasks", at = @At("HEAD"), cancellable = true)
    private static void oldways$addTillingTask(RegistryEntry<VillagerProfession> profession,
                                               float speed,
                                               CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (!profession.matchesKey(VillagerProfession.FARMER)) return;

        VillagerWorkTask farmerWork = new FarmerWorkTask();

        RandomTask<VillagerEntity> bundled = new RandomTask<>(ImmutableList.of(
                Pair.of(farmerWork, 7),
                Pair.of(GoAroundTask.create(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                Pair.of(GoToPosTask.create(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                Pair.of(GoToSecondaryPositionTask.create(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5),

                Pair.of(new FarmerVillagerTask(), 2),
                Pair.of(new FarmerReplantTask(), 3),
                Pair.of(new BoneMealTask(), 4)
        ));

        Pair<Integer, ? extends Task<? super VillagerEntity>> busyFollow =
                VillagerTaskListProviderAccessor.oldways$invokeCreateBusyFollowTask();

        ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> out =
                ImmutableList.<Pair<Integer, ? extends Task<? super VillagerEntity>>>builder()
                        .add(busyFollow)
                        .add(Pair.of(5, bundled))
                        .add(Pair.of(10, new HoldTradeOffersTask(400, 1600)))
                        .add(Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4)))
                        .add(Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)))
                        .add(Pair.of(3, new GiveGiftsToHeroTask(100)))
                        .add(Pair.of(99, ScheduleActivityTask.create()))
                        .build();

        cir.setReturnValue(out);
    }

}
