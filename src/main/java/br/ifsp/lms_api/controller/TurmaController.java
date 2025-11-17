package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "Turmas", description = "Endpoints para gerenciar turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Criar nova turma",
        description = "Cria uma nova turma vinculada a um curso e disciplina existentes. O professor é pego automaticamente do usuário logado."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Turma criada com sucesso",
        content = @Content(schema = @Schema(implementation = TurmaResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Disciplina ou Curso (pai) não encontrado")
    @PostMapping
    public ResponseEntity<TurmaResponseDto> create(
            @Valid @RequestBody TurmaRequestDto requestDto) {
        
        TurmaResponseDto createdTurma = turmaService.createTurma(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTurma);
    }

    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Listar todas as turmas (Apenas ADMIN)",
        description = "Retorna uma lista paginada de todas as turmas cadastradas no sistema."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de turmas")
    @GetMapping
    public ResponseEntity<PagedResponse<TurmaResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(turmaService.getAllTurmas(pageable));
    }
    
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(
        summary = "Listar minhas turmas (Professor)",
        description = "Retorna uma lista paginada de todas as turmas vinculadas ao professor logado."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de turmas")
    @GetMapping("/minhas-turmas")
    public ResponseEntity<PagedResponse<TurmaResponseDto>> getMinhasTurmas(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(turmaService.getMinhasTurmas(pageable));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
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