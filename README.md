# ShadowTale

**Cinematic Graphics Overhaul for Hytale — Asset Pack**

[![Target](https://img.shields.io/badge/Hytale-0.6.8-blue)](https://docs.hytale.com/assets/)
[![Type](https://img.shields.io/badge/Type-Asset%20Pack-purple)](#arquitetura)
[![Status](https://img.shields.io/badge/Status-Foundation%20%2B%20Asset%20Audit%20implemented-orange)](#status-atual)

ShadowTale é um **overhaul gráfico cinematográfico para Hytale**, desenvolvido exclusivamente como **Asset Pack**.

O objetivo é levar a apresentação visual do mundo para uma qualidade mais cinematográfica e realista — com iluminação, atmosfera, clima, água, partículas, superfícies e áudio ambiental de suporte mais expressivos — sem transformar Hytale em outro jogo.

> **ShadowTale = Hytale com apresentação visual de remaster cinematográfico.**

---

## Visão geral

ShadowTale foi projetado para:

- melhorar drasticamente a iluminação e a atmosfera;
- aumentar a profundidade visual entre luz e sombra;
- tornar clima e chuva mais expressivos;
- melhorar a apresentação de água e fluidos;
- aumentar a presença de partículas ambientais;
- tornar superfícies e interações de blocos visualmente mais reativas;
- manter uma identidade artística coerente com Hytale;
- permitir tratamentos diferentes para cada ambiente sem perder uma identidade global;
- priorizar **qualidade visual máxima**.

O projeto utiliza os mecanismos reais de Asset Packs do Hytale. Não haverá um runtime próprio executando lógica para produzir os efeitos.

---

# Arquitetura

ShadowTale será distribuído como **um único Asset Pack**, internamente separado por módulos visuais.

A estratégia principal é:

1. utilizar herança nativa `Parent` sempre que o tipo de asset suportar;
2. alterar somente as propriedades necessárias;
3. criar assets novos apenas quando herança/override não for suficiente;
4. nunca modificar o `Assets.zip` original do Hytale;
5. nunca inventar propriedades ou comportamentos de carregamento;
6. manter ferramentas de validação e desenvolvimento fora do pacote destinado ao jogador.

### Estrutura conceitual

```text
ShadowTale/
├── manifest.json
├── Server/
│   ├── Environments/
│   ├── Weathers/
│   ├── Particles/
│   ├── Item/
│   │   ├── Block/
│   │   │   ├── Blocks/
│   │   │   ├── Fluids/
│   │   │   ├── FluidFX/
│   │   │   ├── Particles/
│   │   │   └── Sounds/
│   │   └── ConnectedBlockRuleSets/
│   └── ...
├── README.md
└── docs/
    └── specs/

# Somente projeto/desenvolvimento — não entra no Asset Pack final
Validation/
```

Essa estrutura é **conceitual**. Os diretórios e tipos efetivamente utilizados serão limitados ao que o Hytale 0.6.8 realmente suporta.

---

# Estratégia de assets

## Herança e overrides

ShadowTale prefere:

```text
Asset original
      ↓
Parent
      ↓
ShadowTale
      ↓
somente as alterações necessárias
```

Isso reduz duplicação e facilita manutenção.

Por exemplo, quando um Environment existente puder ser reutilizado, o ShadowTale não deverá copiar o asset inteiro apenas para modificar um pequeno conjunto de propriedades.

## Assets novos

Assets novos serão utilizados quando:

- o asset original não puder fornecer o resultado desejado;
- a herança não for suficiente;
- for necessário um novo sistema de partículas;
- for necessária uma nova variante visual suportada pelo Hytale;
- ou a documentação oficial permitir uma extensão apropriada.

A existência de um asset novo nunca será justificativa para inventar campos não documentados.

---

# Direção artística

## Cinemático / realista

A direção aprovada é **cinemática/realista**, funcionando como uma espécie de remaster gráfico.

ShadowTale não pretende substituir a identidade visual de Hytale por uma estética completamente diferente.

O resultado deve continuar sendo reconhecivelmente Hytale, porém com:

- iluminação mais cinematográfica;
- atmosfera mais profunda;
- separação mais pronunciada entre luz e sombra quando suportada;
- clima mais expressivo;
- chuva mais convincente;
- partículas atmosféricas mais presentes;
- água e fluidos mais refinados;
- superfícies com maior sensação de materialidade;
- efeitos ambientais mais perceptíveis;
- áudio ambiental de suporte quando o sistema de assets permitir.

---

# Qualidade visual

A política aprovada é:

## **Qualidade máxima**

O desempenho não é o objetivo principal da fase de design.

Quando os sistemas reais do Hytale permitirem, ShadowTale poderá utilizar:

- maior densidade de partículas;
- efeitos atmosféricos mais intensos;
- maior variedade de efeitos;
- chuva mais volumosa;
- efeitos ambientais mais presentes;
- configurações visuais mais ambiciosas.

Entretanto:

> **Qualidade máxima não significa comportamento inventado.**

Não serão utilizados:

- propriedades inexistentes;
- controles de renderer inventados;
- JSONs com campos não suportados;
- mecanismos de carregamento não documentados;
- código externo para simular recursos que não pertencem ao sistema de Asset Packs.

Se determinado efeito exigir código ou plugin para funcionar, ele permanece fora do ShadowTale até existir uma solução nativa/documentada adequada.

---

# Versão-alvo

## Hytale Release 0.6.8

ShadowTale terá uma **versão-alvo fixa**:

**Hytale Release 0.6.8**

O projeto não tentará manter compatibilidade ampla com versões diferentes dentro do mesmo release.

Uma atualização futura do Hytale será tratada como uma nova revisão de compatibilidade.

```text
ShadowTale X.Y.Z
Target: Hytale 0.6.8
```

Não será assumido que uma nova versão do Hytale é compatível apenas porque o Asset Pack ainda carrega.

---

# Ambientes

A abordagem aprovada é:

## Global + específico por ambiente

ShadowTale terá uma identidade cinematográfica global, mas cada ambiente poderá possuir tratamento próprio.

Exemplos de características que podem variar por ambiente:

- iluminação;
- atmosfera;
- clima;
- partículas;
- água/fluidos;
- intensidade visual;
- sensação ambiental.

Conceitualmente:

```text
ShadowTale
│
├── Identidade Cinemática Global
│
├── Ambiente A
│   ├── Lighting / Atmosphere
│   ├── Weather
│   ├── Particles
│   └── Water
│
├── Ambiente B
│   ├── Lighting / Atmosphere
│   ├── Weather
│   ├── Particles
│   └── Water
│
└── Ambiente C
    ├── Lighting / Atmosphere
    ├── Weather
    ├── Particles
    └── Water
```

A intenção é evitar dois extremos:

- um mundo inteiro visualmente uniforme;
- uma coleção de ambientes que parecem pertencer a pacotes diferentes.

---

# Prioridade de desenvolvimento

O desenvolvimento visual seguirá esta ordem:

### 1. Weather / iluminação / atmosfera
A base da transformação cinematográfica em 0.6.8, porque é o codec que documenta fog, céu, sol, lua, curvas de cor e nuvens.

### 2. Environment / contexto ambiental
WaterTint, FluidParticles, WeatherForecasts e demais propriedades efetivamente expostas pelo codec.

### 3. Water / fluids
Apresentação, efeitos, partículas e interação visual suportada.

### 4. Partículas
Partículas ambientais e de interação.

### 5. Blocks / superfícies
Efeitos de superfície, interações, partículas de blocos e Connected Block Rule Sets.

### 6. Áudio ambiental de suporte
Sons associados aos sistemas visuais quando o sistema de assets permitir.

Cada alteração deverá contribuir diretamente para a apresentação cinematográfica do mundo.

---

# Módulos planejados

## Environments

Responsáveis pela base ambiental:

- iluminação;
- atmosfera;
- características ambientais;
- configuração de água quando suportada;
- integração com clima;
- variações por ambiente.

## Weathers

Responsáveis pela apresentação climática:

- chuva;
- tempestades;
- partículas de clima;
- intensidade;
- variações atmosféricas.

## Particle Systems

Responsáveis por:

- partículas ambientais;
- partículas atmosféricas;
- chuva;
- poeira;
- folhas;
- pequenos detritos;
- efeitos de interação;
- variantes reutilizáveis por herança.

## Fluids / Water

Responsáveis por:

- aparência de fluidos;
- partículas;
- efeitos de superfície suportados;
- interação visual;
- variações ambientais.

## Blocks

Responsáveis por:

- apresentação de blocos;
- partículas de interação;
- efeitos de superfície;
- variantes visuais suportadas.

## Connected Block Rule Sets

Responsáveis por sistemas de conexão visual quando aplicáveis.

## Materials / Textures

Serão utilizados somente quando necessários e quando os mecanismos oficiais suportarem a alteração.

## Sounds

Sons de assets poderão ser utilizados como **suporte à apresentação cinematográfica**, sem transformar ShadowTale em um overhaul sonoro independente.

---

# Escopo

## Incluído

ShadowTale pode trabalhar com:

- Environments;
- Weathers;
- Particle Systems;
- Fluids / água;
- Blocks;
- Connected Block Rule Sets;
- materiais e texturas quando necessários;
- sons associados aos assets quando necessários;
- herança `Parent`;
- referências entre assets;
- validação;
- empacotamento;
- ferramentas de desenvolvimento do projeto.

## Fora do escopo

ShadowTale não implementará:

- Java plugins;
- server plugins;
- gameplay;
- lógica de gameplay;
- câmera;
- controles/input;
- inventário;
- combate;
- IA;
- quests;
- progressão;
- UI/HUD;
- menus;
- comandos;
- sistemas administrativos;
- runtime próprio;
- código para implementar comportamento gráfico;
- modificação direta do `Assets.zip` original.

### Regra fundamental

Se uma funcionalidade exigir lógica de plugin/runtime em vez de recursos suportados pelo Asset Pack, ela não será artificialmente simulada.

---

# Validação

ShadowTale terá uma pipeline de validação em camadas.

```text
JSON / Syntax
      ↓
Structure
      ↓
Asset / Type Recognition
      ↓
Supported Properties
      ↓
Reference Resolution
      ↓
Inheritance Graph
      ↓
Cross-Module Validation
      ↓
Packaging Validation
      ↓
Clean Installation
      ↓
Hytale 0.6.8
      ↓
Release Report
```

## Verificações

O sistema deverá verificar, quando aplicável:

- `manifest.json`;
- estrutura de diretórios;
- sintaxe JSON;
- localização dos assets;
- referências;
- referências `Parent`;
- ciclos de herança;
- referências órfãs;
- identificadores duplicados ou conflitantes;
- propriedades não suportadas;
- consistência entre módulos;
- integridade do ZIP;
- conteúdo indevido no pacote final;
- possíveis conflitos com outros Asset Packs.

## Resultado

A validação deverá distinguir:

```text
PASS
WARN
FAIL
```

Um **FAIL crítico bloqueia o release**.

Não serão publicados números de testes, contagens de aprovação ou afirmações de compatibilidade sem uma execução real que produza essas evidências.

---

# Teste de instalação limpa

Antes de qualquer release:

1. gerar o Asset Pack destinado ao jogador;
2. remover arquivos de desenvolvimento;
3. instalar em um ambiente limpo;
4. validar `manifest.json`;
5. validar a estrutura;
6. carregar no Hytale 0.6.8;
7. verificar os módulos implementados;
8. registrar erros e avisos relevantes;
9. produzir o relatório de validação.

Um pacote não será considerado pronto apenas porque os JSONs são sintaticamente válidos.

---

# Compatibilidade com outros Asset Packs

A política aprovada é:

## Compatibilidade prioritária + detecção explícita de conflitos

ShadowTale deverá coexistir com outros Asset Packs sempre que isso for tecnicamente possível sem comprometer a integridade dos assets.

Princípios:

- não modificar diretamente os assets originais do Hytale;
- evitar substituições completas desnecessárias;
- preferir herança nativa;
- utilizar identificadores e referências de maneira deliberada;
- detectar conflitos quando as informações disponíveis permitirem;
- não assumir ordem de carregamento não documentada;
- não declarar compatibilidade total quando houver sobreposição inevitável.

Quando um conflito não puder ser evitado, ele deverá ser **reportado explicitamente**.

---

# Distribuição

ShadowTale será distribuído como um Asset Pack independente.

O arquivo original de assets do Hytale não será substituído nem modificado.

O formato planejado para releases é:

```text
ShadowTale-X.Y.Z-Hytale-0.6.8.zip
```

O pacote destinado ao jogador deverá conter somente os arquivos necessários para carregar o Asset Pack.

Não deverão entrar no ZIP final:

- ferramentas de desenvolvimento;
- arquivos temporários;
- artefatos de testes internos;
- documentação exclusiva do repositório;
- arquivos de build desnecessários.

> A fundação do Asset Pack já existe e possui validação automatizada. O release visual ainda não existe.

---

# Versionamento

ShadowTale utiliza versionamento:

```text
MAJOR.MINOR.PATCH
```

### MAJOR

Mudanças arquiteturais ou de conteúdo incompatíveis.

### MINOR

Novos sistemas ou recursos visuais compatíveis.

### PATCH

Correções, refinamentos e ajustes.

A versão do ShadowTale é independente da versão do Hytale.

Exemplo:

```text
ShadowTale 1.2.3
Hytale target: 0.6.8
```

---

# Release Gate

Nenhuma versão será considerada release-ready sem passar pelo fluxo:

```text
Alteração de asset
      ↓
Validação
      ↓
Testes estruturais
      ↓
Testes de integração
      ↓
Build do pacote
      ↓
Validação do ZIP
      ↓
Instalação limpa
      ↓
Teste no Hytale 0.6.8
      ↓
Relatório
      ↓
Release
```

Um arquivo existente no repositório não significa que uma versão está pronta para distribuição.

---

# Princípios técnicos

ShadowTale seguirá estes princípios durante todo o desenvolvimento:

### 1. Native first

Sempre preferir mecanismos nativos do Hytale.

### 2. Minimal override

Alterar somente o necessário.

### 3. No invented APIs

Não inventar propriedades, codecs, loaders ou comportamentos.

### 4. No original asset modification

O `Assets.zip` original permanece intocado.

### 5. Evidence-based validation

Nenhuma afirmação de sucesso sem validação real.

### 6. Fail closed

Erros críticos impedem releases.

### 7. Visual coherence

Cada módulo deve contribuir para a mesma identidade cinematográfica.

### 8. Asset Pack only

ShadowTale não deve evoluir para um plugin ou mod de gameplay.

---

# Documentação oficial utilizada

A implementação deverá usar a documentação oficial do Hytale como referência para os assets e propriedades efetivamente suportados.

- [Hytale Asset Reference](https://docs.hytale.com/assets/)
- [Asset Packs](https://pre-release.docs.hytale.com/creating-content/asset-packs/)
- [Environments](https://docs.hytale.com/assets/environments/)
- [Weathers](https://docs.hytale.com/assets/weathers/)
- [Particle Systems](https://docs.hytale.com/assets/particles_particlesystem/)
- [Fluids](https://docs.hytale.com/assets/item/block/fluids/)
- [Block Particles](https://docs.hytale.com/assets/item/block/particles/)
- [Connected Block Rule Sets](https://pre-release.docs.hytale.com/assets/item/connectedblockrulesets/)
- [Blocks](https://docs.hytale.com/assets/item/block/blocks/)
- [Block Sounds](https://docs.hytale.com/assets/item/block/sounds/)

Essas referências definem o que é documentado. Elas não autorizam propriedades ou comportamentos que não estejam efetivamente suportados.

---

# Estrutura do projeto

O repositório separa o conteúdo destinado ao jogador das ferramentas de desenvolvimento.

```text
ShadowTale/
├── pack/                         # conteúdo do Asset Pack
│   ├── manifest.json
│   └── Server/
│       ├── Environments/
│       ├── Weathers/
│       ├── Particles/
│       └── Item/
│           ├── Block/
│           │   ├── Fluids/
│           │   └── Particles/
│           └── ConnectedBlockRuleSets/
├── src/                          # ferramentas de validação/empacotamento
├── tests/                        # validação automatizada
├── scripts/                      # comandos de desenvolvimento
└── docs/                         # documentação e evidências
```

Somente o conteúdo de `pack/` é elegível para entrar no ZIP destinado ao jogador. Ferramentas, testes e documentação ficam fora do Asset Pack final.
# Status atual

## Design

**APROVADO**

Todas as decisões principais de arquitetura e escopo foram definidas.

## Implementação

**FUNDAÇÃO IMPLEMENTADA — AUDITORIA REAL PREPARADA — MÓDULOS VISUAIS PENDENTES**

A fundação real do Asset Pack e a infraestrutura de auditoria já estão no repositório:

- `pack/manifest.json` documentado para Hytale Release 0.6.8;
- pipeline de validação com resultados `PASS`, `WARN` e `FAIL`;
- auditoria do `Assets.zip` real para descobrir IDs, Parent, campos e candidatos de override;
- validação de JSON e caminhos de assets suportados;
- detecção de referências `Parent` locais, referências externas/base e ciclos de herança;
- empacotamento determinístico do conteúdo de `pack/`;
- exclusão de ferramentas e arquivos de desenvolvimento do ZIP;
- suíte automatizada atual com 15 testes passando na validação da fundação.

Os módulos visuais ainda pendentes são os overrides reais de Weather/Environment, água/fluidos, Particle Systems, Blocks/superfícies e integração em Hytale 0.6.8.

## Release

**NENHUM RELEASE DISPONÍVEL**

Ainda não existe um ZIP oficial do ShadowTale.

## Próxima etapa

A fundação técnica já foi criada. A próxima fase é começar o conteúdo visual real, nesta ordem:

1. Environment / iluminação / atmosfera;
2. Weather;
3. água/fluidos;
4. Particle Systems;
5. Blocks / superfícies;
6. integração entre módulos;
7. validação de referências e propriedades;
8. instalação limpa;
9. teste visual no Hytale 0.6.8;
10. primeira release visual.

---

# Filosofia do projeto

ShadowTale não pretende adicionar gameplay.

Ele pretende fazer uma única coisa muito bem:

> **fazer o mundo de Hytale parecer uma versão cinematográfica e visualmente remasterizada de si mesmo.**

Qualidade visual máxima, arquitetura nativa, validação rigorosa e nenhuma dependência de código de gameplay são os pilares do projeto.

---

**ShadowTale — Hytale Cinematic Graphics Asset Pack**  
**Target: Hytale Release 0.6.8**


## Nota técnica de 0.6.8

A implementação visual não vai assumir que `Environment` é o controlador de iluminação. Na referência oficial Release 0.6.8, a superfície de `Environment` é pequena, enquanto `Weather` documenta os controles ricos de céu, luz, fog, água e nuvens. O repositório registra essa distinção em `docs/specs/0.6.8-visual-codec-contract.md`.
