package br.ifsp.lms_api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaUpdateDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;

@Service
public class DisciplinaService {
    private final DisciplinaRepository disciplinaRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper; // Mantemos a injeção, mas talvez não usemos diretamente no getAll

    private static final String NOT_FOUND_MSG = "Disciplina com ID %d não encontrada.";

    public DisciplinaService(DisciplinaRepository disciplinaRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.disciplinaRepository = disciplinaRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public DisciplinaResponseDto createDisciplina(DisciplinaRequestDto disciplinaRequestDto) {
        Disciplina disciplina = modelMapper.map(disciplinaRequestDto, Disciplina.class);

        if (disciplinaRequestDto.getTurmas() != null) {
            List<Turma> turmas = disciplinaRequestDto.getTurmas().stream()
                .map(turmaDto -> {
                    Turma turma = modelMapper.map(turmaDto, Turma.class);
                    turma.setDisciplina(disciplina);
                    return turma;
                })
                .collect(Collectors.toList());
            disciplina.setTurmas(turmas);
        }

        Disciplina savedDisciplina = disciplinaRepository.save(disciplina);

        // CORREÇÃO: Conversão manual
        return convertToDto(savedDisciplina);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DisciplinaResponseDto> getAllDisciplinas(Pageable pageable) {
        Page<Disciplina> disciplinaPage = disciplinaRepository.findAll(pageable);
        
        // CORREÇÃO: Conversão manual da lista de entidades para lista de DTOs
        List<DisciplinaResponseDto> dtos = disciplinaPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        // Criar PagedResponse manualmente para evitar o erro do ModelMapper no PagedResponseMapper
        return new PagedResponse<>(
            dtos,
            disciplinaPage.getNumber(),
            disciplinaPage.getSize(),
            disciplinaPage.getTotalElements(),
            disciplinaPage.getTotalPages(),
            disciplinaPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public DisciplinaResponseDto getDisciplinaById(Long id) {
        Disciplina disciplina = findEntityById(id);
        // CORREÇÃO: Conversão manual
        return convertToDto(disciplina);
    }

    @Transactional
    public DisciplinaResponseDto updateDisciplina(Long id, DisciplinaUpdateDto updateDto) {
        Disciplina disciplina = findEntityById(id);

        updateDto.getNomeDisciplina().ifPresent(disciplina::setNomeDisciplina);
        updateDto.getDescricaoDisciplina().ifPresent(disciplina::setDescricaoDisciplina);
        updateDto.getCodigoDisciplina().ifPresent(disciplina::setCodigoDisciplina);

        Disciplina updatedDisciplina = disciplinaRepository.save(disciplina);
        // CORREÇÃO: Conversão manual
        return convertToDto(updatedDisciplina);
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

    // --- MÉTODO AUXILIAR PARA CONVERSÃO SEGURA ---
    private DisciplinaResponseDto convertToDto(Disciplina disciplina) {
        DisciplinaResponseDto dto = new DisciplinaResponseDto();
        
        // Mapeia campos simples manualmente
        dto.setIdDisciplina(disciplina.getIdDisciplina());
        dto.setNomeDisciplina(disciplina.getNomeDisciplina());
        dto.setDescricaoDisciplina(disciplina.getDescricaoDisciplina());
        dto.setCodigoDisciplina(disciplina.getCodigoDisciplina());
        
        // Mapeia a lista de turmas manualmente, item por item
        if (disciplina.getTurmas() != null) {
            List<TurmaResponseDto> turmaDtos = disciplina.getTurmas().stream()
                .map(turma -> {
                    // Aqui usamos o ModelMapper para converter UMA turma isolada (seguro)
                    // OU criamos manualmente se TurmaResponseDto for complexo
                    return modelMapper.map(turma, TurmaResponseDto.class);
                })
                .collect(Collectors.toList());
            dto.setTurmas(turmaDtos);
        }
        
        return dto;
    }
}