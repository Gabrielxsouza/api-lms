package br.ifsp.lms_api.controller;


import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated; 
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.MaterialDeAulaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated 
@RequestMapping("/materiais")
@Tag(name = "Material de Aula", description = "Endpoints para upload e gerenciamento de arquivos de aula")
public class MaterialDeAulaController {

    private final MaterialDeAulaService materialService;

    public MaterialDeAulaController(MaterialDeAulaService materialService) {
        this.materialService = materialService;
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(
        summary = "Fazer upload de novo material",
        description = "Faz o upload de um arquivo (PDF, mídia, etc.) e o vincula a um tópico existente."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Arquivo enviado com sucesso",
        content = @Content(schema = @Schema(implementation = MaterialDeAulaResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Tópico não encontrado")
    @PostMapping(value = "/topico/{idTopico}", consumes = "multipart/form-data")
    public ResponseEntity<MaterialDeAulaResponseDto> uploadMaterial(
            @Parameter(description = "ID do tópico ao qual o material será vinculado") @Valid @PathVariable Long idTopico,
            @Parameter(description = "O arquivo a ser enviado") @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal CustomUserDetails usuarioLogado) {

        MaterialDeAulaResponseDto responseDto = materialService.createMaterial(arquivo, idTopico, usuarioLogado.getId());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(
        summary = "Listar todos os materiais",
        description = "Retorna uma lista paginada de todos os materiais de aula no sistema."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de materiais")
    @GetMapping
    public ResponseEntity<PagedResponse<MaterialDeAulaResponseDto>> getAllMaterialDeAula(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(materialService.getAllMaterialDeAula(pageable));
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(summary = "Buscar material por ID")
    @ApiResponse(
        responseCode = "200",
        description = "Material encontrado",
        content = @Content(schema = @Schema(implementation = MaterialDeAulaResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Material não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<MaterialDeAulaResponseDto> getMaterialById(
            @Parameter(description = "ID do material a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }


   @PreAuthorize("hasRole('ROLE_PROFESSOR')")
   @Operation(
       summary = "Deletar um material",
       description = "Deleta um material de aula (registro no banco) e seu arquivo físico correspondente no storage."
   )
   @ApiResponse(
       responseCode = "200",
       description = "Material deletado com sucesso (retorna o objeto deletado)",
       content = @Content(schema = @Schema(implementation = MaterialDeAulaResponseDto.class))
   )
   @ApiResponse(responseCode = "404", description = "Material não encontrado")
   @DeleteMapping("/{id}")
    public ResponseEntity<MaterialDeAulaResponseDto> deleteMaterial(
            @Parameter(description = "ID do material a ser deletado") @PathVariable Long id, 
            @AuthenticationPrincipal CustomUserDetails usuarioLogado) {
        MaterialDeAulaResponseDto deletedMaterial = materialService.deleteMaterial(id, usuarioLogado.getId());
        return ResponseEntity.ok(deletedMaterial); 
    }

    
    @PreAuthorize("hasAnyRole('ROLE_PROFESSOR', 'ROLE_ALUNO')")
    @Operation(
        summary = "Listar materiais por tópico",
        description = "Retorna uma lista paginada de todos os materiais de aula vinculados a um tópico específico."
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de materiais do tópico")
    @GetMapping("/topico/{idTopico}") 
    public ResponseEntity<PagedResponse<MaterialDeAulaResponseDto>> getMaterialByTopico(
            @Parameter(description = "ID do tópico para buscar os materiais") @Valid @PathVariable Long idTopico, 
            @Parameter(description = "Parâmetros de paginação (page, size, sort)") Pageable pageable) {
        
        
        return ResponseEntity.ok(materialService.getMaterialByTopico(idTopico, pageable));
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @Operation(
        summary = "Atualizar arquivo de um material",
        description = "Substitui o arquivo físico de um material de aula existente por um novo."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Arquivo atualizado com sucesso",
        content = @Content(schema = @Schema(implementation = MaterialDeAulaResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Material não encontrado")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<MaterialDeAulaResponseDto> updateMaterial(
            @Parameter(description = "ID do material a ser atualizado") @PathVariable Long id, 
            @Parameter(description = "O novo arquivo") @Valid @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal CustomUserDetails usuarioLogado) { 

        return ResponseEntity.ok(materialService.updateMaterial(id, arquivo, usuarioLogado.getId()));
    }
}