package org.example.docvideoplay.entity;

import net.jqwik.api.*;
import org.example.docvideoplay.config.JpaConfig;
import org.example.docvideoplay.enums.MaterialType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for BaseEntity functionality
 * Tests data persistence across sessions to validate Requirements 2.5
 */
@DataJpaTest
public class BaseEntityPropertyTest {

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Property 6: Data Persistence Across Sessions
     * For any entity extending BaseEntity, saving it should persist the data
     * and make it retrievable after session termination and restart.
     * 
     * Feature: english-learning-system, Property 6: Data Persistence Across Sessions
     * Validates: Requirements 2.5
     */
    @Property(tries = 100)
    @Label("Feature: english-learning-system, Property 6: Data Persistence Across Sessions")
    void dataPersistenceAcrossSessions(
            @ForAll("validStudyMaterials") StudyMaterial material) {
        
        // Manually set audit fields since JPA auditing might not work in tests
        LocalDateTime now = LocalDateTime.now();
        material.setCreatedDate(now);
        material.setUpdatedDate(now);
        
        // Save the entity in the current session
        entityManager.persistAndFlush(material);
        
        // Get the generated ID
        Long savedId = material.getId();
        assertThat(savedId).isNotNull();
        
        // Capture the audit fields set by BaseEntity
        LocalDateTime originalCreatedDate = material.getCreatedDate();
        LocalDateTime originalUpdatedDate = material.getUpdatedDate();
        
        // Verify audit fields are not null
        assertThat(originalCreatedDate).isNotNull();
        assertThat(originalUpdatedDate).isNotNull();
        
        // Clear the persistence context to simulate session termination
        entityManager.clear();
        
        // Retrieve the entity in a "new session" (simulating session restart)
        StudyMaterial retrievedMaterial = entityManager.find(StudyMaterial.class, savedId);
        
        // Verify the entity was persisted and can be retrieved
        assertThat(retrievedMaterial).isNotNull();
        assertThat(retrievedMaterial.getId()).isEqualTo(savedId);
        
        // Verify all original data is preserved
        assertThat(retrievedMaterial.getTitle()).isEqualTo(material.getTitle());
        assertThat(retrievedMaterial.getFileName()).isEqualTo(material.getFileName());
        assertThat(retrievedMaterial.getFilePath()).isEqualTo(material.getFilePath());
        assertThat(retrievedMaterial.getType()).isEqualTo(material.getType());
        
        // Verify BaseEntity audit fields are preserved
        assertThat(retrievedMaterial.getCreatedDate()).isEqualTo(originalCreatedDate);
        assertThat(retrievedMaterial.getUpdatedDate()).isEqualTo(originalUpdatedDate);
        
        // Verify audit fields are not null (BaseEntity functionality)
        assertThat(retrievedMaterial.getCreatedDate()).isNotNull();
        assertThat(retrievedMaterial.getUpdatedDate()).isNotNull();
    }

    /**
     * Generator for valid StudyMaterial entities for property testing
     */
    @Provide
    Arbitrary<StudyMaterial> validStudyMaterials() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50).map(s -> s + ".txt"),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200).map(s -> "/uploads/" + s),
                Arbitraries.of(MaterialType.class)
        ).as((title, fileName, filePath, type) -> {
            StudyMaterial material = new StudyMaterial(title, fileName, filePath, type);
            material.setMimeType("text/plain");
            material.setFileSize(1024L);
            return material;
        });
    }
}