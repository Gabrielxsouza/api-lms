package br.ifsp.lms_api.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioRequestDto;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioResponseDto;
import br.ifsp.lms_api.service.TentativaQuestionarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.page.PagedResponse;




@RestController
@Validated
@RequestMapping("/tentativaQuestionario")
public class TentativaQuestionarioController {
    private final TentativaQuestionarioService tentativaQuestionarioService;

    public TentativaQuestionarioController(TentativaQuestionarioService tentativaQuestionarioService) {
        this.tentativaQuestionarioService = tentativaQuestionarioService;
    }

    @PreAuthorize("hasRole('ROLE_ALUNO')")
    @PostMapping
    public TentativaQuestionarioResponseDto createTentativaQuestionario(
        @Validated @RequestBody TentativaQuestionarioRequestDto tentativaRequest,
        @AuthenticationPrincipal CustomUserDetails usuarioLogado) { 
    Long idAlunoLogado = usuarioLogado.getId();
    return tentativaQuestionarioService.createTentativaQuestionario(tentativaRequest, idAlunoLogado);
}

    @GetMapping
    public PagedResponse<TentativaQuestionarioResponseDto> getAllTentativasQuestionario(
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return tentativaQuestionarioService.getAllTentativasQuestionario(pageable); 
    }

    @GetMapping("aluno/{alunoId}")
    public PagedResponse<TentativaQuestionarioResponseDto> getTentativasQuestionarioByAlunoId(@PathVariable Long alunoId, @RequestParam(required = false) Pageable pageable) {
        return tentativaQuestionarioService.getTentativasQuestionarioByAlunoId(alunoId, pageable);
    }

    @DeleteMapping("/{idTentativa}")
    public TentativaQuestionarioResponseDto deleteTentativaQuestionario(@PathVariable Long idTentativa) {
        return tentativaQuestionarioService.deleteTentativaQuestionario(idTentativa);
    }
    


    
    

    
    
}
