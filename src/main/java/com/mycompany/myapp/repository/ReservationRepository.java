package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Reservation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Reservation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    /**
     * Find all reservations for the current authenticated user
     */
    @Query("select reservation from Reservation reservation where reservation.user.login = ?#{authentication.name}")
    List<Reservation> findByUserIsCurrentUser();

    /**
     * Count active reservations for a specific user
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId AND r.endTime > :currentTime")
    long countActiveReservationsByUserId(@Param("userId") Long userId, @Param("currentTime") Instant currentTime);

    /**
     * Find overlapping reservations for a specific resource
     */
    @Query("SELECT r FROM Reservation r WHERE r.resource.id = :resourceId AND " + "((r.startTime < :endTime) AND (r.endTime > :startTime))")
    List<Reservation> findOverlappingReservations(
        @Param("resourceId") Long resourceId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Find all reservations for a specific resource
     */
    @Query("SELECT r FROM Reservation r WHERE r.resource.id = :resourceId ORDER BY r.startTime")
    List<Reservation> findByResourceIdOrderByStartTime(@Param("resourceId") Long resourceId);

    /**
     * Find active reservations for a specific user
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.endTime > :currentTime ORDER BY r.startTime")
    List<Reservation> findActiveReservationsByUserId(@Param("userId") Long userId, @Param("currentTime") Instant currentTime);

    /**
     * Find reservations by user ID
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId ORDER BY r.startTime DESC")
    List<Reservation> findByUserIdOrderByStartTimeDesc(@Param("userId") Long userId);

    /**
     * Check if a resource is available for a specific time slot
     */
    @Query(
        "SELECT CASE WHEN COUNT(r) > 0 THEN false ELSE true END FROM Reservation r " +
        "WHERE r.resource.id = :resourceId AND " +
        "((r.startTime < :endTime) AND (r.endTime > :startTime))"
    )
    boolean isResourceAvailable(
        @Param("resourceId") Long resourceId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    default Optional<Reservation> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Reservation> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Reservation> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select reservation from Reservation reservation left join fetch reservation.user",
        countQuery = "select count(reservation) from Reservation reservation"
    )
    Page<Reservation> findAllWithToOneRelationships(Pageable pageable);

    @Query("select reservation from Reservation reservation left join fetch reservation.user")
    List<Reservation> findAllWithToOneRelationships();

    @Query("select reservation from Reservation reservation left join fetch reservation.user where reservation.id =:id")
    Optional<Reservation> findOneWithToOneRelationships(@Param("id") Long id);
}
