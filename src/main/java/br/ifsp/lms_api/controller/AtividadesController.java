package br.ifsp.lms_api.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;
import br.ifsp.lms_api.service.AtividadesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;




@RestController
@RequestMapping("/atividades")
@Tag(name = "Atividades", description = "Operações relacionadas a atividades")
@Validated
public class AtividadesController {
    private final AtividadesService atividadesService;

    public AtividadesController(AtividadesService atividadesService) {
        this.atividadesService = atividadesService;
    }

    @Operation(summary = "Cria uma atividade")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtividadesRequestDto createAtividades(@RequestBody AtividadesRequestDto atividadesRequestDto) {
        return atividadesService.createAtividades(atividadesRequestDto);
    }

    @Operation(summary = "Listar todas as atividades")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<AtividadesResponseDto> getAllAtividades(Pageable pageable) {
        return atividadesService.getAllAtividades(pageable);
    }
    
    
    
    
    
}
