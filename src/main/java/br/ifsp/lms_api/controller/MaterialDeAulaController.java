package br.ifsp.lms_api.controller;


import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.MaterialDeAulaService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/materiais")
public class MaterialDeAulaController {

    private final MaterialDeAulaService materialService;

    public MaterialDeAulaController(MaterialDeAulaService materialService) {
        this.materialService = materialService;
    }

    @PostMapping("/topico/{idTopico}")
    public ResponseEntity<MaterialDeAulaResponseDto> uploadMaterial(@Valid
            @PathVariable Long idTopico,
            @RequestParam("arquivo") MultipartFile arquivo) {
        
        MaterialDeAulaResponseDto responseDto = materialService.createMaterial(arquivo, idTopico);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<MaterialDeAulaResponseDto>> getAllMaterialDeAula(Pageable pageable) {
        return ResponseEntity.ok(materialService.getAllMaterialDeAula(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialDeAulaResponseDto> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MaterialDeAulaResponseDto> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public PagedResponse<MaterialDeAulaResponseDto> getMaterialByTopico(@Valid @PathVariable Long idTopico, Pageable pageable) {
        return materialService.getMaterialByTopico(idTopico, pageable);
    }
    
}