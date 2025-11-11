package br.ifsp.lms_api.controller;

import br.ifsp.lms_api.dto.professorDto.ProfessorRequestDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorResponseDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.ProfessorService;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/professores")
public class ProfessorController {

    private final ProfessorService professorService;

    // Injeção de dependência via construtor
    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PostMapping
    public ResponseEntity<ProfessorResponseDto> create(
            @Valid @RequestBody ProfessorRequestDto requestDto) {
        
        ProfessorResponseDto responseDto = professorService.createProfessor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProfessorResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(professorService.getAllProfessores(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessorResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(professorService.getProfessorById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProfessorResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody ProfessorUpdateDto updateDto) {
        
        ProfessorResponseDto responseDto = professorService.updateProfessor(id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
}