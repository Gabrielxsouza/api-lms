package br.ifsp.lms_api.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AtividadeTextoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AtividadeTextoRepository atividadeTextoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private TopicosRepository topicoRepository;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreate_Success() throws Exception {
        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);

        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade de Texto");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(5));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(500L);

        mockMvc.perform(post("/atividades-texto")
                .with(csrf())
                .with(user("professor").roles("PROFESSOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").exists())
                .andExpect(jsonPath("$.tituloAtividade").value("Nova Atividade de Texto"));

        assertTrue(atividadeTextoRepository.count() > 0);
    }

    @Test
    void testCreate_ValidationFails_400() throws Exception {
        AtividadeTextoRequestDto requestDtoInvalido = new AtividadeTextoRequestDto();
        requestDtoInvalido.setTituloAtividade(null);
        requestDtoInvalido.setNumeroMaximoCaracteres(500L);

        mockMvc.perform(post("/atividades-texto")
                .with(csrf())
                .with(user("professor").roles("PROFESSOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAll_Success() throws Exception {

        try {
            jdbcTemplate.execute("DELETE FROM tentativa_texto");
        } catch (Exception e) {

        }
        atividadeTextoRepository.deleteAll();

        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);


        AtividadeTexto at1 = new AtividadeTexto();
        at1.setTituloAtividade("Atividade 1");
        at1.setStatusAtividade(true);
        at1.setDataInicioAtividade(LocalDate.now());
        at1.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        at1.setNumeroMaximoCaracteres(1000L);

        AtividadeTexto at2 = new AtividadeTexto();
        at2.setTituloAtividade("Atividade 2");
        at2.setStatusAtividade(true);
        at2.setDataInicioAtividade(LocalDate.now());
        at2.setDataFechamentoAtividade(LocalDate.now().plusDays(2));
        at2.setNumeroMaximoCaracteres(500L);
        at2.setTopico(topico);

        atividadeTextoRepository.saveAll(List.of(at1, at2));

        mockMvc.perform(get("/atividades-texto")
                .with(user("professor").roles("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].tituloAtividade").value("Atividade 1"));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);

        AtividadeTexto atAntiga = new AtividadeTexto();
        atAntiga.setTituloAtividade("Título Antigo");
        atAntiga.setStatusAtividade(true);
        atAntiga.setDataInicioAtividade(LocalDate.now());
        atAntiga.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        atAntiga.setNumeroMaximoCaracteres(500L);
        atAntiga.setTopico(topico);

        AtividadeTexto atSalva = atividadeTextoRepository.save(atAntiga);
        Long idParaAtualizar = atSalva.getIdAtividade();

        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Novo"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(999L));

        CustomUserDetails userDetails = mockCustomUser(professor.getIdUsuario());

        mockMvc.perform(patch("/atividades-texto/{id}", idParaAtualizar)
                .with(csrf())
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idParaAtualizar))
                .andExpect(jsonPath("$.tituloAtividade").value("Título Novo"));

        Optional<AtividadeTexto> atDoBanco = atividadeTextoRepository.findById(idParaAtualizar);
        assertTrue(atDoBanco.isPresent());
        assertEquals("Título Novo", atDoBanco.get().getTituloAtividade());
        assertEquals(999L, atDoBanco.get().getNumeroMaximoCaracteres());
    }

    @Test
    void testUpdate_NotFound_404() throws Exception {
        Professor professor = createProfessor();
        CustomUserDetails userDetails = mockCustomUser(professor.getIdUsuario());

        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Novo"));

        mockMvc.perform(patch("/atividades-texto/{id}", 999L)
                .with(csrf())
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_Success() throws Exception {
        AtividadeTexto at = new AtividadeTexto();
        at.setTituloAtividade("Para Deletar");
        at.setStatusAtividade(true);
        at.setDataInicioAtividade(LocalDate.now());
        at.setDataFechamentoAtividade(LocalDate.now().plusDays(1));

        AtividadeTexto atSalva = atividadeTextoRepository.save(at);
        Long idParaDeletar = atSalva.getIdAtividade();

        mockMvc.perform(delete("/atividades-texto/{id}", idParaDeletar)
                .with(csrf())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertFalse(atividadeTextoRepository.findById(idParaDeletar).isPresent());
    }

    private Professor createProfessor() {
        Professor professor = new Professor();
        professor.setNome("Professor Texto");
        professor.setEmail("prof.texto" + System.currentTimeMillis() + "@teste.com");
        professor.setSenha("123456");
        professor.setCpf("999.888.777-66");
        return professorRepository.save(professor);
    }

    private Topicos createHierarchy(Professor professor) {
        Turma turma = new Turma();
        turma.setProfessor(professor);
        turma.setNomeTurma("Turma Texto Integration");
        turma.setSemestre("2025/1");
        turma = turmaRepository.save(turma);

        Topicos topico = new Topicos();
        topico.setTurma(turma);
        topico.setTituloTopico("Tópico Texto Geral");
        return topicoRepository.save(topico);
    }

    private CustomUserDetails mockCustomUser(Long id) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(id);
        when(userDetails.getUsername()).thenReturn("professor_teste");

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR")))
            .when(userDetails).getAuthorities();

        return userDetails;
    }
}
