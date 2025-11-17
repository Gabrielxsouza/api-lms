package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoRequestDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoResponseDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.TentativaTextoService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;



@RestController
@Validated
@RequestMapping("/tentativaTexto")
public class TentativaTextoController {
    private final TentativaTextoService tentativaTextoService;

    public TentativaTextoController(TentativaTextoService tentativaTextoService) {
        this.tentativaTextoService = tentativaTextoService;
    }

    @PreAuthorize("hasRole('ALUNO')") 
    @PostMapping("/{idAtividade}") 
    public TentativaTextoResponseDto createTentativaTexto(
        
        @Validated @RequestBody TentativaTextoRequestDto tentativaRequest, 
        
        @AuthenticationPrincipal CustomUserDetails usuarioLogado,
        @PathVariable("idAtividade") Long idAtividade) {
        
        Long idAlunoLogado = usuarioLogado.getId();
        
        return tentativaTextoService.createTentativaTexto(tentativaRequest, idAlunoLogado, idAtividade);
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PatchMapping("/professor/{idTentativa}")
    public TentativaTextoResponseDto updateTentativaTextoProfessor(@Validated @RequestBody TentativaTextoUpdateDto tentativaRequest, @PathVariable("idTentativa") Long idTentativa) {
        return tentativaTextoService.updateTentativaTextoProfessor(tentativaRequest, idTentativa);
    }

    @PreAuthorize("hasRole('ALUNO')")
    @PatchMapping("/aluno/{idTentativa}") 
    public TentativaTextoResponseDto updateMinhaTentativa(
            @AuthenticationPrincipal CustomUserDetails usuarioLogado, 
            @PathVariable Long idTentativa,
            @RequestBody TentativaTextoUpdateDto tentativaUpdate) {
        
        Long idAlunoLogado = usuarioLogado.getId(); 
        

        return tentativaTextoService.updateTentativaTextoAluno(tentativaUpdate, idTentativa, idAlunoLogado);
    }
    
    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping
    public PagedResponse<TentativaTextoResponseDto> getAllTentativasTexto(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        return tentativaTextoService.getAllTentativasTexto(pageable);
    }
    
    @PreAuthorize("hasRole('ALUNO')")
    @DeleteMapping("/{idTentativa}")
    public TentativaTextoResponseDto deleteTentativaTexto(@PathVariable Long idTentativa, @AuthenticationPrincipal CustomUserDetails usuarioLogado) {
        Long idAlunoLogado = usuarioLogado.getId();
        return tentativaTextoService.deleteTentativaTexto(idTentativa, idAlunoLogado);
    }
    
}
