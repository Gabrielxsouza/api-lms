package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;
import br.ifsp.lms_api.model.Atividades;
import br.ifsp.lms_api.repository.AtividadesRepository;
import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
public class AtividadesService {
    private final AtividadesRepository atividadesRepository;
    private final ModelMapper modelMapper;

    public AtividadesService(AtividadesRepository atividadesRepository, ModelMapper modelMapper) {
        this.atividadesRepository = atividadesRepository;
        this.modelMapper = modelMapper;
    }

    public AtividadesRequestDto createAtividades(AtividadesRequestDto atividadesRequestDto) {
        
        return modelMapper.map(atividadesRepository.save(modelMapper.map(atividadesRequestDto, Atividades.class)), AtividadesRequestDto.class);
    }

    public Page<AtividadesResponseDto> getAllAtividades(Pageable pageable) {
        return atividadesRepository.findAll(pageable).map(atividades -> modelMapper.map(atividades, AtividadesResponseDto.class));
    }
}
