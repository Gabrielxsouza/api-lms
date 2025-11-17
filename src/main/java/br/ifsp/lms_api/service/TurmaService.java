package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@Service
public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final CursoRepository cursoRepository;
    private final ProfessorRepository professorRepository;
    private final AutentificacaoService autentificacaoService;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    private static final String DISCIPLINA_NOT_FOUND_MSG = "Disciplina com ID %d não encontrada.";
    private static final String CURSO_NOT_FOUND_MSG = "Curso com ID %d não encontrado.";
    private static final String PROFESSOR_NOT_FOUND_MSG = "Professor com ID %d não encontrado.";
    private static final String TURMA_NOT_FOUND_MSG = "Turma com ID %d não encontrada.";

    public TurmaService(TurmaRepository turmaRepository, 
                        DisciplinaRepository disciplinaRepository,
                        CursoRepository cursoRepository,
                        ProfessorRepository professorRepository,
                        AutentificacaoService autentificacaoService, 
                        ModelMapper modelMapper, 
                        PagedResponseMapper pagedResponseMapper) {
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
        this.autentificacaoService = autentificacaoService;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }


    @Transactional
    public TurmaResponseDto createTurma(TurmaRequestDto turmaRequestDto) {
        
        Disciplina disciplina = disciplinaRepository.findById(turmaRequestDto.getIdDisciplina())
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format(DISCIPLINA_NOT_FOUND_MSG, turmaRequestDto.getIdDisciplina())
            ));
        
        Curso curso = cursoRepository.findById(turmaRequestDto.getIdCurso())
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format(CURSO_NOT_FOUND_MSG, turmaRequestDto.getIdCurso())
            ));
        
        Professor professor = professorRepository.findById(turmaRequestDto.getIdProfessor())
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format(PROFESSOR_NOT_FOUND_MSG, turmaRequestDto.getIdProfessor())
            ));

        Turma turma = modelMapper.map(turmaRequestDto, Turma.class);
        turma.setIdTurma(null);
        turma.setDisciplina(disciplina);
        turma.setCurso(curso);
        turma.setProfessor(professor); 

        turma = turmaRepository.save(turma);

        return modelMapper.map(turma, TurmaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TurmaResponseDto> getAllTurmas(Pageable pageable) {
        Page<Turma> turmaPage = turmaRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(turmaPage, TurmaResponseDto.class);
    }
    
    @Transactional(readOnly = true)
    public PagedResponse<TurmaResponseDto> getMinhasTurmas(Pageable pageable) {
        
        Usuario usuarioLogado = autentificacaoService.getUsuarioLogado();
        if (!(usuarioLogado instanceof Professor)) {
            throw new AccessDeniedException("Apenas professores podem ver 'minhas turmas'.");
        }
        Professor professorLogado = (Professor) usuarioLogado;
        
        Page<Turma> turmaPage = turmaRepository.findByProfessor(professorLogado, pageable);
        return pagedResponseMapper.toPagedResponse(turmaPage, TurmaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public TurmaResponseDto getTurmaById(Long id) {
        Turma turma = findEntityById(id);
        return modelMapper.map(turma, TurmaResponseDto.class);
    }

    @Transactional
    public TurmaResponseDto updateTurma(Long id, TurmaUpdateDto updateDto) {
        Turma turma = findEntityById(id);
        applyUpdateFromDto(turma, updateDto);
        Turma updatedTurma = turmaRepository.save(turma);
        return modelMapper.map(updatedTurma, TurmaResponseDto.class);
    }

    @Transactional
    public void deleteTurma(Long id) {
        Turma turma = findEntityById(id);
        turmaRepository.delete(turma);
    }

    private Turma findEntityById(Long id) {
        return turmaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(TURMA_NOT_FOUND_MSG, id)));
    }

    private void applyUpdateFromDto(Turma turma, TurmaUpdateDto dto) {
        dto.getNomeTurma().ifPresent(turma::setNomeTurma);
        dto.getSemestre().ifPresent(turma::setSemestre);
    }
}