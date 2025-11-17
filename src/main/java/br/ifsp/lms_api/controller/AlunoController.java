package br.ifsp.lms_api.controller;

import br.ifsp.lms_api.dto.alunoDto.AlunoRequestDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoResponseDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AlunoService;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/alunos")
@Tag(name = "Alunos", description = "Endpoints para gerenciar usuários do tipo Aluno")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Criar novo aluno",
        description = "Cria um novo usuário do tipo Aluno no sistema. A senha deve ser criptografada."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Aluno criado com sucesso",
        content = @Content(schema = @Schema(implementation = AlunoResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida (campos obrigatórios faltando ou mal formatados)")
    @ApiResponse(responseCode = "500", description = "Erro interno (ex: e-mail ou CPF já cadastrado)")
    @PostMapping
    public ResponseEntity<AlunoResponseDto> create(
            @Valid @RequestBody AlunoRequestDto requestDto) {
        
        AlunoResponseDto responseDto = alunoService.createAluno(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Listar todos os alunos (Admin)",
        description = "Retorna uma lista paginada de todos os alunos. Requer permissão de ROLE_ADMIN."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de alunos")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @GetMapping
    
    public ResponseEntity<PagedResponse<AlunoResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(alunoService.getAllAlunos(pageable));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Buscar aluno por ID")
    @ApiResponse(
        responseCode = "200",
        description = "Aluno encontrado",
        content = @Content(schema = @Schema(implementation = AlunoResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<AlunoResponseDto> getById(
            @Parameter(description = "ID do aluno a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(alunoService.getAlunoById(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Atualizar um aluno (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Aluno atualizado com sucesso",
        content = @Content(schema = @Schema(implementation = AlunoResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PatchMapping("/{id}")
    public ResponseEntity<AlunoResponseDto> update(
            @Parameter(description = "ID do aluno a ser atualizado") @PathVariable Long id, 
            @Valid @RequestBody AlunoUpdateDto updateDto) {
        
        AlunoResponseDto responseDto = alunoService.updateAluno(id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Deletar um aluno")
    @ApiResponse(responseCode = "204", description = "Aluno deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do aluno a ser deletado") @PathVariable Long id) {
        alunoService.deleteAluno(id);
        return ResponseEntity.noContent().build();
    }
}