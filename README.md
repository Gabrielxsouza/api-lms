# API de LMS para Cursos de Exatas

Este projeto é a API back-end para um Sistema de Gerenciamento de Aprendizado (LMS) robusto e especializado, construído com Spring Boot. O foco do sistema é atender às necessidades específicas de cursos de ciências exatas, permitindo um gerenciamento detalhado de disciplinas, turmas, tópicos, atividades pedagógicas e, principalmente, uma **análise de desempenho granular baseada em tags**.

## Contexto Acadêmico

Este projeto foi desenvolvido para as disciplinas de **GRUAPIM (APIs e Microsserviços)** e **GRUENGS (Engenharia de Software)** do Instituto Federal de São Paulo (IFSP) - Campus Guarulhos, sob a orientação do Professor Giovani.

### 👨‍💻 Equipe
* Gabriel de Souza Costa
* Gabriel Feitoza da Silva
* Lucas Bento da Silva Batista
* Maria Eduarda Alves Selvatti

---

## ✨ Funcionalidades Principais

A API suporta o CRUD completo, autenticação e regras de negócio complexas para as seguintes funcionalidades:

* **Gerenciamento de Usuários e Segurança:**
    * Estrutura de herança (`SINGLE_TABLE`) para `Usuario`, `Aluno`, `Professor` e `Administrador`.
    * Autenticação via **Spring Security** e **JWT (Tokens)**.
    * Controle de acesso baseado em Roles (`hasRole('ALUNO')`, `hasRole('PROFESSOR')`, etc.).
    * Criptografia de senhas com `BCrypt`.

* **Estrutura Acadêmica:**
    * **Disciplinas e Turmas:** Gerenciamento completo, incluindo criação aninhada e deleção em cascata.
    * **Matrículas:** Vinculação de alunos às turmas (gerenciado por administradores).
    * **Tópicos de Aula:** Organização do conteúdo, com sanitização de HTML (OWASP) para segurança.

* **Material de Aula:**
    * Upload de arquivos (`MultipartFile`) vinculados a tópicos.
    * Integração com o sistema de recomendação de estudos.

* **Atividades e Avaliações (Polimorfismo):**
    * Sistema flexível com herança para diferentes tipos de atividades:
        * **Texto:** Aluno submete uma redação/resposta dissertativa.
        * **Arquivo:** Aluno faz upload de um arquivo (PDF, ZIP, etc.).
        * **Questionário:** Avaliação objetiva com correção automática.

* **Tentativas e Correção:**
    * Fluxo completo de submissão pelo Aluno.
    * **Correção Automática:** Para questionários, a nota é calculada instantaneamente baseada nas alternativas corretas.
    * **Correção Manual:** Para Texto e Arquivo, o Professor lança nota e feedback.
    * Regras de negócio para reenvio e bloqueio de edição após correção.

* **Banco de Questões Inteligente:**
    * Cadastro de questões e alternativas.
    * **Filtros Dinâmicos (JPA Specifications):** Professores podem buscar questões filtrando por **Tags** ou **Palavras-chave** no enunciado.

* **📈 Análise de Desempenho e Recomendação (O Diferencial):**
    * O sistema rastreia o desempenho do aluno por **Tags de Conteúdo** (ex: "Derivadas", "Cálculo 1").
    * **Diagnóstico Automático:** Identifica "Pontos Fracos" (tags com média abaixo do limiar).
    * **Sistema de Recomendação:** Sugere automaticamente **Materiais de Aula** específicos para reforçar os pontos fracos identificados.
    * **Visão Hierárquica:** Relatórios disponíveis em três níveis:
        1.  **Aluno:** Vê seu próprio desempenho e sugestões.
        2.  **Professor:** Vê o desempenho agregado de uma **Turma**.
        3.  **Coordenador:** Vê o desempenho agregado de uma **Disciplina**.

* **População de Dados:**
    * `DataInitializer` robusto que popula o banco (H2) com cenários complexos de teste (alunos, tentativas, notas e materiais) a cada inicialização.

---

## 🗺️ Endpoints da API

A API segue os padrões RESTful e utiliza Swagger (OpenAPI) para documentação.

*(Acesse `http://localhost:8080/swagger-ui.html` para testar)*

### 📊 Análise de Desempenho
* `GET /analise/aluno/meu-desempenho`: Relatório pessoal do aluno (com sugestões de estudo).
    * *Filtros opcionais:* `?disciplinaId=X`, `?dataInicio=YYYY-MM-DD`, `?dataFim=YYYY-MM-DD`.
* `GET /analise/turma/{id}`: Relatório agregado da turma (Visão Professor).
* `GET /analise/disciplina/{id}`: Relatório agregado da disciplina (Visão Coordenador).

### 📝 Submissão de Tentativas
* **Texto:**
    * `POST /tentativaTexto/{idAtividade}`: Aluno envia resposta.
    * `PATCH /tentativaTexto/professor/{id}`: Professor dá nota/feedback.
* **Arquivo:**
    * `POST /tentativaArquivo/{idAtividade}`: Aluno envia arquivo.
    * `PUT /tentativaArquivo/aluno/{id}`: Aluno substitui arquivo.
* **Questionário:**
    * `POST /tentativaQuestionario`: Aluno submete respostas (correção automática).

### 📚 Gestão de Conteúdo (Professor)
* **Questões:**
    * `GET /questoes`: Listagem com filtros (`?tagNome=...&palavraChave=...`).
    * `POST /questoes`: Criar questão com alternativas e tags.
* **Atividades:**
    * `POST /atividades-texto`, `/atividades-arquivo`, `/atividades-questionario`.
* **Materiais:**
    * `POST /materiais/topico/{id}`: Upload de material de aula.

### 🏫 Estrutura (Admin/Professor)
* **Disciplinas:** `POST`, `GET`, `PATCH`, `DELETE`.
* **Turmas:** `POST`, `GET`, `PATCH`, `DELETE`.
* **Tópicos:** `POST`, `GET`, `PATCH`, `DELETE`.
* **Tags:** `POST`, `GET`, `PATCH`, `DELETE`.

### 👥 Usuários e Acesso
* **Autenticação:** `POST /auth/login` (Gera Token JWT).
* **Alunos/Professores:** CRUD completo para gestão de usuários.
* **Matrículas:** `POST /matriculas` (Admin vincula Aluno à Turma).