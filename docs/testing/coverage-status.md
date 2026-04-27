# Coverage Status

Snapshot de cobertura do projeto em `2026-04-27`.

## Leitura importante

O build do projeto usa JaCoCo com gate minimo de `80%` no `verify`, mas com exclusoes de classes consideradas boilerplate:

- `*Application`
- `dto`
- `model`
- `client/dto`

Por isso, existem dois jeitos validos de olhar a cobertura:

1. **Gate de build**
   - e a metrica usada para aprovar ou reprovar `mvn verify`
2. **Relatorio bruto por modulo**
   - inclui classes de apoio que nao entram na regra principal de qualidade

## Gate configurado no build

O gate configurado no `pom.xml` raiz exige:

- `LINE COVEREDRATIO >= 0.80`

Esse e o criterio oficial usado no ciclo `verify`.

## Relatorio bruto por modulo

Cobertura consolidada a partir dos arquivos `target/site/jacoco/jacoco.csv` apos a rodada final de testes:

| Modulo | Cobertura de linhas |
| --- | ---: |
| `finflow-auth` | `90.00%` |
| `finflow-gateway` | `96.61%` |
| `finflow-income` | `84.92%` |
| `finflow-expense` | `84.42%` |
| `finflow-reports` | `92.28%` |

## Recomendacao de leitura profissional

Para acompanhar a qualidade do projeto, use:

- `mvn verify` como gate oficial
- o CSV bruto como diagnostico complementar

No estado atual, o projeto atende aos dois objetivos:

- gate oficial de build acima de 80%
- relatorio bruto por modulo acima de 80% nos modulos principais
