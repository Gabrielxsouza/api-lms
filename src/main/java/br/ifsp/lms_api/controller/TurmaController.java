package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.TurmaService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @PostMapping
    public ResponseEntity<TurmaResponseDto> create(
            @Valid @RequestBody TurmaRequestDto requestDto) {
        
        TurmaResponseDto createdTurma = turmaService.createTurma(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTurma);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TurmaResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(turmaService.getAllTurmas(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TurmaResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody TurmaUpdateDto updateDto) {
        
        TurmaResponseDto updatedTurma = turmaService.updateTurma(id, updateDto);
        return ResponseEntity.ok(updatedTurma);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        turmaService.deleteTurma(id);
        return ResponseEntity.noContent().build();
    }
}