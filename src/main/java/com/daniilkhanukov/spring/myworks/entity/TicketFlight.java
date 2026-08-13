package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_flights", schema = "bookings")
@Check(
        name = "ticket_flights_amount_check",
        constraints = "amount >= 0"
)
@Check(
        name = "ticket_flights_fare_conditions_check",
        constraints = "fare_conditions IN ('Economy', 'Comfort', 'Business')"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketFlight {

    @EmbeddedId
    private TicketFlightId id;


    @MapsId("ticketNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_no", nullable = false)
    private Ticket ticket;

    @MapsId("flightId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "fare_conditions", nullable = false, length = 10)
    private String fareConditions;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
}
