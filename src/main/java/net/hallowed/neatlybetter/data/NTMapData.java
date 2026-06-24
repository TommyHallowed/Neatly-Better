package net.hallowed.neatlybetter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NTMapData extends SavedData {

    private static final Identifier DATA_NAME = Identifier.parse("neatlybetter_structures");

    // ══════════════════════════════════════════════════════════════════════
    //  Persistent structure entry
    // ══════════════════════════════════════════════════════════════════════

    public record StructureEntry(String decoKey, String structurePath, int x, int z) {
        public static final Codec<StructureEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("key").forGetter(StructureEntry::decoKey),
                        Codec.STRING.fieldOf("path").forGetter(StructureEntry::structurePath),
                        Codec.INT.fieldOf("x").forGetter(StructureEntry::x),
                        Codec.INT.fieldOf("z").forGetter(StructureEntry::z)
                ).apply(instance, StructureEntry::new)
        );
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CODEC & SavedDataType
    // ══════════════════════════════════════════════════════════════════════

    public static final Codec<NTMapData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, StructureEntry.CODEC.listOf())
                            .fieldOf("structures")
                            .forGetter(d -> d.structures)
            ).apply(instance, NTMapData::new)
    );

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static SavedDataType<@NotNull NTMapData> type() {
        return new SavedDataType(DATA_NAME, NTMapData::new, CODEC, null);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  State
    // ══════════════════════════════════════════════════════════════════════

    /** Map key (e.g. {@code "map_0"}) → list of structure entries. */
    private final Map<String, List<StructureEntry>> structures;

    /** Transient — tracks which maps have already been restored this session. */
    private final Set<String> restoredThisSession = new HashSet<>();

    // ══════════════════════════════════════════════════════════════════════
    //  Constructors
    // ══════════════════════════════════════════════════════════════════════

    /** Creates an empty instance (used by the Supplier in {@link #type()}). */
    public NTMapData() {
        this.structures = new HashMap<>();
    }

    /** CODEC constructor — wraps the decoded immutable map in a mutable one. */
    private NTMapData(Map<String, List<StructureEntry>> structures) {
        this.structures = new HashMap<>(structures);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Access
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Loads (or creates) the companion data from the overworld's data storage.
     */
    public static NTMapData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(type());
    }

    /** Stores structure entries for a given map, replacing any previous entries. */
    public void putStructures(String mapKey, List<StructureEntry> entries) {
        structures.put(mapKey, List.copyOf(entries));
        setDirty();
    }

    /** Returns the stored entries for a map, or an empty list if none. */
    public List<StructureEntry> getStructures(String mapKey) {
        return structures.getOrDefault(mapKey, List.of());
    }

    /** Returns all map keys that have stored structure entries. */
    public Set<String> getAllMapKeys() {
        return structures.keySet();
    }

    /** Returns {@code true} if this map has entries that haven't been restored yet. */
    public boolean needsRestore(String mapKey) {
        return structures.containsKey(mapKey) && !restoredThisSession.contains(mapKey);
    }

    /** Marks a map as restored for this session (avoids redundant work). */
    public void markRestored(String mapKey) {
        restoredThisSession.add(mapKey);
    }
}
