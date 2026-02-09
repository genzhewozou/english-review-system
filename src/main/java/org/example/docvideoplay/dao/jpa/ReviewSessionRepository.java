package org.example.docvideoplay.dao.jpa;

import org.example.docvideoplay.entity.ReviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ReviewSession entity
 * Provides data access methods for review session management
 */
@Repository
public interface ReviewSessionRepository extends JpaRepository<ReviewSession, Long> {
    
    /**
     * Find all incomplete review sessions
     * @return list of review sessions that are not completed
     */
    List<ReviewSession> findByCompletedFalseOrderByStartTimeDesc();
    
    /**
     * Find all completed review sessions ordered by start time (newest first)
     * @return list of completed review sessions
     */
    List<ReviewSession> findByCompletedTrueOrderByStartTimeDesc();
    
    /**
     * Find the most recent incomplete review session
     * @return optional of the most recent incomplete session
     */
    Optional<ReviewSession> findFirstByCompletedFalseOrderByStartTimeDesc();
    
    /**
     * Find review sessions within a date range
     * @param startTime the start of the date range
     * @param endTime the end of the date range
     * @return list of review sessions within the specified time range
     */
    List<ReviewSession> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find review sessions started today
     * @return list of review sessions started today
     */
    @Query("SELECT rs FROM ReviewSession rs WHERE DATE(rs.startTime) = CURRENT_DATE ORDER BY rs.startTime DESC")
    List<ReviewSession> findSessionsStartedToday();
    
    /**
     * Find review sessions by completion status
     * @param completed whether to find completed or incomplete sessions
     * @return list of review sessions
     */
    List<ReviewSession> findByCompletedOrderByStartTimeDesc(Boolean completed);
    
    /**
     * Count review sessions by completion status
     * @param completed the completion status
     * @return count of sessions with the specified completion status
     */
    long countByCompleted(Boolean completed);
    
    /**
     * Find sessions with accuracy above a threshold
     * @param minCorrectAnswers minimum number of correct answers
     * @param minTotalQuestions minimum total questions to consider
     * @return list of sessions meeting the criteria
     */
    @Query("SELECT rs FROM ReviewSession rs WHERE rs.completed = true AND rs.totalQuestions >= :minTotalQuestions AND (rs.correctAnswers * 100.0 / rs.totalQuestions) >= :minAccuracy ORDER BY rs.startTime DESC")
    List<ReviewSession> findSessionsWithMinimumAccuracy(@Param("minAccuracy") Double minAccuracy, @Param("minTotalQuestions") Integer minTotalQuestions);
    
    /**
     * Get average accuracy for completed sessions
     * @return average accuracy percentage
     */
    @Query("SELECT AVG(rs.correctAnswers * 100.0 / rs.totalQuestions) FROM ReviewSession rs WHERE rs.completed = true AND rs.totalQuestions > 0")
    Double getAverageAccuracy();
    
    /**
     * Find sessions by date range with statistics
     * @param startDate start date for the range
     * @param endDate end date for the range
     * @return list of sessions with basic statistics
     */
    @Query("SELECT rs FROM ReviewSession rs WHERE rs.startTime >= :startDate AND rs.startTime <= :endDate ORDER BY rs.startTime DESC")
    List<ReviewSession> findSessionsInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // User-based queries
    List<ReviewSession> findByUserIdOrderByStartTimeDesc(Long userId);
    List<ReviewSession> findByUserIdAndCompletedFalseOrderByStartTimeDesc(Long userId);
    List<ReviewSession> findByUserIdAndCompletedTrueOrderByStartTimeDesc(Long userId);
    List<ReviewSession> findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    @Query("SELECT rs FROM ReviewSession rs WHERE rs.userId = :userId AND DATE(rs.startTime) = CURRENT_DATE ORDER BY rs.startTime DESC")
    List<ReviewSession> findSessionsStartedTodayByUserId(@Param("userId") Long userId);
    long countByUserIdAndCompleted(Long userId, Boolean completed);
    @Query("SELECT AVG(rs.correctAnswers * 100.0 / rs.totalQuestions) FROM ReviewSession rs WHERE rs.userId = :userId AND rs.completed = true AND rs.totalQuestions > 0")
    Double getAverageAccuracyByUserId(@Param("userId") Long userId);
    @Query("SELECT rs FROM ReviewSession rs WHERE rs.userId = :userId AND rs.completed = true AND rs.totalQuestions >= :minTotalQuestions AND (rs.correctAnswers * 100.0 / rs.totalQuestions) >= :minAccuracy ORDER BY rs.startTime DESC")
    List<ReviewSession> findSessionsWithMinimumAccuracyByUserId(@Param("userId") Long userId, @Param("minAccuracy") Double minAccuracy, @Param("minTotalQuestions") Integer minTotalQuestions);
    @Query("SELECT rs FROM ReviewSession rs WHERE rs.userId = :userId AND rs.startTime >= :startDate AND rs.startTime <= :endDate ORDER BY rs.startTime DESC")
    List<ReviewSession> findSessionsInDateRangeByUserId(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}