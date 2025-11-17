package br.ifsp.lms_api.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioRequestDto;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioResponseDto;
import br.ifsp.lms_api.service.TentativaQuestionarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.page.PagedResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Validated
@RequestMapping("/tentativaQuestionario")
@Tag(name = "Tentativa de Questionário", description = "Endpoints para submissão e visualização de tentativas de questionário")
public class TentativaQuestionarioController {
    private final TentativaQuestionarioService tentativaQuestionarioService;

    public TentativaQuestionarioController(TentativaQuestionarioService tentativaQuestionarioService) {
        this.tentativaQuestionarioService = tentativaQuestionarioService;
    }

    @PreAuthorize("hasRole('ROLE_ALUNO')")
    @Operation(summary = "Submeter tentativa de questionário (Aluno)")
    @ApiResponse(responseCode = "200", description = "Tentativa submetida e corrigida com sucesso")
    @ApiResponse(responseCode = "404", description = "Aluno ou Questionário não encontrado")
    @ApiResponse(responseCode = "400", description = "Limite de tentativas atingido")
    @PostMapping
    public TentativaQuestionarioResponseDto createTentativaQuestionario(
        @Validated @RequestBody TentativaQuestionarioRequestDto tentativaRequest,
        @AuthenticationPrincipal CustomUserDetails usuarioLogado) { 
        
        Long idAlunoLogado = usuarioLogado.getId();
        return tentativaQuestionarioService.createTentativaQuestionario(tentativaRequest, idAlunoLogado);
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(summary = "Listar todas as tentativas (Professor)")
    @ApiResponse(responseCode = "200", description = "Lista paginada de todas as tentativas")
    @GetMapping
    public PagedResponse<TentativaQuestionarioResponseDto> getAllTentativasQuestionario(
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return tentativaQuestionarioService.getAllTentativasQuestionario(pageable); 
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(summary = "Listar tentativas de um aluno específico (Professor)")
    @ApiResponse(responseCode = "200", description = "Lista paginada das tentativas do aluno")
    @GetMapping("/aluno/{alunoId}")
    public PagedResponse<TentativaQuestionarioResponseDto> getTentativasQuestionarioByAlunoId(
            @Parameter(description = "ID do aluno a ser buscado") @PathVariable Long alunoId, 
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        
        return tentativaQuestionarioService.getTentativasQuestionarioByAlunoId(alunoId, pageable);
    }

    @PreAuthorize("hasRole('ROLE_ALUNO')")
    @Operation(summary = "Listar minhas próprias tentativas (Aluno)")
    @ApiResponse(responseCode = "200", description = "Lista paginada das minhas tentativas")
    @GetMapping("/aluno/minhas")
    public PagedResponse<TentativaQuestionarioResponseDto> getMinhasTentativasQuestionario(
            @AuthenticationPrincipal CustomUserDetails usuarioLogado,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        
        Long idAlunoLogado = usuarioLogado.getId();
        return tentativaQuestionarioService.getTentativasQuestionarioByAlunoId(idAlunoLogado, pageable);
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(summary = "Deletar tentativa de questionário (Professor)",
               description = "Permite a um professor deletar uma tentativa. Alunos não podem deletar pois a nota é instantânea.")
    @ApiResponse(responseCode = "200", description = "Tentativa deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Tentativa não encontrada")
    @DeleteMapping("/{idTentativa}")
    public TentativaQuestionarioResponseDto deleteTentativaQuestionario(
            @Parameter(description = "ID da tentativa a ser deletada") @PathVariable Long idTentativa) {
        
        return tentativaQuestionarioService.deleteTentativaQuestionario(idTentativa);
    }
}