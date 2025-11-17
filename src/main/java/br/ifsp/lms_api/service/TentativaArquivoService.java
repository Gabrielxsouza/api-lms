package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

// Repositórios
import br.ifsp.lms_api.repository.TentativaArquivoRepository;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.AtividadeArquivosRepository; 

// Modelos
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.model.TentativaArquivo;

// DTOs
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoResponseDto;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoUpdateDto;

@Service
public class TentativaArquivoService {

    private final TentativaArquivoRepository tentativaArquivoRepository;
    private final AlunoRepository alunoRepository;
    private final AtividadeArquivosRepository atividadeArquivosRepository;
    private final StorageService storageService; // (do MaterialDeAulaService)
    private final ModelMapper modelMapper;

    // Construtor completo
    public TentativaArquivoService(TentativaArquivoRepository tentativaArquivoRepository,
                                 AlunoRepository alunoRepository,
                                 AtividadeArquivosRepository atividadeArquivosRepository,
                                 StorageService storageService,
                                 ModelMapper modelMapper) {
        this.tentativaArquivoRepository = tentativaArquivoRepository;
        this.alunoRepository = alunoRepository;
        this.atividadeArquivosRepository = atividadeArquivosRepository;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
    }

    /**
     * Método de CRIAÇÃO (para o Aluno).
     */
    @Transactional
    public TentativaArquivoResponseDto createTentativaArquivo(
            MultipartFile file, 
            Long idAlunoLogado, // Recebe o ID do aluno (do controller)
            Long idAtividade) {

        // 1. Busca Aluno e Atividade (do TentativaTextoService)
        Aluno aluno = alunoRepository.findById(idAlunoLogado)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado"));
        
        AtividadeArquivos atividade = atividadeArquivosRepository.findById(idAtividade)
                .orElseThrow(() -> new EntityNotFoundException("Atividade de Arquivo não encontrada"));

        // 2. Salva o arquivo (do MaterialDeAulaService)
        String urlArquivo = storageService.createArquivo(file);

        // 3. Cria e Salva a nova entidade
        TentativaArquivo novaTentativa = new TentativaArquivo();
        novaTentativa.setAluno(aluno);
        novaTentativa.setAtividadeArquivo(atividade);
        
        // Dados do arquivo
        novaTentativa.setNomeArquivo(file.getOriginalFilename());
        novaTentativa.setTipoArquivo(file.getContentType());
        novaTentativa.setUrlArquivo(urlArquivo);
        
        TentativaArquivo tentativaSalva = tentativaArquivoRepository.save(novaTentativa);

        return modelMapper.map(tentativaSalva, TentativaArquivoResponseDto.class);
    }

    /**
     * Método de ATUALIZAÇÃO (para o Professor dar nota/feedback).
     * Lógica idêntica ao updateTentativaTextoProfessor.
     */
    @Transactional
    public TentativaArquivoResponseDto updateTentativaArquivoProfessor(
            TentativaArquivoUpdateDto tentativaUpdate, Long idTentativa) {

        TentativaArquivo tentativa = tentativaArquivoRepository.findById(idTentativa)
                .orElseThrow(() -> new EntityNotFoundException("Tentativa de Arquivo nao encontrada"));

        // Atualiza nota e feedback
        tentativaUpdate.getNota().ifPresent(tentativa::setNota);
        tentativaUpdate.getFeedback().ifPresent(tentativa::setFeedBack); 

        TentativaArquivo tentativaSalva = tentativaArquivoRepository.save(tentativa);
        return modelMapper.map(tentativaSalva, TentativaArquivoResponseDto.class);
    }

    /**
     * Método de DELEÇÃO.
     * Deleta o ARQUIVO FÍSICO e a ENTIDADE.
     */
    @Transactional
    public TentativaArquivoResponseDto deleteTentativaArquivo(Long idTentativa) {
        TentativaArquivo tentativa = tentativaArquivoRepository.findById(idTentativa)
            .orElseThrow(() -> new EntityNotFoundException("Tentativa com ID " + idTentativa + " nao encontrada"));

        // 1. Deletar arquivo do storage (do MaterialDeAulaService)
        try {
            String urlArquivo = tentativa.getUrlArquivo(); 
            if (urlArquivo != null && !urlArquivo.isEmpty()) {
                String nomeArquivo = urlArquivo.substring(urlArquivo.lastIndexOf('/') + 1);
                storageService.deleteFile(nomeArquivo);
            }
        } catch (Exception e) {
            System.err.println("Falha ao deletar arquivo fisico: " + e.getMessage());
            throw new RuntimeException("Nao foi possivel deletar o arquivo fisico. Rollback.", e);
        }

        // 2. Deletar entidade do banco
        tentativaArquivoRepository.delete(tentativa);

        return modelMapper.map(tentativa, TentativaArquivoResponseDto.class);
    }
}