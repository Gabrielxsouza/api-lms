package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.repository.AdministradorRepository;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.MatriculaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AutentificacaoServiceIntegrationTest {

    @Autowired
    private AutentificacaoService autentificacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private DisciplinaRepository disciplinaRepository;
    @Autowired
    private TurmaRepository turmaRepository;
    @Autowired
    private MatriculaRepository matriculaRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private AdministradorRepository administradorRepository;

    @BeforeEach
    void setUp() {
        matriculaRepository.deleteAll();
        turmaRepository.deleteAll();

        professorRepository.deleteAll();
        administradorRepository.deleteAll();
        alunoRepository.deleteAll();
        cursoRepository.deleteAll();
        disciplinaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void loadUserByUsername_DeveCarregarAdminPorEmail() {

        Administrador admin = new Administrador();
        admin.setNome("Admin Auth");
        admin.setEmail("admin.auth@test.com");
        admin.setCpf("12312312312");
        admin.setSenha("123456");
        admin.setTipoUsuario("ADMIN");
        usuarioRepository.save(admin);

        UserDetails userDetails = autentificacaoService.loadUserByUsername("admin.auth@test.com");

        assertNotNull(userDetails);
        assertEquals("admin.auth@test.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_DeveCarregarAlunoPorRA() {
        Aluno aluno = new Aluno();
        aluno.setNome("Aluno Auth");
        aluno.setEmail("aluno.auth@test.com");
        aluno.setRa("GU300999");
        aluno.setCpf("99999999999");
        aluno.setSenha("123456");
        aluno.setTipoUsuario("ALUNO");
        alunoRepository.save(aluno);

        UserDetails userDetails = autentificacaoService.loadUserByUsername("GU300999");

        assertNotNull(userDetails);

        assertEquals("aluno.auth@test.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUNO")));
    }

    @Test
    void loadUserByUsername_NaoEncontrado_LancaExcecao() {
        assertThrows(UsernameNotFoundException.class, () -> {
            autentificacaoService.loadUserByUsername("usuario.fantasma@test.com");
        });
    }
}