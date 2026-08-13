package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tickets", schema = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @Column(name = "ticket_no", nullable = false, columnDefinition = "char(13)")
    private String ticketNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_ref", nullable = false)
    private Booking booking;

    @Column(name = "passenger_id", nullable = false, length = 20)
    private String passengerId;

    @Column(name = "passenger_name", nullable = false, columnDefinition = "text")
    private String passengerName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contact_data", columnDefinition = "jsonb")
    private String contactData;
}
