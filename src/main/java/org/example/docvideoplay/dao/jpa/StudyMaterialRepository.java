package org.example.docvideoplay.dao.jpa;

import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.enums.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for StudyMaterial entity
 * Provides data access methods for study material management
 */
@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    
    /**
     * Find all materials by type, ordered by creation date (newest first)
     * @param type the material type to filter by
     * @return list of materials of the specified type
     */
    List<StudyMaterial> findByTypeOrderByCreatedDateDesc(MaterialType type);
    
    /**
     * Find all materials ordered by creation date (newest first)
     * @return list of all materials ordered by creation date
     */
    List<StudyMaterial> findAllByOrderByCreatedDateDesc();
    
    /**
     * Find materials by title containing the search term (case insensitive)
     * @param title the search term for title
     * @return list of materials matching the title search
     */
    List<StudyMaterial> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find materials by file name containing the search term (case insensitive)
     * @param fileName the search term for file name
     * @return list of materials matching the file name search
     */
    List<StudyMaterial> findByFileNameContainingIgnoreCase(String fileName);
    
    /**
     * Find material by file path (exact match)
     * @param filePath the exact file path
     * @return optional material with the specified file path
     */
    Optional<StudyMaterial> findByFilePath(String filePath);
    
    /**
     * Count materials by type
     * @param type the material type
     * @return count of materials of the specified type
     */
    long countByType(MaterialType type);
    
    /**
     * Find materials with highlights count using custom query
     * @return list of materials with their highlight counts
     */
    @Query("SELECT sm FROM StudyMaterial sm LEFT JOIN FETCH sm.highlights WHERE SIZE(sm.highlights) > 0 ORDER BY sm.createdDate DESC")
    List<StudyMaterial> findMaterialsWithHighlights();
    
    /**
     * Find materials by type with highlights eagerly loaded
     * @param type the material type
     * @return list of materials with highlights loaded
     */
    @Query("SELECT DISTINCT sm FROM StudyMaterial sm LEFT JOIN FETCH sm.highlights WHERE sm.type = :type ORDER BY sm.createdDate DESC")
    List<StudyMaterial> findByTypeWithHighlights(@Param("type") MaterialType type);
}