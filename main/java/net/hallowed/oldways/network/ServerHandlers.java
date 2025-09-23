package net.hallowed.oldways.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ServerHandlers {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(
                EnderCheckPackets.EnderCheckRequest.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    boolean compass = hasInEnder(player, Items.COMPASS);
                    boolean clock   = hasInEnder(player, Items.CLOCK);
                    ServerPlayNetworking.send(player,
                            new EnderCheckPackets.EnderCheckResponse(compass, clock));
                });
    }

    private static boolean hasInEnder(ServerPlayerEntity p, net.minecraft.item.Item item) {
        var inv = p.getEnderChestInventory();
        for (int i = 0; i < inv.size(); i++) if (inv.getStack(i).isOf(item)) return true;
        return false;
    }

    private ServerHandlers() {}
}
