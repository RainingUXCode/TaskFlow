
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

O **Observer** é um padrão de projeto comportamental que estabelece uma relação entre um objeto responsável por publicar mudanças de estado (*Subject*) e outros objetos interessados em receber essas atualizações (*Observers*).

No TaskFlow, o `GerenciadorTarefas` desempenha o papel de **Subject**. Após uma alteração efetiva no estado das tarefas, publica um `EventoTarefa` para os objetos inscritos na interface `Observador`.

Três observadores concretos respondem ao mesmo evento:

| Observador | Responsabilidade |
| --- | --- |
| `QuadroObservador` | Atualizar os cartões exibidos no quadro Kanban. |
| `ContadorObservador` | Atualizar os totais de tarefas de cada etapa. |
| `NotificacaoObservador` | Apresentar notificações visuais sobre as ações realizadas. |

O evento informa o tipo de operação, a tarefa afetada e uma cópia imutável da lista de tarefas após a alteração.

O gerenciador conhece os observadores somente pela interface `Observador`, permitindo registrar novas implementações sem alterar seu mecanismo de publicação.

O [diagrama de classes](docs/diagrama-classes.md) apresenta os principais relacionamentos da arquitetura.

### Funcionamento do Observer no TaskFlow

O fluxo de atualização ocorre da seguinte maneira:

1. O usuário realiza uma ação, como criar, movimentar ou excluir uma tarefa.
2. O `GerenciadorTarefas` processa a operação e atualiza o estado das tarefas.
3. Após a alteração, o gerenciador cria e publica um `EventoTarefa`.
4. Os observadores registrados recebem o evento por meio do método `atualizar(EventoTarefa)`.
5. Cada observador executa sua responsabilidade, atualizando o quadro, os contadores ou as notificações.

Dessa forma, uma única alteração pode produzir diferentes atualizações na interface, sem que o gerenciador precise conhecer diretamente os componentes visuais.

### Por que escolhemos o padrão Observer?

O padrão **Observer** foi escolhido porque o TaskFlow possui diferentes componentes da interface que precisam reagir às mesmas alterações realizadas nas tarefas.

Sempre que uma tarefa é criada, movimentada ou excluída, três elementos precisam ser atualizados: o quadro Kanban, os contadores de tarefas e as notificações visuais.

Sem o Observer, o gerenciador poderia precisar conhecer e acionar diretamente cada componente visual, aumentando o acoplamento entre as regras de negócio e a interface.

Com o Observer, o `GerenciadorTarefas` apenas publica os eventos, enquanto cada observador inscrito assume a responsabilidade de atualizar seu próprio componente.

Essa abordagem foi considerada a mais adequada por oferecer:

- **Baixo acoplamento:** o gerenciador depende apenas do contrato `Observador`, sem conhecer as implementações concretas.
- **Separação de responsabilidades:** cada observador possui uma função específica e independente.
- **Extensibilidade:** novos observadores podem ser adicionados sem modificar o mecanismo de notificação.
- **Consistência:** os componentes recebem os eventos após a alteração do estado das tarefas.
- **Manutenibilidade:** alterações em um componente visual podem ser realizadas sem exigir modificações nos demais observadores.

Além disso, o padrão contribui para a aplicação dos princípios SOLID, principalmente o **Princípio da Responsabilidade Única (SRP)**, o **Princípio Aberto/Fechado (OCP)** e o **Princípio da Inversão de Dependência (DIP)**.

Portanto, o Observer atende diretamente à principal necessidade arquitetural do TaskFlow: permitir que diferentes componentes reajam automaticamente às alterações das tarefas, mantendo a lógica de gerenciamento separada das responsabilidades visuais.

### Outros padrões de projeto que poderiam ser utilizados

Embora o Observer tenha sido escolhido como padrão principal, outros padrões de projeto também poderiam ser aplicados ao TaskFlow, dependendo das necessidades e funcionalidades da aplicação.

| Padrão | Possível aplicação no TaskFlow | Por que não foi escolhido como principal? |
| --- | --- | --- |
| **Mediator** | Centralizar a comunicação entre os componentes da interface por meio de um objeto intermediário. | Introduziria um coordenador para componentes que, neste projeto, precisam principalmente reagir às mesmas alterações de estado. |
| **Command** | Encapsular operações como criar, movimentar ou excluir tarefas em objetos independentes. | Seria útil para implementar funcionalidades como desfazer e refazer operações, mas não resolveria diretamente a atualização dos múltiplos componentes. |
| **Strategy** | Permitir diferentes estratégias de ordenação, classificação ou priorização de tarefas. | Poderia ser utilizado em expansões futuras, mas não atende diretamente à necessidade central de sincronização dos componentes. |

Esses padrões não são necessariamente incompatíveis com o Observer. Eles poderiam ser incorporados futuramente para atender a novas necessidades, sem substituir o mecanismo principal de notificações.

O **Observer foi considerado a alternativa mais adequada** porque resolve diretamente o problema de comunicação entre o gerenciador de tarefas e os diferentes componentes que precisam responder às mudanças.

Sua implementação permite uma arquitetura organizada, extensível e com menor dependência entre os módulos, sem introduzir complexidade desnecessária para o escopo atual da aplicação.

## Princípios SOLID

Além do Observer, a arquitetura do TaskFlow foi organizada considerando os cinco princípios SOLID, buscando facilitar a manutenção, a compreensão e a evolução do código.

| Princípio | Aplicação |
| --- | --- |
| **SRP — Responsabilidade Única** | Modelo, gerenciamento de tarefas e componentes visuais possuem responsabilidades delimitadas. |
| **OCP — Aberto/Fechado** | Novos observadores podem implementar o contrato existente e ser registrados sem modificar o mecanismo de notificação. |
| **LSP — Substituição de Liskov** | Os três observadores implementam o mesmo contrato e podem ser registrados por meio da abstração `Observador`. |
| **ISP — Segregação de Interfaces** | A interface `Observador` expõe apenas a operação `atualizar(EventoTarefa)`. |
| **DIP — Inversão de Dependência** | `GerenciadorTarefas` depende do contrato `Observador`, enquanto a interface recebe ações funcionais configuradas em `Main`. |

A utilização desses princípios complementa o padrão Observer e favorece a separação de responsabilidades entre as regras de negócio e os componentes da interface.

## Como executar

### Pré-requisitos

- JDK com suporte ao Java 17.
- Visual Studio Code com a extensão **Extension Pack for Java**, caso utilize o VS Code.
- Ambiente com suporte à interface gráfica Java Swing.

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

Após a execução, a interface desktop do TaskFlow será iniciada, permitindo o gerenciamento de tarefas pelo quadro Kanban.

## Testes

O projeto contém **10 verificações automatizadas**: nove cenários de gerenciamento de tarefas e eventos, e um cenário de integração dos três observadores Swing.

Os testes foram implementados em Java, sem a necessidade de JUnit ou de bibliotecas externas de testes.

Depois de compilar as fontes com o comando acima, execute:

```powershell
java -cp out com.taskflow.service.GerenciadorTarefasTeste
java -cp out com.taskflow.view.NotificacaoObservadorTeste
```

Os cenários contemplam verificações relacionadas a:

- Criação, alteração e exclusão de tarefas.
- Validação de identificadores e status.
- Registro e remoção de observadores.
- Publicação e consistência dos eventos.
- Imutabilidade dos dados disponibilizados aos observadores.
- Tratamento de operações inválidas.
- Isolamento de falhas durante a notificação.
- Integração entre o quadro Kanban, os contadores e as notificações visuais.

A documentação registra **10 cenários aprovados** na validação realizada para a preparação da entrega.

Consulte a [documentação de testes](docs/testes.md) para conhecer os cenários, resultados esperados, critérios de execução e detalhes da validação.

## Documentação complementar

O repositório também disponibiliza materiais complementares para compreender a arquitetura e verificar os comportamentos implementados:

- [Diagrama de classes — núcleo Observer](docs/diagrama-classes.md)
- [Documentação dos testes automatizados](docs/testes.md)

Esses documentos complementam a explicação do padrão Observer, apresentando seus relacionamentos, responsabilidades e verificações de funcionamento.
