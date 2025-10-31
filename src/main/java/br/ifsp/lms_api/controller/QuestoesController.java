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
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.service.QuestoesService;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/questoes")
public class QuestoesController {

    private final QuestoesService questoesService;

    public QuestoesController(QuestoesService questoesService) {
        this.questoesService = questoesService;
    }

    @PostMapping
    public ResponseEntity<QuestoesResponseDto> createQuestao(@Valid @RequestBody QuestoesRequestDto questao) {
        QuestoesResponseDto createdQuestao = questoesService.createQuestao(questao);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestao);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<QuestoesResponseDto>> getAllQuestoes(Pageable pageable) {
        return ResponseEntity.ok(questoesService.getAllQuestoes(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuestoesResponseDto> updateQuestao(@PathVariable Long id, @Valid @RequestBody QuestoesUpdateDto questaoUpdate) {
        QuestoesResponseDto updatedQuestao = questoesService.updateQuestao(id, questaoUpdate);
        return ResponseEntity.ok(updatedQuestao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestao(@PathVariable Long id) {
        questoesService.deleteQuestao(id);
        return ResponseEntity.noContent().build();
    }
    
}
