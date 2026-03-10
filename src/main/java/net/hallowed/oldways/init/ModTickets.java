package net.hallowed.oldways.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.TicketType;

/**
 * Custom ticket types for the Old Ways mod.
 *
 * <p>In 1.21.11+, {@link TicketType} is a record: {@code TicketType(long timeout, int flags)}.
 * <ul>
 *   <li>{@code timeout = 0} → never auto-expires; must be removed manually.</li>
 *   <li>{@code FLAG_LOADING = 2} → tells the distance manager this ticket triggers chunk loading.</li>
 * </ul>
 */
public class ModTickets {

    /**
     * Ticket for map chunk scanning.  Keeps chunks loaded just long enough
     * for the scanner to read their colour data, then is explicitly removed.
     */
    public static final TicketType MAP_SCAN = Registry.register(
            BuiltInRegistries.TICKET_TYPE,
            Identifier.fromNamespaceAndPath("old_ways", "map_scan"),
            new TicketType(0L, TicketType.FLAG_LOADING)
    );

    /** Call from {@code OldWays.onInitialize()} to force static init. */
    public static void init() {}
}
