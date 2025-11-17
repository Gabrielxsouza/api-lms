package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.dto.matriculaDto.MatriculaRequestDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaResponseDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Matricula;
import br.ifsp.lms_api.model.Status;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.MatriculaRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public MatriculaService(MatriculaRepository matriculaRepository, AlunoRepository alunoRepository,
            TurmaRepository turmaRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.matriculaRepository = matriculaRepository;
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public MatriculaResponseDto createMatricula(MatriculaRequestDto dto) {
        Aluno aluno = alunoRepository.findById(dto.getIdAluno())
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado com ID: " + dto.getIdAluno()));

        Turma turma = turmaRepository.findById(dto.getIdTurma())
                .orElseThrow(() -> new EntityNotFoundException("Turma não encontrada com ID: " + dto.getIdTurma()));

        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setStatusMatricula(dto.getStatusMatricula());

        Matricula savedMatricula = matriculaRepository.save(matricula);

        return new MatriculaResponseDto(savedMatricula);
    }

    @Transactional(readOnly = true)
    public MatriculaResponseDto getMatriculaById(Long id) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula com ID " + id + " não encontrada."));
        return modelMapper.map(matricula, MatriculaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MatriculaResponseDto> getAllMatriculas(Pageable pageable) {
        Page<Matricula> matriculaPage = matriculaRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(matriculaPage, MatriculaResponseDto.class);
    }

    @Transactional
    public void deleteMatricula(Long id) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula com ID " + id + " não encontrada."));
        matriculaRepository.delete(matricula);
    }

    @Transactional
    public MatriculaResponseDto updateMatricula(long id, MatriculaUpdateDto dto) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Matrícula com ID " + id + " não encontrada."));
        if (dto.getStatusMatricula().isPresent()) {
            String novoStatusStr = dto.getStatusMatricula().get();
            try {
                Status novoStatus = Status.valueOf(novoStatusStr.toUpperCase());
                matricula.setStatusMatricula(novoStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Valor de status inválido: " + novoStatusStr);
            }
        }
        Matricula updatedMatricula = matriculaRepository.save(matricula);
        return modelMapper.map(updatedMatricula, MatriculaResponseDto.class);
    }

}
