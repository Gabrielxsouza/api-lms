package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.repository.DisciplinaRepository;

@Service
public class DisciplinaService {
    private final DisciplinaRepository disciplinaRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.disciplinaRepository = disciplinaRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    public DisciplinaResponseDto createDisciplina(DisciplinaRequestDto disciplinaRequestDto) {
        return modelMapper.map(disciplinaRepository.save(modelMapper.map(disciplinaRequestDto, Disciplina.class)), DisciplinaResponseDto.class);
    }
}
