package br.ifsp.lms_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.service.DisciplinaService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/disciplina")
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
}
