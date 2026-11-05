# Museu da Computação

> Do passado ao presente: um stand de memórias.

Exposição interativa sobre a evolução da computação, das engrenagens de 1642 aos agentes de IA. O visitante percorre uma linha do tempo, compara como se programava antes e agora, roda linguagens antigas de verdade, conversa com uma IA de 1966, treina um neurônio artificial e deixa a própria história em um mural compartilhado.

Projeto desenvolvido no curso de Engenharia de Software do **Senai FATESG**.

O back-end é escrito em **Java puro, sem nenhuma dependência externa**: servidor HTTP, roteador, parser e serializador JSON, interpretador BASIC e chatbot ELIZA foram implementados do zero. O front-end é HTML, CSS e JavaScript (ES Modules) sem frameworks.

---

## Seções do museu

| # | Seção | O que tem |
|---|-------|-----------|
| 01 | **Linha do tempo** | Marcos da computação desde 1642, com filtro por tema, e uma votação "Onde você entrou nessa história?" com gráfico por década. |
| 02 | **Antes x Agora** | Cartões comparando o jeito antigo e o atual de programar, e um slider "antes e depois" entre o Turbo Pascal de 1983 e um editor moderno com IA. |
| 03 | **Linguagens** | Os 10 primeiros números de Fibonacci em 13 linguagens (FORTRAN, LISP, COBOL, BASIC, Pascal, C, C++, Python, Java, JavaScript, Go, Rust e Kotlin). BASIC roda no servidor e JavaScript roda no navegador. |
| 04 | **Você reconhece?** | Cartões com trechos clássicos que todo programador "das antigas" já viu. Tente lembrar antes de virar. |
| 05 | **Inteligência Artificial** | Converse com a **ELIZA** (1966), que roda no back-end, e treine um **Perceptron** (1958) clicando em um canvas. |
| 06 | **Mural de Memórias** | Visitantes publicam lembranças (primeiro computador, primeiro código...) que ficam salvas e visíveis para todos. |

Há também um botão para ouvir o som da internet discada, gerado no navegador.

---

## Requisitos

- **JDK 17 ou superior** (o código usa `record`, `switch` com setas e pattern matching em `instanceof`)

Não é necessário Maven, Gradle nem qualquer biblioteca.

---

## Como executar

Todos os comandos devem ser executados **na raiz do projeto**, porque o servidor procura as pastas `web/` e `data/` a partir do diretório atual.

### Windows (PowerShell)

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

### Linux / macOS

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out Main
```

### IntelliJ IDEA

Abra a pasta do projeto, execute a classe `Main` e confirme que o *Working directory* da configuração de execução é a raiz do projeto.

Depois, acesse **http://localhost:8080**.

---

## Configuração

A configuração é feita por variáveis de ambiente:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `PORT` | `8080` | Porta HTTP do servidor. |
| `MUSEU_ADMIN_KEY` | *(vazia)* | Chave para apagar memórias do mural. Sem ela, a remoção fica desabilitada. |

Exemplo no PowerShell:

```powershell
$env:PORT = "9000"; $env:MUSEU_ADMIN_KEY = "minha-chave-secreta"
java -cp out Main
```

---

## Persistência dos dados

As memórias do mural e os votos por década são gravados em arquivos JSON dentro de `data/runtime/` e recarregados quando o servidor inicia, então **sobrevivem a reinícios**:

```
data/runtime/memories.json
data/runtime/eraVotes.json
```

A gravação é feita em um arquivo temporário que depois substitui o original, para evitar corrupção se o processo cair no meio da escrita. A pasta `data/runtime/` está no `.gitignore` e não vai para o repositório.

> **Ao publicar na internet:** em plataformas com disco temporário (Render no plano grátis, Heroku, contêineres sem volume etc.), os arquivos são apagados a cada deploy ou reinício. Nesses casos, monte um volume persistente em `data/runtime/`.

---

## API

Todas as respostas são em JSON.

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/health` | Status do servidor e horário de início. |
| `GET` | `/api/timeline?category=` | Eventos da linha do tempo, com filtro opcional por categoria. |
| `GET` | `/api/languages` | Linguagens e seus programas de Fibonacci. |
| `GET` | `/api/comparisons` | Comparações "Antes x Agora". |
| `GET` | `/api/challenges` | Cartões do "Você reconhece?". |
| `GET` | `/api/guide-questions` | Perguntas de inspiração do mural. |
| `GET` | `/api/eras` | Décadas disponíveis. |
| `GET` | `/api/era-votes` | Totais da votação por década. |
| `POST` | `/api/era-votes` | Registra um voto. Corpo: `{"era": "anos90"}` |
| `GET` | `/api/memories?era=` | Até 200 memórias mais recentes, com filtro opcional por década. |
| `POST` | `/api/memories` | Publica uma memória (ver abaixo). |
| `DELETE` | `/api/memories/{id}` | Remove uma memória. Exige o cabeçalho `X-Admin-Key`. |
| `POST` | `/api/interpreters/basic` | Executa um programa BASIC. Corpo: `{"source": "10 PRINT \"OLA\""}` |
| `POST` | `/api/ai/eliza` | Conversa com a ELIZA. Corpo: `{"message": "Eu me sinto cansado"}` |

Códigos de década aceitos: `anos70`, `anos80`, `anos90`, `anos2000`, `anos2010`, `anos2020`.

### Exemplo: publicar uma memória

```bash
curl -X POST http://localhost:8080/api/memories \
  -H "Content-Type: application/json" \
  -d '{"text": "Meu primeiro código foi um 10 PRINT na escola", "era": "anos90", "authorName": "Ana"}'
```

| Campo | Obrigatório | Regra |
|-------|-------------|-------|
| `text` | Sim | De 3 a 280 caracteres. |
| `era` | Sim | Um dos códigos de década. |
| `authorName` | Não | Até 40 caracteres. Vazio aparece como anônimo. |
| `guideQuestion` | Não | Até 200 caracteres. |

---

## Interpretador BASIC

Escrito do zero em Java (tokenizador, parser e avaliador de expressões). Suporta:

- **Comandos:** `PRINT`, `LET`, `IF ... THEN`, `GOTO`, `GOSUB` / `RETURN`, `FOR ... TO ... STEP` / `NEXT`, `REM`, `END`
- **Funções:** `ABS`, `INT`, `SQR`, `RND`, `LEN`, `STR$`

Como o código vem de visitantes, a execução tem limites de segurança: até 10.000 caracteres de código, 50.000 passos (para cortar loops infinitos), 20.000 caracteres de saída e 200 níveis de `GOSUB`.

```basic
10 LET A = 0
20 LET B = 1
30 FOR I = 1 TO 10
40 PRINT A
50 LET T = A + B
60 LET A = B
70 LET B = T
80 NEXT I
```

---

## Proteções

- **Limite de envios por IP:** 10 memórias e 30 votos por minuto (janela deslizante).
- **Corpo das requisições** limitado a 64 KB.
- **Validação e normalização** de todo texto enviado pelos visitantes.
- **Remoção de memórias** só com a chave de administrador.

---

## Estrutura do projeto

```
MuseuDaComputacao/
├── src/
│   ├── Main.java                  # Ponto de entrada
│   └── museu/
│       ├── MuseuApplication.java  # Monta os controllers e sobe o servidor
│       ├── ai/                    # ELIZA (chatbot por regras)
│       ├── config/                # Leitura das variáveis de ambiente
│       ├── content/               # Conteúdo somente leitura (JSON)
│       ├── era/                   # Votação por década
│       ├── health/                # Rota de status
│       ├── http/                  # Servidor HTTP, roteador e arquivos estáticos
│       ├── interpreter/basic/     # Interpretador BASIC
│       ├── json/                  # Parser e serializador JSON próprios
│       ├── memory/                # Mural de memórias
│       ├── shared/                # Exceções, segurança e utilitários de texto
│       └── timeline/              # Linha do tempo
├── web/                           # Front-end (HTML, CSS e JS)
│   ├── index.html
│   ├── css/
│   └── js/
│       ├── ai/                    # Chat da ELIZA e Perceptron
│       ├── core/                  # Cliente da API, navegação e utilitários
│       ├── runners/               # Execução de código (BASIC, JS, saída gravada)
│       └── sections/              # Uma pasta lógica por seção do museu
└── data/
    ├── content/                   # Conteúdo editável do museu (JSON)
    └── runtime/                   # Dados gerados em execução (ignorado pelo Git)
```

### Editando o conteúdo

Para adicionar eventos à linha do tempo, linguagens, comparações, desafios ou regras da ELIZA, basta editar os JSON em `data/content/` e reiniciar o servidor. Não é preciso recompilar.

| Arquivo | Conteúdo |
|---------|----------|
| `timeline.json` | Eventos da linha do tempo |
| `languages.json` | Linguagens e seus programas |
| `comparisons.json` | Comparações "Antes x Agora" |
| `challenges.json` | Cartões do "Você reconhece?" |
| `guideQuestions.json` | Perguntas de inspiração do mural |
| `eliza.json` | Regras e respostas da ELIZA |
