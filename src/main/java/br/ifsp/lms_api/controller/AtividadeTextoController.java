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
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.service.AtividadeTextoService;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/atividades-texto") 
public class AtividadeTextoController {

    private final AtividadeTextoService atividadeTextoService;

    public AtividadeTextoController(AtividadeTextoService atividadeTextoService) {
        this.atividadeTextoService = atividadeTextoService;
    }

    @PostMapping
    public ResponseEntity<AtividadeTextoResponseDto> create(
            @Valid @RequestBody AtividadeTextoRequestDto atividadeTextoRequestDto) {
        
        AtividadeTextoResponseDto responseDto = atividadeTextoService.createAtividadeTexto(atividadeTextoRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AtividadeTextoResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(atividadeTextoService.getAllAtividadesTexto(pageable));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<AtividadeTextoResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody AtividadeTextoUpdateDto atividadeTextoUpdateDto) {
        
        AtividadeTextoResponseDto responseDto = atividadeTextoService.updateAtividadeTexto(id, atividadeTextoUpdateDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        atividadeTextoService.deleteAtividadeTexto(id);
        return ResponseEntity.noContent().build();
    }
}