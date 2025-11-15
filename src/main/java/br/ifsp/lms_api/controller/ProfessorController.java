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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/professores")
@Tag(name = "Professores", description = "Endpoints para gerenciar usuários do tipo Professor")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @Operation(
        summary = "Criar novo professor",
        description = "Cria um novo usuário do tipo Professor no sistema. A senha deve ser criptografada."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Professor criado com sucesso",
        content = @Content(schema = @Schema(implementation = ProfessorResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida (campos obrigatórios faltando ou mal formatados)")
    @ApiResponse(responseCode = "500", description = "Erro interno (ex: e-mail ou CPF já cadastrado)")
    @PostMapping
    public ResponseEntity<ProfessorResponseDto> create(
            @Valid @RequestBody ProfessorRequestDto requestDto) {
        
        ProfessorResponseDto responseDto = professorService.createProfessor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
        summary = "Listar todos os professores",
        description = "Retorna uma lista paginada de todos os usuários do tipo Professor."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de professores")
    @GetMapping
    public ResponseEntity<PagedResponse<ProfessorResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(professorService.getAllProfessores(pageable));
    }

    @Operation(summary = "Buscar professor por ID")
    @ApiResponse(
        responseCode = "200",
        description = "Professor encontrado",
        content = @Content(schema = @Schema(implementation = ProfessorResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<ProfessorResponseDto> getById(
            @Parameter(description = "ID do professor a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(professorService.getProfessorById(id));
    }

    @Operation(summary = "Atualizar um professor (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Professor atualizado com sucesso",
        content = @Content(schema = @Schema(implementation = ProfessorResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PatchMapping("/{id}")
    public ResponseEntity<ProfessorResponseDto> update(
            @Parameter(description = "ID do professor a ser atualizado") @PathVariable Long id, 
            @Valid @RequestBody ProfessorUpdateDto updateDto) {
        
        ProfessorResponseDto responseDto = professorService.updateProfessor(id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Deletar um professor")
    @ApiResponse(responseCode = "204", description = "Professor deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do professor a ser deletado") @PathVariable Long id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
}