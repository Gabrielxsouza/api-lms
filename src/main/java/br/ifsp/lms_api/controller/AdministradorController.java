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

import br.ifsp.lms_api.dto.adminDto.AdminRequestDto;
import br.ifsp.lms_api.dto.adminDto.AdminResponseDto;
import br.ifsp.lms_api.dto.adminDto.AdminUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AdministradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
@Validated
public class AdministradorController {

    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @Operation(summary = "Criar novo administrador", description = "Cria um novo administrador no sistema.")
    @ApiResponse(responseCode = "201", description = "Administrador criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Entrada inválida (campos obrigatórios faltando ou mal formatados)")
    @ApiResponse(responseCode = "500", description = "Erro interno (ex: e-mail ou CPF já cadastrado)")
    @PostMapping
    public ResponseEntity<AdminResponseDto> create(@Valid @RequestBody AdminRequestDto requestDto) {
        AdminResponseDto responseDto = administradorService.createAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Busca todos os administradores", description = "Retorna uma lista de todos os administradores cadastrados no sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de administradores retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedResponse<AdminResponseDto>> getAllAdmin(
            @Parameter(description = "Parâmetros de paginação") Pageable pageable) {
        return ResponseEntity.ok(administradorService.getAllAdmin(pageable));
    }

    @Operation(summary = "Atualiza um administrador", description = "Atualiza os dados de um administrador existente.")
    @ApiResponse(responseCode = "200", description = "Administrador atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Entrada inválida (campos obrigatórios faltando ou mal formatados)")
    @ApiResponse(responseCode = "404", description = "Administrador não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno (ex: e-mail ou CPF já cadastrado)")
    @PatchMapping("/{id}")
    public ResponseEntity<AdminResponseDto> updateAdmin(
            @Parameter(description = "ID do administrador a ser atualizado") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateDto updateDto) {
        AdminResponseDto responseDto = administradorService.updateAdmin(id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Deleta um administrador", description = "Remove um administrador do sistema.")
    @ApiResponse(responseCode = "204", description = "Administrador deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Administrador não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@Parameter(description = "ID do administrador a ser deletado") @PathVariable Long id) {
        administradorService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

}
