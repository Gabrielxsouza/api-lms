package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.CursoDto.CursoRequestDto;
import br.ifsp.lms_api.dto.CursoDto.CursoUpdateDto;
import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.repository.AdministradorRepository;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.service.AutentificacaoService;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CursoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private EntityManager entityManager;

    @MockBean
    private AutentificacaoService autentificacaoService;

    private Curso cursoExistente;
    private CustomUserDetails adminUserDetails;
    private Administrador admin;

    @BeforeEach
    void setUp() {
    
        entityManager.createNativeQuery("DELETE FROM atividade_arquivos_permitidos").executeUpdate();
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
        
        turmaRepository.deleteAll();
        cursoRepository.deleteAll();
        disciplinaRepository.deleteAll();
        professorRepository.deleteAll();
        administradorRepository.deleteAll();

        admin = new Administrador();
        admin.setNome("Admin Curso");
        admin.setEmail("admin.curso@test.com");
        admin.setCpf("11122233344");
        admin.setSenha("123456");
        admin.setTipoUsuario("ADMIN");
        admin = administradorRepository.save(admin);
        adminUserDetails = new CustomUserDetails(admin);

        cursoExistente = new Curso();
        cursoExistente.setNomeCurso("Sistemas de Informação");
        cursoExistente.setCodigoCurso("BSI");
        cursoExistente.setDescricaoCurso("Bacharelado em SI");
        cursoExistente = cursoRepository.save(cursoExistente);

        entityManager.flush();
        entityManager.clear();
        
        cursoExistente = cursoRepository.findById(cursoExistente.getIdCurso()).get();
        
        when(autentificacaoService.getUsuarioLogado()).thenReturn(admin);
    }

    @Test
    void createCurso_Success() throws Exception {
        CursoRequestDto requestDto = new CursoRequestDto(
            "Engenharia Civil", "Bacharelado", "ENC"
        );

        mockMvc.perform(post("/cursos")
                .with(user(adminUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCurso").exists())
                .andExpect(jsonPath("$.nomeCurso", is("Engenharia Civil")));

        assertEquals(2, cursoRepository.count());
    }

    @Test
    void createCurso_InvalidInput() throws Exception {
        CursoRequestDto requestDto = new CursoRequestDto(
            "", "", ""
        );

        mockMvc.perform(post("/cursos")
                .with(user(adminUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        assertEquals(1, cursoRepository.count());
    }

    @Test
    void getAllCursos_Success() throws Exception {
   
        mockMvc.perform(get("/cursos")
                .with(user(adminUserDetails)) 
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomeCurso", is("Sistemas de Informação")));
    }

    @Test
    void updateCurso_Success() throws Exception {
        Long id = cursoExistente.getIdCurso();
        CursoUpdateDto updateDto = new CursoUpdateDto(
            Optional.of("Sistemas Atualizado"),
            Optional.empty(),
            Optional.empty()
        );

        mockMvc.perform(patch("/cursos/{id}", id)
                .with(user(adminUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCurso", is("Sistemas Atualizado")));
    }

    @Test
    void deleteCurso_Success() throws Exception {
        Long id = cursoExistente.getIdCurso();

        mockMvc.perform(delete("/cursos/{id}", id)
                .with(user(adminUserDetails)))
                .andExpect(status().isNoContent());

        assertFalse(cursoRepository.findById(id).isPresent());
        assertEquals(0, cursoRepository.count());
    }
}