package br.ifsp.lms_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.service.TentativaArquivoService;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoResponseDto;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoUpdateDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/tentativaArquivo")
@Tag(name = "Tentativa de Arquivo", description = "Endpoints para submissão e correção de atividades de arquivo")
public class TentativaArquivoController {

    private final TentativaArquivoService tentativaArquivoService;

    public TentativaArquivoController(TentativaArquivoService tentativaArquivoService) {
        this.tentativaArquivoService = tentativaArquivoService;
    }

    @PreAuthorize("hasRole('ALUNO')")
    @Operation(
        summary = "Submeter tentativa de arquivo (Aluno)",
        description = "Faz o upload de um arquivo como tentativa para uma atividade específica."
    )
    @ApiResponse(responseCode = "200", description = "Arquivo enviado com sucesso", content = @Content(schema = @Schema(implementation = TentativaArquivoResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Acesso negado (Não é um ALUNO)")
    @ApiResponse(responseCode = "404", description = "Aluno ou Atividade não encontrada")
    @PostMapping(value = "/{idAtividade}", consumes = "multipart/form-data")
    public ResponseEntity<TentativaArquivoResponseDto> createTentativaArquivo(

        @Parameter(description = "O arquivo a ser enviado como tentativa")
        @RequestParam("arquivo") MultipartFile arquivo,

        @AuthenticationPrincipal CustomUserDetails usuarioLogado,

        @Parameter(description = "ID da atividade para a qual o arquivo está sendo enviado")
        @PathVariable("idAtividade") Long idAtividade) {

        Long idAlunoLogado = usuarioLogado.getId();

        TentativaArquivoResponseDto responseDto = tentativaArquivoService.createTentativaArquivo(
            arquivo, idAlunoLogado, idAtividade
        );
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(
        summary = "Corrigir tentativa (Professor)",
        description = "Permite a um Professor adicionar nota e feedback a uma tentativa de arquivo."
    )
    @ApiResponse(responseCode = "200", description = "Correção salva com sucesso", content = @Content(schema = @Schema(implementation = TentativaArquivoResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Acesso negado (Não é um PROFESSOR)")
    @ApiResponse(responseCode = "404", description = "Tentativa não encontrada")
    @PatchMapping("/professor/{idTentativa}")
    public ResponseEntity<TentativaArquivoResponseDto> updateTentativaArquivoProfessor(

            @Valid @RequestBody TentativaArquivoUpdateDto tentativaUpdate,

            @Parameter(description = "ID da tentativa a ser corrigida")
            @PathVariable("idTentativa") Long idTentativa) {

        TentativaArquivoResponseDto responseDto = tentativaArquivoService.updateTentativaArquivoProfessor(
            tentativaUpdate, idTentativa
        );
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('ALUNO')")
    @Operation(
        summary = "Substituir envio de arquivo (Aluno)",
        description = "Permite a um Aluno substituir um arquivo enviado, desde que a atividade ainda não tenha sido corrigida."
    )
    @ApiResponse(responseCode = "200", description = "Arquivo substituído com sucesso", content = @Content(schema = @Schema(implementation = TentativaArquivoResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Acesso negado (Não é o dono da tentativa ou a tentativa já foi corrigida)")
    @ApiResponse(responseCode = "404", description = "Tentativa não encontrada")
    @PutMapping(value = "/aluno/{idTentativa}", consumes = "multipart/form-data")
    public ResponseEntity<TentativaArquivoResponseDto> updateTentativaArquivoAluno(
        @AuthenticationPrincipal CustomUserDetails usuarioLogado,
        @Parameter(description = "ID da tentativa a ser substituída")
        @PathVariable Long idTentativa,
        @Parameter(description = "O novo arquivo a ser enviado")
        @RequestParam("arquivo") MultipartFile arquivo) {

        Long idAlunoLogado = usuarioLogado.getId();
        TentativaArquivoResponseDto responseDto = tentativaArquivoService.updateTentativaArquivoAluno(
            idTentativa, idAlunoLogado, arquivo
        );
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('ALUNO')")
    @Operation(
        summary = "Deletar tentativa de arquivo (Aluno)",
        description = "Deleta uma tentativa (registro no banco) e seu arquivo físico correspondente."
    )
    @ApiResponse(responseCode = "200", description = "Tentativa deletada com sucesso", content = @Content(schema = @Schema(implementation = TentativaArquivoResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Acesso negado (Não é o dono da tentativa ou já foi corrigida)") // <-- RESPOSTA ADICIONADA
    @ApiResponse(responseCode = "404", description = "Tentativa não encontrada")
    @DeleteMapping("/{idTentativa}")
    public ResponseEntity<TentativaArquivoResponseDto> deleteTentativaArquivo(
            @AuthenticationPrincipal CustomUserDetails usuarioLogado,
            @Parameter(description = "ID da tentativa a ser deletada")
            @PathVariable Long idTentativa) {

        Long idAlunoLogado = usuarioLogado.getId();

        TentativaArquivoResponseDto responseDto = tentativaArquivoService.deleteTentativaArquivo(idTentativa, idAlunoLogado);
        return ResponseEntity.ok(responseDto);
    }
}
