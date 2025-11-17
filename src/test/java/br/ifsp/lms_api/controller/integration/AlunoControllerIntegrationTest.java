package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; 
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.dto.alunoDto.AlunoRequestDto;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.repository.AlunoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AlunoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlunoRepository alunoRepository;

    private Aluno alunoExistente;

    @BeforeEach
    void setUp() {
        alunoRepository.deleteAll();

        alunoExistente = new Aluno();
        alunoExistente.setNome("Aluno Integração");
        alunoExistente.setEmail("integracao@test.com");
        alunoExistente.setSenha("senha123");
        alunoExistente.setCpf("111.222.333-44");
        alunoExistente.setRa("RA999");
        alunoExistente = alunoRepository.save(alunoExistente);
    }

    @Test
    void testCreateAluno_Success() throws Exception {
        AlunoRequestDto requestDto = new AlunoRequestDto(
            "Novo Aluno", "novo@test.com", "senha123", "999.888.777-66", "RA888"
        );


    }

    @Test
    void testGetById_Success() throws Exception {
   
    }
}