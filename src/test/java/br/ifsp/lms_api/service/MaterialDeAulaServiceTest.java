package br.ifsp.lms_api.service;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.repository.MaterialDeAulaRepository;
import br.ifsp.lms_api.repository.TopicosRepository;

@ExtendWith(MockitoExtension.class)
class MaterialDeAulaServiceTest {

    // 1. Mocks para TODAS as dependências
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

    // 2. Injete os mocks no service
    @InjectMocks
    private MaterialDeAulaService materialService;

    @Test
    void testCreateMaterial_Success() {
        // --- 1. Arrange (Arrumar) ---
        MockMultipartFile file = new MockMultipartFile(
            "arquivo", "documento.pdf", "application/pdf", "conteudo".getBytes()
        );
        Long idTopico = 1L;
        String urlMock = "http://storage.com/uuid_documento.pdf";

        Topicos topicoMock = new Topicos();
        MaterialDeAula materialSalvo = new MaterialDeAula();
        materialSalvo.setIdMaterialDeAula(10L);
        MaterialDeAulaResponseDto responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(10L);

        when(topicosRepository.findById(idTopico)).thenReturn(Optional.of(topicoMock));
        when(storageService.createArquivo(any(MultipartFile.class))).thenReturn(urlMock);
        when(materialRepository.save(any(MaterialDeAula.class))).thenReturn(materialSalvo);
        when(modelMapper.map(materialSalvo, MaterialDeAulaResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        MaterialDeAulaResponseDto result = materialService.createMaterial(file, idTopico);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(10L, result.getIdMaterialDeAula());

        verify(topicosRepository).findById(idTopico);
        verify(storageService).createArquivo(file);
        
        ArgumentCaptor<MaterialDeAula> captor = ArgumentCaptor.forClass(MaterialDeAula.class);
        verify(materialRepository).save(captor.capture());

        MaterialDeAula materialCapturado = captor.getValue();
        assertEquals("documento.pdf", materialCapturado.getNomeArquivo());
        assertEquals("application/pdf", materialCapturado.getTipoArquivo());
        assertEquals(urlMock, materialCapturado.getUrlArquivo());
        assertEquals(topicoMock, materialCapturado.getTopico());
    }

    @Test
    void testCreateMaterial_TopicoNotFound_ShouldThrowException() {
        // --- 1. Arrange (Arrumar) ---
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "app/pdf", "c".getBytes());
        Long idTopicoInexistente = 99L;

        // ** CORREÇÃO DE BUG DE TESTE **
        // O seu service (real) lança ResourceNotFoundException, não EntityNotFoundException
        when(topicosRepository.findById(idTopicoInexistente))
            .thenThrow(new ResourceNotFoundException("Tópico com ID " + idTopicoInexistente + " não encontrado"));

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            materialService.createMaterial(file, idTopicoInexistente);
        });

        assertEquals("Tópico com ID 99 não encontrado", exception.getMessage());
        verify(storageService, never()).createArquivo(any());
        verify(materialRepository, never()).save(any());
    }

    // --- TESTE ATUALIZADO ---
    @Test
    void testDeleteMaterial_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idMaterial = 1L;
        String nomeArquivoUnico = "uuid_arquivo.pdf";
        String urlArquivoMock = "http://localhost/uploads/" + nomeArquivoUnico;

        MaterialDeAula materialMock = new MaterialDeAula();
        materialMock.setIdMaterialDeAula(idMaterial);
        materialMock.setUrlArquivo(urlArquivoMock);
        
        MaterialDeAulaResponseDto responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(idMaterial);

        // Configura os mocks para a nova lógica
        when(materialRepository.findById(idMaterial)).thenReturn(Optional.of(materialMock));
        doNothing().when(storageService).deleteFile(nomeArquivoUnico); // Espera o nome extraído
        doNothing().when(materialRepository).delete(materialMock);
        when(modelMapper.map(materialMock, MaterialDeAulaResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        MaterialDeAulaResponseDto result = materialService.deleteMaterial(idMaterial);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(idMaterial, result.getIdMaterialDeAula());

        // Verifica a nova sequência de eventos
        verify(materialRepository).findById(idMaterial);
        verify(storageService).deleteFile(nomeArquivoUnico); // Verifica se o service chamou delete no arquivo
        verify(materialRepository).delete(materialMock);     // Verifica se o service chamou delete no repo
    }

    // --- TESTE ATUALIZADO ---
    @Test
    void testDeleteMaterial_StorageFails_ShouldThrowException() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idMaterial = 1L;
        String nomeArquivoUnico = "uuid_arquivo.pdf";
        String urlArquivoMock = "http://localhost/uploads/" + nomeArquivoUnico;

        MaterialDeAula materialMock = new MaterialDeAula();
        materialMock.setIdMaterialDeAula(idMaterial);
        materialMock.setUrlArquivo(urlArquivoMock);

        when(materialRepository.findById(idMaterial)).thenReturn(Optional.of(materialMock));
        // Simula o StorageService falhando (como você mudou, ele pode lançar IOException)
        doThrow(new IOException("Falha de IO")).when(storageService).deleteFile(nomeArquivoUnico);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        // Seu service (real) agora captura a IOException e lança uma RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            materialService.deleteMaterial(idMaterial);
        });
        
        assertEquals("Nao foi possivel deletar o arquivo fisico. Rollback.", exception.getMessage());

        // Garante que, se o arquivo físico falhou ao deletar,
        // a entrada do banco de dados NÃO foi deletada (Rollback)
        verify(materialRepository, never()).delete(any());
    }

    @Test
    void testDeleteMaterial_NotFound_ShouldThrowException() throws IOException {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 99L;
        when(materialRepository.findById(idInexistente))
            .thenThrow(new ResourceNotFoundException("Material com ID " + idInexistente + " nao encontrado"));

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            materialService.deleteMaterial(idInexistente);
        });

        assertEquals("Material com ID 99 nao encontrado", exception.getMessage());
        verify(storageService, never()).deleteFile(anyString());
    }

    // --- TESTE TOTALMENTE REESCRITO ---
    @Test
    void testUpdateMaterial_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idMaterial = 1L;
        
        // 1a. O NOVO arquivo que será enviado
        MockMultipartFile novoArquivo = new MockMultipartFile(
            "arquivo", "novo.pdf", "application/pdf", "novo conteudo".getBytes()
        );
        String urlNovoArquivo = "http://storage.com/novo_uuid.pdf";
        
        // 1b. O material ANTIGO (que será encontrado no banco)
        String nomeArquivoAntigo = "antigo_uuid.pdf";
        String urlArquivoAntigo = "http://storage.com/" + nomeArquivoAntigo;
        MaterialDeAula materialAntigo = new MaterialDeAula();
        materialAntigo.setIdMaterialDeAula(idMaterial);
        materialAntigo.setUrlArquivo(urlArquivoAntigo);
        
        // 1c. O DTO de resposta (com os dados novos)
        MaterialDeAulaResponseDto responseDto = new MaterialDeAulaResponseDto();
        responseDto.setIdMaterialDeAula(idMaterial);
        responseDto.setNomeArquivo("novo.pdf");
        responseDto.setUrlArquivo(urlNovoArquivo);

        // 1d. Configura os Mocks
        when(materialRepository.findById(idMaterial)).thenReturn(Optional.of(materialAntigo));
        when(storageService.createArquivo(novoArquivo)).thenReturn(urlNovoArquivo);
        when(materialRepository.save(any(MaterialDeAula.class))).thenReturn(materialAntigo); // Retorna a entidade (agora atualizada)
        doNothing().when(storageService).deleteFile(nomeArquivoAntigo); // Simula a deleção do arquivo antigo
        when(modelMapper.map(materialAntigo, MaterialDeAulaResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        MaterialDeAulaResponseDto result = materialService.updateMaterial(idMaterial, novoArquivo);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(urlNovoArquivo, result.getUrlArquivo()); // Verifica se o DTO de resposta tem a URL nova

        // Verifica a sequência de eventos
        verify(materialRepository).findById(idMaterial);        // 1. Encontrou o antigo
        verify(storageService).createArquivo(novoArquivo);       // 2. Criou o novo
        
        // 3. Salvou a entidade com os *novos* dados
        ArgumentCaptor<MaterialDeAula> captor = ArgumentCaptor.forClass(MaterialDeAula.class);
        verify(materialRepository).save(captor.capture());
        assertEquals("novo.pdf", captor.getValue().getNomeArquivo());
        assertEquals(urlNovoArquivo, captor.getValue().getUrlArquivo());
        
        // 4. Deletou o arquivo antigo
        verify(storageService).deleteFile(nomeArquivoAntigo);
    }
}

