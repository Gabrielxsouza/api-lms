package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.TopicosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@Validated
@RequestMapping("/topicos")
@Tag(name = "Tópicos", description = "Endpoints para gerenciar os tópicos de uma turma")
public class TopicosController {
    private final TopicosService topicosService;

    public TopicosController(TopicosService topicosService) {
        this.topicosService = topicosService;
    }

    @Operation(
        summary = "Criar novo tópico",
        description = "Cria um novo tópico, vinculando-o a uma turma e, opcionalmente, a atividades e tags."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Tópico criado com sucesso",
        content = @Content(schema = @Schema(implementation = TopicosResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Turma, Atividade ou Tag não encontrada")
    @PostMapping
    public ResponseEntity<TopicosResponseDto> createTopico(@Valid @RequestBody TopicosRequestDto topicos) {
        TopicosResponseDto responseDto = topicosService.createTopico(topicos);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
        summary = "Listar todos os tópicos",
        description = "Retorna uma lista paginada de todos os tópicos."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de tópicos")
    @GetMapping
    public ResponseEntity<PagedResponse<TopicosResponseDto>> getAllTopicos(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(topicosService.getAllTopicos(pageable));
    }

    @Operation(summary = "Buscar tópico por ID")
    @ApiResponse(
        responseCode = "200",
        description = "Tópico encontrado",
        content = @Content(schema = @Schema(implementation = TopicosResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Tópico não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<TopicosResponseDto> getTopicoById(
            @Parameter(description = "ID do tópico a ser buscado") @Valid @PathVariable Long id) {
        return ResponseEntity.ok(topicosService.getTopicoById(id));
    }

    @Operation(
        summary = "Listar tópicos por ID da Turma",
        description = "Retorna uma lista paginada de todos os tópicos pertencentes a uma turma específica."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de tópicos")
    @GetMapping("/turma/{idTurma}")
    public ResponseEntity<PagedResponse<TopicosResponseDto>> getTopicosByTurmaId(
            @Parameter(description = "ID da turma para buscar os tópicos") @Valid @PathVariable Long idTurma, 
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(topicosService.getTopicosByIdTurma(idTurma, pageable));
    }

    @Operation(summary = "Deletar um tópico")
    @ApiResponse(
        responseCode = "200",
        description = "Tópico deletado com sucesso (retorna o objeto deletado)",
        content = @Content(schema = @Schema(implementation = TopicosResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Tópico não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<TopicosResponseDto> deleteTopico(
            @Parameter(description = "ID do tópico a ser deletado") @PathVariable Long id) {
        return ResponseEntity.ok(topicosService.deleteTopico(id));
    }

    @Operation(summary = "Atualizar um tópico (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Tópico atualizado com sucesso",
        content = @Content(schema = @Schema(implementation = TopicosResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Tópico não encontrado")
    @PatchMapping("/{id}")
    public ResponseEntity<TopicosResponseDto> updateTopico(
            @Parameter(description = "ID do tópico a ser atualizado") @PathVariable Long id, 
            @Valid @RequestBody TopicosUpdateDto topicos) {
        return ResponseEntity.ok(topicosService.updateTopico(id, topicos));
    }
}