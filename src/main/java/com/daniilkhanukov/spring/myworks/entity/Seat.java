package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "seats", schema = "bookings")
@Check(
        name = "seats_fare_condition_check",
        constraints = "fare_conditions IN ('Economy', 'Comfort', 'Business')"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @EmbeddedId
    private SeatId id;

    @MapsId("aircraftCode")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "aircraft_code",
            nullable = false,
            foreignKey = @ForeignKey(
                    foreignKeyDefinition = "FOREIGN KEY (aircraft_code) REFERENCES bookings.aircrafts(aircraft_code) ON DELETE CASCADE"
            )
    )
    private Aircraft aircraft;

    @Column(name = "fare_conditions", nullable = false, length = 10)
    private String fareConditions;
}
