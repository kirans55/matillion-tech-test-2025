package com.matillion.techtest2025.service;

import com.matillion.techtest2025.controller.response.DataAnalysisResponse;
import com.matillion.techtest2025.model.ColumnStatistics;
import com.matillion.techtest2025.repository.ColumnStatisticsRepository;
import com.matillion.techtest2025.repository.DataAnalysisRepository;
import com.matillion.techtest2025.repository.entity.ColumnStatisticsEntity;
import com.matillion.techtest2025.repository.entity.DataAnalysisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Service layer containing business logic for data analysis.
 * <p>
 * Responsible for parsing data, calculating statistics, and persisting results.
 */
@Service
@RequiredArgsConstructor
public class DataAnalysisService {

    private final DataAnalysisRepository dataAnalysisRepository;
    private final ColumnStatisticsRepository columnStatisticsRepository;

    /**
     * Analyzes CSV data and returns statistics.
     * <p>
     * Parses the CSV, calculates statistics (row count, column count, character count,
     * null counts per column), persists the results to the database, and returns the analysis.
     * <p>
     * <b>Note:</b> Current implementation is incomplete. Part 1 of the tech test
     * requires implementing the CSV parsing and analysis logic.
     *
     * @param data raw CSV data (rows separated by newlines, columns by commas)
     * @return analysis results
     */
 public DataAnalysisResponse analyzeCsvData(String data) {
    // 🔒 1. Reject completely empty CSVs
    if (data == null || data.trim().isEmpty()) {
        throw new IllegalArgumentException("CSV data cannot be empty");
    }

    // 🔒 2. Reject obviously invalid CSVs (missing commas)
    if (!data.contains(",")) {
        throw new IllegalArgumentException("Invalid CSV format");
    }

    // 🔒 3. Reject forbidden content ("Sonny Hayes")
    if (data.toLowerCase().contains("sonny hayes")) {
        throw new IllegalArgumentException("Forbidden content detected");
    }

    // ✅ Continue with normal parsing logic


    // Split into lines, trimming whitespace
    List<String> lines = data.lines()
            .filter(line -> !line.trim().isEmpty())
            .toList();

    if (lines.isEmpty()) {
        // no data at all
        DataAnalysisEntity emptyEntity = DataAnalysisEntity.builder()
                .originalData(data)
                .numberOfRows(0)
                .numberOfColumns(0)
                .totalCharacters(0)
                .createdAt(OffsetDateTime.now())
                .build();

        dataAnalysisRepository.save(emptyEntity);
        return new DataAnalysisResponse(0, 0, 0, List.of(), emptyEntity.getCreatedAt());
    }

    // Parse header
    String header = lines.get(0);
    String[] columns = header.split(",");
    int numberOfColumns = columns.length;

    // Parse data rows
    List<String[]> rows = new ArrayList<>();
    for (int i = 1; i < lines.size(); i++) {
        rows.add(lines.get(i).split(",", -1)); // keep empty values
    }
    int numberOfRows = rows.size();

    long totalCharacters = data.length();
    // ✅ Validate structure consistency: all rows must match header length
 // ✅ Validate structure consistency: all rows must match header length
    boolean invalidStructure = rows.stream().anyMatch(r -> r.length != columns.length);
    if (invalidStructure) {
    throw new IllegalArgumentException("Invalid CSV structure: inconsistent number of columns");
    }

    // Create entity
    DataAnalysisEntity entity = DataAnalysisEntity.builder()
            .originalData(data)
            .numberOfRows(numberOfRows)
            .numberOfColumns(numberOfColumns)
            .totalCharacters(totalCharacters)
            .createdAt(OffsetDateTime.now())
            .build();

    List<ColumnStatisticsEntity> columnStatsList = new ArrayList<>();

    // Analyze each column
    for (int i = 0; i < numberOfColumns; i++) {
        final int index = i;
        String columnName = columns[index].trim();

        List<String> columnValues = rows.stream()
                .map(row -> index < row.length ? row[index].trim() : "")
                .toList();

        // Null count (empty or missing values)
        long nullCount = columnValues.stream()
                .filter(v -> v == null || v.isBlank())
                .count();

        // Unique non-null values
        Set<String> uniqueValues = columnValues.stream()
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toSet());
        int uniqueCount = uniqueValues.size();

        ColumnStatisticsEntity stat = ColumnStatisticsEntity.builder()
                .dataAnalysis(entity)
                .columnName(columnName)
                .nullCount((int) nullCount)
                .uniqueCount(uniqueCount)
                .build();

        columnStatsList.add(stat);
    }

    entity.setColumnStatistics(columnStatsList);
    dataAnalysisRepository.save(entity);

    // Map entities to DTOs
    List<com.matillion.techtest2025.model.ColumnStatistics> dtoStats = columnStatsList.stream()
            .map(cs -> new com.matillion.techtest2025.model.ColumnStatistics(
                    cs.getColumnName(),
                    cs.getNullCount(),
                    cs.getUniqueCount()
            ))
            .toList();

    // Return response
    return new DataAnalysisResponse(
            numberOfRows,
            numberOfColumns,
            totalCharacters,
            dtoStats,
            entity.getCreatedAt()
    );
  }
}