package br.ifsp.lms_api.controller;

import br.ifsp.lms_api.dto.alunoDto.AlunoRequestDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoResponseDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AlunoService;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    // Injeção de dependência via construtor
    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    public ResponseEntity<AlunoResponseDto> create(
            @Valid @RequestBody AlunoRequestDto requestDto) {
        
        AlunoResponseDto responseDto = alunoService.createAluno(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AlunoResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(alunoService.getAllAlunos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(alunoService.getAlunoById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AlunoResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody AlunoUpdateDto updateDto) {
        
        AlunoResponseDto responseDto = alunoService.updateAluno(id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alunoService.deleteAluno(id);
        return ResponseEntity.noContent().build();
    }
}