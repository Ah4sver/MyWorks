package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TicketFlightId implements Serializable {

    @Column(name = "ticket_no", nullable = false, length = 13)
    private String ticketNo;

    @Column(name = "flight_id", nullable = false)
    private Integer flightId;
}
