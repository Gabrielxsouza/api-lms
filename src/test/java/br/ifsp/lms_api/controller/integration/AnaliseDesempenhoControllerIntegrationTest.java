package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.model.TentativaTexto;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;
import br.ifsp.lms_api.repository.MaterialDeAulaRepository;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.repository.TentativaTextoRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.service.AutentificacaoService;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AnaliseDesempenhoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private AtividadeTextoRepository atividadeTextoRepository;
    @Autowired private TentativaTextoRepository tentativaTextoRepository;
    @Autowired private TopicosRepository topicosRepository;
    @Autowired private MaterialDeAulaRepository materialDeAulaRepository;
    @Autowired private EntityManager entityManager;

    @MockBean private AutentificacaoService autentificacaoService;

    private Aluno aluno;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
      
        entityManager.createNativeQuery("DELETE FROM atividade_arquivos_permitidos").executeUpdate(); // <--- ESTA LINHA FALTAVA
        entityManager.createNativeQuery("DELETE FROM atividade_tags").executeUpdate();
        
      
        entityManager.createNativeQuery("DELETE FROM topico_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questao_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questionario_questoes").executeUpdate();
        
     
        entityManager.createNativeQuery("DELETE FROM tentativa_texto").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentativa_questionario").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentativa_arquivo").executeUpdate();
        
     
        entityManager.createNativeQuery("DELETE FROM material_de_aula").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM alternativas").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questoes").executeUpdate();
        
     
        entityManager.createNativeQuery("DELETE FROM atividade").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM topicos").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM matricula").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM turma").executeUpdate();
        
        
        entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate(); 
        entityManager.createNativeQuery("DELETE FROM tags").executeUpdate();

    
        aluno = new Aluno();
        aluno.setNome("Aluno Analise");
        aluno.setEmail("analise@aluno.com");
        aluno.setCpf("11122233344");
        aluno.setSenha("123456");
        aluno.setTipoUsuario("ALUNO");
        aluno.setRa("RA001");
        aluno = alunoRepository.save(aluno);
        userDetails = new CustomUserDetails(aluno);

     
        Tag tagCalculo = new Tag();
        tagCalculo.setNome("Cálculo");
        tagCalculo = tagRepository.save(tagCalculo);

      
        AtividadeTexto atividade = new AtividadeTexto();
        atividade.setTituloAtividade("Prova de Limites");
        atividade.setDataInicioAtividade(LocalDate.now());
        atividade.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        atividade.setStatusAtividade(true);
        atividade.setNumeroMaximoCaracteres(100L);
        atividade.setTags(Set.of(tagCalculo));
        atividade = atividadeTextoRepository.save(atividade);

     
        TentativaTexto tentativa = new TentativaTexto();
        tentativa.setAluno(aluno);
        tentativa.setAtividadeTexto(atividade);
        tentativa.setTextoResposta("Resposta errada");
        tentativa.setNota(3.0); 
        tentativaTextoRepository.save(tentativa);

 
        Topicos topicoApoio = new Topicos();
        topicoApoio.setTituloTopico("Reforço de Cálculo");
        topicoApoio.setTags(new HashSet<>(Set.of(tagCalculo)));
        topicoApoio = topicosRepository.save(topicoApoio);

        MaterialDeAula material = new MaterialDeAula();
        material.setNomeArquivo("Livro de Calculo.pdf");
        material.setUrlArquivo("/downloads/livro.pdf");
        material.setTipoArquivo("PDF");
        material.setTopico(topicoApoio);
        materialDeAulaRepository.save(material);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getMeuRelatorio_ShouldReturnWeakPointsAndSuggestions() throws Exception {
        mockMvc.perform(get("/analise/aluno/meu-desempenho")
                .with(user(userDetails))) 
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.desempenhoGeral[0].nomeTag", is("Cálculo")))
                .andExpect(jsonPath("$.desempenhoGeral[0].mediaNota", is(3.0)))
                

                .andExpect(jsonPath("$.pontosFracos", hasSize(1)))
                .andExpect(jsonPath("$.pontosFracos[0].nomeTag", is("Cálculo")))
                

                .andExpect(jsonPath("$.sugestoesEstudo", hasSize(1)))
                .andExpect(jsonPath("$.sugestoesEstudo[0].nomeMaterial", is("Livro de Calculo.pdf")));
    }
}