package br.ifsp.lms_api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.dto.CursoDto.CursoRequestDto;
import br.ifsp.lms_api.dto.CursoDto.CursoResponseDto;
import br.ifsp.lms_api.dto.CursoDto.CursoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper; // <-- IMPORTAR
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma; // <-- IMPORTAR
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;

@Service
public class CursoService {
    private final CursoRepository cursoRepository;
    private final DisciplinaRepository disciplinaRepository; // <-- ADICIONAR
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper; 

    private static final String NOT_FOUND_MSG = "Curso com ID %d não encontrado.";
    private static final String DISC_NOT_FOUND_MSG = "Disciplina com ID %d não encontrada."; // <-- ADICIONAR

    // Atualize o construtor
    public CursoService(CursoRepository cursoRepository, 
                        DisciplinaRepository disciplinaRepository, // <-- ADICIONAR
                        ModelMapper modelMapper, 
                        PagedResponseMapper pagedResponseMapper) {
        this.cursoRepository = cursoRepository;
        this.disciplinaRepository = disciplinaRepository; // <-- ADICIONAR
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public CursoResponseDto createCurso(CursoRequestDto cursoRequestDto) {
        Curso curso = modelMapper.map(cursoRequestDto, Curso.class);

        // Lógica de criação de turmas aninhadas (ATUALIZADA)
        List<Turma> turmas = cursoRequestDto.getTurmas().stream()
            .map(turmaDto -> {
                // 1. Buscar a Disciplina (como fizemos antes)
                Disciplina disciplina = disciplinaRepository.findById(turmaDto.getDisciplinaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(DISC_NOT_FOUND_MSG, turmaDto.getDisciplinaId())
                    ));

                // 2. CORREÇÃO: Criar a Turma manualmente (evitando o ModelMapper)
                Turma turma = new Turma(); // <-- Garantia de ser 100% NOVO (transient)
                turma.setNomeTurma(turmaDto.getNomeTurma());
                turma.setSemestre(turmaDto.getSemestre());
                
                // 3. Ligar a Turma às suas duas entidades "pais"
                turma.setCurso(curso); 
                turma.setDisciplina(disciplina);
                
                return turma;
            })
            .collect(Collectors.toList());

        curso.setTurmas(turmas);
        Curso savedCurso = cursoRepository.save(curso);
        return modelMapper.map(savedCurso, CursoResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CursoResponseDto> getAllCursos(Pageable pageable) {
        Page<Curso> cursoPage = cursoRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(cursoPage, CursoResponseDto.class);
    }

    @Transactional(readOnly = true)
    public CursoResponseDto getCursoById(Long id) {
        Curso curso = findEntityById(id);
        return modelMapper.map(curso, CursoResponseDto.class);
    }

    @Transactional
    public CursoResponseDto updateCurso(Long id, CursoUpdateDto updateDto) {
        Curso curso = findEntityById(id);

        // Lógica de PATCH com Optional (igual à sua)
        updateDto.getNomeCurso().ifPresent(curso::setNomeCurso);
        updateDto.getDescricaoCurso().ifPresent(curso::setDescricaoCurso);
        updateDto.getCodigoCurso().ifPresent(curso::setCodigoCurso);

        Curso updatedCurso = cursoRepository.save(curso);
        return modelMapper.map(updatedCurso, CursoResponseDto.class);
    }

    @Transactional
    public void deleteCurso(Long id) {
        Curso curso = findEntityById(id);
        cursoRepository.delete(curso);
    }

    // Método helper para buscar entidade (igual ao seu)
    private Curso findEntityById(Long id) {
        return cursoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }
}