package br.ifsp.lms_api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import br.ifsp.lms_api.config.CustomUserDetails;

import br.ifsp.lms_api.dto.analise.RelatorioDesempenhoResponseDto;
import br.ifsp.lms_api.service.AnaliseDesempenhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/analise")
@Tag(name = "Análise de Desempenho", description = "Endpoints para relatórios de desempenho")
public class AnaliseDesempenhoController {

    private final AnaliseDesempenhoService analiseService;

    public AnaliseDesempenhoController(AnaliseDesempenhoService analiseService) {
        this.analiseService = analiseService;
    }

    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Gerar meu relatório de desempenho (Aluno)")
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso.")
    @GetMapping("/aluno/meu-desempenho")
    public RelatorioDesempenhoResponseDto getMeuRelatorio(
            @AuthenticationPrincipal CustomUserDetails usuarioLogado) {
        
        Long idAlunoLogado = usuarioLogado.getId();
        return analiseService.gerarRelatorioAluno(idAlunoLogado);
    }
}