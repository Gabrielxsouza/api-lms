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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/disciplinas")
@Tag(name = "Disciplinas", description = "Endpoints para gerenciar disciplinas e suas turmas")
public class DisciplinaController {
    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @Operation(
        summary = "Criar nova disciplina",
        description = "Cria uma nova disciplina e, opcionalmente, cria turmas aninhadas na mesma requisição."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Disciplina criada com sucesso",
        content = @Content(schema = @Schema(implementation = DisciplinaResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida (ex: campos obrigatórios faltando)")
    @PostMapping
    public ResponseEntity<DisciplinaResponseDto> createDisciplina(@Valid @RequestBody DisciplinaRequestDto disciplina) {
        DisciplinaResponseDto createdDisciplina = disciplinaService.createDisciplina(disciplina);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDisciplina);
    }

    @Operation(
        summary = "Listar todas as disciplinas",
        description = "Retorna uma lista paginada de todas as disciplinas."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de disciplinas")
    @GetMapping
    public ResponseEntity<PagedResponse<DisciplinaResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(disciplinaService.getAllDisciplinas(pageable));
    }

    @Operation(summary = "Atualizar uma disciplina (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Disciplina atualizada com sucesso",
        content = @Content(schema = @Schema(implementation = DisciplinaResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Disciplina não encontrada")
    @PatchMapping("/{id}")
    public ResponseEntity<DisciplinaResponseDto> update(
            @Parameter(description = "ID da disciplina a ser atualizada") @PathVariable Long id, 
            @Valid @RequestBody DisciplinaUpdateDto updateDto) {
        
        DisciplinaResponseDto updatedDisciplina = disciplinaService.updateDisciplina(id, updateDto);
        return ResponseEntity.ok(updatedDisciplina);
    }

    @Operation(summary = "Deletar uma disciplina")
    @ApiResponse(responseCode = "204", description = "Disciplina deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Disciplina não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da disciplina a ser deletada") @PathVariable Long id) {
        disciplinaService.deleteDisciplina(id);
        return ResponseEntity.noContent().build();
    }
}