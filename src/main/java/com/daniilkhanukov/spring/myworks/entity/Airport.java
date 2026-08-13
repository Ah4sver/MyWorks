package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "airports", schema = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Airport {

    @Id
    @Column(name = "airport_code", nullable = false, columnDefinition = "char(3)")
    private String airportCode;

    @Column(name = "airport_name", nullable = false, columnDefinition = "text")
    private String airportName;

    @Column(name = "city", nullable = false, columnDefinition = "text")
    private String city;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "timezone", nullable = false, columnDefinition = "text")
    private String timezone;
}
