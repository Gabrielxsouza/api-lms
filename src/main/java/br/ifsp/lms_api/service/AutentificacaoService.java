package br.ifsp.lms_api.service;

import br.ifsp.lms_api.config.CustomUserDetails; // <-- Importe a nova classe
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutentificacaoService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;

    public AutentificacaoService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Optional<Usuario> usuarioPorEmail = usuarioRepository.findByEmail(username);
        
        if (usuarioPorEmail.isPresent()) {
            return new CustomUserDetails(usuarioPorEmail.get());
        }
        Optional<Aluno> alunoPorRa = alunoRepository.findByRa(username);
        
        if (alunoPorRa.isPresent()) {
            return new CustomUserDetails(alunoPorRa.get()); 
        }
        
        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }
}