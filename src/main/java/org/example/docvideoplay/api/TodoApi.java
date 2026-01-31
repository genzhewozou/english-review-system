package org.example.docvideoplay.api;

import org.example.docvideoplay.dto.api.TodoItemParamsDto;
import org.example.docvideoplay.dto.api.TodoItemResultDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API interface for todo list and task management operations
 * Handles todo item creation, completion, and listing
 */
@RequestMapping("/api/todos")
public interface TodoApi {
    
    /**
     * Get all todo items
     * 
     * @param completed Optional filter for completed todos (true/false)
     * @param overdue Optional filter for overdue todos (true/false)
     * @return ResponseEntity containing list of todo items
     */
    @GetMapping
    ResponseEntity<List<TodoItemResultDto>> getTodoItems(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Boolean overdue
    );
    
    /**
     * Create a new todo item
     * 
     * @param params Todo item parameters (title, description, dueDate, type, relatedHighlightId)
     * @return ResponseEntity containing the created todo item details
     */
    @PostMapping
    ResponseEntity<TodoItemResultDto> createTodoItem(@Valid @RequestBody TodoItemParamsDto params);
    
    /**
     * Get a specific todo item by ID
     * 
     * @param id The todo item ID
     * @return ResponseEntity containing the todo item details
     */
    @GetMapping("/{id}")
    ResponseEntity<TodoItemResultDto> getTodoItem(@PathVariable Long id);
    
    /**
     * Update a todo item
     * 
     * @param id The todo item ID
     * @param params Updated todo item parameters
     * @return ResponseEntity containing the updated todo item details
     */
    @PutMapping("/{id}")
    ResponseEntity<TodoItemResultDto> updateTodoItem(
            @PathVariable Long id, 
            @Valid @RequestBody TodoItemParamsDto params
    );
    
    /**
     * Mark a todo item as completed
     * 
     * @param id The todo item ID to complete
     * @return ResponseEntity containing the updated todo item details
     */
    @PostMapping("/{id}/complete")
    ResponseEntity<TodoItemResultDto> completeTodoItem(@PathVariable Long id);
    
    /**
     * Delete a todo item by ID
     * 
     * @param id The todo item ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTodoItem(@PathVariable Long id);
    
    /**
     * Get todo items due today
     * 
     * @return ResponseEntity containing list of todo items due today
     */
    @GetMapping("/due-today")
    ResponseEntity<List<TodoItemResultDto>> getTodoItemsDueToday();
    
    /**
     * Get overdue todo items
     * 
     * @return ResponseEntity containing list of overdue todo items
     */
    @GetMapping("/overdue")
    ResponseEntity<List<TodoItemResultDto>> getOverdueTodoItems();
}