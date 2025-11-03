package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public TurmaService(TurmaRepository turmaRepository, DisciplinaRepository disciplinaRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    public TurmaResponseDto createTurma(TurmaRequestDto turmaRequestDto) {
        Disciplina disciplina = disciplinaRepository.findById(turmaRequestDto.getIdDisciplina())
            .orElseThrow(() -> new EntityNotFoundException("Disciplina com ID " + turmaRequestDto.getIdDisciplina() + " não encontrada"));
        Turma turma = modelMapper.map(turmaRequestDto, Turma.class);   
        turma.setDisciplina(disciplina); 
        turma.setIdTurma(null);       
        turma = turmaRepository.save(turma);

        return modelMapper.map(turma, TurmaResponseDto.class);
    }

    
}
