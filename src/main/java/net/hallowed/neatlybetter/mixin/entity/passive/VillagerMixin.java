package net.hallowed.neatlybetter.mixin.entity.passive;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.hallowed.neatlybetter.init.ModGameRules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Villager.class)
public abstract class VillagerMixin {

    @Shadow
    @Final
    private GossipContainer gossips;

    @Unique
    private static final int INFINITE_CURE_SOFT_CAP = 300;

    @Inject(method = "onReputationEventFrom", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$infiniteCuringDiscounts(ReputationEventType type, Entity entity, CallbackInfo ci) {
        if (type != ReputationEventType.ZOMBIE_VILLAGER_CURED) return;
        Villager self = (Villager) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.getGameRules().get(ModGameRules.VILLAGER_INFINITE_CURING_DISCOUNTS)) return;

        UUID uuid = entity.getUUID();

        Object2IntMap<GossipType> existing = this.gossips.getGossipEntries().get(uuid);
        int prevMajor = existing != null ? existing.getOrDefault(GossipType.MAJOR_POSITIVE, 0) : 0;
        int prevMinor = existing != null ? existing.getOrDefault(GossipType.MINOR_POSITIVE, 0) : 0;

        this.gossips.add(uuid, GossipType.MAJOR_POSITIVE, 20);
        this.gossips.add(uuid, GossipType.MINOR_POSITIVE, 25);

        Object2IntMap<GossipType> gossipMap = this.gossips.getGossipEntries().get(uuid);
        if (gossipMap != null) {
            gossipMap.put(GossipType.MAJOR_POSITIVE, Math.min(prevMajor + 20, INFINITE_CURE_SOFT_CAP));
            gossipMap.put(GossipType.MINOR_POSITIVE, Math.min(prevMinor + 25, INFINITE_CURE_SOFT_CAP));
        }

        ci.cancel();
    }

    @Inject(method = "getPlayerReputation", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$globalCuringPrices(Player player, CallbackInfoReturnable<Integer> cir) {
        Villager self = (Villager) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.getGameRules().get(ModGameRules.VILLAGER_GLOBAL_CURING_PRICES)) return;

        UUID playerUUID = player.getUUID();

        int personalReputation = this.gossips.getReputation(playerUUID,
                type -> type != GossipType.MAJOR_POSITIVE && type != GossipType.MINOR_POSITIVE);

        int bestCureReputation = 0;
        for (UUID uuid : this.gossips.getGossipEntries().keySet()) {
            int cureRep = this.gossips.getReputation(uuid,
                    type -> type == GossipType.MAJOR_POSITIVE || type == GossipType.MINOR_POSITIVE);
            bestCureReputation = Math.max(bestCureReputation, cureRep);
        }

        cir.setReturnValue(personalReputation + bestCureReputation);
    }
}
