package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "aircrafts", schema = "bookings")
@Check(name = "aircraft_range_check", constraints = "range > 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Aircraft {

    @Id
    @Column(name = "aircraft_code", nullable = false, columnDefinition = "char(3)")
    private String aircraftCode;

    @Column(name = "model", nullable = false, columnDefinition = "text")
    private String model;

    @Column(name = "range", nullable = false)
    private Integer range;
}
