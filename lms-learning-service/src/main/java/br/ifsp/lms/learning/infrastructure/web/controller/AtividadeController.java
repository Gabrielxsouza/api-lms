package br.ifsp.lms.learning.infrastructure.web.controller;

import br.ifsp.lms.learning.application.usecase.CriarAtividadeUseCase;
import br.ifsp.lms.learning.domain.model.Atividade;
import br.ifsp.lms.learning.domain.model.AtividadeQuestionario;
import br.ifsp.lms.learning.infrastructure.web.dto.AtividadeResponse;
import br.ifsp.lms.learning.infrastructure.web.dto.CreateArquivosRequest;
import br.ifsp.lms.learning.infrastructure.web.dto.CreateQuestionarioRequest;
import br.ifsp.lms.learning.infrastructure.web.dto.CreateTextoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/atividades")
@RequiredArgsConstructor
public class AtividadeController {

        private final CriarAtividadeUseCase criarAtividadeUseCase;
        private final br.ifsp.lms.learning.application.usecase.BuscarAtividadeUseCase buscarAtividadeUseCase;
        private final br.ifsp.lms.learning.application.usecase.DeletarAtividadeUseCase deletarAtividadeUseCase;
        private final br.ifsp.lms.learning.application.usecase.ListarAtividadesUseCase listarAtividadesUseCase;
        private final br.ifsp.lms.learning.application.usecase.AtualizarAtividadeUseCase atualizarAtividadeUseCase;

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
                                .topicoId(request.getIdTopico())
                                .tags(request.getTags())
                                .build();

                Atividade created = criarAtividadeUseCase.execute(domain);
                return ResponseEntity.ok(toResponse(created));
        }

        @PutMapping("/questionario/{id}")
        public ResponseEntity<AtividadeResponse> atualizarQuestionario(@PathVariable Long id,
                        @RequestBody CreateQuestionarioRequest request) {
                // Simply map and save. In real world, we'd fetch and merge, but here we
                // overwrite or expect full data.
                // Assuming request has all data field.
                AtividadeQuestionario domain = AtividadeQuestionario.builder()
                                .id(id)
                                .titulo(request.getTituloAtividade())
                                .descricao(request.getDescricaoAtividade())
                                .dataInicio(request.getDataInicioAtividade())
                                .dataFechamento(request.getDataFechamentoAtividade())
                                .status(request.getStatusAtividade())
                                .duracaoMinutes(request.getDuracaoQuestionario())
                                .tentativasPermitidas(request.getNumeroTentativas())
                                .topicoId(request.getIdTopico())
                                .tags(request.getTags())
                                .build();
                Atividade updated = atualizarAtividadeUseCase.execute(domain);
                return ResponseEntity.ok(toResponse(updated));
        }

        @PutMapping("/texto/{id}")
        public ResponseEntity<AtividadeResponse> atualizarTexto(@PathVariable Long id,
                        @RequestBody CreateTextoRequest request) {
                br.ifsp.lms.learning.domain.model.AtividadeTexto domain = br.ifsp.lms.learning.domain.model.AtividadeTexto
                                .builder()
                                .id(id)
                                .titulo(request.getTituloAtividade())
                                .descricao(request.getDescricaoAtividade())
                                .dataInicio(request.getDataInicioAtividade())
                                .dataFechamento(request.getDataFechamentoAtividade())
                                .status(request.getStatusAtividade())
                                .numeroMaximoCaracteres(request.getNumeroMaximoCaracteres())
                                .topicoId(request.getIdTopico())
                                .tags(request.getTags())
                                .build();
                Atividade updated = atualizarAtividadeUseCase.execute(domain);
                return ResponseEntity.ok(toResponse(updated));
        }

        @PutMapping("/arquivos/{id}")
        public ResponseEntity<AtividadeResponse> atualizarArquivos(@PathVariable Long id,
                        @RequestBody CreateArquivosRequest request) {
                br.ifsp.lms.learning.domain.model.AtividadeArquivos domain = br.ifsp.lms.learning.domain.model.AtividadeArquivos
                                .builder()
                                .id(id)
                                .titulo(request.getTituloAtividade())
                                .descricao(request.getDescricaoAtividade())
                                .dataInicio(request.getDataInicioAtividade())
                                .dataFechamento(request.getDataFechamentoAtividade())
                                .status(request.getStatusAtividade())
                                .arquivosPermitidos(request.getArquivosPermitidos())
                                .topicoId(request.getIdTopico())
                                .tags(request.getTags())
                                .build();
                Atividade updated = atualizarAtividadeUseCase.execute(domain);
                return ResponseEntity.ok(toResponse(updated));
        }

        @PostMapping("/texto")
        public ResponseEntity<AtividadeResponse> criarTexto(@RequestBody CreateTextoRequest request) {
                br.ifsp.lms.learning.domain.model.AtividadeTexto domain = br.ifsp.lms.learning.domain.model.AtividadeTexto
                                .builder()
                                .titulo(request.getTituloAtividade())
                                .descricao(request.getDescricaoAtividade())
                                .dataInicio(request.getDataInicioAtividade())
                                .dataFechamento(request.getDataFechamentoAtividade())
                                .status(request.getStatusAtividade())
                                .numeroMaximoCaracteres(request.getNumeroMaximoCaracteres())
                                .topicoId(request.getIdTopico())
                                .tags(request.getTags())
                                .build();

                Atividade created = criarAtividadeUseCase.execute(domain);
                return ResponseEntity.ok(toResponse(created));
        }

        @PostMapping("/arquivos")
        public ResponseEntity<AtividadeResponse> criarArquivos(@RequestBody CreateArquivosRequest request) {
                br.ifsp.lms.learning.domain.model.AtividadeArquivos domain = br.ifsp.lms.learning.domain.model.AtividadeArquivos
                                .builder()
                                .titulo(request.getTituloAtividade())
                                .descricao(request.getDescricaoAtividade())
                                .dataInicio(request.getDataInicioAtividade())
                                .dataFechamento(request.getDataFechamentoAtividade())
                                .status(request.getStatusAtividade())
                                .arquivosPermitidos(request.getArquivosPermitidos())
                                .topicoId(request.getIdTopico())
                                .tags(request.getTags())
                                .build();

                Atividade created = criarAtividadeUseCase.execute(domain);

                return ResponseEntity.ok(toResponse(created));
        }

        @GetMapping
        public ResponseEntity<java.util.List<AtividadeResponse>> getAllAtividades() {
                java.util.List<AtividadeResponse> responses = listarAtividadesUseCase.execute().stream()
                                .map(this::toResponse)
                                .collect(java.util.stream.Collectors.toList());
                return ResponseEntity.ok(responses);
        }

        @GetMapping("/{id}")
        public ResponseEntity<AtividadeResponse> getAtividade(@PathVariable Long id) {
                return buscarAtividadeUseCase.execute(id)
                                .map(this::toResponse)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAtividade(@PathVariable Long id) {
                deletarAtividadeUseCase.execute(id);
                return ResponseEntity.noContent().build();
        }

        private AtividadeResponse toResponse(Atividade created) {
                String type = "QUESTIONARIO";
                if (created instanceof br.ifsp.lms.learning.domain.model.AtividadeTexto) {
                        type = "TEXTO";
                } else if (created instanceof br.ifsp.lms.learning.domain.model.AtividadeArquivos) {
                        type = "ARQUIVOS";
                }

                return AtividadeResponse.builder()
                                .idAtividade(created.getId())
                                .tituloAtividade(created.getTitulo())
                                .descricaoAtividade(created.getDescricao())
                                .tipoAtividade(type)
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
                                .numeroMaximoCaracteres(
                                                created instanceof br.ifsp.lms.learning.domain.model.AtividadeTexto
                                                                ? ((br.ifsp.lms.learning.domain.model.AtividadeTexto) created)
                                                                                .getNumeroMaximoCaracteres()
                                                                : null)
                                .arquivosPermitidos(
                                                created instanceof br.ifsp.lms.learning.domain.model.AtividadeArquivos
                                                                ? ((br.ifsp.lms.learning.domain.model.AtividadeArquivos) created)
                                                                                .getArquivosPermitidos()
                                                                : null)
                                .idTopico(created.getTopicoId())
                                .tags(created.getTags() != null ? new java.util.ArrayList<>(created.getTags()) : null)
                                .build();
        }
}
