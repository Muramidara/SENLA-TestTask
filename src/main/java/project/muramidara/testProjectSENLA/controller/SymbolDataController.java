package project.muramidara.testProjectSENLA.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.muramidara.testProjectSENLA.dto.ErrorDto;
import project.muramidara.testProjectSENLA.exception.IllegalDateValueException;
import project.muramidara.testProjectSENLA.exception.SymbolDataNotFoundException;
import project.muramidara.testProjectSENLA.service.SymbolDataService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SymbolDataController {
    private final SymbolDataService symbolDataService;

    @GetMapping("/price")
    public ResponseEntity<?> findLast(){
        try{
         var dto = symbolDataService.getLastSymbolData();
            return ResponseEntity.ok(dto);
        } catch (SymbolDataNotFoundException e){
            return ResponseEntity.status(404).body(
                    ErrorDto.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/prices/average")
    public ResponseEntity<?> findAverage(@RequestParam LocalDate from, @RequestParam LocalDate to){
        try{
            var dto = symbolDataService.getAveragePrice(from, to);
            return ResponseEntity.ok(dto);
        } catch (SymbolDataNotFoundException | IllegalDateValueException e){
            return ResponseEntity.status(404).body(
                    ErrorDto.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/prices")
    public ResponseEntity<?> findAllInPeriod(@RequestParam LocalDate from,
                                         @RequestParam LocalDate to,
                                         @RequestParam(required = false, defaultValue = "0") int page,
                                         @RequestParam(required = false, defaultValue = "10") int size){
        try{
            var dto = symbolDataService.getAllSymbolsInPeriod(from, to, page, size);
            return ResponseEntity.ok(dto);
        } catch (IllegalDateValueException e){
            return ResponseEntity.status(404).body(
                    ErrorDto.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/prices/trend")
    public ResponseEntity<?> findTrendInPeriod(@RequestParam LocalDate from,
                                             @RequestParam LocalDate to){
        try{
            var dto = symbolDataService.getTrend(from, to);
            return ResponseEntity.ok(dto);
        } catch (SymbolDataNotFoundException | IllegalDateValueException e){
            return ResponseEntity.status(404).body(
                    ErrorDto.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
