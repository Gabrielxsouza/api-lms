package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.professorDto.ProfessorRequestDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorResponseDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.repository.ProfessorRepository;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String NOT_FOUND_MSG = "Professor com ID %d não encontrado.";

    public ProfessorService(ProfessorRepository professorRepository,
                            ModelMapper modelMapper,
                            PagedResponseMapper pagedResponseMapper,
                            PasswordEncoder passwordEncoder) {
        this.professorRepository = professorRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ProfessorResponseDto createProfessor(ProfessorRequestDto dto) {
        Professor professor = modelMapper.map(dto, Professor.class);

        professor.setSenha(passwordEncoder.encode(dto.getSenha()));
        professor.setIdUsuario(null); 

        Professor savedProfessor = professorRepository.save(professor);
        return modelMapper.map(savedProfessor, ProfessorResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProfessorResponseDto> getAllProfessores(Pageable pageable) {
        Page<Professor> professorPage = professorRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(professorPage, ProfessorResponseDto.class);
    }

    @Transactional(readOnly = true)
    public ProfessorResponseDto getProfessorById(Long id) {
        Professor professor = findEntityById(id);
        return modelMapper.map(professor, ProfessorResponseDto.class);
    }

    @Transactional
    public ProfessorResponseDto updateProfessor(Long id, ProfessorUpdateDto dto) {
        Professor professor = findEntityById(id);
        applyUpdateFromDto(professor, dto);
        Professor updatedProfessor = professorRepository.save(professor);
        return modelMapper.map(updatedProfessor, ProfessorResponseDto.class);
    }

    @Transactional
    public void deleteProfessor(Long id) {
        Professor professor = findEntityById(id);
        professorRepository.delete(professor);
    }

    // --- Métodos Auxiliares ---

    private Professor findEntityById(Long id) {
        return professorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    private void applyUpdateFromDto(Professor professor, ProfessorUpdateDto dto) {
        dto.getNome().ifPresent(professor::setNome);
        dto.getEmail().ifPresent(professor::setEmail);
        dto.getDepartamento().ifPresent(professor::setDepartamento);

        dto.getSenha().ifPresent(novaSenha -> {
            professor.setSenha(passwordEncoder.encode(novaSenha));
        });
    }
}