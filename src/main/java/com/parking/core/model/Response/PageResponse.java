package com.parking.core.model.Response;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int currentPage,
    int totalPages,
    long total
    ) {}
