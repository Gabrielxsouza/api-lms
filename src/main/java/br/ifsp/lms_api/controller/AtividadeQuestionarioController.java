package br.ifsp.lms_api.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioRequestDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AtividadeQuestionarioService;
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
@RequestMapping("/atividades-questionario")
@Tag(name = "Atividades (Questionário)", description = "Endpoints para gerenciar atividades do tipo 'Questionário'")
public class AtividadeQuestionarioController {
    private final AtividadeQuestionarioService atividadeQuestionarioService;

    public AtividadeQuestionarioController(AtividadeQuestionarioService atividadeQuestionarioService) {
        this.atividadeQuestionarioService = atividadeQuestionarioService;
    }

    @Operation(
        summary = "Criar novo questionário",
        description = "Cria uma nova atividade do tipo questionário, opcionalmente com questões já vinculadas."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Questionário criado com sucesso",
        content = @Content(schema = @Schema(implementation = AtividadeQuestionarioResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @PostMapping
    public ResponseEntity<AtividadeQuestionarioResponseDto> create(
            @Valid @RequestBody AtividadeQuestionarioRequestDto atividadeQuestionarioRequestDto) {

        AtividadeQuestionarioResponseDto responseDto = atividadeQuestionarioService.createAtividadeQuestionario(atividadeQuestionarioRequestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
        summary = "Listar todos os questionários",
        description = "Retorna uma lista paginada de todas as atividades do tipo 'Questionário'."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de questionários")
    @GetMapping
    public ResponseEntity<PagedResponse<AtividadeQuestionarioResponseDto>> getAll(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(atividadeQuestionarioService.getAllAtividadesQuestionario(pageable));
    }

    @Operation(summary = "Buscar questionário por ID")
    @ApiResponse(
        responseCode = "200",
        description = "Questionário encontrado",
        content = @Content(schema = @Schema(implementation = AtividadeQuestionarioResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Questionário não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<AtividadeQuestionarioResponseDto> getAtividadeQuestionarioById(
            @Parameter(description = "ID do questionário a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(atividadeQuestionarioService.getAtividadeQuestionarioById(id));
    }

    @Operation(summary = "Atualizar um questionário (PATCH)")
    @ApiResponse(
        responseCode = "200",
        description = "Questionário atualizado com sucesso",
        content = @Content(schema = @Schema(implementation = AtividadeQuestionarioResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Questionário não encontrado")
    @PatchMapping("/{id}")
    public ResponseEntity<AtividadeQuestionarioResponseDto> update(
            @Parameter(description = "ID do questionário a ser atualizado") @PathVariable Long id, 
            @Valid @RequestBody AtividadeQuestionarioUpdateDto atividadeQuestionarioUpdateDto) {
        
        AtividadeQuestionarioResponseDto responseDto = atividadeQuestionarioService.updateAtividadeQuestionario(id, atividadeQuestionarioUpdateDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
        summary = "Adicionar questões a um questionário",
        description = "Vincula uma lista de questões existentes a um questionário existente, baseado em seus IDs."
    )
    @ApiResponse(responseCode = "200", description = "Questões adicionadas com sucesso")
    @ApiResponse(responseCode = "404", description = "Questionário ou uma das Questões não encontrada")
    @PostMapping("/{idQuestionario}/questoes")
    public ResponseEntity<AtividadeQuestionarioResponseDto> adicionarQuestoesAoQuestionario(
            @Parameter(description = "ID do questionário") @PathVariable Long idQuestionario,
            @Parameter(description = "Lista de IDs das questões a serem adicionadas") @RequestBody List<Long> idsDasQuestoes) {

        AtividadeQuestionarioResponseDto questionarioAtualizado =
                atividadeQuestionarioService.adicionarQuestoes(idQuestionario, idsDasQuestoes);

        return ResponseEntity.ok(questionarioAtualizado);
    }


    @Operation(
        summary = "Remover questões de um questionário",
        description = "Desvincula uma lista de questões específicas de um questionário."
    )
    @ApiResponse(responseCode = "200", description = "Questões removidas com sucesso")
    @ApiResponse(responseCode = "404", description = "Questionário não encontrado")
    @DeleteMapping("/{idQuestionario}/questoes")
    public ResponseEntity<AtividadeQuestionarioResponseDto> removerQuestoesDoQuestionario(
            @Parameter(description = "ID do questionário") @PathVariable Long idQuestionario,
            @Parameter(description = "Lista de IDs das questões a serem removidas") @RequestBody List<Long> idsDasQuestoes) {

        try {
            AtividadeQuestionarioResponseDto questionarioAtualizado = atividadeQuestionarioService.removerQuestoes(idQuestionario, idsDasQuestoes);
            return ResponseEntity.ok(questionarioAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Remover TODAS as questões de um questionário",
        description = "Desvincula todas as questões atualmente associadas a um questionário."
    )
    @ApiResponse(responseCode = "200", description = "Todas as questões foram removidas")
    @ApiResponse(responseCode = "404", description = "Questionário não encontrado")
    @DeleteMapping("/{idQuestionario}/questoes/todas")
    public ResponseEntity<AtividadeQuestionarioResponseDto> removerQuestoesDoQuestionario(
            @Parameter(description = "ID do questionário") @PathVariable Long idQuestionario) {

        try {
            AtividadeQuestionarioResponseDto questionarioAtualizado = atividadeQuestionarioService.removerQuestoes(idQuestionario);
            return ResponseEntity.ok(questionarioAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}