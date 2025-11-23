package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq; 
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.analise.RelatorioDesempenhoResponseDto;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.model.TentativaTexto;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.TentativaArquivoRepository;
import br.ifsp.lms_api.repository.TentativaQuestionarioRepository;
import br.ifsp.lms_api.repository.TentativaTextoRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
class AnaliseDesempenhoServiceTest {

    @Mock private TentativaTextoRepository tentativaTextoRepo;
    @Mock private TentativaArquivoRepository tentativaArquivoRepo;
    @Mock private TentativaQuestionarioRepository tentativaQuestionarioRepo;
    @Mock private TopicosRepository topicosRepo;
    @Mock private TurmaRepository turmaRepo;
    @Mock private DisciplinaRepository disciplinaRepo;

    @InjectMocks private AnaliseDesempenhoService analiseService;

    private Long idAluno = 1L;
    private Tag tagMatematica;
    private TentativaTexto tentativaRuim;

    @BeforeEach
    void setUp() {
        tagMatematica = new Tag();
        tagMatematica.setNome("Matemática");

        AtividadeTexto atividade = new AtividadeTexto();
        atividade.setTags(Set.of(tagMatematica));

        tentativaRuim = new TentativaTexto();
        tentativaRuim.setNota(4.0); 
        tentativaRuim.setAtividadeTexto(atividade);
    }

    @Test
    void gerarRelatorioAluno_DeveIdentificarPontoFracoESugerirMaterial() {

        when(tentativaTextoRepo.findByAluno_IdUsuario(eq(idAluno), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(tentativaRuim)));
        
        when(tentativaArquivoRepo.findByAluno_IdUsuario(eq(idAluno), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));
            
        when(tentativaQuestionarioRepo.findByAluno_IdUsuario(eq(idAluno), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));


        Topicos topicoRecuperacao = new Topicos();
        topicoRecuperacao.setTituloTopico("Aula de Reforço");
        
        MaterialDeAula material = new MaterialDeAula();
        material.setNomeArquivo("Apostila.pdf");
        material.setUrlArquivo("link.com");
        topicoRecuperacao.setMateriaisDeAula(List.of(material));

        when(topicosRepo.findByTags_NomeIn(anySet())).thenReturn(List.of(topicoRecuperacao));


        RelatorioDesempenhoResponseDto relatorio = analiseService.gerarRelatorioAluno(idAluno);


        assertNotNull(relatorio);
        
        assertFalse(relatorio.getDesempenhoGeral().isEmpty());
        assertEquals("Matemática", relatorio.getDesempenhoGeral().get(0).getNomeTag());
        assertEquals(4.0, relatorio.getDesempenhoGeral().get(0).getMediaNota());

        assertFalse(relatorio.getPontosFracos().isEmpty());
        assertEquals("Matemática", relatorio.getPontosFracos().get(0).getNomeTag());

        assertFalse(relatorio.getSugestoesEstudo().isEmpty());
        assertEquals("Apostila.pdf", relatorio.getSugestoesEstudo().get(0).getNomeMaterial());
    }
    
}