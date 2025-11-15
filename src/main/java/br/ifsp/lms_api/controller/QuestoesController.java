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

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.service.QuestoesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/questoes")
@Tag(name = "Questões", description = "Endpoints para gerenciar questões e suas alternativas")
public class QuestoesController {

    private final QuestoesService questoesService;

    public QuestoesController(QuestoesService questoesService) {
        this.questoesService = questoesService;
    }

    @Operation(
        summary = "Criar nova questão",
        description = "Cria uma nova questão, incluindo sua lista de alternativas aninhadas."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Questão criada com sucesso",
        content = @Content(schema = @Schema(implementation = QuestoesResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PostMapping
    public ResponseEntity<QuestoesResponseDto> createQuestao(@Valid @RequestBody QuestoesRequestDto questao) {
        QuestoesResponseDto createdQuestao = questoesService.createQuestao(questao);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestao);
    }

    @Operation(
        summary = "Listar todas as questões",
        description = "Retorna uma lista paginada de todas as questões."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de questões")
    @GetMapping
    public ResponseEntity<PagedResponse<QuestoesResponseDto>> getAllQuestoes(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(questoesService.getAllQuestoes(pageable));
    }

    @Operation(summary = "Atualizar uma questão (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Questão atualizada com sucesso",
        content = @Content(schema = @Schema(implementation = QuestoesResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Questão não encontrada")
    @PatchMapping("/{id}")
    public ResponseEntity<QuestoesResponseDto> updateQuestao(
            @Parameter(description = "ID da questão a ser atualizada") @PathVariable Long id, 
            @Valid @RequestBody QuestoesUpdateDto questaoUpdate) {
        QuestoesResponseDto updatedQuestao = questoesService.updateQuestao(id, questaoUpdate);
        return ResponseEntity.ok(updatedQuestao);
    }

    @Operation(summary = "Deletar uma questão")
    @ApiResponse(responseCode = "204", description = "Questão deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Questão não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestao(
            @Parameter(description = "ID da questão a ser deletada") @PathVariable Long id) {
        questoesService.deleteQuestao(id);
        return ResponseEntity.noContent().build();
    }
    
}