# Repositório de Trabalhos Práticos: Base de Dados de Jogos da Steam
Este repositório contém implementações de técnicas de manipulação e processamento de dados a uma base de jogos da Steam aplicando os conceitos aprendidos na matéria de Algoritmos e Estruturas de Dados III da universidade PUC Minas pelo professor Hayala Curto.

## Estrutura do Arquivo de Dados
O arquivo binário gerado segue esta estrutura:
[Último ID Inserido (4 bytes)] + [Registro 1] + [Registro 2] + ... + [Registro N]
```
Cada registro possui a seguinte organização:
| Componente          | Tamanho    | Descrição                          |
|---------------------|------------|------------------------------------|
| Último ID Inserido  | 4 bytes    | Identificador único                |
| Flag de Lápide      | 1 byte     | `00` = ativo, `FF` = inativo       |
| Tamanho do Registro | 4 bytes    | Tamanho total em bytes             |
| ID do Registro      | 4 bytes    | Identificador único                |
| Campos SteamGame    | Variável   | Dados do jogo                      |
| Flag de Lápide      | 1 byte     | `00` = ativo, `FF` = inativo       |
| Tamanho do Registro | 4 bytes    | Tamanho total em bytes             |
| ID do Registro      | 4 bytes    | Identificador único                |
| Campos SteamGame    | Variável   | Dados do jogo                      |
... (repetição desse padrão)
```
**Estrutura da Classe SteamGame:**
```java
public class SteamGame {
    private int id;                  // 4 bytes
    private int appid;               // 4 bytes
    private String name;             // Variável (precedido por 4 bytes de comprimento)
    private Long release_date;       // 8 bytes
    private Boolean english;         // 1 byte
    private String developer;        // Variável
    private String publisher;        // Variável
    private String platforms;        // 7 bytes (fixo)
    private int required_age;        // 4 bytes
    private List<String> categories; // Variável
    private List<String> genres;     // Variável
    private List<String> steamspy_tags; // Variável
    private int achievements;        // 4 bytes
    private int positive_ratings;    // 4 bytes
    private int negative_ratings;    // 4 bytes
    private int average_playtime;    // 4 bytes
    private int median_playtime;     // 4 bytes
    private String owners;           // Variável
    private float price;             // 4 bytes
}
```
**Características da Base:**
- Total de registros: 27,075
- Campos variáveis usam prefixo de 4 bytes para armazenar tamanho
- Listas são armazenadas como: 
  - [Nº elementos (4 bytes)] 
  - Para cada elemento: [Tamanho do elemento (4 bytes)] + [Dados do elemento (bytes)]
 

## Trabalhos Práticos
O projeto está organizado em quatro etapas (TPs) principais.
### TP1: Base de Dados, Manipulação Sequencial e Ordenação Externa
**Objetivo:** Implementação de operações básicas em arquivo sequencial
**Funcionalidades:**
- Criação da base de dados a partir do dataset original
- Inserção de novos registros com geração automática de ID
- Pesquisa linear sequencial por diferentes campos (ID, appID e Nome)
- Atualização de registros existentes
- Exclusão lógica com flag de lápide (`FF`)
- Ordenação externa por substituição usando Heap

### TP2: Arquivo Indexado e Estruturas de Indexação
**Objetivo:** Implementação de técnicas de indexação para acesso eficiente
**Técnicas Implementadas:**
- **Árvore B+**
  - Indexação por ID
  - Busca eficiente e range queries
  - Atualização dinâmica dos índices ao inserir/atualizar/remover
- **Tabela Hash**
  - Hashing extensível para ID
  - Buckets dinâmicos com redistribuição
  - Tratamento de colisões
  - Atualização dinâmica dos índices ao inserir/atualizar/remover
- **Lista Invertida**
  - Indexação de campos textuais (categorias de jogos)
  - Recuperação eficiente de registros por termos
  - Atualização dinâmica dos índices ao inserir/atualizar/remover para cada categoria

### TP3 : Compactação, Busca em Texto e Criptografia
**Objetivo:** Implementação de técnicas avançadas de processamento
#### Parte 1: Compactação e Casamento de Padrões
- **Compactação Huffman**
  - Codificação/decodificação completa de todo o arquivo
  - Cálculo de taxa de compressão
- **Compactação LZW (Lempel-Ziv-Welch)**
  - Implementação para compressão completa de todo o arquivo
  - Cálculo de taxa de compressão
- **Algoritmos de Casamento de Padrões**
  - Knuth-Morris-Pratt (KMP)
  - Boyer-Moore para busca eficiente
  - Cálculo da quantidade de saltos e aproveitamento de texto

#### TP4 (Parte 2 do TP3): Criptografia de Dados 
- **Criptografia Simétrica**
  - Offset da representação binária
- **Criptografia Assimétrica (RSA)**
  - Geração de chaves pública/privada de até 1024 bits
  - Criptografia/descriptografia completa de todo o arquivo
## Base de Dados
- **Fonte:** [Kaggle Steam Dataset](https://www.kaggle.com/datasets/nikdavis/steam-store-games/data)
- **Registros:** 27,075 jogos
- **Tamanho:** 5.82 MB
  
## Como Executar
1. Clonar o repositório:
```bash
git clone https://github.com/MatheusSCxr/TPsAeds3.git
```
2. Compilar e Executar a classe "DataBase.java" na pasta "main" de cada TP.

## Dependências
- Java JDK 11+
  
## Créditos
- Parte dos códigos de **Indexação e Compactação** foram adaptados do repositório do [Professor Marcos Kutova](https://github.com/kutova/AEDsIII)
- Parte dos códigos de **Casamento de Padrões** foram adaptados do [Site GeekForGeeks](https://www.geeksforgeeks.org/) 
