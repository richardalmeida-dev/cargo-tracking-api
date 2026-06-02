package com.example.tracking.shipment;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados do shipment. As transições válidas estão codificadas como dados
 * (não como if/switch) num {@link EnumMap} imutável, o que mantém o domínio testável
 * por matriz exaustiva e impossibilita transições fora do grafo.
 *
 * <p>{@code DELIVERED} e {@code RETURNED} são terminais — não aceitam evento posterior.
 * {@code EXCEPTION} é o único estado recuperável e o único caminho para {@code RETURNED}.
 */
public enum ShipmentStatus {
    CREATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION,
    RETURNED;

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> TRANSITIONS =
            new EnumMap<>(ShipmentStatus.class);

    static {
        TRANSITIONS.put(CREATED,          EnumSet.of(PICKED_UP, EXCEPTION));
        TRANSITIONS.put(PICKED_UP,        EnumSet.of(IN_TRANSIT, EXCEPTION));
        TRANSITIONS.put(IN_TRANSIT,       EnumSet.of(OUT_FOR_DELIVERY, EXCEPTION));
        TRANSITIONS.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED, EXCEPTION));
        TRANSITIONS.put(EXCEPTION,        EnumSet.of(IN_TRANSIT, OUT_FOR_DELIVERY, RETURNED));
        TRANSITIONS.put(DELIVERED,        EnumSet.noneOf(ShipmentStatus.class));
        TRANSITIONS.put(RETURNED,         EnumSet.noneOf(ShipmentStatus.class));
    }

    /** Verdadeiro se {@code target} é alcançável a partir deste estado em um único passo. */
    public boolean canTransitionTo(ShipmentStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    /** Estado terminal: nenhuma transição parte daqui. */
    public boolean isTerminal() {
        return TRANSITIONS.get(this).isEmpty();
    }
}
