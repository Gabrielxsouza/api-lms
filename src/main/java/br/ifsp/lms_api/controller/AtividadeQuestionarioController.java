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
public class AtividadeQuestionarioController {
    private final AtividadeQuestionarioService atividadeQuestionarioService;

    public AtividadeQuestionarioController(AtividadeQuestionarioService atividadeQuestionarioService) {
        this.atividadeQuestionarioService = atividadeQuestionarioService;
    }

    @PostMapping
    public ResponseEntity<AtividadeQuestionarioResponseDto> create(
            @Valid @RequestBody AtividadeQuestionarioRequestDto atividadeQuestionarioRequestDto) {

        AtividadeQuestionarioResponseDto responseDto = atividadeQuestionarioService.createAtividadeQuestionario(atividadeQuestionarioRequestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AtividadeQuestionarioResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(atividadeQuestionarioService.getAllAtividadesQuestionario(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtividadeQuestionarioResponseDto> getAtividadeQuestionarioById(@PathVariable Long id) {
        return ResponseEntity.ok(atividadeQuestionarioService.getAtividadeQuestionarioById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AtividadeQuestionarioResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody AtividadeQuestionarioUpdateDto atividadeQuestionarioUpdateDto) {
        
        AtividadeQuestionarioResponseDto responseDto = atividadeQuestionarioService.updateAtividadeQuestionario(id, atividadeQuestionarioUpdateDto);
        return ResponseEntity.ok(responseDto);
    }

   @PostMapping("/{idQuestionario}/questoes")
    public ResponseEntity<AtividadeQuestionarioResponseDto> adicionarQuestoesAoQuestionario(
            @PathVariable Long idQuestionario,
            @RequestBody List<Long> idsDasQuestoes) {

        AtividadeQuestionarioResponseDto questionarioAtualizado =
                atividadeQuestionarioService.adicionarQuestoes(idQuestionario, idsDasQuestoes);

        return ResponseEntity.ok(questionarioAtualizado);
    }


    @DeleteMapping("/{idQuestionario}/questoes")
    public ResponseEntity<AtividadeQuestionarioResponseDto> removerQuestoesDoQuestionario(
            @PathVariable Long idQuestionario,
            @RequestBody List<Long> idsDasQuestoes) {

        try {
            AtividadeQuestionarioResponseDto questionarioAtualizado = atividadeQuestionarioService.removerQuestoes(idQuestionario, idsDasQuestoes);
            return ResponseEntity.ok(questionarioAtualizado);
        } catch (RuntimeException e) {
            // Idealmente, trate exceções específicas (ex: NotFoundException)
            return ResponseEntity.notFound().build();
        }
    }

        @DeleteMapping("/{idQuestionario}/questoes/todas")
    public ResponseEntity<AtividadeQuestionarioResponseDto> removerQuestoesDoQuestionario(
            @PathVariable Long idQuestionario) {

        try {
            AtividadeQuestionarioResponseDto questionarioAtualizado = atividadeQuestionarioService.removerQuestoes(idQuestionario);
            return ResponseEntity.ok(questionarioAtualizado);
        } catch (RuntimeException e) {
            // Idealmente, trate exceções específicas (ex: NotFoundException)
            return ResponseEntity.notFound().build();
        }
    }

    

    


    
}
