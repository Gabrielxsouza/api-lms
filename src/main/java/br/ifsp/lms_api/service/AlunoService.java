package br.ifsp.lms_api.service;

import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.dto.alunoDto.AlunoRequestDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoResponseDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder; // 1. IMPORTAR
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final PasswordEncoder passwordEncoder; // 2. INJETAR

    private static final String NOT_FOUND_MSG = "Aluno com ID %d não encontrado.";

    // 3. ATUALIZAR CONSTRUTOR
    public AlunoService(AlunoRepository alunoRepository,
                        ModelMapper modelMapper,
                        PagedResponseMapper pagedResponseMapper,
                        PasswordEncoder passwordEncoder) { // ADICIONAR
        this.alunoRepository = alunoRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.passwordEncoder = passwordEncoder; // ADICIONAR
    }

    @Transactional
    public AlunoResponseDto createAluno(AlunoRequestDto dto) {
        // Mapeia DTO "plano" para Entidade (herança funciona aqui)
        Aluno aluno = modelMapper.map(dto, Aluno.class);

        // 4. HASH DA SENHA ANTES DE SALVAR
        aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
        aluno.setIdUsuario(null); // Garante que é um INSERT

        Aluno savedAluno = alunoRepository.save(aluno);
        return modelMapper.map(savedAluno, AlunoResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AlunoResponseDto> getAllAlunos(Pageable pageable) {
        Page<Aluno> alunoPage = alunoRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(alunoPage, AlunoResponseDto.class);
    }

    @Transactional(readOnly = true)
    public AlunoResponseDto getAlunoById(Long id) {
        Aluno aluno = findEntityById(id);
        return modelMapper.map(aluno, AlunoResponseDto.class);
    }

    @Transactional
    public AlunoResponseDto updateAluno(Long id, AlunoUpdateDto dto) {
        Aluno aluno = findEntityById(id);
        applyUpdateFromDto(aluno, dto);
        Aluno updatedAluno = alunoRepository.save(aluno);
        return modelMapper.map(updatedAluno, AlunoResponseDto.class);
    }

    @Transactional
    public void deleteAluno(Long id) {
        Aluno aluno = findEntityById(id);
        alunoRepository.delete(aluno);
    }

    // --- Métodos Auxiliares ---

    private Aluno findEntityById(Long id) {
        return alunoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    private void applyUpdateFromDto(Aluno aluno, AlunoUpdateDto dto) {
        dto.getNome().ifPresent(aluno::setNome);
        dto.getEmail().ifPresent(aluno::setEmail);
        dto.getRa().ifPresent(aluno::setRa);

        // 5. LÓGICA DE ATUALIZAÇÃO DE SENHA
        // Só atualiza a senha se uma nova senha for enviada
        dto.getSenha().ifPresent(novaSenha -> {
            aluno.setSenha(passwordEncoder.encode(novaSenha));
        });
    }
}