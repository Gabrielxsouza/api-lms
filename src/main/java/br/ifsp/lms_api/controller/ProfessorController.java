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

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.professorDto.ProfessorRequestDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorResponseDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorUpdateDto;
import br.ifsp.lms_api.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/professores")
@Tag(name = "Professores", description = "Endpoints para gerenciar professores")
@SecurityRequirement(name = "bearerAuth")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @Operation(summary = "Criar um novo professor (Somente ADMIN)")
    @ApiResponse(responseCode = "201", description = "Professor criado com sucesso")
    public ResponseEntity<ProfessorResponseDto> create(
            @Valid @RequestBody ProfessorRequestDto requestDto) {
        
        ProfessorResponseDto createdProfessor = professorService.createProfessor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfessor);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Listar todos os professores (Somente ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista paginada de professores")
    public ResponseEntity<PagedResponse<ProfessorResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(professorService.getAllProfessores(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Buscar professor por ID (Somente ADMIN)")
    @ApiResponse(responseCode = "200", description = "Professor encontrado")
    @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    public ResponseEntity<ProfessorResponseDto> getById(
            @Parameter(description = "ID do professor a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(professorService.getProfessorById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PROFESSOR', 'ROLE_ALUNO')")
    @Operation(summary = "Atualizar um professor (Somente ADMIN)")
    @ApiResponse(responseCode = "200", description = "Professor atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    public ResponseEntity<ProfessorResponseDto> update(
            @Parameter(description = "ID do professor a ser atualizado") @PathVariable Long id,
            @Valid @RequestBody ProfessorUpdateDto requestDto) {
        
        ProfessorResponseDto updatedProfessor = professorService.updateProfessor(id, requestDto);
        return ResponseEntity.ok(updatedProfessor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletar um professor (Somente ADMIN)")
    @ApiResponse(responseCode = "204", description = "Professor deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do professor a ser deletado") @PathVariable Long id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
}