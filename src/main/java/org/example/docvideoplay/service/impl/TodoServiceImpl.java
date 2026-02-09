package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.dao.jpa.TodoItemRepository;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.ReviewSession;
import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.enums.TodoType;
import org.example.docvideoplay.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TodoService for managing todo items and review scheduling.
 * Handles automatic scheduling for spaced repetition and synchronization with review sessions.
 */
@Service
@Transactional
public class TodoServiceImpl implements TodoService {
    
    private final TodoItemRepository todoItemRepository;
    
    @Autowired
    public TodoServiceImpl(TodoItemRepository todoItemRepository) {
        this.todoItemRepository = todoItemRepository;
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type) {
        TodoItem todoItem = new TodoItem(title, description, dueDate, type);
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Long userId) {
        TodoItem todoItem = new TodoItem(userId, title, description, dueDate, type);
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Card relatedCard) {
        TodoItem todoItem = new TodoItem(title, description, dueDate, type, relatedCard.getId());
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Card relatedCard, Long userId) {
        TodoItem todoItem = new TodoItem(userId, title, description, dueDate, type, relatedCard.getId());
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, ReviewSession relatedSession) {
        TodoItem todoItem = new TodoItem(title, description, dueDate, type, relatedSession.getId());
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, ReviewSession relatedSession, Long userId) {
        TodoItem todoItem = new TodoItem(userId, title, description, dueDate, type, relatedSession.getId());
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getAllIncompleteTodoItems() {
        return todoItemRepository.findByCompletedFalseOrderByDueDateAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getAllIncompleteTodoItems(Long userId) {
        return todoItemRepository.findByUserIdAndCompletedFalseOrderByDueDateAsc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getAllCompletedTodoItems() {
        return todoItemRepository.findByCompletedTrueOrderByUpdatedDateDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getAllCompletedTodoItems(Long userId) {
        return todoItemRepository.findByUserIdAndCompletedTrueOrderByUpdatedDateDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsByType(TodoType type, Boolean completed) {
        return todoItemRepository.findByTypeAndCompletedOrderByDueDateAsc(type, completed);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsByType(TodoType type, Boolean completed, Long userId) {
        return todoItemRepository.findByUserIdAndTypeAndCompletedOrderByDueDateAsc(userId, type, completed);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueToday() {
        return todoItemRepository.findTodoItemsDueToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueToday(Long userId) {
        return todoItemRepository.findTodoItemsDueTodayByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getOverdueTodoItems() {
        return todoItemRepository.findOverdueTodoItems();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getOverdueTodoItems(Long userId) {
        return todoItemRepository.findOverdueTodoItemsByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueInNextDays(int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        return todoItemRepository.findTodoItemsDueInNextDays(endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueInNextDays(int days, Long userId) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        return todoItemRepository.findTodoItemsDueInNextDaysByUserId(userId, endDate);
    }
    
    @Override
    public TodoItem completeTodoItem(Long todoItemId) {
        Optional<TodoItem> todoItemOpt = todoItemRepository.findById(todoItemId);
        if (todoItemOpt.isPresent()) {
            TodoItem todoItem = todoItemOpt.get();
            todoItem.markCompleted();
            return todoItemRepository.save(todoItem);
        }
        throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
    }
    
    @Override
    public TodoItem completeTodoItem(Long todoItemId, Long userId) {
        Optional<TodoItem> todoItemOpt = todoItemRepository.findById(todoItemId);
        if (todoItemOpt.isPresent()) {
            TodoItem todoItem = todoItemOpt.get();
            if (!todoItem.getUserId().equals(userId)) {
                throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
            }
            todoItem.markCompleted();
            return todoItemRepository.save(todoItem);
        }
        throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
    }
    
    @Override
    public TodoItem updateTodoItem(Long todoItemId, String title, String description, LocalDate dueDate) {
        Optional<TodoItem> todoItemOpt = todoItemRepository.findById(todoItemId);
        if (todoItemOpt.isPresent()) {
            TodoItem todoItem = todoItemOpt.get();
            todoItem.setTitle(title);
            todoItem.setDescription(description);
            todoItem.setDueDate(dueDate);
            return todoItemRepository.save(todoItem);
        }
        throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
    }
    
    @Override
    public TodoItem updateTodoItem(Long todoItemId, String title, String description, LocalDate dueDate, Long userId) {
        Optional<TodoItem> todoItemOpt = todoItemRepository.findById(todoItemId);
        if (todoItemOpt.isPresent()) {
            TodoItem todoItem = todoItemOpt.get();
            if (!todoItem.getUserId().equals(userId)) {
                throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
            }
            todoItem.setTitle(title);
            todoItem.setDescription(description);
            todoItem.setDueDate(dueDate);
            return todoItemRepository.save(todoItem);
        }
        throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
    }
    
    @Override
    public void deleteTodoItem(Long todoItemId) {
        if (todoItemRepository.existsById(todoItemId)) {
            todoItemRepository.deleteById(todoItemId);
        } else {
            throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
        }
    }
    
    @Override
    public void deleteTodoItem(Long todoItemId, Long userId) {
        Optional<TodoItem> todoItemOpt = todoItemRepository.findById(todoItemId);
        if (todoItemOpt.isPresent()) {
            TodoItem todoItem = todoItemOpt.get();
            if (!todoItem.getUserId().equals(userId)) {
                throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
            }
            todoItemRepository.deleteById(todoItemId);
        } else {
            throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
        }
    }
    
    @Override
    public void scheduleReviewReminder(Card card) {
        if (card.getNextReviewDate() == null || card.getId() == null || card.getUserId() == null) {
            return; // No review date set, card not persisted, or no user, cannot schedule
        }
        
        // Check if there's already a todo item for this card's next review
        List<TodoItem> existingTodos = todoItemRepository.findByRelatedCardIdOrderByDueDateAsc(card.getId());
        boolean hasExistingReviewTodo = existingTodos.stream()
                .anyMatch(todo -> todo.getType() == TodoType.REVIEW_SESSION && 
                                !todo.getCompleted() && 
                                todo.getDueDate() != null &&
                                todo.getDueDate().equals(card.getNextReviewDate()));
        
        if (!hasExistingReviewTodo) {
            String title = "Review: " + (card.getText().length() > 30 ? 
                    card.getText().substring(0, 30) + "..." : card.getText());
            String description = "Review the word/phrase: \"" + card.getText() + "\"";
            if (card.getContext() != null && !card.getContext().trim().isEmpty()) {
                description += "\nContext: " + card.getContext();
            }
            
            createTodoItem(title, description, card.getNextReviewDate(), TodoType.REVIEW_SESSION, card, card.getUserId());
        }
    }
    
    @Override
    public void synchronizeWithReviewCompletion(Card card) {
        // Find all incomplete review session todo items for this card
        List<TodoItem> reviewTodos = todoItemRepository.findByRelatedCardIdOrderByDueDateAsc(card.getId());
        
        for (TodoItem todo : reviewTodos) {
            if (todo.getType() == TodoType.REVIEW_SESSION && !todo.getCompleted()) {
                // If the card was reviewed (has a lastReviewDate) and the todo is due on or before today,
                // mark it as completed
                if (card.getLastReviewDate() != null && 
                    todo.getDueDate() != null && 
                    !todo.getDueDate().isAfter(LocalDate.now())) {
                    todo.markCompleted();
                    todoItemRepository.save(todo);
                }
            }
        }
        
        // Schedule the next review reminder if there's a next review date
        if (card.getNextReviewDate() != null && card.getNextReviewDate().isAfter(LocalDate.now())) {
            scheduleReviewReminder(card);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsByCard(Long cardId) {
        return todoItemRepository.findByRelatedCardIdOrderByDueDateAsc(cardId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsByCard(Long cardId, Long userId) {
        return todoItemRepository.findByUserIdAndRelatedCardIdOrderByDueDateAsc(userId, cardId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTodoItemsByStatus(Boolean completed) {
        return todoItemRepository.countByCompleted(completed);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTodoItemsByStatus(Boolean completed, Long userId) {
        return todoItemRepository.countByUserIdAndCompleted(userId, completed);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countOverdueTodoItems() {
        return todoItemRepository.countOverdueTodoItems();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countOverdueTodoItems(Long userId) {
        return todoItemRepository.countOverdueTodoItemsByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> searchTodoItemsByTitle(String searchTerm) {
        return todoItemRepository.findByTitleContainingIgnoreCaseOrderByDueDateAsc(searchTerm);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> searchTodoItemsByTitle(String searchTerm, Long userId) {
        return todoItemRepository.findByUserIdAndTitleContainingIgnoreCaseOrderByDueDateAsc(userId, searchTerm);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueBetween(LocalDate startDate, LocalDate endDate) {
        return todoItemRepository.findTodoItemsDueBetween(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueBetween(LocalDate startDate, LocalDate endDate, Long userId) {
        return todoItemRepository.findTodoItemsDueBetweenByUserId(userId, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getReviewSessionTodoItems() {
        return todoItemRepository.findReviewSessionTodoItems();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getReviewSessionTodoItems(Long userId) {
        return todoItemRepository.findReviewSessionTodoItemsByUserId(userId);
    }
}
