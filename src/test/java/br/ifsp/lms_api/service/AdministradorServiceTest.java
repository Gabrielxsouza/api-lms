package br.ifsp.lms_api.service;

import br.ifsp.lms_api.dto.adminDto.AdminRequestDto;
import br.ifsp.lms_api.dto.adminDto.AdminResponseDto;
import br.ifsp.lms_api.dto.adminDto.AdminUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.repository.AdministradorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministradorServiceTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private AdministradorService administradorService;

    @Test
    @DisplayName("Deve criar um administrador com sucesso")
    void shouldCreateAdminSuccessfully() {

        AdminRequestDto requestDto = new AdminRequestDto();
        requestDto.setNome("Admin Teste");
        requestDto.setEmail("admin@teste.com");

        Administrador administradorEntity = new Administrador();
        administradorEntity.setNome("Admin Teste");

        Administrador savedAdmin = new Administrador();
        savedAdmin.setIdUsuario(1L);
        savedAdmin.setNome("Admin Teste");

        AdminResponseDto responseDto = new AdminResponseDto();
        responseDto.setIdUsuario(1L);
        responseDto.setNome("Admin Teste");

        when(modelMapper.map(requestDto, Administrador.class)).thenReturn(administradorEntity);
        when(administradorRepository.save(administradorEntity)).thenReturn(savedAdmin);
        when(modelMapper.map(savedAdmin, AdminResponseDto.class)).thenReturn(responseDto);

        AdminResponseDto result = administradorService.createAdmin(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getIdUsuario());
        assertEquals("Admin Teste", result.getNome());

        verify(administradorRepository, times(1)).save(administradorEntity);
    }

    @Test
    @DisplayName("Deve retornar uma lista paginada de administradores")
    void shouldGetAllAdminsPaged() {

        Pageable pageable = PageRequest.of(0, 10);
        Administrador admin = new Administrador();
        List<Administrador> adminList = Collections.singletonList(admin);
        Page<Administrador> adminPage = new PageImpl<>(adminList);

        AdminResponseDto responseDto = new AdminResponseDto();
        PagedResponse<AdminResponseDto> pagedResponse = new PagedResponse<>(
                Collections.singletonList(responseDto), 0, 1, 1, 1, true);

        when(administradorRepository.findAll(pageable)).thenReturn(adminPage);

        when(pagedResponseMapper.toPagedResponse(adminPage, AdminResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<AdminResponseDto> result = administradorService.getAllAdmin(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(administradorRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve deletar um administrador pelo ID")
    void shouldDeleteAdmin() {

        Long id = 1L;

        when(administradorRepository.existsById(id)).thenReturn(true);

        doNothing().when(administradorRepository).deleteById(id);

        administradorService.deleteAdmin(id);

        verify(administradorRepository, times(1)).existsById(id);
        verify(administradorRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve atualizar um administrador existente com sucesso")
    void shouldUpdateAdminSuccessfully() {
        Long id = 1L;

        AdminUpdateDto updateDto = new AdminUpdateDto();
        updateDto.setNome(Optional.of("Nome Atualizado"));
        updateDto.setEmail(Optional.empty());
        updateDto.setSenha(Optional.empty());

        Administrador existingAdmin = new Administrador();
        existingAdmin.setIdUsuario(id);
        existingAdmin.setNome("Nome Antigo");

        Administrador updatedAdmin = new Administrador();
        updatedAdmin.setIdUsuario(id);
        updatedAdmin.setNome("Nome Atualizado");

        AdminResponseDto responseDto = new AdminResponseDto();
        responseDto.setIdUsuario(id);
        responseDto.setNome("Nome Atualizado");

        when(administradorRepository.findById(id)).thenReturn(Optional.of(existingAdmin));

        when(administradorRepository.save(any(Administrador.class))).thenReturn(updatedAdmin);

        when(modelMapper.map(updatedAdmin, AdminResponseDto.class)).thenReturn(responseDto);

        AdminResponseDto result = administradorService.updateAdmin(id, updateDto);

        assertNotNull(result);
        assertEquals("Nome Atualizado", result.getNome());

        verify(administradorRepository, times(1)).findById(id);
        verify(administradorRepository, times(1)).save(any(Administrador.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar administrador inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentAdmin() {

        Long id = 99L;
        AdminUpdateDto updateDto = new AdminUpdateDto();

        when(administradorRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            administradorService.updateAdmin(id, updateDto);
        });

        assertEquals("Administrador não encontrado", exception.getMessage());

        verify(administradorRepository, times(1)).findById(id);
        verify(administradorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar administrador com email já existente")
    void shouldThrowExceptionWhenCreatingDuplicateAdmin() {

        AdminRequestDto requestDto = new AdminRequestDto();
        requestDto.setNome("Carlos Duplicado");
        requestDto.setEmail("email.existente@teste.com");

        Administrador adminEntity = new Administrador();
        adminEntity.setNome("Carlos Duplicado");

        when(modelMapper.map(requestDto, Administrador.class)).thenReturn(adminEntity);

        when(administradorRepository.save(any(Administrador.class)))
            .thenThrow(new RuntimeException("Erro ao salvar: Email já cadastrado"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            administradorService.createAdmin(requestDto);
        });

        assertEquals("Erro ao salvar: Email já cadastrado", exception.getMessage());
        verify(administradorRepository, times(1)).save(any(Administrador.class));
        verify(modelMapper, never()).map(any(), eq(AdminResponseDto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar administrador inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentAdmin() {

        Long id = 99L;

        when(administradorRepository.existsById(id)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            administradorService.deleteAdmin(id);
        });

        verify(administradorRepository, never()).deleteById(id);
    }
}
