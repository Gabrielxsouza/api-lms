package br.ifsp.lms_api.initialData;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.model.*;
import br.ifsp.lms_api.repository.*;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    // (Repositórios existentes)
    private final TagRepository tagRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final TopicosRepository topicosRepository;
    private final QuestoesRepository questoesRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final AdministradorRepository administradorRepository;
    // Removed Atividade repos
    private final PasswordEncoder passwordEncoder;
    private final MatriculaRepository matriculaRepository;

    // --- ADICIONAR ESTES REPOSITÓRIOS ---
    private final MaterialDeAulaRepository materialDeAulaRepository;
    private final TentativaTextoRepository tentativaTextoRepository;
    private final TentativaQuestionarioRepository tentativaQuestionarioRepository;

    public DataInitializer(TagRepository tagRepository,
            DisciplinaRepository disciplinaRepository,
            TurmaRepository turmaRepository,
            TopicosRepository topicosRepository,
            QuestoesRepository questoesRepository,
            AlunoRepository alunoRepository,
            ProfessorRepository professorRepository,
            AdministradorRepository administradorRepository,
            // Removed Atividade repos
            PasswordEncoder passwordEncoder,
            MaterialDeAulaRepository materialDeAulaRepository,
            TentativaTextoRepository tentativaTextoRepository,
            TentativaQuestionarioRepository tentativaQuestionarioRepository,
            MatriculaRepository matriculaRepository) {

        // (Atribuições existentes)
        this.tagRepository = tagRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.topicosRepository = topicosRepository;
        this.questoesRepository = questoesRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.administradorRepository = administradorRepository;
        // Removed Atividade repos
        this.passwordEncoder = passwordEncoder;

        // --- ADICIONAR ESTAS ATRIBUIÇÕES ---
        this.materialDeAulaRepository = materialDeAulaRepository;
        this.tentativaTextoRepository = tentativaTextoRepository;
        this.tentativaQuestionarioRepository = tentativaQuestionarioRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (tagRepository.count() > 0) {
            System.out.println(">>> BANCO DE DADOS JÁ POPULADO. SKIP DATA SEEDER.");
            return;
        }

        System.out.println(">>> INICIANDO O DATA SEEDER (POPULANDO BANCO DE DADOS)...");

        // --- TAGS ---
        Tag tagCalculo = new Tag();
        tagCalculo.setNome("Cálculo 1");
        Tag tagDerivadas = new Tag();
        tagDerivadas.setNome("Derivadas");
        Tag tagP1 = new Tag();
        tagP1.setNome("Prova P1");
        Tag tagPOO = new Tag();
        tagPOO.setNome("POO");

        tagRepository.saveAll(List.of(tagCalculo, tagDerivadas, tagP1, tagPOO));

        // --- USUÁRIOS ---
        Aluno aluno = new Aluno();
        aluno.setNome("Maria Eduarda Alves Selvatti");
        aluno.setEmail("maria.selvatti@aluno.ifsp.edu.br");
        aluno.setSenha(passwordEncoder.encode("123456"));
        aluno.setCpf("123.456.789-00");
        aluno.setRa("GU3000001");
        alunoRepository.save(aluno);

        Aluno aluno2 = new Aluno();
        aluno2.setNome("Gabriel Feitoza");
        aluno2.setEmail("gabrielfeitoza@aluno.ifsp.edu.br");
        aluno2.setSenha(passwordEncoder.encode("123456"));
        aluno2.setCpf("111.113.111-11");
        aluno2.setRa("GU3000002");
        alunoRepository.save(aluno2);

        Professor prof = new Professor();
        prof.setNome("Prof. Giovani");
        prof.setEmail("giovani@ifsp.edu.br");
        prof.setSenha(passwordEncoder.encode("123456"));
        prof.setCpf("987.654.321-00");
        prof.setDepartamento("Engenharia da Computação");
        professorRepository.save(prof);

        Administrador admin = new Administrador();
        admin.setNome("Admin");
        admin.setEmail("admin@lms.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setCpf("111.111.111-11");
        administradorRepository.save(admin);

        // --- ESTRUTURA DE CURSO ---
        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Engenharia de Software");
        disciplina.setCodigoDisciplina("ESL708");
        disciplina.setDescricaoDisciplina("Disciplina de teste");

        Turma turma = new Turma();
        turma.setNomeTurma("Turma A - 2025");
        turma.setSemestre("2025/2");
        turma.setProfessor(prof);
        prof.setTurmas(List.of(turma));
        turma.setDisciplina(disciplina);
        disciplina.setTurmas(List.of(turma));
        disciplinaRepository.save(disciplina);

        // --- TÓPICO 1 ---
        Topicos topico1 = new Topicos();
        topico1.setTituloTopico("Tópico 1 - Prova P1");
        topico1.setConteudoHtml("<p>Este tópico contém a P1.</p>");
        topico1.setTurma(turma);
        topico1.setTags(Set.of(tagP1));
        topicosRepository.save(topico1);

        /*
         * ACTIVITIES AND ATTEMPTS SEEDING DISABLED - MOVED TO MICROSERVICE
         *
         * AtividadeTexto atividadeTexto = new AtividadeTexto();
         * // ...
         * atividadeTextoRepository.save(atividadeTexto);
         * 
         * AtividadeArquivos atividadeArquivo = new AtividadeArquivos();
         * // ...
         * atividadeArquivosRepository.save(atividadeArquivo);
         * 
         * Questoes questao1 = new Questoes();
         * // ...
         * questoesRepository.save(questao1);
         * 
         * AtividadeQuestionario questionario = new AtividadeQuestionario();
         * // ...
         * atividadeQuestionarioRepository.save(questionario);
         * 
         * TentativaTexto tentTexto = new TentativaTexto();
         * // ...
         * tentativaTextoRepository.save(tentTexto);
         * 
         * TentativaQuestionario tentQuest = new TentativaQuestionario();
         * // ...
         * tentativaQuestionarioRepository.save(tentQuest);
         */

        System.out.println(">>> Matriculando alunos na turma...");
        Matricula matriculaMaria = new Matricula();
        matriculaMaria.setAluno(aluno);
        matriculaMaria.setTurma(turma);
        matriculaMaria.setStatusMatricula(br.ifsp.lms_api.model.Status.ATIVA);
        matriculaRepository.save(matriculaMaria);

        Matricula matriculaGabriel = new Matricula();
        matriculaGabriel.setAluno(aluno2);
        matriculaGabriel.setTurma(turma);
        matriculaGabriel.setStatusMatricula(br.ifsp.lms_api.model.Status.ATIVA);
        matriculaRepository.save(matriculaGabriel);

        System.out.println(">>> DATA SEEDER CONCLUÍDO. APLICAÇÃO PRONTA!");
    }
}