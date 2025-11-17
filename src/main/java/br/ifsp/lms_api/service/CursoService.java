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
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.repository.CursoRepository;

@Service
public class CursoService {
    private final CursoRepository cursoRepository;

    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    private static final String NOT_FOUND_MSG = "Curso com ID %d não encontrado.";


    public CursoService(CursoRepository cursoRepository,
                        ModelMapper modelMapper,
                        PagedResponseMapper pagedResponseMapper) {
        this.cursoRepository = cursoRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public CursoResponseDto createCurso(CursoRequestDto cursoRequestDto) {
        Curso curso = modelMapper.map(cursoRequestDto, Curso.class);


        Curso savedCurso = cursoRepository.save(curso);

        return convertCursoToDto(savedCurso);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CursoResponseDto> getAllCursos(Pageable pageable) {
        Page<Curso> cursoPage = cursoRepository.findAll(pageable);

        List<CursoResponseDto> dtoList = cursoPage.getContent().stream()
            .map(this::convertCursoToDto)
            .collect(Collectors.toList());

        return new PagedResponse<>(
            dtoList,
            cursoPage.getNumber(),
            cursoPage.getSize(),
            cursoPage.getTotalElements(),
            cursoPage.getTotalPages(),
            cursoPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public CursoResponseDto getCursoById(Long id) {
        Curso curso = findEntityById(id);
        return convertCursoToDto(curso);
    }

    @Transactional
    public CursoResponseDto updateCurso(Long id, CursoUpdateDto updateDto) {
        Curso curso = findEntityById(id);

        updateDto.getNomeCurso().ifPresent(curso::setNomeCurso);
        updateDto.getDescricaoCurso().ifPresent(curso::setDescricaoCurso);
        updateDto.getCodigoCurso().ifPresent(curso::setCodigoCurso);

        Curso updatedCurso = cursoRepository.save(curso);
        return convertCursoToDto(updatedCurso);
    }

    @Transactional
    public void deleteCurso(Long id) {
        Curso curso = findEntityById(id);
        cursoRepository.delete(curso);
    }

    private Curso findEntityById(Long id) {
        return cursoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    private CursoResponseDto convertCursoToDto(Curso curso) {
        CursoResponseDto responseDto = new CursoResponseDto();
        responseDto.setIdCurso(curso.getIdCurso());
        responseDto.setNomeCurso(curso.getNomeCurso());
        responseDto.setDescricaoCurso(curso.getDescricaoCurso());
        responseDto.setCodigoCurso(curso.getCodigoCurso());

        if (curso.getTurmas() != null) { 
            List<TurmaResponseDto> turmaDtos = curso.getTurmas()
                .stream()
                .map(turma -> modelMapper.map(turma, TurmaResponseDto.class))
                .collect(Collectors.toList());
            responseDto.setTurmas(turmaDtos);
        }

        return responseDto;
    }
}
