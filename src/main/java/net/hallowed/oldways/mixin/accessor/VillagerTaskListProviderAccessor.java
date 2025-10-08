package net.hallowed.oldways.mixin.accessor;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VillagerTaskListProvider.class)
public interface VillagerTaskListProviderAccessor {
    @Invoker("createBusyFollowTask")
    static Pair<Integer, Task<LivingEntity>> oldways$invokeCreateBusyFollowTask() {
        throw new AssertionError();
    }
}
