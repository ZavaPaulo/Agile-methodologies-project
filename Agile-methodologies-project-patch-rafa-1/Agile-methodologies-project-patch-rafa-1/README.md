# Agile Methodologies Projects
## Documentação técnica do sistema

**Projeto:** Réplica simplificada de um marketplace inspirado no Mercado Livre  
**Repositório:** [Agile-methodologies-project](https://github.com/ZavaPaulo/Agile-methodologies-project)  
**Status:** Em desenvolvimento  
**Versão deste documento:** 1.0  
**Última atualização:** 16/10/2026

---

## 1. Visão geral

O **Agile Methodologies Projects** é um projeto acadêmico de desenvolvimento de software cujo objetivo é construir uma réplica simplificada de um marketplace, inspirada na experiência de navegação e compra do Mercado Livre.

A aplicação executada pelo terminal deverá permitir o gerenciamento básico de anúncios e o fluxo de compra de produtos. O projeto também utiliza práticas de organização ágil para distribuir responsabilidades, acompanhar o progresso e facilitar a colaboração entre os integrantes.

> **Nota de escopo:** este documento descreve os requisitos identificados no planejamento do projeto e as decisões técnicas discutidas durante o desenvolvimento. Recursos ainda não implementados devem ser considerados planejados, não entregues.

## 2. Objetivos

### 2.1 Objetivo geral

Desenvolver uma aplicação de marketplace simplificada que permita cadastrar, consultar e excluir anúncios de produtos, gerenciar um carrinho de compras e simular a aquisição de produtos com atualização do estoque.

### 2.2 Objetivos específicos

- Implementar operações de cadastro, consulta, atualização e exclusão de produtos (CRUD).
- Apresentar os produtos e as opções de operação diretamente no terminal.
- Permitir adicionar e remover produtos do carrinho.
- Calcular subtotais e o valor total da compra.
- Simular a finalização de uma compra.
- Atualizar a quantidade disponível após uma compra.
- Identificar produtos de forma única.
- Organizar o trabalho em cards no Trello e manter o código-fonte em repositório Git.

## 3. Escopo funcional

### 3.1 Funcionalidades previstas

| ID | Funcionalidade | Descrição |
|---|---|---|
| RF-01 | Cadastrar produto | Registrar um produto com nome, descrição, preço e quantidade inicial em estoque. |
| RF-02 | Consultar produtos | Listar os produtos cadastrados e consultar um produto específico. |
| RF-03 | Atualizar produto | Editar os dados de um anúncio existente. |
| RF-04 | Excluir anúncio | Remover um anúncio do catálogo. |
| RF-05 | Adicionar ao carrinho | Incluir um produto e uma quantidade no carrinho. |
| RF-06 | Remover do carrinho | Retirar um item do carrinho. |
| RF-07 | Calcular total | Calcular subtotais e o total dos itens do carrinho. |
| RF-08 | Finalizar compra | Simular a confirmação de uma compra. |
| RF-09 | Atualizar estoque | Deduzir do estoque a quantidade comprada. |
| RF-10 | Controlar disponibilidade | Impedir compras acima do estoque e indicar produtos esgotados. |
| RF-11 | Identificar produtos | Atribuir um identificador único a cada produto para operações de consulta e gerenciamento. |

### 3.2 Fora do escopo confirmado

Não há confirmação, na definição atual do projeto, de integração com pagamentos reais, autenticação, contas de usuários, entrega, cálculo de frete, sistema de avaliações ou comunicação com os serviços reais do Mercado Livre. Esses recursos não devem ser considerados requisitos da versão atual sem aprovação do grupo.

## 4. Regras de negócio

- **RN-01 — Identificação:** cada produto deve possuir um identificador único e estável.
- **RN-02 — Nome:** o nome do produto é obrigatório e não pode ser vazio.
- **RN-03 — Descrição:** na implementação inicial, a descrição é obrigatória; o grupo pode rever essa regra caso decida torná-la opcional.
- **RN-04 — Preço:** o preço deve ser positivo e finito.
- **RN-05 — Estoque:** a quantidade disponível não pode ser negativa.
- **RN-06 — Compra:** a quantidade solicitada deve ser maior que zero e não pode ultrapassar o estoque disponível.
- **RN-07 — Reposição:** a quantidade adicionada ao estoque deve ser maior que zero.
- **RN-08 — Esgotamento:** quando o estoque chegar a zero, o produto deve ser indicado como esgotado.
- **RN-09 — Totalização:** os valores da compra devem ser calculados no backend ou na camada responsável pelas regras de negócio, e não dependerem exclusivamente da entrada informada no terminal.
- **RN-10 — Compra inválida:** uma compra recusada não deve alterar o estoque.
- **RN-11 — Exclusão:** a exclusão deve afetar o anúncio correto, identificado pelo seu ID. O comportamento para produtos que já estejam em um carrinho deve ser definido na integração.

## 5. Arquitetura lógica

A arquitetura abaixo é uma proposta de organização por responsabilidades para uma aplicação executada pelo terminal, conforme a definição atual do projeto.

```text
Interface de terminal (CLI)
            |
            v
Camada de aplicação / serviços
            |
            +------> Catálogo e gerenciamento de produtos
            |
            +------> Carrinho e finalização de compra
            |
            v
Coleções em memória durante a execução
```

### 5.1 Regras de negócio e serviços

Responsável por validar as operações e garantir a consistência dos dados.

Responsabilidades previstas:

- Gerenciar o ciclo de vida dos produtos.
- Validar os dados de entrada.
- Controlar inclusão e remoção de itens do carrinho.
- Calcular valores da compra.
- Validar novamente o estoque ao finalizar a compra.
- Atualizar o estoque e indicar produtos esgotados.
- Gerenciar os dados em memória durante a execução.

### 5.2 Modelo `Produto`

A classe `Produto` representa um anúncio do catálogo. A implementação Java discutida pelo grupo contém os seguintes dados:

| Atributo | Tipo previsto | Finalidade |
|---|---|---|
| `idProduto` | `String` | Identificador único do produto. |
| `nomeProduto` | `String` | Nome exibido no catálogo. |
| `detalheProduto` | `String` | Descrição do produto. |
| `valor` | `double` | Preço unitário na implementação inicial. |
| `quantidadeDisponivel` | `int` | Quantidade disponível em estoque. |

A classe representa os dados de um produto. Esta documentação não detalha os métodos internos da classe, conforme solicitado.

> **Consideração técnica:** `double` é mantido aqui por compatibilidade com a implementação inicial. Para aplicações financeiras, é preferível usar `BigDecimal` e definir regras explícitas de arredondamento.

## 6. Organização do código

Estrutura sugerida, a ser adaptada às pastas existentes:

```text
src/
├── model/
│   └── Produto.java
├── service/
│   ├── ProdutoService.java
│   └── CarrinhoService.java
├── controller/             # se a arquitetura utilizar controllers
└── Main.java
```

Essa estrutura é uma sugestão conceitual, não uma descrição garantida da estrutura atual do repositório.

### Responsabilidades sugeridas

- `Produto`: representação dos dados de um produto.
- `ProdutoService`: regras de cadastro, atualização e exclusão.
- `CarrinhoService`: regras de carrinho e compra.
- `Main` ou camada de inicialização: iniciar a aplicação e conectar os componentes.

## 7. Fluxos principais

### 7.1 Cadastro de produto

1. O usuário seleciona a opção de cadastro no terminal e informa os dados solicitados.
2. A aplicação valida nome, descrição, preço e estoque.
3. O sistema cria um identificador único.
4. O produto é registrado em uma coleção em memória.
5. O terminal apresenta o resultado.

### 7.2 Adição ao carrinho

1. O usuário seleciona um produto e informa a quantidade.
2. O sistema localiza o produto pelo ID.
3. A aplicação verifica se o produto existe e se há estoque suficiente.
4. O item é incluído ou atualizado no carrinho.
5. O subtotal e o total são recalculados.
6. O terminal exibe o carrinho atualizado.

### 7.3 Finalização de compra

1. O usuário solicita a finalização.
2. O sistema verifica se o carrinho contém itens.
3. O backend valida novamente os produtos e o estoque.
4. O total é calculado com base nos dados válidos.
5. A compra simulada é confirmada.
6. O estoque é atualizado.
7. O carrinho é limpo e o terminal apresenta a confirmação.

Se alguma validação falhar, a compra deve ser recusada sem deduzir parcialmente o estoque. Em implementações mais robustas, a confirmação da compra e a atualização do estoque devem ser tratadas como uma operação atômica.

## 8. Estratégia de testes

Os testes devem cobrir os fluxos normais, entradas inválidas e situações de limite.

| Área | Cenários mínimos |
|---|---|
| Produto | Criar produto válido; rejeitar nome vazio, preço inválido e estoque negativo. |
| Identificador | Confirmar que produtos diferentes recebem IDs diferentes. |
| Estoque | Repor quantidade positiva; rejeitar zero e valores negativos; impedir estouro numérico. |
| Compra | Comprar quantidade válida; rejeitar quantidade zero, negativa ou acima do estoque. |
| Disponibilidade | Confirmar que estoque zero é exibido como esgotado. |
| CRUD | Cadastrar, consultar, editar e excluir o produto correto pelo ID. |
| Carrinho | Adicionar e remover itens; validar quantidades e conferir subtotais e total. |
| Integração | Validar o fluxo entre terminal, regras de negócio e coleções em memória. |

## 9. Gestão do projeto e colaboração

O planejamento do trabalho é acompanhado no Trello, com listas de tarefas e cards que devem conter:

- Título identificável.
- Descrição do resultado esperado.
- Responsável principal.
- Checklist de execução.
- Critério de conclusão.
- Etiquetas de área, como Backend, Integração ou Testes.

O código-fonte é mantido no GitHub. Recomenda-se que cada integrante trabalhe em alterações delimitadas, escreva mensagens de commit descritivas e revise mudanças antes da integração à versão principal.

### Divisão inicial de responsabilidades

| Integrante | Responsabilidade sugerida |
|---|---|
| Paulo Zava | Modelo e operações de produtos no backend. |
| Bruno Santana | Carrinho, finalização de compra e regras de estoque. |
| Guilherme Mota | Fluxo de catálogo, cadastro e gerenciamento de anúncios pelo terminal. |
| Henrique | Fluxo do carrinho e da compra pelo terminal. |
| Rafael Schnorr | Integração, testes, documentação e preparação da entrega. |

A distribuição acima é uma proposta de planejamento e deve ser confirmada pelo grupo.

## 10. Critérios de aceite do projeto

A versão será considerada pronta para demonstração quando:

- [ ] O usuário puder cadastrar e consultar produtos.
- [ ] Os anúncios puderem ser editados e excluídos.
- [ ] Cada produto tiver um identificador único e estável.
- [ ] O usuário puder adicionar e remover itens do carrinho.
- [ ] Os subtotais e o total forem calculados corretamente.
- [ ] A compra simulada validar o estoque antes de confirmar.
- [ ] O estoque for reduzido somente após uma compra válida.
- [ ] Produtos com estoque zero forem identificados como esgotados.
- [ ] Os principais cenários de sucesso e erro forem testados.
- [ ] O repositório contiver instruções de configuração e execução pelo terminal.
- [ ] Os cards de entrega estiverem revisados e concluídos no Trello.

## 11. Como executar

As instruções definitivas de execução dependem da versão do Java, das ferramentas de build e da estrutura efetivamente configuradas no repositório.

Antes de publicar este documento como versão final, preencher:

- **Pré-requisitos:** versão do Java, runtime e ferramentas necessárias.
- **Instalação:** comandos para obter dependências.
- **Configuração:** arquivos, variáveis e caminhos necessários.
- **Execução:** comando para iniciar a aplicação.
- **Testes:** comando para executar a suíte de testes.

Não foram incluídos comandos de execução específicos porque a estrutura real e a configuração atual do projeto não foram verificadas nesta documentação. Os dados são mantidos em memória durante a execução; esta documentação não prevê gravação de produtos em arquivo local.

## 12. Referências do projeto

- **Repositório GitHub:** https://github.com/ZavaPaulo/Agile-methodologies-project
- **Site de referência visual:** https://www.mercadolivre.com.br/
- **Quadro Trello:** https://trello.com/invite/b/6ad0d940df775bc707fd4948/ATTI18bbbaebbae001a04f49d719de47a7208EF0F22F/agil-methodologies-projects

---

## Histórico de versões

| Versão | Data | Descrição |
|---|---|---|
| 1.0 | 16/10/2026 | Criação da documentação inicial de visão geral, escopo, regras de negócio, arquitetura proposta, testes e organização do projeto. |

**Responsáveis pela manutenção da documentação:** equipe do Agile Methodologies Projects.
