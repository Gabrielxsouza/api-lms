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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/turmas")
@Tag(name = "Turmas", description = "Endpoints para gerenciar turmas (vinculadas a disciplinas)")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @Operation(
        summary = "Criar nova turma avulsa",
        description = "Cria uma nova turma vinculada a uma disciplina existente (pelo idDisciplina)."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Turma criada com sucesso",
        content = @Content(schema = @Schema(implementation = TurmaResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Disciplina (pai) não encontrada")
    @PostMapping
    public ResponseEntity<TurmaResponseDto> create(
            @Valid @RequestBody TurmaRequestDto requestDto) {
        
        TurmaResponseDto createdTurma = turmaService.createTurma(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTurma);
    }

    @Operation(
        summary = "Listar todas as turmas",
        description = "Retorna uma lista paginada de todas as turmas cadastradas."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de turmas")
    @GetMapping
    public ResponseEntity<PagedResponse<TurmaResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(turmaService.getAllTurmas(pageable));
    }

    @Operation(summary = "Atualizar uma turma (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Turma atualizada com sucesso",
        content = @Content(schema = @Schema(implementation = TurmaResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Turma não encontrada")
    @PatchMapping("/{id}")
    public ResponseEntity<TurmaResponseDto> update(
            @Parameter(description = "ID da turma a ser atualizada") @PathVariable Long id, 
            @Valid @RequestBody TurmaUpdateDto updateDto) {
        
        TurmaResponseDto updatedTurma = turmaService.updateTurma(id, updateDto);
        return ResponseEntity.ok(updatedTurma);
    }

    @Operation(summary = "Deletar uma turma")
    @ApiResponse(responseCode = "204", description = "Turma deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Turma não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da turma a ser deletada") @PathVariable Long id) {
        turmaService.deleteTurma(id);
        return ResponseEntity.noContent().build();
    }
}