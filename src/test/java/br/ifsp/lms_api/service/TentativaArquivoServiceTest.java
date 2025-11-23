package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException; // Importação necessária
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoResponseDto;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoUpdateDto;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.model.TentativaArquivo;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.AtividadeArquivosRepository;
import br.ifsp.lms_api.repository.TentativaArquivoRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TentativaArquivoServiceTest {

    @Mock
    private TentativaArquivoRepository tentativaArquivoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private AtividadeArquivosRepository atividadeArquivosRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TentativaArquivoService service;

    @Test
    void createTentativaArquivo_Success() throws IOException { // Adicionado throws IOException
        Long idAluno = 1L;
        Long idAtividade = 10L;
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        Aluno alunoMock = new Aluno();
        alunoMock.setIdUsuario(idAluno);

        AtividadeArquivos atividadeMock = new AtividadeArquivos();


        TentativaArquivo tentativaSalva = new TentativaArquivo();

        tentativaSalva.setUrlArquivo("http://storage/test.pdf");

        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(alunoMock));
        when(atividadeArquivosRepository.findById(idAtividade)).thenReturn(Optional.of(atividadeMock));
        when(storageService.createArquivo(any(MultipartFile.class))).thenReturn("http://storage/test.pdf");
        when(tentativaArquivoRepository.save(any(TentativaArquivo.class))).thenReturn(tentativaSalva);
        when(modelMapper.map(any(TentativaArquivo.class), eq(TentativaArquivoResponseDto.class)))
                .thenReturn(new TentativaArquivoResponseDto());

        TentativaArquivoResponseDto result = service.createTentativaArquivo(file, idAluno, idAtividade);

        assertNotNull(result);
        verify(storageService).createArquivo(file);
        verify(tentativaArquivoRepository).save(any(TentativaArquivo.class));
    }

    @Test
    void createTentativaArquivo_WhenAlunoNotFound_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "bytes".getBytes());
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            service.createTentativaArquivo(file, 1L, 10L)
        );
    }

    @Test
    void createTentativaArquivo_WhenAtividadeNotFound_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "bytes".getBytes());
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(new Aluno()));
        when(atividadeArquivosRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            service.createTentativaArquivo(file, 1L, 10L)
        );
    }

    @Test
    void updateTentativaArquivoProfessor_Success() {
        Long idTentativa = 1L;
        TentativaArquivoUpdateDto dto = new TentativaArquivoUpdateDto();
        dto.setNota(Optional.of(9.5));
        dto.setFeedback(Optional.of("Bom trabalho"));

        TentativaArquivo tentativa = new TentativaArquivo();


        when(tentativaArquivoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));
        when(tentativaArquivoRepository.save(any(TentativaArquivo.class))).thenReturn(tentativa);
        when(modelMapper.map(any(TentativaArquivo.class), eq(TentativaArquivoResponseDto.class)))
                .thenReturn(new TentativaArquivoResponseDto());

        TentativaArquivoResponseDto result = service.updateTentativaArquivoProfessor(dto, idTentativa);

        assertNotNull(result);
        assertEquals(9.5, tentativa.getNota());
        assertEquals("Bom trabalho", tentativa.getFeedBack());
    }

    @Test
    void updateTentativaArquivoAluno_Success() throws IOException { // Adicionado throws IOException
        Long idTentativa = 1L;
        Long idAluno = 50L;
        MockMultipartFile novoArquivo = new MockMultipartFile("file", "novo.pdf", "application/pdf", "new".getBytes());

        Aluno alunoOwner = new Aluno();
        alunoOwner.setIdUsuario(idAluno);

        TentativaArquivo tentativa = new TentativaArquivo();

        tentativa.setAluno(alunoOwner);
        tentativa.setUrlArquivo("http://bucket/antigo.pdf");
        tentativa.setNota(null);

        when(tentativaArquivoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));
        when(storageService.createArquivo(novoArquivo)).thenReturn("http://bucket/novo.pdf");
        when(tentativaArquivoRepository.save(tentativa)).thenReturn(tentativa);
        when(modelMapper.map(any(TentativaArquivo.class), eq(TentativaArquivoResponseDto.class)))
                .thenReturn(new TentativaArquivoResponseDto());

        service.updateTentativaArquivoAluno(idTentativa, idAluno, novoArquivo);

        verify(storageService).deleteFile("antigo.pdf");
        verify(tentativaArquivoRepository).save(tentativa);
    }

    @Test
    void updateTentativaArquivoAluno_WhenUserNotOwner_ShouldThrowAccessDenied() {
        Long idTentativa = 1L;
        Long idAlunoLogado = 99L;
        Long idAlunoDono = 50L;

        Aluno dono = new Aluno();
        dono.setIdUsuario(idAlunoDono);

        TentativaArquivo tentativa = new TentativaArquivo();
        tentativa.setAluno(dono);

        when(tentativaArquivoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));

        assertThrows(AccessDeniedException.class, () ->
            service.updateTentativaArquivoAluno(idTentativa, idAlunoLogado, null)
        );
    }

    @Test
    void updateTentativaArquivoAluno_WhenAlreadyGraded_ShouldThrowAccessDenied() {
        Long idAluno = 50L;
        Aluno dono = new Aluno();
        dono.setIdUsuario(idAluno);

        TentativaArquivo tentativa = new TentativaArquivo();
        tentativa.setAluno(dono);
        tentativa.setNota(10.0);

        when(tentativaArquivoRepository.findById(1L)).thenReturn(Optional.of(tentativa));

        assertThrows(AccessDeniedException.class, () ->
            service.updateTentativaArquivoAluno(1L, idAluno, null)
        );
    }

    @Test
    void deleteTentativaArquivo_Success() throws IOException {
        Long idTentativa = 1L;
        Long idAluno = 50L;

        Aluno dono = new Aluno();
        dono.setIdUsuario(idAluno);

        TentativaArquivo tentativa = new TentativaArquivo();
        tentativa.setAluno(dono);
        tentativa.setUrlArquivo("http://bucket/arquivo.pdf");
        tentativa.setNota(null);

        when(tentativaArquivoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));
        when(modelMapper.map(any(TentativaArquivo.class), eq(TentativaArquivoResponseDto.class)))
                .thenReturn(new TentativaArquivoResponseDto());

        service.deleteTentativaArquivo(idTentativa, idAluno);

        verify(storageService).deleteFile("arquivo.pdf");
        verify(tentativaArquivoRepository).delete(tentativa);
    }

    @Test
    void deleteTentativaArquivo_WhenStorageFails_ShouldThrowRuntimeException() throws IOException {
        Long idTentativa = 1L;
        Long idAluno = 50L;

        Aluno dono = new Aluno();
        dono.setIdUsuario(idAluno);

        TentativaArquivo tentativa = new TentativaArquivo();
        tentativa.setAluno(dono);
        tentativa.setUrlArquivo("http://bucket/arquivo.pdf");
        tentativa.setNota(null);

        when(tentativaArquivoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));
        doThrow(new RuntimeException("Erro S3")).when(storageService).deleteFile(anyString());

        assertThrows(RuntimeException.class, () ->
            service.deleteTentativaArquivo(idTentativa, idAluno)
        );

        verify(tentativaArquivoRepository, never()).delete(any());
    }
}
