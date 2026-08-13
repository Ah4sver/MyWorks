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
public class SeatId implements Serializable {

    @Column(name = "aircraft_code", nullable = false, columnDefinition = "char(3)")
    private String aircraftCode;

    @Column(name = "seat_no", nullable = false, length = 4)
    private String seatNo;
}
