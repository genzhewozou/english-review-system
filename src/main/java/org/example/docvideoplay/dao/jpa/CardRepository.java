package org.example.docvideoplay.dao.jpa;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository for Card entity
 * Provides data access methods for card management and spaced repetition queries
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    /**
     * Find all cards for a specific material, ordered by position
     * @param materialId the ID of the study material
     * @return list of cards ordered by position
     */
    List<Card> findByMaterialIdOrderByStartPositionAsc(Long materialId);
    
    /**
     * Find cards due for review on or before the specified date
     * @param date the review date threshold
     * @return list of cards due for review
     */
    List<Card> findByNextReviewDateLessThanEqual(LocalDate date);
    
    /**
     * Find cards due for review today
     * @return list of cards due for review today
     */
    @Query("SELECT c FROM Card c WHERE c.nextReviewDate <= CURRENT_DATE ORDER BY c.nextReviewDate ASC")
    List<Card> findCardsDueToday();
    
    /**
     * Find overdue cards (past due date)
     * @return list of overdue cards
     */
    @Query("SELECT c FROM Card c WHERE c.nextReviewDate < CURRENT_DATE ORDER BY c.nextReviewDate ASC")
    List<Card> findOverdueCards();
    
    /**
     * Find cards by material ID
     * @param materialId the ID of the study material
     * @return list of cards for the material
     */
    List<Card> findByMaterialId(Long materialId);
    
    /**
     * Find cards that have never been reviewed
     * @return list of cards with no review history
     */
    @Query("SELECT c FROM Card c WHERE c.lastReviewDate IS NULL ORDER BY c.createdDate ASC")
    List<Card> findNeverReviewedCards();
    
    /**
     * Find cards by text content (case insensitive search)
     * @param text the text to search for
     * @return list of cards containing the specified text
     */
    List<Card> findByTextContainingIgnoreCase(String text);
    
    /**
     * Find cards with user comments
     * @return list of cards that have user comments
     */
    @Query("SELECT c FROM Card c WHERE c.userComment IS NOT NULL AND c.userComment != '' ORDER BY c.createdDate DESC")
    List<Card> findCardsWithComments();
    
    /**
     * Count cards for a specific material
     * @param materialId the ID of the study material
     * @return count of cards for the material
     */
    long countByMaterialId(Long materialId);
    
    /**
     * Find cards due for review between two dates
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of cards due for review in the date range
     */
    @Query("SELECT c FROM Card c WHERE c.nextReviewDate BETWEEN :startDate AND :endDate ORDER BY c.nextReviewDate ASC")
    List<Card> findCardsDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find cards by repetition count (for spaced repetition analysis)
     * @param repetitionCount the number of repetitions
     * @return list of cards with the specified repetition count
     */
    List<Card> findByRepetitionCount(Integer repetitionCount);
    
    /**
     * Find all cards ordered by creation date (newest first)
     * @return list of all cards ordered by creation date descending
     */
    List<Card> findAllByOrderByCreatedDateDesc();
    
    // User-based queries
    List<Card> findByUserId(Long userId);
    List<Card> findByUserIdOrderByCreatedDateDesc(Long userId);
    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.nextReviewDate <= CURRENT_DATE ORDER BY c.nextReviewDate ASC")
    List<Card> findCardsDueTodayByUserId(@Param("userId") Long userId);
    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.nextReviewDate < CURRENT_DATE ORDER BY c.nextReviewDate ASC")
    List<Card> findOverdueCardsByUserId(@Param("userId") Long userId);
    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.lastReviewDate IS NULL ORDER BY c.createdDate ASC")
    List<Card> findNeverReviewedCardsByUserId(@Param("userId") Long userId);
    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.userComment IS NOT NULL AND c.userComment != '' ORDER BY c.createdDate DESC")
    List<Card> findCardsWithCommentsByUserId(@Param("userId") Long userId);
    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.nextReviewDate BETWEEN :startDate AND :endDate ORDER BY c.nextReviewDate ASC")
    List<Card> findCardsDueBetweenByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find cards by deck ID
     * @param deckId the ID of the deck
     * @return list of cards for the deck
     */
    List<Card> findByDeckId(Long deckId);
}