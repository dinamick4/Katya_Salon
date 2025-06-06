package org.cosmetology.repository;

import org.cosmetology.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.telephone = :telephone ORDER BY a.appointmentDate DESC")
    List<Appointment> findByTelephone(@Param("telephone") String telephone);
}