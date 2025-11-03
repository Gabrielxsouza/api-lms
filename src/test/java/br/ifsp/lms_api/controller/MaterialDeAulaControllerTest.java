package br.ifsp.lms_api.controller;

// Imports do JUnit
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Imports do Spring Test
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod; // <-- Import para HttpMethod.PUT
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders; 

// Imports Estáticos (para get(), post(), status(), etc.)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// Imports de DTOs, Service e Exceções
import com.fasterxml.jackson.databind.ObjectMapper;
import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.MaterialDeAulaService;
import org.springframework.web.multipart.MultipartFile; // <-- Import

import java.util.List;

@WebMvcTest(MaterialDeAulaController.class) 
class MaterialDeAulaControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean 
    private MaterialDeAulaService materialService;

    private MaterialDeAulaResponseDto responseDto;

    @BeforeEach
    void setUp() {
        
        responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(1L);
        responseDto.setNomeArquivo("documento.pdf");
        responseDto.setUrlArquivo("http://storage.com/documento.pdf");
        responseDto.setTipoArquivo("application/pdf");
        
        objectMapper.findAndRegisterModules(); 
    }

    @Test
    void testUploadMaterial_Success() throws Exception {
        
        Long idTopico = 1L;

        
        MockMultipartFile file = new MockMultipartFile(
            "arquivo",            
            "documento.pdf",      
            "application/pdf",    
            "conteudo do pdf".getBytes()
        );

        
        when(materialService.createMaterial(any(MultipartFile.class), eq(idTopico)))
            .thenReturn(responseDto);

        
        mockMvc.perform(MockMvcRequestBuilders.multipart("/materiais/topico/{idTopico}", idTopico)
                .file(file)) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idMaterialDeAula").value(1L))
                .andExpect(jsonPath("$.nomeArquivo").value("documento.pdf"));
        
        verify(materialService, times(1)).createMaterial(any(MultipartFile.class), eq(idTopico));
    }

    @Test
    void testUploadMaterial_TopicoNotFound_404() throws Exception {
        
        Long idTopicoInexistente = 99L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "file.txt", "text/plain", "t".getBytes());
        
        when(materialService.createMaterial(any(MultipartFile.class), eq(idTopicoInexistente)))
            .thenThrow(new ResourceNotFoundException("Tópico não encontrado"));
        
        mockMvc.perform(MockMvcRequestBuilders.multipart("/materiais/topico/{idTopico}", idTopicoInexistente)
                .file(file))
                .andExpect(status().isNotFound()); 
    }

    @Test
    void testGetMaterialById_Success() throws Exception {
        
        when(materialService.getMaterialById(1L)).thenReturn(responseDto);
        
        mockMvc.perform(get("/materiais/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterialDeAula").value(1L));
    }

    @Test
    void testDeleteMaterial_Success_200() throws Exception {
        when(materialService.deleteMaterial(1L)).thenReturn(responseDto);

        mockMvc.perform(delete("/materiais/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterialDeAula").value(1L)); 
        
        verify(materialService, times(1)).deleteMaterial(1L);
    }
    
    @Test
    void testDeleteMaterial_NotFound_404() throws Exception {
        when(materialService.deleteMaterial(99L))
            .thenThrow(new ResourceNotFoundException("Material não encontrado"));

        mockMvc.perform(delete("/materiais/{id}", 99L))
                .andExpect(status().isNotFound()); 
        
        verify(materialService, times(1)).deleteMaterial(99L);
    }


    @Test
    void testGetMaterialByTopico_Success() throws Exception {
        
        Long idTopico = 5L;
        
        PagedResponse<MaterialDeAulaResponseDto> pagedResponse = mock(PagedResponse.class);
        List<MaterialDeAulaResponseDto> content = List.of(responseDto);

        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        when(materialService.getMaterialByTopico(eq(idTopico), any(Pageable.class)))
            .thenReturn(pagedResponse);

        mockMvc.perform(get("/materiais/topico/{idTopico}", idTopico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idMaterialDeAula").value(1L));
        
        verify(materialService, times(1)).getMaterialByTopico(eq(idTopico), any(Pageable.class));
    }

    // --- TESTE ATUALIZADO ---
    @Test
    void testUpdateMaterial_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        
        // 1a. Cria o novo arquivo (o "body" do @RequestParam)
        MockMultipartFile novoArquivo = new MockMultipartFile(
            "arquivo", // O nome do @RequestParam("arquivo")
            "novo.pdf",
            "application/pdf",
            "novo conteudo".getBytes()
        );

        // 1b. Cria o DTO de resposta que o service (mock) vai retornar
        MaterialDeAulaResponseDto updatedResponseDto = new MaterialDeAulaResponseDto();
        updatedResponseDto.setIdMaterialDeAula(id);
        updatedResponseDto.setNomeArquivo("novo.pdf");
        
        // 1c. Simula o service
        when(materialService.updateMaterial(eq(id), any(MultipartFile.class)))
            .thenReturn(updatedResponseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        // 2a. Usa 'multipart(HttpMethod.PUT, ...)' para simular um PUT com upload
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/materiais/{id}", id)
                .file(novoArquivo)) // Anexa o arquivo
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.idMaterialDeAula").value(id))
                .andExpect(jsonPath("$.nomeArquivo").value("novo.pdf"));
        
        // 2b. Verifica se o service foi chamado com o ID e o arquivo
        verify(materialService, times(1)).updateMaterial(eq(id), any(MultipartFile.class));
    }
}

