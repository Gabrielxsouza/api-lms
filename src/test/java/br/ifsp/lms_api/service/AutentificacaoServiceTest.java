package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.ifsp.lms_api.model.Administrador; 
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AutentificacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AutentificacaoService autentificacaoService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadUserByUsername_DeveEncontrarUsuarioPorEmail() {
        String email = "admin@test.com";
        
        Usuario usuario = new Administrador(); 
        usuario.setEmail(email);
        usuario.setSenha("123456");
        usuario.setTipoUsuario("ADMIN"); 

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = autentificacaoService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_DeveEncontrarAlunoPorRa_QuandoNaoAcharEmail() {
        String ra = "GU300123";
        
        Aluno aluno = new Aluno();
        aluno.setRa(ra);
        aluno.setEmail("aluno@test.com"); 
        aluno.setSenha("123456");
        aluno.setTipoUsuario("ALUNO");

        when(usuarioRepository.findByEmail(ra)).thenReturn(Optional.empty());
        when(alunoRepository.findByRa(ra)).thenReturn(Optional.of(aluno));

        UserDetails userDetails = autentificacaoService.loadUserByUsername(ra);

        assertNotNull(userDetails);
        assertEquals(aluno.getEmail(), userDetails.getUsername()); 
    }

    @Test
    void loadUserByUsername_DeveLancarException_QuandoNaoEncontrarNinguem() {
        String login = "inexistente";

        when(usuarioRepository.findByEmail(login)).thenReturn(Optional.empty());
        when(alunoRepository.findByRa(login)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            autentificacaoService.loadUserByUsername(login);
        });
    }

    @Test
    void getUsuarioLogado_DeveRetornarUsuarioDoContexto() {
        Usuario usuarioMock = new Administrador();
        usuarioMock.setIdUsuario(1L);
        usuarioMock.setEmail("logado@test.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        
        when(authentication.getPrincipal()).thenReturn(usuarioMock); 

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        Usuario resultado = autentificacaoService.getUsuarioLogado();

        assertNotNull(resultado);
        assertEquals("logado@test.com", resultado.getEmail());
    }

    @Test
    void getUsuarioLogado_DeveLancarException_SeNaoAutenticado() {
        SecurityContextHolder.clearContext();

        assertThrows(UsernameNotFoundException.class, () -> {
            autentificacaoService.getUsuarioLogado();
        });
    }
}