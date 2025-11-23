package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.controller.AnaliseDesempenhoController;
import br.ifsp.lms_api.dto.analise.RelatorioDesempenhoResponseDto;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.service.AnaliseDesempenhoService;

@WebMvcTest(AnaliseDesempenhoController.class)
@EnableMethodSecurity(prePostEnabled = true)
public class AnaliseDesempenhoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AnaliseDesempenhoService analiseService;

    @Test
    void getMeuRelatorio_Aluno_Success() throws Exception {

        Aluno aluno = new Aluno();
        aluno.setIdUsuario(1L);
        aluno.setEmail("aluno@test.com");
        aluno.setSenha("123");
        aluno.setTipoUsuario("ALUNO");
        CustomUserDetails userDetails = new CustomUserDetails(aluno);

        when(analiseService.gerarRelatorioAluno(1L)).thenReturn(new RelatorioDesempenhoResponseDto(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        mockMvc.perform(get("/analise/aluno/meu-desempenho")
                .with(user(userDetails))) 
                .andExpect(status().isOk());
    }

    @Test
    void getMeuRelatorio_Admin_Forbidden() throws Exception {

        mockMvc.perform(get("/analise/aluno/meu-desempenho")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRelatorioTurma_Professor_Success() throws Exception {
        when(analiseService.gerarRelatorioTurma(anyLong())).thenReturn(new RelatorioDesempenhoResponseDto());

        mockMvc.perform(get("/analise/turma/1")
                .with(user("prof").roles("PROFESSOR")))
                .andExpect(status().isOk());
    }
    
    @Test
    void getRelatorioTurma_Aluno_Forbidden() throws Exception {
        mockMvc.perform(get("/analise/turma/1")
                .with(user("aluno").roles("ALUNO")))
                .andExpect(status().isForbidden());
    }
}