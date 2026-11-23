package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findAllByTenantId(Long tenantId);

    Optional<Block> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndProfessionalId(Long tenantId, Long professionalId);

    boolean existsByTenantIdAndChairId(Long tenantId, Long chairId);

    @Query("""
            select (count(b) > 0) from Block b
            where b.tenantId = :tenantId
              and b.professional.id = :professionalId
              and b.startAt < :endAt and b.endAt > :startAt
            """)
    boolean existsOverlappingForProfessional(@Param("tenantId") Long tenantId,
                                             @Param("professionalId") Long professionalId,
                                             @Param("startAt") LocalDateTime startAt,
                                             @Param("endAt") LocalDateTime endAt);

    @Query("""
            select (count(b) > 0) from Block b
            where b.tenantId = :tenantId
              and b.chair.id = :chairId
              and b.startAt < :endAt and b.endAt > :startAt
            """)
    boolean existsOverlappingForChair(@Param("tenantId") Long tenantId,
                                      @Param("chairId") Long chairId,
                                      @Param("startAt") LocalDateTime startAt,
                                      @Param("endAt") LocalDateTime endAt);
}
