-- Add indexes for foreign key columns that lost their constraints

-- StudyMaterial
CREATE INDEX idx_study_materials_user_id ON study_materials(user_id);

-- Card
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_material_id ON cards(material_id);
CREATE INDEX idx_cards_deck_id ON cards(deck_id);

-- ReviewRecord
CREATE INDEX idx_review_records_session_id ON review_records(session_id);
CREATE INDEX idx_review_records_card_id ON review_records(card_id);

-- Deck
CREATE INDEX idx_decks_user_id ON decks(user_id);

-- ReviewSession
CREATE INDEX idx_review_sessions_user_id ON review_sessions(user_id);

-- TodoItem
CREATE INDEX idx_todo_items_user_id ON todo_items(user_id);
CREATE INDEX idx_todo_items_related_card_id ON todo_items(related_card_id);
CREATE INDEX idx_todo_items_related_session_id ON todo_items(related_session_id);

-- CardTemplate
CREATE INDEX idx_card_templates_user_id ON card_templates(user_id);

-- Tag
CREATE INDEX idx_tags_user_id ON tags(user_id);

-- ReviewSession ElementCollection
CREATE INDEX idx_review_session_cards_session_id ON review_session_cards(session_id);
