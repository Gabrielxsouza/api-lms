package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.matriculaDto.MatriculaRequestDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaResponseDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/matriculas")
@Tag(name = "Matrículas", description = "Endpoints para gerenciar matrículas de alunos em cursos")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @Operation(summary = "Criar nova matrícula", description = "Cria uma nova matrícula para um aluno em um curso específico.")
    @ApiResponse(responseCode = "201", description = "Matrícula criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Entrada inválida (campos obrigatórios faltando ou mal formatados)")
    @ApiResponse(responseCode = "500", description = "Erro interno (ex: aluno ou curso não encontrado)")
    @PostMapping
    public ResponseEntity<MatriculaResponseDto> create(@Valid @RequestBody MatriculaRequestDto requestDto) {
        MatriculaResponseDto responseDto = matriculaService.createMatricula(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Listar todas as matrículas", description = "Retorna uma lista paginada de todas as matrículas de alunos em cursos.")
    @ApiResponse(responseCode = "200", description = "Lista paginada de matrículas")
    @GetMapping
    public ResponseEntity<PagedResponse<MatriculaResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(matriculaService.getAllMatriculas(pageable));
    }

    @Operation(summary = "Obter matrícula por ID", description = "Retorna os detalhes de uma matrícula específica com base no seu ID.")
    @ApiResponse(responseCode = "200", description = "Detalhes da matrícula")
    @ApiResponse(responseCode = "404", description = "Matrícula não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponseDto> getMatriculaById(
            @Parameter(description = "ID da matrícula") @Valid @PathVariable Long id) {
        return ResponseEntity.ok(matriculaService.getMatriculaById(id));
    }

    @Operation(summary = "Deletar uma matrícula", description = "Deleta uma matrícula existente com base no seu ID.")
    @ApiResponse(responseCode = "204", description = "Matrícula deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Matrícula não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatricula(
            @Parameter(description = "ID da matrícula") @Valid @PathVariable Long id) {
        matriculaService.deleteMatricula(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar uma matrícula", description = "Atualiza os detalhes de uma matrícula existente.")
    @ApiResponse(responseCode = "200", description = "Matrícula atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Entrada inválida (campos obrigatórios faltando ou mal formatados)")
    @ApiResponse(responseCode = "404", description = "Matrícula não encontrada")
    @PatchMapping("/{id}")
    public ResponseEntity<MatriculaResponseDto> updateMatricula(
            @Parameter(description = "ID da matrícula") @PathVariable Long id,
            @Valid @RequestBody MatriculaUpdateDto requestDto) {
        MatriculaResponseDto responseDto = matriculaService.updateMatricula(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

}
