package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.service.AtividadeTextoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/atividades-texto") 
@Tag(name = "Atividades (Texto)", description = "Endpoints para gerenciar atividades do tipo 'Envio de Texto'")
public class AtividadeTextoController {

    private final AtividadeTextoService atividadeTextoService;

    public AtividadeTextoController(AtividadeTextoService atividadeTextoService) {
        this.atividadeTextoService = atividadeTextoService;
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(
        summary = "Criar nova atividade de texto",
        description = "Cria uma nova atividade onde a resposta esperada é um texto dissertativo."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Atividade de texto criada com sucesso",
        content = @Content(schema = @Schema(implementation = AtividadeTextoResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PostMapping
    public ResponseEntity<AtividadeTextoResponseDto> create(
            @Valid @RequestBody AtividadeTextoRequestDto atividadeTextoRequestDto) {
        
        AtividadeTextoResponseDto responseDto = atividadeTextoService.createAtividadeTexto(atividadeTextoRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(
        summary = "Listar todas as atividades de texto",
        description = "Retorna uma lista paginada de todas as atividades do tipo 'Envio de Texto'."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de atividades")
    @GetMapping
    public ResponseEntity<PagedResponse<AtividadeTextoResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(atividadeTextoService.getAllAtividadesTexto(pageable));
    }


    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(summary = "Atualizar uma atividade de texto (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Atividade atualizada com sucesso",
        content = @Content(schema = @Schema(implementation = AtividadeTextoResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Atividade não encontrada")
    @PatchMapping("/{id}")
    public ResponseEntity<AtividadeTextoResponseDto> update(
            @Parameter(description = "ID da atividade a ser atualizada") @PathVariable Long id, 
            @Valid @RequestBody AtividadeTextoUpdateDto atividadeTextoUpdateDto,
            @AuthenticationPrincipal CustomUserDetails usuarioLogado) {
        
        AtividadeTextoResponseDto responseDto = atividadeTextoService.updateAtividadeTexto(id, atividadeTextoUpdateDto, usuarioLogado.getId());
        return ResponseEntity.ok(responseDto);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletar uma atividade de texto")
    @ApiResponse(responseCode = "204", description = "Atividade deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Atividade não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da atividade a ser deletada") @PathVariable Long id) {
        atividadeTextoService.deleteAtividadeTexto(id);
        return ResponseEntity.noContent().build();
    }
}