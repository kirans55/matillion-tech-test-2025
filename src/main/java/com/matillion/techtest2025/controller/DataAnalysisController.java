package com.matillion.techtest2025.controller;

import com.matillion.techtest2025.controller.response.DataAnalysisResponse;
import com.matillion.techtest2025.repository.DataAnalysisRepository;
import com.matillion.techtest2025.repository.entity.DataAnalysisEntity;
import com.matillion.techtest2025.service.DataAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class DataAnalysisController {

    private final DataAnalysisService dataAnalysisService;
    private final DataAnalysisRepository dataAnalysisRepository;

    // ✅ POST /api/analysis/ingestCsv
    @PostMapping(value = "/ingestCsv", consumes = "multipart/form-data")
    public ResponseEntity<DataAnalysisResponse> ingestCsv(@RequestParam("file") MultipartFile file) {
      try {
         String csvData = new String(file.getBytes());
         DataAnalysisResponse response = dataAnalysisService.analyzeCsvData(csvData);
         return ResponseEntity.ok(response);
      } catch (Exception e) {
         return ResponseEntity.badRequest().build();
     }
  }


    // ✅ GET /api/analysis/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DataAnalysisResponse> getAnalysisById(@PathVariable Long id) {
        return dataAnalysisRepository.findById(id)
                .map(entity -> ResponseEntity.ok(DataAnalysisResponse.fromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ DELETE /api/analysis/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysisById(@PathVariable Long id) {
        if (!dataAnalysisRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        dataAnalysisRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
