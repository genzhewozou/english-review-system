package org.example.docvideoplay.repository;

import org.example.docvideoplay.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByUserId(Long userId);
    List<Deck> findByIsPublicTrue();
    List<Deck> findByUserIdAndIsPublicTrue(Long userId);
    Deck findByIdAndUserId(Long id, Long userId);
    Deck findByIdAndIsPublicTrue(Long id);
}