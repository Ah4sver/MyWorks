package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.OffsetDateTime;

@Entity
@Table(name = "flights", schema = "bookings", uniqueConstraints = {
        @UniqueConstraint(
        name = "flights_flight_no_scheduled_departure_key",
        columnNames = {"flight_no", "scheduled_departure"}
        )
})
@Check(
        name = "flights_scheduled_arrival_check",
        constraints = "scheduled_arrival > scheduled_departure"
)
@Check(
        name = "flights_actual_arrival_check",
        constraints = "actual_arrival IS NULL OR (actual_departure IS NOT NULL AND actual_arrival IS NOT NULL AND (actual_arrival > actual_departure))"
)
@Check(
        name = "flights_status_check",
        constraints = "status IN ('On Time', 'Delayed','Departed','Arrived','Scheduled','Cancelled')"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_id", nullable = false)
    private Integer flightId;

    @Column(name = "flight_no", nullable = false, columnDefinition = "char(6)")
    private String flightNo;

    @Column(name = "scheduled_departure", nullable = false)
    private OffsetDateTime scheduledDeparture;

    @Column(name = "scheduled_arrival", nullable = false)
    private OffsetDateTime scheduledArrival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_airport", nullable = false)
    private Airport departureAirport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_airport", nullable = false)
    private Airport arrivalAirport;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_code", nullable = false)
    private Aircraft aircraftCode;

    @Column(name = "actual_departure")
    private OffsetDateTime actualDeparture;

    @Column(name = "actual_arrival")
    private OffsetDateTime actualArrival;

}
