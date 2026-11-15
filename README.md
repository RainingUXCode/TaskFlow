# TaskFlow

**TaskFlow** é uma aplicação desktop de gerenciamento de tarefas em um quadro Kanban. Desenvolvido com Java e Swing, o projeto demonstra o padrão de projeto comportamental **Observer** e a aplicação dos princípios **SOLID** por meio de componentes independentes e atualização automática da interface.

## Funcionalidades

- Criar tarefas com título, descrição opcional e status inicial.
- Organizar tarefas nas etapas **A Fazer**, **Em Andamento** e **Concluído**.
- Alterar o status por meio dos controles dos cartões e excluir tarefas.
- Pesquisar tarefas por título, desconsiderando diferenças entre maiúsculas/minúsculas e acentuação.
- Visualizar a quantidade total de tarefas em cada etapa, mesmo durante a pesquisa.
- Receber notificações visuais quando uma tarefa é criada, movimentada ou excluída.

## Tecnologias

- **Java 17**
- **Java Swing** para a interface desktop
- **Java2D** para personalização visual
- **JDK** para compilação e execução dos testes automatizados

O projeto não requer frameworks ou bibliotecas externas.

## Estrutura do projeto

```text
src/
├── main/java/com/taskflow/
│   ├── Main.java
│   ├── model/        # Tarefa e Status
│   ├── observer/     # Contrato Observador e EventoTarefa
│   ├── service/      # GerenciadorTarefas
│   └── view/         # Interface Swing e observadores visuais
└── test/java/com/taskflow/
    ├── service/      # Testes de regras de negócio e eventos
    └── view/         # Teste de integração dos observadores

docs/
├── diagrama-classes.md
└── testes.md
```

## Arquitetura e padrão Observer

O `GerenciadorTarefas` desempenha o papel de **Subject**. Após uma alteração efetiva no estado das tarefas, publica um `EventoTarefa` para os objetos inscritos na interface `Observador`.

Três observadores concretos respondem ao mesmo evento:

| Observador | Responsabilidade |
| --- | --- |
| `QuadroObservador` | Atualizar os cartões exibidos no quadro Kanban. |
| `ContadorObservador` | Atualizar os totais de tarefas de cada etapa. |
| `NotificacaoObservador` | Apresentar notificações visuais sobre as ações realizadas. |

O evento informa o tipo de operação, a tarefa afetada e uma cópia imutável da lista de tarefas após a alteração. O gerenciador conhece os observadores somente pela interface `Observador`, permitindo registrar novas implementações sem alterar seu mecanismo de publicação.

O [diagrama de classes](docs/diagrama-classes.md) apresenta os principais relacionamentos da arquitetura.

## Princípios SOLID

| Princípio | Aplicação |
| --- | --- |
| **SRP — Responsabilidade Única** | Modelo, gerenciamento de tarefas e componentes visuais possuem responsabilidades delimitadas. |
| **OCP — Aberto/Fechado** | Novos observadores podem implementar o contrato existente e ser registrados sem modificar o mecanismo de notificação. |
| **LSP — Substituição de Liskov** | Os três observadores implementam o mesmo contrato e podem ser registrados por meio da abstração `Observador`. |
| **ISP — Segregação de Interfaces** | A interface `Observador` expõe apenas a operação `atualizar(EventoTarefa)`. |
| **DIP — Inversão de Dependência** | `GerenciadorTarefas` depende do contrato `Observador`, enquanto a interface recebe ações funcionais configuradas em `Main`. |

## Como executar

### VS Code

1. Abra a pasta do projeto no **Visual Studio Code** com o **Extension Pack for Java** instalado.
2. Configure o JDK 17 em **Java: Configure Java Runtime**.
3. Em **Executar e Depurar**, selecione **Executar TaskFlow** e pressione **F5**.

### Compilação e execução pelo PowerShell

Na raiz do projeto, execute:

```powershell
$fontes = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -d out @fontes
java -cp out com.taskflow.Main
```

## Testes

O projeto contém **10 verificações automatizadas**: nove cenários de gerenciamento de tarefas e eventos, e um cenário de integração dos três observadores Swing.

Depois de compilar as fontes com o comando acima, execute:

```powershell
java -cp out com.taskflow.service.GerenciadorTarefasTeste
java -cp out com.taskflow.view.NotificacaoObservadorTeste
```

Consulte a [documentação de testes](docs/testes.md) para conhecer os cenários, resultados esperados e critérios de execução.
