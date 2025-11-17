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

import br.ifsp.lms_api.dto.CursoDto.CursoRequestDto;
import br.ifsp.lms_api.dto.CursoDto.CursoResponseDto;
import br.ifsp.lms_api.dto.CursoDto.CursoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse; 
import br.ifsp.lms_api.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/cursos") 
@Tag(name = "Cursos", description = "Endpoints para gerenciar cursos e suas turmas") 
public class CursoController {
    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Criar novo curso",
        description = "Cria um novo curso e, opcionalmente, cria turmas aninhadas na mesma requisição."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Curso criado com sucesso",
        content = @Content(schema = @Schema(implementation = CursoResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida (ex: campos obrigatórios faltando)")
    @PostMapping
    public ResponseEntity<CursoResponseDto> createCurso(@Valid @RequestBody CursoRequestDto curso) {
        CursoResponseDto createdCurso = cursoService.createCurso(curso);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCurso);
    }

    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Listar todos os cursos",
        description = "Retorna uma lista paginada de todos os cursos."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de cursos")
    @GetMapping
    public ResponseEntity<PagedResponse<CursoResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(cursoService.getAllCursos(pageable));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")  
    @Operation(summary = "Atualizar um curso (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Curso atualizado com sucesso",
        content = @Content(schema = @Schema(implementation = CursoResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    @PatchMapping("/{id}")
    public ResponseEntity<CursoResponseDto> update(
            @Parameter(description = "ID do curso a ser atualizado") @PathVariable Long id, 
            @Valid @RequestBody CursoUpdateDto updateDto) {
        
        CursoResponseDto updatedCurso = cursoService.updateCurso(id, updateDto);
        return ResponseEntity.ok(updatedCurso);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletar um curso")
    @ApiResponse(responseCode = "204", description = "Curso deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do curso a ser deletado") @PathVariable Long id) {
        cursoService.deleteCurso(id);
        return ResponseEntity.noContent().build();
    }
}