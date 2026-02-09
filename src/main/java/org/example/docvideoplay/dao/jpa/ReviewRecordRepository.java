package org.example.docvideoplay.dao.jpa;

import org.example.docvideoplay.entity.ReviewRecord;
import org.example.docvideoplay.enums.AnswerQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository for ReviewRecord entity
 * Provides data access methods for individual review record management
 */
@Repository
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {
    
    /**
     * Find all review records for a specific session
     * @param sessionId the ID of the review session
     * @return list of review records for the session
     */
    List<ReviewRecord> findBySessionIdOrderByReviewTimeAsc(Long sessionId);
    
    /**
     * Find all review records for a specific card
     * @param cardId the ID of the card
     * @return list of review records for the card ordered by review time
     */
    List<ReviewRecord> findByCardIdOrderByReviewTimeDesc(Long cardId);
    
    /**
     * Find review records by answer quality
     * @param quality the answer quality to filter by
     * @return list of review records with the specified quality
     */
    List<ReviewRecord> findByQualityOrderByReviewTimeDesc(AnswerQuality quality);
    
    /**
     * Find review records within a time range
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of review records within the specified time range
     */
    List<ReviewRecord> findByReviewTimeBetweenOrderByReviewTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find the most recent review record for a card
     * @param cardId the ID of the card
     * @return the most recent review record for the card
     */
    @Query("SELECT rr FROM ReviewRecord rr WHERE rr.cardId = :cardId ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findMostRecentByCardIdList(@Param("cardId") Long cardId);
    
    /**
     * Find the most recent review record for a card (convenience method)
     * @param cardId the ID of the card
     * @return the most recent review record for the card, or null if none exists
     */
    default ReviewRecord findMostRecentByCardId(Long cardId) {
        List<ReviewRecord> records = findMostRecentByCardIdList(cardId);
        return records.isEmpty() ? null : records.get(0);
    }
    
    /**
     * Count review records by answer quality
     * @param quality the answer quality
     * @return count of review records with the specified quality
     */
    long countByQuality(AnswerQuality quality);
    
    /**
     * Count review records for a specific card
     * @param cardId the ID of the card
     * @return count of review records for the card
     */
    long countByCardId(Long cardId);
    
    /**
     * Find correct answers (PERFECT, CORRECT, DIFFICULT) for statistics
     * @return list of review records with correct answers
     */
    @Query("SELECT rr FROM ReviewRecord rr WHERE rr.quality IN ('PERFECT', 'CORRECT', 'DIFFICULT') ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findCorrectAnswers();
    
    /**
     * Find incorrect answers (INCORRECT, REMEMBERED, BLACKOUT) for analysis
     * @return list of review records with incorrect answers
     */
    @Query("SELECT rr FROM ReviewRecord rr WHERE rr.quality IN ('INCORRECT', 'REMEMBERED', 'BLACKOUT') ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findIncorrectAnswers();
    
    /**
     * Get average response time for completed reviews
     * @return average response time in seconds
     */
    @Query("SELECT AVG(rr.responseTimeSeconds) FROM ReviewRecord rr WHERE rr.responseTimeSeconds IS NOT NULL")
    Double getAverageResponseTime();
    
    /**
     * Find review records for today
     * @return list of review records created today
     */
    @Query("SELECT rr FROM ReviewRecord rr WHERE DATE(rr.reviewTime) = CURRENT_DATE ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findReviewsToday();
    
    /**
     * Find review records by session and card with details
     * @param sessionId the session ID
     * @param cardId the card ID
     * @return list of review records matching both criteria
     */
    @Query("SELECT rr FROM ReviewRecord rr WHERE rr.sessionId = :sessionId AND rr.cardId = :cardId")
    List<ReviewRecord> findBySessionIdAndCardId(@Param("sessionId") Long sessionId, @Param("cardId") Long cardId);
    
    /**
     * Get quality distribution statistics
     * @return list of quality counts for reporting
     */
    @Query("SELECT rr.quality, COUNT(rr) FROM ReviewRecord rr GROUP BY rr.quality ORDER BY COUNT(rr) DESC")
    List<Object[]> getQualityDistribution();
    
    // User-based queries
    @Query("SELECT rr FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findByUserIdOrderByReviewTimeDesc(@Param("userId") Long userId);
    @Query("SELECT rr FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId AND rr.cardId = :cardId ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findByUserIdAndCardIdOrderByReviewTimeDesc(@Param("userId") Long userId, @Param("cardId") Long cardId);
    @Query("SELECT rr FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId AND rr.reviewTime BETWEEN :startTime AND :endTime ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findByUserIdAndReviewTimeBetweenOrderByReviewTimeDesc(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    @Query("SELECT rr FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId AND rr.quality IN ('PERFECT', 'CORRECT', 'DIFFICULT') ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findCorrectAnswersByUserId(@Param("userId") Long userId);
    @Query("SELECT rr FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId AND rr.quality IN ('INCORRECT', 'REMEMBERED', 'BLACKOUT') ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findIncorrectAnswersByUserId(@Param("userId") Long userId);
    @Query("SELECT AVG(rr.responseTimeSeconds) FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId AND rr.responseTimeSeconds IS NOT NULL")
    Double getAverageResponseTimeByUserId(@Param("userId") Long userId);
    @Query("SELECT rr FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId AND DATE(rr.reviewTime) = CURRENT_DATE ORDER BY rr.reviewTime DESC")
    List<ReviewRecord> findReviewsTodayByUserId(@Param("userId") Long userId);
    @Query("SELECT rr.quality, COUNT(rr) FROM ReviewRecord rr JOIN ReviewSession rs ON rr.sessionId = rs.id WHERE rs.userId = :userId GROUP BY rr.quality ORDER BY COUNT(rr) DESC")
    List<Object[]> getQualityDistributionByUserId(@Param("userId") Long userId);
}