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
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.service.AtividadeArquivosService;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/atividades-arquivo")
public class AtividadeArquivosController {

    private final AtividadeArquivosService atividadeArquivosService;

    public AtividadeArquivosController(AtividadeArquivosService atividadeArquivosService) {
        this.atividadeArquivosService = atividadeArquivosService;
    }

    @PostMapping
    public ResponseEntity<AtividadeArquivosResponseDto> create(
            @Valid @RequestBody AtividadeArquivosRequestDto atividadeArquivosRequestDto) {
        
        AtividadeArquivosResponseDto responseDto = atividadeArquivosService.createAtividadeArquivos(atividadeArquivosRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AtividadeArquivosResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(atividadeArquivosService.getAllAtividadesArquivos(pageable));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<AtividadeArquivosResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody AtividadeArquivosUpdateDto atividadeArquivosUpdateDto) {
        
        AtividadeArquivosResponseDto responseDto = atividadeArquivosService.updateAtividadeArquivos(id, atividadeArquivosUpdateDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        atividadeArquivosService.deleteAtividadeArquivos(id);
        return ResponseEntity.noContent().build();
    }
}