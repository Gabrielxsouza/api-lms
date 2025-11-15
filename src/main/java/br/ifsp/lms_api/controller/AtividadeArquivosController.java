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
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.service.AtividadeArquivosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/atividades-arquivo")
@Tag(name = "Atividades (Arquivo)", description = "Endpoints para gerenciar atividades do tipo 'Envio de Arquivo'")
public class AtividadeArquivosController {

    private final AtividadeArquivosService atividadeArquivosService;

    public AtividadeArquivosController(AtividadeArquivosService atividadeArquivosService) {
        this.atividadeArquivosService = atividadeArquivosService;
    }

    @Operation(
        summary = "Criar nova atividade de arquivo",
        description = "Cria uma nova atividade onde a resposta esperada é o upload de um ou mais arquivos."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Atividade de arquivo criada com sucesso",
        content = @Content(schema = @Schema(implementation = AtividadeArquivosResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PostMapping
    public ResponseEntity<AtividadeArquivosResponseDto> create(
            @Valid @RequestBody AtividadeArquivosRequestDto atividadeArquivosRequestDto) {
        
        AtividadeArquivosResponseDto responseDto = atividadeArquivosService.createAtividadeArquivos(atividadeArquivosRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
        summary = "Listar todas as atividades de arquivo",
        description = "Retorna uma lista paginada de todas as atividades do tipo 'Envio de Arquivo'."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de atividades")
    @GetMapping
    public ResponseEntity<PagedResponse<AtividadeArquivosResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(atividadeArquivosService.getAllAtividadesArquivos(pageable));
    }


    @Operation(summary = "Atualizar uma atividade de arquivo (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Atividade atualizada com sucesso",
        content = @Content(schema = @Schema(implementation = AtividadeArquivosResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Atividade não encontrada")
    @PatchMapping("/{id}")
    public ResponseEntity<AtividadeArquivosResponseDto> update(
            @Parameter(description = "ID da atividade a ser atualizada") @PathVariable Long id, 
            @Valid @RequestBody AtividadeArquivosUpdateDto atividadeArquivosUpdateDto) {
        
        AtividadeArquivosResponseDto responseDto = atividadeArquivosService.updateAtividadeArquivos(id, atividadeArquivosUpdateDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Deletar uma atividade de arquivo")
    @ApiResponse(responseCode = "204", description = "Atividade deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Atividade não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da atividade a ser deletada") @PathVariable Long id) {
        atividadeArquivosService.deleteAtividadeArquivos(id);
        return ResponseEntity.noContent().build();
    }
}