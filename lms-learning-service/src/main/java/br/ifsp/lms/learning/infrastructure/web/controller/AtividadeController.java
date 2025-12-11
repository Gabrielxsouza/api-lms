package br.ifsp.lms.learning.infrastructure.web.controller;

import br.ifsp.lms.learning.application.usecase.CriarAtividadeUseCase;
import br.ifsp.lms.learning.domain.model.Atividade;
import br.ifsp.lms.learning.domain.model.AtividadeQuestionario;
import br.ifsp.lms.learning.infrastructure.web.dto.AtividadeResponse;
import br.ifsp.lms.learning.infrastructure.web.dto.CreateQuestionarioRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/atividades")
@RequiredArgsConstructor
public class AtividadeController {

        private final CriarAtividadeUseCase criarAtividadeUseCase;

        @PostMapping("/questionario")
        public ResponseEntity<AtividadeResponse> criarQuestionario(@RequestBody CreateQuestionarioRequest request) {

                AtividadeQuestionario domain = AtividadeQuestionario.builder()
                                .titulo(request.getTituloAtividade())
                                .descricao(request.getDescricaoAtividade())
                                .dataInicio(request.getDataInicioAtividade())
                                .dataFechamento(request.getDataFechamentoAtividade())
                                .status(request.getStatusAtividade())
                                .duracaoMinutes(request.getDuracaoQuestionario())
                                .tentativasPermitidas(request.getNumeroTentativas())
                                .build();

                Atividade created = criarAtividadeUseCase.execute(domain);

                // Map Domain -> DTO (Matching Monolith Structure)
                AtividadeResponse response = AtividadeResponse.builder()
                                .idAtividade(created.getId())
                                .tituloAtividade(created.getTitulo())
                                .descricaoAtividade(created.getDescricao())
                                .tipoAtividade("QUESTIONARIO")
                                .statusAtividade(created.getStatus())
                                .dataInicioAtividade(created.getDataInicio())
                                .dataFechamentoAtividade(created.getDataFechamento())
                                .duracaoQuestionario(
                                                created instanceof AtividadeQuestionario
                                                                ? ((AtividadeQuestionario) created).getDuracaoMinutes()
                                                                : null)
                                .numeroTentativas(created instanceof AtividadeQuestionario
                                                ? ((AtividadeQuestionario) created).getTentativasPermitidas()
                                                : null)
                                .build();

                return ResponseEntity.ok(response);
        }
}
