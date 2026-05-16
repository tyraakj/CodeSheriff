package com.backend.CodeSheriff.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses
 * Provides pagination metadata along with the data
 * 
 * @author CodeSheriff Team
 * @version 1.0
 * @param <T> The type of data being paginated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponseDTO<T> {
    
    private List<T> content;
    private PaginationMetadata pagination;
    
    /**
     * Pagination metadata
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadata {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }
    
    /**
     * Factory method to create PagedResponseDTO from Spring Data Page
     */
    public static <T> PagedResponseDTO<T> fromPage(Page<T> page) {
        PaginationMetadata metadata = PaginationMetadata.builder()
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
        
        return PagedResponseDTO.<T>builder()
            .content(page.getContent())
            .pagination(metadata)
            .build();
    }
    
    /**
     * Factory method with custom content transformation
     */
    public static <T, R> PagedResponseDTO<R> fromPage(Page<T> page, List<R> transformedContent) {
        PaginationMetadata metadata = PaginationMetadata.builder()
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
        
        return PagedResponseDTO.<R>builder()
            .content(transformedContent)
            .pagination(metadata)
            .build();
    }
    
    /**
     * Create empty paged response
     */
    public static <T> PagedResponseDTO<T> empty() {
        PaginationMetadata metadata = PaginationMetadata.builder()
            .currentPage(0)
            .pageSize(0)
            .totalElements(0)
            .totalPages(0)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();
        
        return PagedResponseDTO.<T>builder()
            .content(List.of())
            .pagination(metadata)
            .build();
    }
}

// Made with Bob
