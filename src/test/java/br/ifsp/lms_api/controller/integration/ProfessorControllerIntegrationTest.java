package br.ifsp.lms_api.controller.integration;

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
import br.ifsp.lms_api.dto.professorDto.ProfessorRequestDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorUpdateDto;
import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.repository.AdministradorRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.service.AutentificacaoService;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProfessorControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private EntityManager entityManager;
    @MockBean private AutentificacaoService autentificacaoService;

    private Professor professorExistente;
    private CustomUserDetails adminUserDetails;
    private CustomUserDetails professorUserDetails;

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
        entityManager.createNativeQuery("DELETE FROM turma").executeUpdate();
        
        turmaRepository.deleteAll();
        professorRepository.deleteAll();
        administradorRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();


        Administrador admin = new Administrador();
        admin.setNome("Admin System");
        admin.setEmail("admin@sys.com");
        admin.setCpf("11111111111");
        admin.setSenha("123456");
        admin.setTipoUsuario("ADMIN");
        admin = administradorRepository.save(admin);
        adminUserDetails = new CustomUserDetails(admin);

        professorExistente = new Professor();
        professorExistente.setNome("Prof. Initial");
        professorExistente.setEmail("prof.init@sys.com");
        professorExistente.setCpf("22222222222");
        professorExistente.setSenha("123456");
        professorExistente.setTipoUsuario("PROFESSOR");
        professorExistente.setDepartamento("Matemática");
        professorExistente = professorRepository.save(professorExistente);
        professorUserDetails = new CustomUserDetails(professorExistente);

        entityManager.flush();
        entityManager.clear();
        
        professorExistente = professorRepository.findById(professorExistente.getIdUsuario()).get();
        
        when(autentificacaoService.getUsuarioLogado()).thenReturn(admin);
    }

    @Test
    void createProfessor_Success() throws Exception {
        ProfessorRequestDto request = new ProfessorRequestDto(
            "Novo Professor",
            "novo.prof@sys.com",
            "123456",
            "33333333333",
            "Física"
        );

        mockMvc.perform(post("/professores")
                .with(user(adminUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Novo Professor")))
                .andExpect(jsonPath("$.departamento", is("Física")));

        assertEquals(2, professorRepository.count()); 
    }

    @Test
    void getAllProfessores_Success() throws Exception {
        mockMvc.perform(get("/professores")
                .with(user(adminUserDetails))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Prof. Initial")));
    }

    @Test
    void updateProfessor_Success() throws Exception {
        Long id = professorExistente.getIdUsuario();
        ProfessorUpdateDto updateDto = new ProfessorUpdateDto(
            Optional.of("Prof. Updated"),
            Optional.empty(),
            Optional.empty(),
            Optional.of("Estatística")
        );

        mockMvc.perform(patch("/professores/{id}", id)
                .with(user(professorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Prof. Updated")));
                
        Professor profAtualizado = professorRepository.findById(id).get();
        assertEquals("Estatística", profAtualizado.getDepartamento());
    }

    @Test
    void deleteProfessor_Success() throws Exception {
        Long id = professorExistente.getIdUsuario();

        mockMvc.perform(delete("/professores/{id}", id)
                .with(user(adminUserDetails)))
                .andExpect(status().isNoContent());

        assertFalse(professorRepository.findById(id).isPresent());
    }
}