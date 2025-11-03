package com.matillion.techtest2025.controller.response;

import com.matillion.techtest2025.model.ColumnStatistics;
import com.matillion.techtest2025.repository.entity.DataAnalysisEntity;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response DTO (Data Transfer Object) for data analysis results.
 */
public record DataAnalysisResponse(
        int numberOfRows,
        int numberOfColumns,
        long totalCharacters,
        List<ColumnStatistics> columnStatistics,
        OffsetDateTime createdAt
) {
    // ✅ Add this static helper so controllers can convert entity -> response easily
    public static DataAnalysisResponse fromEntity(DataAnalysisEntity entity) {
        List<ColumnStatistics> stats = entity.getColumnStatistics().stream()
                .map(cs -> new ColumnStatistics(
                        cs.getColumnName(),
                        cs.getNullCount(),
                        cs.getUniqueCount()
                ))
                .toList();

        return new DataAnalysisResponse(
                entity.getNumberOfRows(),
                entity.getNumberOfColumns(),
                entity.getTotalCharacters(),
                stats,
                entity.getCreatedAt()
        );
    }
}
