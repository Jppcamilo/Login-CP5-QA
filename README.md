# CheckPoint 5 – Automação de Testes de Login (saucedemo.com)

Suíte de testes funcionais automatizados para a tela de login do [saucedemo.com](https://www.saucedemo.com/), construída em Java com Selenium WebDriver e JUnit 5, seguindo o roteiro Dado/Quando/E/Então apresentado em aula.

## Tecnologias utilizadas

| Ferramenta | Versão | Papel |
|---|---|---|
| Java (JDK) | 17+ | Linguagem do projeto |
| Maven | — | Gerenciador de dependências e build |
| Selenium WebDriver | 4.27.0 | Automação do navegador (Selenium Manager baixa o ChromeDriver sozinho, sem configuração manual) |
| JUnit 5 (Jupiter) | 5.11.3 | Framework de testes (`@Test`, `@BeforeEach`, `@AfterEach`, asserções) |

## Estrutura do projeto

```
projeto login/
├── pom.xml
└── src/main/java/org/example/Login.java   # classe única com CT1 a CT5
```

Toda a suíte (`@BeforeEach`, `@AfterEach` e os 5 casos de teste) está em **um único arquivo**, `Login.java`, dentro do pacote `org.example`, em `src/main/java` — mesma organização usada no projeto que está no GitHub (`Login-CP5-QA`).

> **Nota técnica:** por padrão o Maven só executa automaticamente com `mvn test` classes que estão em `src/test/java`. Como aqui a classe está em `src/main/java`, os testes rodam normalmente pela IDE (clicando na seta verde ao lado da classe ou de cada método no IntelliJ), mas não são descobertos por um `mvn test` na linha de comando. Por isso o `junit-jupiter` no `pom.xml` está sem `<scope>test</scope>` — sem isso, o JUnit não ficaria disponível para compilar uma classe que mora em `main`.

## Como executar

Pelo IntelliJ: abra o projeto, deixe o Maven baixar as dependências e clique na seta verde ao lado da classe `Login` (roda os 5 testes) ou de um método específico (roda só aquele caso).

## O que cada teste faz

Os cenários vieram do plano de testes da Atividade 2 (mapa de status code → Dado/Quando/Então) e do glossário de status codes da Atividade 1. Foram automatizados os status marcados como **✓ aplicável ao login** que produzem um resultado visível na interface do saucedemo.com — já que os testes rodam contra o front-end público do site, sem acesso ao backend real.

### CT1 — Login com sucesso (200 / 302)
**Dado** que o teste abre o navegador e acessa `saucedemo.com`, confirmando pela URL e pelo título da página ("Swag Labs") que caiu no lugar certo.
**Quando** preenche o campo de usuário com `standard_user` e o de senha com `secret_sauce` (credenciais válidas).
**E** clica no botão "Login".
**Então** espera (via `WebDriverWait`) a URL mudar para `inventory.html` e confirma que o ícone do carrinho está visível na página.
Isso comprova o caminho feliz: um usuário com conta ativa e credenciais corretas consegue autenticar e é redirecionado — o comportamento que os status 200 (autenticado com sucesso) e 302 (redirecionamento) descrevem juntos no plano.

### CT2 — Campo usuário vazio (400)
**Dado** que está na tela de login.
**Quando** deixa o campo de usuário em branco e preenche só a senha.
**E** clica em "Login".
**Então** verifica que a URL não mudou (não saiu da tela de login) e que aparece a mensagem `"Epic sadface: Username is required"`.
Isso confirma que o sistema valida campos obrigatórios antes de tentar autenticar — o mesmo espírito do status 400 (requisição malformada/incompleta) do plano.

### CT3 — Campo senha vazio (400)
Mesma lógica do CT2, só que invertida: preenche o usuário (`standard_user`) e deixa a senha vazia. Verifica que continua na tela de login e que aparece `"Epic sadface: Password is required"`. Junto com o CT2, cobre os dois jeitos de disparar o cenário 400 descrito no plano ("campo de e-mail ou senha vazio").

### CT4 — Credenciais inválidas (401)
**Dado** que está na tela de login.
**Quando** informa o usuário válido `standard_user`, mas com uma senha incorreta.
**E** clica em "Login".
**Então** verifica que permanece na tela de login e que a mensagem é a genérica `"Epic sadface: Username and password do not match any user in this service"` — sem indicar se o erro foi no usuário ou na senha.
Esse último ponto é importante: é o mesmo motivo de segurança citado no plano para o 401 — a mensagem não deve revelar qual dos dois campos está errado, senão um atacante conseguiria descobrir quais usuários existem.

### CT5 — Conta bloqueada (403)
**Dado** que está na tela de login.
**Quando** informa as credenciais de uma conta de teste já bloqueada pelo próprio saucedemo (`locked_out_user` / `secret_sauce`).
**E** clica em "Login".
**Então** verifica que continua na tela de login e que aparece a mensagem específica `"Epic sadface: Sorry, this user has been locked out."`.
Isso representa o cenário do status 403: a conta existe e a senha está certa, mas o acesso é negado por causa do estado da conta (bloqueada/suspensa) — diferente do 401, que é sobre credenciais erradas.

## Cenários deixados de fora (e por quê)

### Não se aplicam ao login (já indicado no próprio plano de testes)
Os status **201 (Criado)**, **204 (Sem retorno)**, **404 (Não encontrado)** e **409 (Conflito/Duplicado)** foram marcados como **✗ não aplicável** na Atividade 2, pois pertencem a fluxos de cadastro, logout ou recursos inexistentes — não à autenticação em si. Por isso não geraram teste.

### Aplicáveis ao sistema, mas fora do alcance desta automação de UI
Os status **408 (Timeout Cliente)**, **429 (Muitos acessos)**, **499 (Cliente desconectou)**, **500 (Erro Interno)**, **502 (Erro de Gateway)**, **503 (Indisponível)** e **504 (Timeout Servidor)** são cenários legítimos do ponto de vista do sistema (o plano os marca como ✓ e todos caem no pilar **Confiabilidade**), mas dependem de comportamentos do **backend e da infraestrutura** (rede instável, servidor sobrecarregado, serviço fora do ar, limite de tentativas) que não podem ser provocados manipulando apenas a interface do saucedemo.com — um site público de demonstração, sem servidor real sob nosso controle, sem endpoint de autenticação exposto e sem mecanismo de rate limiting implementado.

Para automatizar esses casos seria necessário:
- **Testes de API/backend** (ex.: RestAssured, Postman/Newman) contra um serviço próprio ou mockado, onde é possível simular respostas 500/502/503/504 e limites de requisição (429);
- **Simulação de condições de rede** (ex.: Chrome DevTools Protocol para throttling ou queda de conexão) para reproduzir timeouts (408, 504) e desconexões (499);
- Esses tipos de teste ficam fora do escopo de uma suíte de UI com Selenium + JUnit, que testa o comportamento visível ao usuário, não o comportamento interno do servidor.
