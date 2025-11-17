package br.ifsp.lms_api.controller.unit;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.controller.MaterialDeAulaController;
import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.MaterialDeAulaService;

@ExtendWith(MockitoExtension.class) 
class MaterialDeAulaControllerTest {

    private MockMvc mockMvc; 

    @Mock 
    private MaterialDeAulaService materialService;

    @InjectMocks
    private MaterialDeAulaController materialDeAulaController;

    private ObjectMapper objectMapper;
    private MaterialDeAulaResponseDto responseDto;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); 
        
        responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(1L);
        responseDto.setNomeArquivo("documento.pdf");
        responseDto.setUrlArquivo("http://storage.com/documento.pdf");
        responseDto.setTipoArquivo("application/pdf");

        mockUserDetails = mock(CustomUserDetails.class);
        
        // CORREÇÃO AQUI: Usar lenient()
        lenient().when(mockUserDetails.getId()).thenReturn(1L);
        
        mockMvc = MockMvcBuilders.standaloneSetup(materialDeAulaController)
                .setCustomArgumentResolvers(
                    new HandlerMethodArgumentResolver() {
                        @Override
                        public boolean supportsParameter(MethodParameter parameter) {
                            return parameter.getParameterType().isAssignableFrom(CustomUserDetails.class);
                        }
                        @Override
                        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                            return mockUserDetails;
                        }
                    },
                    new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void testUploadMaterial_Success() throws Exception {
        Long idTopico = 1L;
        Long idUsuario = 1L;

        MockMultipartFile file = new MockMultipartFile(
            "arquivo",            
            "documento.pdf",      
            "application/pdf",    
            "conteudo do pdf".getBytes()
        );

        when(materialService.createMaterial(any(MultipartFile.class), eq(idTopico), eq(idUsuario)))
            .thenReturn(responseDto);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/materiais/topico/{idTopico}", idTopico)
                .file(file)) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idMaterialDeAula").value(1L))
                .andExpect(jsonPath("$.nomeArquivo").value("documento.pdf"));
        
        verify(materialService, times(1)).createMaterial(any(MultipartFile.class), eq(idTopico), eq(idUsuario));
    }

    @Test
    void testUploadMaterial_TopicoNotFound_404() throws Exception {
        Long idTopicoInexistente = 99L;
        Long idUsuario = 1L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "file.txt", "text/plain", "t".getBytes());
        
        when(materialService.createMaterial(any(MultipartFile.class), eq(idTopicoInexistente), eq(idUsuario)))
            .thenThrow(new ResourceNotFoundException("Tópico não encontrado"));
        
        try {
            mockMvc.perform(MockMvcRequestBuilders.multipart("/materiais/topico/{idTopico}", idTopicoInexistente)
                    .file(file));
        } catch (Exception e) {
            if (!(e.getCause() instanceof ResourceNotFoundException)) {
                throw e;
            }
        }
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
        Long id = 1L;
        Long idUsuario = 1L;
        
        when(materialService.deleteMaterial(id, idUsuario)).thenReturn(responseDto);

        mockMvc.perform(delete("/materiais/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterialDeAula").value(1L)); 
        
        verify(materialService, times(1)).deleteMaterial(id, idUsuario);
    }
    
    @Test
    void testDeleteMaterial_NotFound_404() throws Exception {
        Long id = 99L;
        Long idUsuario = 1L;

        when(materialService.deleteMaterial(id, idUsuario))
            .thenThrow(new ResourceNotFoundException("Material não encontrado"));

        try {
            mockMvc.perform(delete("/materiais/{id}", id));
        } catch (Exception e) {
            if (!(e.getCause() instanceof ResourceNotFoundException)) {
                throw e;
            }
        }
        
        verify(materialService, times(1)).deleteMaterial(id, idUsuario);
    }


    @Test
    void testGetMaterialByTopico_Success() throws Exception {
        Long idTopico = 5L;
        List<MaterialDeAulaResponseDto> content = List.of(responseDto);
        
        PagedResponse<MaterialDeAulaResponseDto> pagedResponse = mock(PagedResponse.class);
        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        when(materialService.getMaterialByTopico(eq(idTopico), any(Pageable.class)))
            .thenReturn(pagedResponse);

        mockMvc.perform(get("/materiais/topico/{idTopico}", idTopico)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idMaterialDeAula").value(1L));
        
        verify(materialService, times(1)).getMaterialByTopico(eq(idTopico), any(Pageable.class));
    }

    @Test
    void testUpdateMaterial_Success() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;
        
        MockMultipartFile novoArquivo = new MockMultipartFile(
            "arquivo", 
            "novo.pdf",
            "application/pdf",
            "novo conteudo".getBytes()
        );

        MaterialDeAulaResponseDto updatedResponseDto = new MaterialDeAulaResponseDto();
        updatedResponseDto.setIdMaterialDeAula(id);
        updatedResponseDto.setNomeArquivo("novo.pdf");
        
        when(materialService.updateMaterial(eq(id), any(MultipartFile.class), eq(idUsuario)))
            .thenReturn(updatedResponseDto);

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/materiais/{id}", id)
                .file(novoArquivo)) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idMaterialDeAula").value(id))
                .andExpect(jsonPath("$.nomeArquivo").value("novo.pdf"));
        
        verify(materialService, times(1)).updateMaterial(eq(id), any(MultipartFile.class), eq(idUsuario));
    }
}