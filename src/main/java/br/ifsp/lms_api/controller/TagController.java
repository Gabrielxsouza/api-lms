package br.ifsp.lms_api.controller;

import br.ifsp.lms_api.dto.TagDto.TagRequestDto;
import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.TagDto.TagUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.TagService;

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
@RequestMapping("/tags")
@Tag(name = "Tags", description = "Endpoints para gerenciar tags de conteúdo (ex: 'Cálculo', 'Derivadas')")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(
        summary = "Criar nova tag",
        description = "Cria uma nova tag de conteúdo. O nome da tag deve ser único."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Tag criada com sucesso",
        content = @Content(schema = @Schema(implementation = TagResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida (nome em branco)")
    @ApiResponse(responseCode = "500", description = "Erro interno (ex: tag com nome duplicado)")
    @PostMapping
    public ResponseEntity<TagResponseDto> create(
            @Valid @RequestBody TagRequestDto requestDto) {
        
        TagResponseDto responseDto = tagService.createTag(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Listar todas as tags",
        description = "Retorna uma lista paginada de todas as tags de conteúdo cadastradas."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de tags")
    @GetMapping
    public ResponseEntity<PagedResponse<TagResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(tagService.getAllTags(pageable));
    }

    @PreAuthorize("permitAll()")
    @Operation(summary = "Buscar tag por ID")
    @ApiResponse(
        responseCode = "200",
        description = "Tag encontrada",
        content = @Content(schema = @Schema(implementation = TagResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Tag não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getById(
            @Parameter(description = "ID da tag a ser buscada") @PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(summary = "Atualizar uma tag (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Tag atualizada com sucesso",
        content = @Content(schema = @Schema(implementation = TagResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Tag não encontrada")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PatchMapping("/{id}")
    public ResponseEntity<TagResponseDto> update(
            @Parameter(description = "ID da tag a ser atualizada") @PathVariable Long id, 
            @Valid @RequestBody TagUpdateDto updateDto) {
        
        TagResponseDto responseDto = tagService.updateTag(id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletar uma tag")
    @ApiResponse(responseCode = "204", description = "Tag deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Tag não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da tag a ser deletada") @PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}