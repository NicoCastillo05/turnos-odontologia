package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByTenantId(Long tenantId);

    Optional<Appointment> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndProfessionalId(Long tenantId, Long professionalId);

    boolean existsByTenantIdAndChairId(Long tenantId, Long chairId);

    boolean existsByTenantIdAndServiceId(Long tenantId, Long serviceId);

    @Query("""
            select (count(a) > 0) from Appointment a
            where a.tenantId = :tenantId
              and a.professional.id = :professionalId
              and a.status <> com.turnos.odontologia.domain.AppointmentStatus.CANCELLED
              and a.startAt < :endAt and a.endAt > :startAt
              and (:excludedId is null or a.id <> :excludedId)
            """)
    boolean existsOverlappingForProfessional(@Param("tenantId") Long tenantId,
                                             @Param("professionalId") Long professionalId,
                                             @Param("startAt") LocalDateTime startAt,
                                             @Param("endAt") LocalDateTime endAt,
                                             @Param("excludedId") Long excludedId);

    @Query("""
            select (count(a) > 0) from Appointment a
            where a.tenantId = :tenantId
              and a.chair.id = :chairId
              and a.status <> com.turnos.odontologia.domain.AppointmentStatus.CANCELLED
              and a.startAt < :endAt and a.endAt > :startAt
              and (:excludedId is null or a.id <> :excludedId)
            """)
    boolean existsOverlappingForChair(@Param("tenantId") Long tenantId,
                                      @Param("chairId") Long chairId,
                                      @Param("startAt") LocalDateTime startAt,
                                      @Param("endAt") LocalDateTime endAt,
                                      @Param("excludedId") Long excludedId);
}
