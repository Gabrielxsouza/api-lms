package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper; // <-- CORRIGIDO
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.repository.MaterialDeAulaRepository;
import br.ifsp.lms_api.repository.TopicosRepository;

@Service
public class MaterialDeAulaService {

    private final MaterialDeAulaRepository materialRepository;
    private final TopicosRepository topicosRepository;
    private final StorageService storageService;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public MaterialDeAulaService(MaterialDeAulaRepository materialRepository,
                                 TopicosRepository topicosRepository,
                                 StorageService storageService,
                                 ModelMapper modelMapper,
                                 PagedResponseMapper pagedResponseMapper) {
        this.materialRepository = materialRepository;
        this.topicosRepository = topicosRepository;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    public MaterialDeAulaResponseDto createMaterial(MultipartFile file, Long idTopico) {
        Topicos topico = topicosRepository.findById(idTopico)
                .orElseThrow(() -> new ResourceNotFoundException("Tópico com ID " + idTopico + " não encontrado"));

        String urlArquivo = storageService.createArquivo(file);

        MaterialDeAula novoMaterial = new MaterialDeAula();
        novoMaterial.setNomeArquivo(file.getOriginalFilename());
        novoMaterial.setTipoArquivo(file.getContentType());
        novoMaterial.setUrlArquivo(urlArquivo);
        novoMaterial.setTopico(topico); 

        novoMaterial = materialRepository.save(novoMaterial);
        return modelMapper.map(novoMaterial, MaterialDeAulaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public MaterialDeAulaResponseDto getMaterialById(Long id) {
        MaterialDeAula material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material com ID " + id + " nao encontrado"));
        return modelMapper.map(material, MaterialDeAulaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MaterialDeAulaResponseDto> getMaterialByTopico(Long idTopico, Pageable pageable) {
        Page<MaterialDeAula> materiais = materialRepository.findByTopicoIdTopico(idTopico, pageable);
        return pagedResponseMapper.toPagedResponse(materiais, MaterialDeAulaResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MaterialDeAulaResponseDto> getAllMaterialDeAula(Pageable pageable) {
        Page<MaterialDeAula> materiais = materialRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(materiais, MaterialDeAulaResponseDto.class);
    }

    @Transactional
    public MaterialDeAulaResponseDto deleteMaterial(Long id) {
        MaterialDeAula material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material com ID " + id + " nao encontrado")); // <-- CORRIGIDO

        try {
            String urlArquivo = material.getUrlArquivo(); 
            if (urlArquivo != null && !urlArquivo.isEmpty()) {
                String nomeArquivo = urlArquivo.substring(urlArquivo.lastIndexOf('/') + 1);
                storageService.deleteFile(nomeArquivo);
            }
        } catch (Exception e) {
            System.err.println("Falha ao deletar arquivo fisico: " + e.getMessage());
            throw new RuntimeException("Nao foi possivel deletar o arquivo fisico. Rollback.", e);
        }

        materialRepository.delete(material);
        return modelMapper.map(material, MaterialDeAulaResponseDto.class);
    }

    @Transactional
    public MaterialDeAulaResponseDto updateMaterial(Long id, MultipartFile novoArquivo) { // <-- MUDANÇA: Recebe MultipartFile

        MaterialDeAula materialToUpdate = materialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Material com ID " + id + " nao encontrado")); // <-- CORRIGIDO

        String urlArquivoAntigo = materialToUpdate.getUrlArquivo();

        String urlNovoArquivo = storageService.createArquivo(novoArquivo);

        materialToUpdate.setNomeArquivo(novoArquivo.getOriginalFilename());
        materialToUpdate.setTipoArquivo(novoArquivo.getContentType());
        materialToUpdate.setUrlArquivo(urlNovoArquivo); 
        
        materialToUpdate = materialRepository.save(materialToUpdate);

        try {
            if (urlArquivoAntigo != null && !urlArquivoAntigo.isEmpty()) {
                String nomeArquivoAntigo = urlArquivoAntigo.substring(urlArquivoAntigo.lastIndexOf('/') + 1);
                storageService.deleteFile(nomeArquivoAntigo);
            }
        } catch (Exception e) {
            System.err.println("Falha ao deletar arquivo fisico antigo: " + e.getMessage());
        }
        
        return modelMapper.map(materialToUpdate, MaterialDeAulaResponseDto.class);
    }
}

