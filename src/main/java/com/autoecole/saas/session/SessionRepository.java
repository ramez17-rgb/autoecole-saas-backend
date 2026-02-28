package com.autoecole.saas.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    // Check if the monitor already has an overlapping session
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Session s WHERE s.monitor.id = :monitorId " +
            "AND s.sessionDate = :sessionDate " +
            "AND s.status != 'CANCELLED' " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime)) " +
            "AND s.id != :excludeSessionId")
    boolean hasOverlappingMonitorSession(
            @Param("monitorId") UUID monitorId,
            @Param("sessionDate") LocalDate sessionDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeSessionId") UUID excludeSessionId);

    // Initial check (no session to exclude)
    default boolean hasOverlappingMonitorSession(UUID monitorId, LocalDate sessionDate, LocalTime startTime,
            LocalTime endTime) {
        return hasOverlappingMonitorSession(monitorId, sessionDate, startTime, endTime, UUID.randomUUID());
    }

    // Check if a student already has an overlapping session
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Session s JOIN s.students st WHERE st.id IN :studentIds " +
            "AND s.sessionDate = :sessionDate " +
            "AND s.status != 'CANCELLED' " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime)) " +
            "AND s.id != :excludeSessionId")
    boolean hasOverlappingStudentSession(
            @Param("studentIds") List<UUID> studentIds,
            @Param("sessionDate") LocalDate sessionDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeSessionId") UUID excludeSessionId);

    default boolean hasOverlappingStudentSession(List<UUID> studentIds, LocalDate sessionDate, LocalTime startTime,
            LocalTime endTime) {
        return hasOverlappingStudentSession(studentIds, sessionDate, startTime, endTime, UUID.randomUUID());
    }

    // Filters for Admin
    @Query("SELECT s FROM Session s WHERE s.tenant.id = :tenantId " +
            "AND (:monitorId IS NULL OR s.monitor.id = :monitorId) " +
            "AND (:studentId IS NULL OR EXISTS (SELECT st FROM s.students st WHERE st.id = :studentId)) " +
            "AND (CAST(:fromDate AS date) IS NULL OR s.sessionDate >= :fromDate) " +
            "AND (CAST(:toDate AS date) IS NULL OR s.sessionDate <= :toDate) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "ORDER BY s.sessionDate DESC, s.startTime DESC")
    List<Session> findFilteredSessions(
            @Param("tenantId") UUID tenantId,
            @Param("monitorId") UUID monitorId,
            @Param("studentId") UUID studentId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") SessionStatus status);

    // Queries for Students/Monitors
    List<Session> findByStudents_IdOrderBySessionDateDescStartTimeDesc(UUID studentId);

    List<Session> findByMonitor_IdOrderBySessionDateDescStartTimeDesc(UUID monitorId);
}
