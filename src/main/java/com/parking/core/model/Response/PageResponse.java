package com.parking.core.model.Response;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param content     the list of items in the current page
 * @param currentPage the zero-based current page index
 * @param totalPages  the total number of pages
 * @param total       the total number of items across all pages
 * @param <T>         the type of items in the page
 */
public record PageResponse<T>(
    List<T> content,
    int currentPage,
    int totalPages,
    long total
    ) {}
