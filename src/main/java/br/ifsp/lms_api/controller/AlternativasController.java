package br.ifsp.lms_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AlternativasService;
import jakarta.validation.Valid;

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


@RestController
@Validated
@RequestMapping("/alternativas")
public class AlternativasController {

    private final AlternativasService alternativasService;

    public AlternativasController(AlternativasService alternativasService) {
        this.alternativasService = alternativasService;
    }

    @PostMapping
    public ResponseEntity<AlternativasResponseDto> createAlternativas(@Valid @RequestBody AlternativasRequestDto alternativas) {
        AlternativasResponseDto createdAlternativa = alternativasService.createAlternativa(alternativas);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlternativa);
    }
    
    @GetMapping
    public ResponseEntity<PagedResponse<AlternativasResponseDto>> getAllAlternativas(Pageable pageable) {
        return ResponseEntity.ok(alternativasService.getAllAlternativas(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AlternativasResponseDto> updateAlternativa(@PathVariable Long id, @RequestBody @Valid AlternativasUpdateDto alternativaDto){
        AlternativasResponseDto updatedAlternativa = alternativasService.updateAlternativa(id, alternativaDto);
        return ResponseEntity.ok(updatedAlternativa);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlternativa(@PathVariable Long id){
        alternativasService.deleteAlternativa(id);
        return ResponseEntity.noContent().build();
    }
    
}
