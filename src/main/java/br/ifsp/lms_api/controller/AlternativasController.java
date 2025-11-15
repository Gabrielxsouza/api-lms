package br.ifsp.lms_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AlternativasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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


@RestController
@Validated
@RequestMapping("/alternativas")
@Tag(name = "Alternativas", description = "Endpoints para gerenciar as alternativas de uma questão")
public class AlternativasController {

    private final AlternativasService alternativasService;

    public AlternativasController(AlternativasService alternativasService) {
        this.alternativasService = alternativasService;
    }

    @Operation(
        summary = "Criar nova alternativa",
        description = "Cria uma nova alternativa vinculada a uma questão existente."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Alternativa criada com sucesso",
        content = @Content(schema = @Schema(implementation = AlternativasResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida (campos obrigatórios faltando)")
    @ApiResponse(responseCode = "404", description = "Questão (pai) não encontrada")
    @PostMapping
    public ResponseEntity<AlternativasResponseDto> createAlternativas(@Valid @RequestBody AlternativasRequestDto alternativas) {
        AlternativasResponseDto createdAlternativa = alternativasService.createAlternativa(alternativas);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlternativa);
    }

    @Operation(
        summary = "Listar todas as alternativas",
        description = "Retorna uma lista paginada de todas as alternativas no sistema."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de alternativas")
    @GetMapping
    public ResponseEntity<PagedResponse<AlternativasResponseDto>> getAllAlternativas(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(alternativasService.getAllAlternativas(pageable));
    }

    @Operation(summary = "Buscar alternativa por ID")
    @ApiResponse(
        responseCode = "200",
        description = "Alternativa encontrada",
        content = @Content(schema = @Schema(implementation = AlternativasResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Alternativa não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<AlternativasResponseDto> getAlternativaById(
            @Parameter(description = "ID da alternativa a ser buscada") @PathVariable Long id) {
        AlternativasResponseDto alternativa = alternativasService.getAlternativaById(id);
        return ResponseEntity.ok(alternativa);
    }

    @Operation(summary = "Atualizar uma alternativa (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Alternativa atualizada com sucesso",
        content = @Content(schema = @Schema(implementation = AlternativasResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Alternativa não encontrada")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PatchMapping("/{id}")
    public ResponseEntity<AlternativasResponseDto> updateAlternativa(
            @Parameter(description = "ID da alternativa a ser atualizada") @PathVariable Long id, 
            @RequestBody @Valid AlternativasUpdateDto alternativaDto){
        AlternativasResponseDto updatedAlternativa = alternativasService.updateAlternativa(id, alternativaDto);
        return ResponseEntity.ok(updatedAlternativa);
    }

    @Operation(summary = "Deletar uma alternativa")
    @ApiResponse(responseCode = "204", description = "Alternativa deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Alternativa não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlternativa(
            @Parameter(description = "ID da alternativa a ser deletada") @PathVariable Long id){
        alternativasService.deleteAlternativa(id);
        return ResponseEntity.noContent().build();
    }

}