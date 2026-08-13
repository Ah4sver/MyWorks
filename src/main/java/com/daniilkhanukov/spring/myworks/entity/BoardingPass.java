package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boarding_passes", schema = "bookings", uniqueConstraints = {
        @UniqueConstraint(
                name = "boarding_passes_flight_id_boarding_no_key",
                columnNames = {"flight_id", "boarding_no"}
        ),
        @UniqueConstraint(
                name = "boarding_passes_flight_id_seat_no_key",
                columnNames = {"flight_id", "seat_no"}
        )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardingPass {

    @EmbeddedId
    private BoardingPassId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(
                    name = "ticket_no",
                    referencedColumnName = "ticket_no",
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "flight_id",
                    referencedColumnName = "flight_id",
                    insertable = false,
                    updatable = false
            )
    })
    private TicketFlight ticketFlight;

    @Column(name = "boarding_no", nullable = false)
    private Integer boardingNo;

    @Column(name = "seat_no", nullable = false, length = 4)
    private String seatNo;
}
