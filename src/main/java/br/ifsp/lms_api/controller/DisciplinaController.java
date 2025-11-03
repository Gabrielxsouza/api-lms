package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse; 
import br.ifsp.lms_api.service.DisciplinaService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/disciplinas")
public class DisciplinaController {
    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @PostMapping
    public ResponseEntity<DisciplinaResponseDto> createDisciplina(@Valid @RequestBody DisciplinaRequestDto disciplina) {
        DisciplinaResponseDto createdDisciplina = disciplinaService.createDisciplina(disciplina);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDisciplina);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<DisciplinaResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(disciplinaService.getAllDisciplinas(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DisciplinaResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody DisciplinaUpdateDto updateDto) {
        
        DisciplinaResponseDto updatedDisciplina = disciplinaService.updateDisciplina(id, updateDto);
        return ResponseEntity.ok(updatedDisciplina);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        disciplinaService.deleteDisciplina(id);
        return ResponseEntity.noContent().build();
    }
}