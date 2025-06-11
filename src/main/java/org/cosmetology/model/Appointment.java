package org.cosmetology.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telephone", nullable = false)
    private String telephone;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "recommend", nullable = false)
    private String recommend;

    // photos — это список фотографий в формате BASE64
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "appointment_photos",
            joinColumns=@JoinColumn(name="appointment_id"))
    @Column(name = "photo_base64", columnDefinition = "TEXT")
    private List<String> photos = new ArrayList<>();
}