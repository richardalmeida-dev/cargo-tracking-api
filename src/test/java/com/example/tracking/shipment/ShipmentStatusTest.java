package com.example.tracking.shipment;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentStatusTest {

    private static final Set<String> VALID = Set.of(
            "CREATED->PICKED_UP",
            "CREATED->EXCEPTION",
            "PICKED_UP->IN_TRANSIT",
            "PICKED_UP->EXCEPTION",
            "IN_TRANSIT->OUT_FOR_DELIVERY",
            "IN_TRANSIT->EXCEPTION",
            "OUT_FOR_DELIVERY->DELIVERED",
            "OUT_FOR_DELIVERY->EXCEPTION",
            "EXCEPTION->IN_TRANSIT",
            "EXCEPTION->OUT_FOR_DELIVERY",
            "EXCEPTION->RETURNED"
    );

    static Stream<Arguments> allPairs() {
        Stream.Builder<Arguments> builder = Stream.builder();
        for (ShipmentStatus from : ShipmentStatus.values()) {
            for (ShipmentStatus to : ShipmentStatus.values()) {
                builder.add(Arguments.of(from, to));
            }
        }
        return builder.build();
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allPairs")
    void canTransitionTo_matchesMatrix(ShipmentStatus from, ShipmentStatus to) {
        boolean expected = VALID.contains(from.name() + "->" + to.name());
        assertThat(from.canTransitionTo(to)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(ShipmentStatus.class)
    void isTerminal_matchesMatrix(ShipmentStatus status) {
        boolean expected = (status == ShipmentStatus.DELIVERED || status == ShipmentStatus.RETURNED);
        assertThat(status.isTerminal()).isEqualTo(expected);
    }
}
