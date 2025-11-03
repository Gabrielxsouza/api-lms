package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.TopicosService;
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
public class TopicosController {
    private final TopicosService topicosService;

    public TopicosController(TopicosService topicosService) {
        this.topicosService = topicosService;
    }

    @PostMapping
    public TopicosResponseDto createTopico(@Valid @RequestBody TopicosRequestDto topicos) {
        return topicosService.createTopico(topicos);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TopicosResponseDto>> getAllQuestoes(Pageable pageable) {
        return ResponseEntity.ok(topicosService.getAllTopicos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicosResponseDto> getTopicoById(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(topicosService.getTopicoById(id));
    }

    @GetMapping("/turma/{idTurma}")
    public ResponseEntity<PagedResponse<TopicosResponseDto>> getTopicosByTurmaId(@Valid @PathVariable Long idTurma, Pageable pageable) {
        return ResponseEntity.ok(topicosService.getTopicosByIdTurma(idTurma, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TopicosResponseDto> deleteTopico(@PathVariable Long id) {
        return ResponseEntity.ok(topicosService.deleteTopico(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TopicosResponseDto> updateTopico(@PathVariable Long id, @Valid @RequestBody TopicosUpdateDto topicos) {
        return ResponseEntity.ok(topicosService.updateTopico(id, topicos));
    }

    @PostMapping("path")
    public ResponseEntity<TopicosResponseDto> adicionarAtividadeAoTopico(@Valid @RequestBody TopicosRequestDto topicos) {
        return ResponseEntity.ok(topicosService.createTopico(topicos));
    }



}
