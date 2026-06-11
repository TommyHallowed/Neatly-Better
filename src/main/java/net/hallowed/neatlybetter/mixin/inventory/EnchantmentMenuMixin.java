package net.hallowed.neatlybetter.mixin.inventory;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.network.NTNetwork;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hallowed.neatlybetter.util.LapisUtil;
import net.hallowed.neatlybetter.util.LapisVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {

    @Shadow @Final
    public Container enchantSlots;

    @Unique private Player neatlybetter$player;

    @Inject(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL")
    )
    private void neatlybetter$onInit(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        this.neatlybetter$player = inventory.player;

        if (!NTServerConfig.CONFIG.lapisStaysInEnchanting.get()) return;

        EnchantingTableBlockEntity enchTable = LapisUtil.getEnchantingTableBlockEntity(neatlybetter$player);
        if (enchTable != null) {
            int stored = LapisUtil.getLapisCount(neatlybetter$player.level(), enchTable);
            if (stored > 0) {
                enchantSlots.setItem(1, new ItemStack(Items.LAPIS_LAZULI, stored));
            }
        }
    }

    @Inject(method = "slotsChanged(Lnet/minecraft/world/Container;)V", at = @At("TAIL"))
    private void neatlybetter$onSlotsChanged(Container container, CallbackInfo ci) {
        if (neatlybetter$player == null) return;
        if (!NTServerConfig.CONFIG.lapisStaysInEnchanting.get()) return;

        ItemStack lapisStack = enchantSlots.getItem(1);
        int lapisCount = lapisStack.is(Items.LAPIS_LAZULI) ? lapisStack.getCount() : 0;

        EnchantingTableBlockEntity enchTable = LapisUtil.getEnchantingTableBlockEntity(neatlybetter$player);
        if (enchTable == null) return;

        Level level = neatlybetter$player.level();

        if (lapisCount == LapisUtil.getLapisCount(level, enchTable)) return;

        LapisUtil.saveLapisCount(level, enchTable, lapisCount);

        if (!level.isClientSide()) {
            BlockPos tablePos = enchTable.getBlockPos();
            NTNetwork.LapisCountPayload payload = new NTNetwork.LapisCountPayload(lapisCount, tablePos);

            for (ServerPlayer serverPlayer : ((ServerLevel) level).getServer().getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(serverPlayer, payload);
            }

            UUID ownerUUID = neatlybetter$player.getUUID();
            for (Player other : level.getServer().getPlayerList().getPlayers()) {
                if (other.getUUID().equals(ownerUUID)) continue;

                if (!LapisVariables.lastEnchantingTableInteraction.containsKey(other.getUUID())) continue;
                if (!tablePos.equals(LapisVariables.lastEnchantingTableInteraction.get(other.getUUID()))) continue;

                if (other.containerMenu instanceof EnchantmentMenu) {
                    ItemStack syncStack = lapisCount > 0
                            ? new ItemStack(Items.LAPIS_LAZULI, lapisCount)
                            : ItemStack.EMPTY;
                    other.containerMenu.getSlot(1).set(syncStack);
                }
            }
        }
    }

    @Inject(method = "removed(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"))
    private void neatlybetter$onRemoved(Player player, CallbackInfo ci) {
        this.neatlybetter$player = null;
    }
}