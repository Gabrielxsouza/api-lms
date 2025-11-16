package br.ifsp.lms_api.initialData;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // 1. IMPORTAR
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.model.*; // Importe todos os seus models
import br.ifsp.lms_api.repository.*; // Importe todos os seus repositórios

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    // 2. INJETAR TODOS OS REPOSITÓRIOS + PASSWORD ENCODER
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

        // --- 1. Criar Tags ---
        Tag tagCalculo = new Tag();
        tagCalculo.setNome("Cálculo 1");
        Tag tagDerivadas = new Tag();
        tagDerivadas.setNome("Derivadas");
        Tag tagP1 = new Tag();
        tagP1.setNome("Prova P1");
        
        tagRepository.saveAll(List.of(tagCalculo, tagDerivadas, tagP1));

        // --- 2. Criar Usuários ---
        Aluno aluno = new Aluno();
        aluno.setNome("Maria Eduarda Alves Selvatti");
        aluno.setEmail("maria.selvatti@aluno.ifsp.edu.br");
        aluno.setSenha(passwordEncoder.encode("123456"));
        aluno.setCpf("123.456.789-00");
        aluno.setRa("GU3000001");
        alunoRepository.save(aluno);

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

        // --- 3. Criar Disciplina e Turma ---
        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Engenharia de Software");
        disciplina.setCodigoDisciplina("ESL708");
        disciplina.setDescricaoDisciplina("Disciplina de teste");

        Turma turma = new Turma();
        turma.setNomeTurma("Turma A - 2025");
        turma.setSemestre("2025/2");
        
        // "Amarra" os dois lados
        turma.setDisciplina(disciplina);
        disciplina.setTurmas(List.of(turma));
        
        disciplinaRepository.save(disciplina); // Salva a disciplina (e a turma em cascata)

        // --- 4. Criar Tópico ---
        Topicos topico1 = new Topicos();
        topico1.setTituloTopico("Tópico 1 - Prova P1");
        topico1.setConteudoHtml("<p>Este tópico contém a P1.</p>");
        topico1.setTurma(turma); // Vincula à Turma
        topico1.setTags(Set.of(tagCalculo));
        topicosRepository.save(topico1); // Salva o tópico

        // --- 5. Criar AtividadeTexto (e vincular ao Tópico) ---
        AtividadeTexto atividadeTexto = new AtividadeTexto();
        atividadeTexto.setTituloAtividade("Redação P1 (Texto)");
        atividadeTexto.setDataInicioAtividade(LocalDate.now());
        atividadeTexto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        atividadeTexto.setStatusAtividade(true);
        atividadeTexto.setNumeroMaximoCaracteres(1000L); // Use 'L' para Long
        atividadeTexto.setTags(Set.of(tagP1));
        atividadeTexto.setTopico(topico1); // Vincula ao Tópico
        atividadeTextoRepository.save(atividadeTexto);

        // --- 6. Criar AtividadeArquivos (e vincular ao Tópico) ---
        AtividadeArquivos atividadeArquivo = new AtividadeArquivos();
        atividadeArquivo.setTituloAtividade("Upload P1 (Arquivo)");
        atividadeArquivo.setDataInicioAtividade(LocalDate.now());
        atividadeArquivo.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        atividadeArquivo.setStatusAtividade(true);
        atividadeArquivo.setArquivosPermitidos(List.of(".pdf", ".zip"));
        atividadeArquivo.setTags(Set.of(tagP1));
        atividadeArquivo.setTopico(topico1); // Vincula ao Tópico
        atividadeArquivosRepository.save(atividadeArquivo);

        // --- 7. Criar Questões/Alternativas ---
        Alternativas alt1 = new Alternativas(null, "2x", true, null);
        Alternativas alt2 = new Alternativas(null, "x^2", false, null);
        
        Questoes questao1 = new Questoes();
        questao1.setEnunciado("Qual a derivada de f(x) = x^2 ?");
        questao1.setTags(Set.of(tagCalculo, tagDerivadas));
        
        // "Amarra" os dois lados (Questao <-> Alternativa)
        questao1.setAlternativas(List.of(alt1, alt2));
        alt1.setQuestoes(questao1);
        alt2.setQuestoes(questao1);
        
        questoesRepository.save(questao1); // Salva a Questão (e Alternativas em cascata)

        // --- 8. Criar AtividadeQuestionario (e vincular tudo) ---
        AtividadeQuestionario questionario = new AtividadeQuestionario();
        questionario.setTituloAtividade("Simulado P1 de Cálculo");
        questionario.setDataInicioAtividade(LocalDate.now());
        questionario.setDataFechamentoAtividade(LocalDate.now().plusDays(10));
        questionario.setStatusAtividade(true);
        questionario.setDuracaoQuestionario(60L);
        questionario.setNumeroTentativas(3);
        questionario.setTopico(topico1); // Vincula ao Tópico
        questionario.setTags(Set.of(tagP1, tagCalculo)); // Vincula Tags
        questionario.setQuestoes(List.of(questao1)); // Vincula Questões
        
        atividadeQuestionarioRepository.save(questionario);

        System.out.println(">>> DATA SEEDER CONCLUÍDO. APLICAÇÃO PRONTA!");
    }
}