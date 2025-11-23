package br.ifsp.lms_api.service;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.MaterialDeAulaRepository;
import br.ifsp.lms_api.repository.TopicosRepository;

@ExtendWith(MockitoExtension.class)
class MaterialDeAulaServiceTest {

    @Mock
    private MaterialDeAulaRepository materialRepository;

    @Mock
    private TopicosRepository topicosRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private MaterialDeAulaService materialService;

    private Long idProfessorDono = 1L;
    private Long idOutroProfessor = 99L;
    private Professor professor;
    private Turma turma;
    private Topicos topico;
    private MaterialDeAula materialMock;

    @BeforeEach
    void setUp() {

        professor = new Professor();
        professor.setIdUsuario(idProfessorDono);
        
        turma = new Turma();
        turma.setProfessor(professor);
        
        topico = new Topicos();
        topico.setIdTopico(1L);
        topico.setTurma(turma);

        materialMock = new MaterialDeAula();
        materialMock.setIdMaterialDeAula(1L);
        materialMock.setTopico(topico);
    }

    @Test
    void testCreateMaterial_Success() {
        MockMultipartFile file = new MockMultipartFile(
            "arquivo", "documento.pdf", "application/pdf", "conteudo".getBytes()
        );
        String urlMock = "http://storage.com/uuid_documento.pdf";

        MaterialDeAula materialSalvo = new MaterialDeAula();
        materialSalvo.setIdMaterialDeAula(10L);
        MaterialDeAulaResponseDto responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(10L);

        when(topicosRepository.findById(1L)).thenReturn(Optional.of(topico));
        when(storageService.createArquivo(any(MultipartFile.class))).thenReturn(urlMock);
        when(materialRepository.save(any(MaterialDeAula.class))).thenReturn(materialSalvo);
        when(modelMapper.map(materialSalvo, MaterialDeAulaResponseDto.class)).thenReturn(responseDto);

    
        MaterialDeAulaResponseDto result = materialService.createMaterial(file, 1L, idProfessorDono);

        assertNotNull(result);
        assertEquals(10L, result.getIdMaterialDeAula());

        verify(topicosRepository).findById(1L);
        verify(storageService).createArquivo(file);
        
        ArgumentCaptor<MaterialDeAula> captor = ArgumentCaptor.forClass(MaterialDeAula.class);
        verify(materialRepository).save(captor.capture());

        MaterialDeAula materialCapturado = captor.getValue();
        assertEquals("documento.pdf", materialCapturado.getNomeArquivo());
        assertEquals("application/pdf", materialCapturado.getTipoArquivo());
        assertEquals(urlMock, materialCapturado.getUrlArquivo());
        assertEquals(topico, materialCapturado.getTopico());
    }

    @Test
    void testCreateMaterial_AccessDenied() {
        MockMultipartFile file = new MockMultipartFile("f", "f.pdf", "type", "c".getBytes());
        when(topicosRepository.findById(1L)).thenReturn(Optional.of(topico));

    
        assertThrows(AccessDeniedException.class, () -> {
            materialService.createMaterial(file, 1L, idOutroProfessor);
        });

        verify(storageService, never()).createArquivo(any());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testCreateMaterial_TopicoNotFound_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "app/pdf", "c".getBytes());
        Long idTopicoInexistente = 99L;

        when(topicosRepository.findById(idTopicoInexistente))
            .thenThrow(new ResourceNotFoundException("Tópico com ID " + idTopicoInexistente + " não encontrado"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            materialService.createMaterial(file, idTopicoInexistente, idProfessorDono);
        });

        assertEquals("Tópico com ID 99 não encontrado", exception.getMessage());
        verify(storageService, never()).createArquivo(any());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testDeleteMaterial_Success() throws Exception {
        Long idMaterial = 1L;
        String nomeArquivoUnico = "uuid_arquivo.pdf";
        String urlArquivoMock = "http://localhost/uploads/" + nomeArquivoUnico;

        materialMock.setUrlArquivo(urlArquivoMock);
        
        MaterialDeAulaResponseDto responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(idMaterial);

        when(materialRepository.findById(idMaterial)).thenReturn(Optional.of(materialMock));
        doNothing().when(storageService).deleteFile(nomeArquivoUnico); 
        doNothing().when(materialRepository).delete(materialMock);
        when(modelMapper.map(materialMock, MaterialDeAulaResponseDto.class)).thenReturn(responseDto);

   
        MaterialDeAulaResponseDto result = materialService.deleteMaterial(idMaterial, idProfessorDono);

        assertNotNull(result);
        assertEquals(idMaterial, result.getIdMaterialDeAula());

        verify(materialRepository).findById(idMaterial);
        verify(storageService).deleteFile(nomeArquivoUnico); 
        verify(materialRepository).delete(materialMock);     
    }
    
@Test
   
    void testDeleteMaterial_AccessDenied() throws Exception { 
        Long idMaterial = 1L;
        when(materialRepository.findById(idMaterial)).thenReturn(Optional.of(materialMock));

        assertThrows(AccessDeniedException.class, () -> {
            materialService.deleteMaterial(idMaterial, idOutroProfessor);
        });

        verify(materialRepository, never()).delete(any());
        
      
        verify(storageService, never()).deleteFile(anyString());
    }

    @Test
    void testDeleteMaterial_StorageFails_ShouldThrowException() throws Exception {
        Long idMaterial = 1L;
        String nomeArquivoUnico = "uuid_arquivo.pdf";
        String urlArquivoMock = "http://localhost/uploads/" + nomeArquivoUnico;

        materialMock.setUrlArquivo(urlArquivoMock);

        when(materialRepository.findById(idMaterial)).thenReturn(Optional.of(materialMock));
        doThrow(new IOException("Falha de IO")).when(storageService).deleteFile(nomeArquivoUnico);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
    
            materialService.deleteMaterial(idMaterial, idProfessorDono);
        });
        
        assertEquals("Nao foi possivel deletar o arquivo fisico. Rollback.", exception.getMessage());

        verify(materialRepository, never()).delete(any());
    }

    @Test
    void testDeleteMaterial_NotFound_ShouldThrowException() throws IOException {
        Long idInexistente = 99L;
        when(materialRepository.findById(idInexistente))
            .thenThrow(new ResourceNotFoundException("Material com ID " + idInexistente + " nao encontrado"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            materialService.deleteMaterial(idInexistente, idProfessorDono);
        });

        assertEquals("Material com ID 99 nao encontrado", exception.getMessage());
        verify(storageService, never()).deleteFile(anyString());
    }

    @Test
    void testUpdateMaterial_Success() throws Exception {
        Long idMaterial = 1L;
        
        MockMultipartFile novoArquivo = new MockMultipartFile(
            "arquivo", "novo.pdf", "application/pdf", "novo conteudo".getBytes()
        );
        String urlNovoArquivo = "http://storage.com/novo_uuid.pdf";
        
        String nomeArquivoAntigo = "antigo_uuid.pdf";
        String urlArquivoAntigo = "http://storage.com/" + nomeArquivoAntigo;
        
        materialMock.setUrlArquivo(urlArquivoAntigo);
        
        MaterialDeAulaResponseDto responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(idMaterial);
        responseDto.setNomeArquivo("novo.pdf");
        responseDto.setUrlArquivo(urlNovoArquivo);

        when(materialRepository.findById(idMaterial)).thenReturn(Optional.of(materialMock));
        when(storageService.createArquivo(novoArquivo)).thenReturn(urlNovoArquivo);
        when(materialRepository.save(any(MaterialDeAula.class))).thenReturn(materialMock); 
        doNothing().when(storageService).deleteFile(nomeArquivoAntigo); 
        when(modelMapper.map(materialMock, MaterialDeAulaResponseDto.class)).thenReturn(responseDto);

     
        MaterialDeAulaResponseDto result = materialService.updateMaterial(idMaterial, novoArquivo, idProfessorDono);

        assertNotNull(result);
        assertEquals(urlNovoArquivo, result.getUrlArquivo()); 

        verify(materialRepository).findById(idMaterial);        
        verify(storageService).createArquivo(novoArquivo);       
        
        ArgumentCaptor<MaterialDeAula> captor = ArgumentCaptor.forClass(MaterialDeAula.class);
        verify(materialRepository).save(captor.capture());
        assertEquals("novo.pdf", captor.getValue().getNomeArquivo());
        assertEquals(urlNovoArquivo, captor.getValue().getUrlArquivo());
        
        verify(storageService).deleteFile(nomeArquivoAntigo);
    }
}