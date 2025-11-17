package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.dto.adminDto.AdminRequestDto;
import br.ifsp.lms_api.dto.adminDto.AdminResponseDto;
import br.ifsp.lms_api.dto.adminDto.AdminUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.repository.AdministradorRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public AdministradorService(AdministradorRepository administradorRepository,
            ModelMapper modelMapper,
            PagedResponseMapper pagedResponseMapper) {
        this.administradorRepository = administradorRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public AdminResponseDto createAdmin(AdminRequestDto requestDto) {
        Administrador administrador = modelMapper.map(requestDto, Administrador.class);
        Administrador savedAdmin = administradorRepository.save(administrador);
        return modelMapper.map(savedAdmin, AdminResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminResponseDto> getAllAdmin(Pageable pageable) {
        Page<Administrador> admins = administradorRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(admins, AdminResponseDto.class);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        if (!administradorRepository.existsById(id)) {
        throw new RuntimeException("Administrador com id " + id + " nao encontrado");
        }
        administradorRepository.deleteById(id);
    }

    @Transactional
    public AdminResponseDto updateAdmin(Long id, AdminUpdateDto dto) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));
        if (dto.getNome().isPresent()) {
            administrador.setNome(dto.getNome().get());
        }
        if (dto.getEmail().isPresent()) {
            administrador.setEmail(dto.getEmail().get());
        }
        Administrador updatedAdmin = administradorRepository.save(administrador);
        return modelMapper.map(updatedAdmin, AdminResponseDto.class);
    }

}
