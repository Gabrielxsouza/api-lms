package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisciplinaService {
    private final DisciplinaRepository disciplinaRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    private static final String NOT_FOUND_MSG = "Disciplina com ID %d não encontrada.";

    public DisciplinaService(DisciplinaRepository disciplinaRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.disciplinaRepository = disciplinaRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public DisciplinaResponseDto createDisciplina(DisciplinaRequestDto disciplinaRequestDto) {
        Disciplina disciplina = modelMapper.map(disciplinaRequestDto, Disciplina.class);

        List<Turma> turmas = disciplinaRequestDto.getTurmas().stream()
            .map(turmaDto -> {

                Turma turma = modelMapper.map(turmaDto, Turma.class);
                turma.setDisciplina(disciplina);
                return turma;
            })
            .collect(Collectors.toList());

        disciplina.setTurmas(turmas);

        Disciplina savedDisciplina = disciplinaRepository.save(disciplina);

        return modelMapper.map(savedDisciplina, DisciplinaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DisciplinaResponseDto> getAllDisciplinas(Pageable pageable) {
        Page<Disciplina> disciplinaPage = disciplinaRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(disciplinaPage, DisciplinaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public DisciplinaResponseDto getDisciplinaById(Long id) {
        Disciplina disciplina = findEntityById(id);
        return modelMapper.map(disciplina, DisciplinaResponseDto.class);
    }

    @Transactional
    public DisciplinaResponseDto updateDisciplina(Long id, DisciplinaUpdateDto updateDto) {
        Disciplina disciplina = findEntityById(id);

        updateDto.getNomeDisciplina().ifPresent(disciplina::setNomeDisciplina);
        updateDto.getDescricaoDisciplina().ifPresent(disciplina::setDescricaoDisciplina);
        updateDto.getCodigoDisciplina().ifPresent(disciplina::setCodigoDisciplina);

        Disciplina updatedDisciplina = disciplinaRepository.save(disciplina);
        return modelMapper.map(updatedDisciplina, DisciplinaResponseDto.class);
    }

    @Transactional
    public void deleteDisciplina(Long id) {
        Disciplina disciplina = findEntityById(id);
        disciplinaRepository.delete(disciplina);
    }

    private Disciplina findEntityById(Long id) {
        return disciplinaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }
}