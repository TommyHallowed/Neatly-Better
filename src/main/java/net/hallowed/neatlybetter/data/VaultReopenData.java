package net.hallowed.neatlybetter.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class VaultReopenData {

    public static final Codec<VaultReopenData> CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG)
                    .xmap(map -> {
                        VaultReopenData d = new VaultReopenData();
                        d.unlockAt.putAll(map);
                        return d;
                    }, d -> Map.copyOf(d.unlockAt));

    private final HashMap<UUID, Long> unlockAt = new HashMap<>();

    public VaultReopenData() {}

    public void record(UUID player, long unlockGameTime) {
        unlockAt.put(player, unlockGameTime);
    }

    public boolean processExpirations(long currentGameTime, Consumer<UUID> onExpired) {
        boolean[] any = { false };
        unlockAt.entrySet().removeIf(entry -> {
            if (currentGameTime >= entry.getValue()) {
                onExpired.accept(entry.getKey());
                any[0] = true;
                return true;
            }
            return false;
        });
        return any[0];
    }

    public boolean isEmpty() {
        return unlockAt.isEmpty();
    }
}