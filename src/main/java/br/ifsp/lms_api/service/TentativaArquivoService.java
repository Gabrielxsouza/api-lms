package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

// Importações
import br.ifsp.lms_api.repository.TentativaArquivoRepository;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.TentativaArquivo;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoResponseDto;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoUpdateDto;
import br.ifsp.lms_api.integration.LearningServiceClient;
import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.exception.AccessDeniedException; // <-- RE-ADICIONADO (pois o controller exige)

@Service
public class TentativaArquivoService {

    private final TentativaArquivoRepository tentativaArquivoRepository;
    private final AlunoRepository alunoRepository;
    private final LearningServiceClient learningServiceClient;
    private final StorageService storageService;
    private final ModelMapper modelMapper;

    // Construtor completo
    public TentativaArquivoService(TentativaArquivoRepository tentativaArquivoRepository,
            AlunoRepository alunoRepository,
            LearningServiceClient learningServiceClient,
            StorageService storageService,
            ModelMapper modelMapper) {
        this.tentativaArquivoRepository = tentativaArquivoRepository;
        this.alunoRepository = alunoRepository;
        this.learningServiceClient = learningServiceClient;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
    }

    // Método CREATE (não muda)
    @Transactional
    public TentativaArquivoResponseDto createTentativaArquivo(
            MultipartFile file,
            Long idAlunoLogado,
            Long idAtividade) {

        Aluno aluno = alunoRepository.findById(idAlunoLogado)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado"));

        // Verify activity exists via Client
        AtividadesResponseDto atividadeDto = learningServiceClient.getAtividadeById(idAtividade);
        if (atividadeDto == null) {
            throw new EntityNotFoundException("Atividade não encontrada");
        }
        // Ideally enforce type check, but assuming ID is correct from caller.
        // We could check instanceof AtividadeArquivosResponseDto if needed.

        String urlArquivo = storageService.createArquivo(file);

        TentativaArquivo novaTentativa = new TentativaArquivo();
        novaTentativa.setAluno(aluno);
        novaTentativa.setIdAtividade(idAtividade);
        novaTentativa.setNomeArquivo(file.getOriginalFilename());
        novaTentativa.setTipoArquivo(file.getContentType());
        novaTentativa.setUrlArquivo(urlArquivo);

        TentativaArquivo tentativaSalva = tentativaArquivoRepository.save(novaTentativa);
        return modelMapper.map(tentativaSalva, TentativaArquivoResponseDto.class);
    }

    @Transactional
    public TentativaArquivoResponseDto updateTentativaArquivoProfessor(
            TentativaArquivoUpdateDto tentativaUpdate, Long idTentativa) {

        TentativaArquivo tentativa = tentativaArquivoRepository.findById(idTentativa)
                .orElseThrow(() -> new EntityNotFoundException("Tentativa de Arquivo nao encontrada"));

        tentativaUpdate.getNota().ifPresent(tentativa::setNota);
        tentativaUpdate.getFeedback().ifPresent(tentativa::setFeedBack);

        TentativaArquivo tentativaSalva = tentativaArquivoRepository.save(tentativa);
        return modelMapper.map(tentativaSalva, TentativaArquivoResponseDto.class);
    }

    @Transactional
    public TentativaArquivoResponseDto updateTentativaArquivoAluno(
            Long idTentativa, Long idAlunoLogado, MultipartFile novoArquivo) {

        TentativaArquivo tentativa = tentativaArquivoRepository.findById(idTentativa)
                .orElseThrow(() -> new EntityNotFoundException("Tentativa de Arquivo nao encontrada"));

        if (!tentativa.getAluno().getIdUsuario().equals(idAlunoLogado)) {
            throw new AccessDeniedException("Você não tem permissão para editar a tentativa de outro aluno.");
        }

        if (tentativa.getNota() != null) {
            throw new AccessDeniedException("Não é possível editar uma tentativa que já foi avaliada.");
        }

        String urlArquivoAntigo = tentativa.getUrlArquivo();

        String urlNovoArquivo = storageService.createArquivo(novoArquivo);

        tentativa.setNomeArquivo(novoArquivo.getOriginalFilename());
        tentativa.setTipoArquivo(novoArquivo.getContentType());
        tentativa.setUrlArquivo(urlNovoArquivo);

        TentativaArquivo tentativaSalva = tentativaArquivoRepository.save(tentativa);

        try {
            if (urlArquivoAntigo != null && !urlArquivoAntigo.isEmpty()) {
                String nomeArquivoAntigo = urlArquivoAntigo.substring(urlArquivoAntigo.lastIndexOf('/') + 1);
                storageService.deleteFile(nomeArquivoAntigo);
            }
        } catch (Exception e) {
            System.err.println("Falha ao deletar arquivo fisico antigo: " + e.getMessage());
        }

        return modelMapper.map(tentativaSalva, TentativaArquivoResponseDto.class);
    }

    @Transactional
    public TentativaArquivoResponseDto deleteTentativaArquivo(Long idTentativa, Long idAlunoLogado) { // <-- ASSINATURA
                                                                                                      // MUDOU

        TentativaArquivo tentativa = tentativaArquivoRepository.findById(idTentativa)
                .orElseThrow(() -> new EntityNotFoundException("Tentativa com ID " + idTentativa + " nao encontrada"));

        if (!tentativa.getAluno().getIdUsuario().equals(idAlunoLogado)) {
            throw new AccessDeniedException("Você não tem permissão para deletar a tentativa de outro aluno.");
        }

        if (tentativa.getNota() != null) {
            throw new AccessDeniedException("Não é possível deletar uma tentativa que já foi avaliada.");
        }

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

        tentativaArquivoRepository.delete(tentativa);

        return modelMapper.map(tentativa, TentativaArquivoResponseDto.class);
    }
}