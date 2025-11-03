package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.repository.MaterialDeAulaRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class MaterialDeAulaService {

    private final MaterialDeAulaRepository materialRepository;
    private final TopicosRepository topicosRepository;
    private final StorageService storageService;
    private final ModelMapper modelMapper;

    public MaterialDeAulaService(MaterialDeAulaRepository materialRepository,
                                 TopicosRepository topicosRepository,
                                 StorageService storageService,
                                 ModelMapper modelMapper) {
        this.materialRepository = materialRepository;
        this.topicosRepository = topicosRepository;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
    }

    public MaterialDeAulaResponseDto salvarMaterial(MultipartFile file, Long idTopico) {
        // 1. Busca o Tópico "pai"
        Topicos topico = topicosRepository.findById(idTopico)
            .orElseThrow(() -> new EntityNotFoundException("Tópico com ID " + idTopico + " não encontrado"));

        // 2. Salva o arquivo físico e obtém a URL
        String urlArquivo = storageService.salvarArquivo(file);

        // 3. Cria a entidade MaterialDeAula
        MaterialDeAula novoMaterial = new MaterialDeAula();
        novoMaterial.setNomeArquivo(file.getOriginalFilename());
        novoMaterial.setTipoArquivo(file.getContentType());
        novoMaterial.setUrlArquivo(urlArquivo);
        novoMaterial.setTopico(topico); // <-- Associa ao Tópico

        // 4. Salva a entidade no banco
        novoMaterial = materialRepository.save(novoMaterial);

        // 5. Retorna o DTO de resposta
        return modelMapper.map(novoMaterial, MaterialDeAulaResponseDto.class);
    }
}