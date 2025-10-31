package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.repository.AlternativasRepository;

@Service
public class AlternativasService {
    private final AlternativasRepository alternativasRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public AlternativasService(AlternativasRepository alternativasRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.alternativasRepository = alternativasRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }


    public AlternativasResponseDto createAlternativa(AlternativasRequestDto alternativaRequestDto) {
        Alternativas alternativa = modelMapper.map(alternativaRequestDto, Alternativas.class);
        Alternativas savedAlternativa = alternativasRepository.save(alternativa);
        return modelMapper.map(savedAlternativa, AlternativasResponseDto.class);
    }

    public PagedResponse<AlternativasResponseDto> getAllAlternativas(Pageable pageable) {
        Page<Alternativas> alternativas = alternativasRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(alternativas, AlternativasResponseDto.class);
    }

    public AlternativasResponseDto updateAlternativa(Long id, AlternativasUpdateDto updateDto) {
        Alternativas existingAlternativa = alternativasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa not found with id: " + id));

        
        updateDto.getAlternativa().ifPresent(existingAlternativa::setAlternativa);
        updateDto.getAlternativaCorreta().ifPresent(existingAlternativa::setAlternativaCorreta);
        Alternativas updatedAlternativa = alternativasRepository.save(existingAlternativa);
        return modelMapper.map(updatedAlternativa, AlternativasResponseDto.class);
    }

    public void deleteAlternativa(Long id) {
        Alternativas alternativa = alternativasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa not found with id: " + id));
        alternativasRepository.delete(alternativa);
    }

}
