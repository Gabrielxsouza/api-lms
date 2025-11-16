package br.ifsp.lms_api.initialData;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.model.*; 
import br.ifsp.lms_api.repository.*; 

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TagRepository tagRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final TopicosRepository topicosRepository;
    private final QuestoesRepository questoesRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final AdministradorRepository administradorRepository;
    private final AtividadeTextoRepository atividadeTextoRepository;
    private final AtividadeArquivosRepository atividadeArquivosRepository;
    private final AtividadeQuestionarioRepository atividadeQuestionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(TagRepository tagRepository,
                           DisciplinaRepository disciplinaRepository,
                           TurmaRepository turmaRepository,
                           TopicosRepository topicosRepository,
                           QuestoesRepository questoesRepository,
                           AlunoRepository alunoRepository,
                           ProfessorRepository professorRepository,
                           AdministradorRepository administradorRepository,
                           AtividadeTextoRepository atividadeTextoRepository,
                           AtividadeArquivosRepository atividadeArquivosRepository,
                           AtividadeQuestionarioRepository atividadeQuestionarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.tagRepository = tagRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.topicosRepository = topicosRepository;
        this.questoesRepository = questoesRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.administradorRepository = administradorRepository;
        this.atividadeTextoRepository = atividadeTextoRepository;
        this.atividadeArquivosRepository = atividadeArquivosRepository;
        this.atividadeQuestionarioRepository = atividadeQuestionarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> INICIANDO O DATA SEEDER (POPULANDO BANCO DE DADOS)...");

        Tag tagCalculo = new Tag();
        tagCalculo.setNome("Cálculo 1");
        Tag tagDerivadas = new Tag();
        tagDerivadas.setNome("Derivadas");
        Tag tagP1 = new Tag();
        tagP1.setNome("Prova P1");
        
        tagRepository.saveAll(List.of(tagCalculo, tagDerivadas, tagP1));

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

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Engenharia de Software");
        disciplina.setCodigoDisciplina("ESL708");
        disciplina.setDescricaoDisciplina("Disciplina de teste");

        Turma turma = new Turma();
        turma.setNomeTurma("Turma A - 2025");
        turma.setSemestre("2025/2");
        
        turma.setDisciplina(disciplina);
        disciplina.setTurmas(List.of(turma));
        
        disciplinaRepository.save(disciplina); 

        Topicos topico1 = new Topicos();
        topico1.setTituloTopico("Tópico 1 - Prova P1");
        topico1.setConteudoHtml("<p>Este tópico contém a P1.</p>");
        topico1.setTurma(turma); 
        topico1.setTags(Set.of(tagCalculo));
        topicosRepository.save(topico1); 

        AtividadeTexto atividadeTexto = new AtividadeTexto();
        atividadeTexto.setTituloAtividade("Redação P1 (Texto)");
        atividadeTexto.setDataInicioAtividade(LocalDate.now());
        atividadeTexto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        atividadeTexto.setStatusAtividade(true);
        atividadeTexto.setNumeroMaximoCaracteres(1000L); 
        atividadeTexto.setTags(Set.of(tagP1));
        atividadeTexto.setTopico(topico1); 
        atividadeTextoRepository.save(atividadeTexto);


        AtividadeArquivos atividadeArquivo = new AtividadeArquivos();
        atividadeArquivo.setTituloAtividade("Upload P1 (Arquivo)");
        atividadeArquivo.setDataInicioAtividade(LocalDate.now());
        atividadeArquivo.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        atividadeArquivo.setStatusAtividade(true);
        atividadeArquivo.setArquivosPermitidos(List.of(".pdf", ".zip"));
        atividadeArquivo.setTags(Set.of(tagP1));
        atividadeArquivo.setTopico(topico1); 
        atividadeArquivosRepository.save(atividadeArquivo);

        Alternativas alt1 = new Alternativas(null, "2x", true, null);
        Alternativas alt2 = new Alternativas(null, "x^2", false, null);
        
        Questoes questao1 = new Questoes();
        questao1.setEnunciado("Qual a derivada de f(x) = x^2 ?");
        questao1.setTags(Set.of(tagCalculo, tagDerivadas));
        
        questao1.setAlternativas(List.of(alt1, alt2));
        alt1.setQuestoes(questao1);
        alt2.setQuestoes(questao1);
        
        questoesRepository.save(questao1);

        AtividadeQuestionario questionario = new AtividadeQuestionario();
        questionario.setTituloAtividade("Simulado P1 de Cálculo");
        questionario.setDataInicioAtividade(LocalDate.now());
        questionario.setDataFechamentoAtividade(LocalDate.now().plusDays(10));
        questionario.setStatusAtividade(true);
        questionario.setDuracaoQuestionario(60L);
        questionario.setNumeroTentativas(3);
        questionario.setTopico(topico1); 
        questionario.setTags(Set.of(tagP1, tagCalculo)); 
        questionario.setQuestoes(List.of(questao1));
        
        atividadeQuestionarioRepository.save(questionario);

        System.out.println(">>> DATA SEEDER CONCLUÍDO. APLICAÇÃO PRONTA!");
    }
}