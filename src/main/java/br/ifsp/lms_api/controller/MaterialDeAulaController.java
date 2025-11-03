package br.ifsp.lms_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.service.MaterialDeAulaService;

@RestController
@RequestMapping("/materiais")
public class MaterialDeAulaController {

    private final MaterialDeAulaService materialService;

    public MaterialDeAulaController(MaterialDeAulaService materialService) {
        this.materialService = materialService;
    }

    /**
     * Endpoint para associar um novo material a um tópico.
     * Use {idTopico} para manter a hierarquia REST.
     */
    @PostMapping("/topico/{idTopico}")
    public ResponseEntity<MaterialDeAulaResponseDto> uploadMaterial(
            @PathVariable Long idTopico,
            @RequestParam("arquivo") MultipartFile arquivo) {
        
        MaterialDeAulaResponseDto responseDto = materialService.salvarMaterial(arquivo, idTopico);
        return ResponseEntity.ok(responseDto);
    }
}