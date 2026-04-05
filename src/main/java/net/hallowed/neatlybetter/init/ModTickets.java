package net.hallowed.neatlybetter.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.TicketType;

public class ModTickets {

    /**
     * Ticket for map chunk scanning.  Keeps chunks loaded just long enough
     * for the scanner to read their color data, then is explicitly removed.
     */
    public static final TicketType MAP_SCAN = Registry.register(
            BuiltInRegistries.TICKET_TYPE,
            Identifier.fromNamespaceAndPath("old_ways", "map_scan"),
            new TicketType(0L, TicketType.FLAG_LOADING)
    );

    public static void init() {}
}
