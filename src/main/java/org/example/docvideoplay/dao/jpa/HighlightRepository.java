package org.example.docvideoplay.dao.jpa;

import org.example.docvideoplay.entity.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository for Highlight entity
 * Provides data access methods for highlight management and spaced repetition queries
 */
@Repository
public interface HighlightRepository extends JpaRepository<Highlight, Long> {
    
    /**
     * Find all highlights for a specific material, ordered by position
     * @param materialId the ID of the study material
     * @return list of highlights ordered by position
     */
    List<Highlight> findByMaterialIdOrderByStartPositionAsc(Long materialId);
    
    /**
     * Find highlights due for review on or before the specified date
     * @param date the review date threshold
     * @return list of highlights due for review
     */
    List<Highlight> findByNextReviewDateLessThanEqual(LocalDate date);
    
    /**
     * Find highlights due for review today
     * @return list of highlights due for review today
     */
    @Query("SELECT h FROM Highlight h WHERE h.nextReviewDate <= CURRENT_DATE ORDER BY h.nextReviewDate ASC")
    List<Highlight> findHighlightsDueToday();
    
    /**
     * Find overdue highlights (past due date)
     * @return list of overdue highlights
     */
    @Query("SELECT h FROM Highlight h WHERE h.nextReviewDate < CURRENT_DATE ORDER BY h.nextReviewDate ASC")
    List<Highlight> findOverdueHighlights();
    
    /**
     * Find highlights by material ID with review records eagerly loaded
     * @param materialId the ID of the study material
     * @return list of highlights with review history loaded
     */
    @Query("SELECT DISTINCT h FROM Highlight h LEFT JOIN FETCH h.reviewHistory WHERE h.material.id = :materialId ORDER BY h.startPosition ASC")
    List<Highlight> findByMaterialIdWithReviewHistory(@Param("materialId") Long materialId);
    
    /**
     * Find highlights that have never been reviewed
     * @return list of highlights with no review history
     */
    @Query("SELECT h FROM Highlight h WHERE h.lastReviewDate IS NULL ORDER BY h.createdDate ASC")
    List<Highlight> findNeverReviewedHighlights();
    
    /**
     * Find highlights by text content (case insensitive search)
     * @param text the text to search for
     * @return list of highlights containing the specified text
     */
    List<Highlight> findByTextContainingIgnoreCase(String text);
    
    /**
     * Find highlights with user comments
     * @return list of highlights that have user comments
     */
    @Query("SELECT h FROM Highlight h WHERE h.userComment IS NOT NULL AND h.userComment != '' ORDER BY h.createdDate DESC")
    List<Highlight> findHighlightsWithComments();
    
    /**
     * Count highlights for a specific material
     * @param materialId the ID of the study material
     * @return count of highlights for the material
     */
    long countByMaterialId(Long materialId);
    
    /**
     * Find highlights due for review between two dates
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of highlights due for review in the date range
     */
    @Query("SELECT h FROM Highlight h WHERE h.nextReviewDate BETWEEN :startDate AND :endDate ORDER BY h.nextReviewDate ASC")
    List<Highlight> findHighlightsDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find highlights by repetition count (for spaced repetition analysis)
     * @param repetitionCount the number of repetitions
     * @return list of highlights with the specified repetition count
     */
    List<Highlight> findByRepetitionCount(Integer repetitionCount);
    
    /**
     * Find all highlights ordered by creation date (newest first)
     * @return list of all highlights ordered by creation date descending
     */
    List<Highlight> findAllByOrderByCreatedDateDesc();
}