# Calculadora de Empréstimos

Desafio técnico TOTVS: tela web que calcula a evolução de um empréstimo seguindo as fórmulas
da planilha `Calculadora_Emprestimos.xlsx`.

| Camada    | Tecnologia                                   |
|-----------|----------------------------------------------|
| Back-end  | Java 21, Spring Boot 4, Bean Validation      |
| Front-end | React 19, TypeScript, Vite                   |
| Testes    | JUnit 5 / AssertJ / MockMvc, Vitest          |

## Como executar

Pré-requisitos: **JDK 21+** e **Node.js 20.19+**. O Maven é baixado automaticamente pelo wrapper.

```bash
# 1. API (http://localhost:8080)
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# 2. Tela (http://localhost:5173)
cd frontend
npm ci
npm run dev
```

O Vite encaminha `/api` para o back-end (proxy de mesma origem), então a API não precisa liberar CORS.
Para usar outro endereço da API: `API_URL=http://host:porta npm run dev`.

### Testes e build

```bash
cd backend && ./mvnw test
cd frontend && npm test && npm run lint && npm run build
```

## Funcionalidades

- Cinco campos obrigatórios: data inicial, data final, primeiro pagamento, valor do empréstimo e taxa de juros (% ao ano).
- O botão **Calcular** só é habilitado com todos os campos válidos.
- Validações (na tela e na API):
  - a data final deve ser maior que a data inicial;
  - a data de primeiro pagamento deve ser maior que a data inicial e menor que a data final.
- Grid com a data inicial, a data final, todos os fins de mês do período e as parcelas.

## Regras de cálculo

Base de dias fixa em **360** (célula F2) e quantidade de parcelas (G2) igual ao número de pagamentos.
Para cada linha, em relação à linha anterior (`ant`):

| Coluna         | Fórmula                                                                   |
|----------------|---------------------------------------------------------------------------|
| Amortização    | `valor / quantidade de parcelas` (somente em data de pagamento)           |
| Saldo          | `Saldo_ant − Amortização`                                                 |
| Provisão       | `((1 + taxa) ^ ((data − data_ant) / 360) − 1) × (Saldo_ant + Acumulado_ant)` |
| Pago           | `Acumulado_ant + Provisão` (somente em data de pagamento)                 |
| Acumulado      | `Acumulado_ant + Provisão − Pago`                                         |
| Total          | `Amortização + Pago`                                                      |
| Saldo Devedor  | `Saldo + Acumulado`                                                       |
| Consolidada    | `k/n` nas datas de pagamento                                              |

Datas de pagamento:

- Uma parcela por mês, a partir do primeiro pagamento, no mesmo dia. Em meses mais curtos, a parcela
  cai no último dia do mês (31/01/2024 → 29/02/2024).
- A data final sempre é uma parcela (a última).
- Como na planilha de referência, parcelas em sábado, domingo ou feriado nacional (incluindo Carnaval,
  Sexta-feira Santa e Corpus Christi) vão para o próximo dia útil (ex.: 15/06/2024 → 17/06/2024).
  A data inicial, os fins de mês e a data final não são ajustados.

Os testes do back-end comparam as 241 linhas do exemplo da planilha com o resultado da API.

## Segurança e desempenho

- Validação de entrada no servidor (Bean Validation), com limites de valor, taxa e período (até 100 anos).
- Erros em Problem Details (RFC 9457), sem stack trace ou mensagens internas.
- Cabeçalhos de segurança na API e CSP no `vite preview`; sem CORS aberto.
- Limites de tamanho de requisição e cabeçalho no Tomcat.
- Compressão das respostas JSON e virtual threads.
- Cálculo em uma única passada (O(n)), feriados calculados só para os anos do período.
- Na tela: formatação das linhas memorizada, cancelamento da requisição anterior ao recalcular.

## Estrutura

```
backend/
  calculo/   motor de cálculo, calendário de dias úteis e parâmetros
  api/       controller REST, DTO com validações, tratamento de erros, cabeçalhos
frontend/src/
  components/  Campo, FormularioEmprestimo, TabelaResultados
  services/    cliente da API
  utils/       validação e formatação
```
