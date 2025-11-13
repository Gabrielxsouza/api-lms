package br.ifsp.lms_api.initialData;

import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injeta o BCrypt que você já criou

    @Override
    public void run(String... args) throws Exception {
        // --- 1. VERIFICA SE O BANCO JÁ TEM DADOS ---
        // Se já tiver usuários, não faz nada, para não duplicar.
        if (usuarioRepository.count() > 0) {
            System.out.println("O banco de dados já está populado.");
            return;
        }

        System.out.println("Populando o banco de dados com usuários iniciais...");

        // --- 2. CRIA O ADMINISTRADOR ---
        Administrador admin = new Administrador();
        admin.setNome("Admin do Sistema");
        admin.setEmail("admin@ifsp.edu.br");
        admin.setCpf("000.000.000-00");
        // CRIPTOGRAFA A SENHA ANTES DE SALVAR
        admin.setSenha(passwordEncoder.encode("admin123"));

        // --- 3. CRIA O PROFESSOR ---
        Professor prof = new Professor();
        prof.setNome("Professora Ana");
        prof.setEmail("ana.prof@ifsp.edu.br");
        prof.setCpf("111.111.111-11");
        prof.setSenha(passwordEncoder.encode("prof123"));
        prof.setDepartamento("Informática");

        // --- 4. CRIA O ALUNO ---
        Aluno aluno = new Aluno();
        aluno.setNome("Aluno Bruno");
        aluno.setEmail("bruno.aluno@ifsp.edu.br");
        aluno.setCpf("222.222.222-22");
        aluno.setSenha(passwordEncoder.encode("aluno123"));
        aluno.setRa("SP123456");
        
        // --- 5. SALVA TODOS NO BANCO ---
        // Salva todos os usuários de uma vez
        usuarioRepository.saveAll(List.of(admin, prof, aluno));
        
        System.out.println("Usuários criados com sucesso!");
        System.out.println("Admin: admin@ifsp.edu.br / admin123");
        System.out.println("Professor: ana.prof@ifsp.edu.br / prof123");
        System.out.println("Aluno: bruno.aluno@ifsp.edu.br / aluno123");
    }
}