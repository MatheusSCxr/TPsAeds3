package main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;
import models.ArvoreElemento;
import models.HashElemento;
import models.ListaElemento;
import services.*;

public class DataBase {
    public static int totalGames; //variável de controle do número de registros ativos no banco de dados
    public static int totalDeleted; //variável de controle do número de registros inativos no banco de dados
    public static boolean hasData; //variável que indica se existe ou não um banco de dados
    public static short indexStatus; //tipo de indexação atual 0 = nenhuma, 1 arvoreB+, 2 hash, 3 lista invertida.
    public static Index_ArvoreBMais<ArvoreElemento> arvore; //árvore para indexação
    public static Index_HashExtensivel<HashElemento> hash; //hash para indexação
    public static Index_ListaInvertida lista;

    public static void clearIndex(){
        System.out.println("[Index] -> Removendo índices antigos...");
        try {
            //fechar streams atualmente abertas
            switch (indexStatus) {
                case 1 -> {
                    if (arvore != null){
                        File file = new File("./src/resources/db_Index/arvoreBMais.db");
                        if (file.exists()) {
                            arvore.arquivo.close();
                            file.delete();
                        }
                        System.out.println("[Index] -> Indexação por Árvore B+ removida com sucesso");
                    }
                }
                case 2 -> {
                    if (hash != null){
                        File file = new File("./src/resources/db_Index/hash_diretorio.db");
                        if (file.exists()) {
                            hash.arqDiretorio.close();
                            file.delete();
                        }
                        File file2 = new File("./src/resources/db_Index/hash_cestos.db");
                        if (file2.exists()) {
                            hash.arqCestos.close();
                            file2.delete();
                        }
                        System.out.println("[Index] -> Indexação por Hash Extensível removida com sucesso");
                    }
                }
                case 3 -> {
                    if (lista != null){
                        File file = new File("./src/resources/db_Index/lista_dicionario.db");
                        if (file.exists()) {
                            lista.arqDicionario.close();
                            file.delete();
                        }
                        File file2 = new File("./src/resources/db_Index/lista_blocos.db");
                        if (file2.exists()) {
                            lista.arqBlocos.close();
                            file2.delete();
                        }
                        System.out.println("[Index] -> Indexação por Lista Invertida removida com sucesso");
                    }
                }
                default -> System.out.println("[INFO] -> Não foram encontrados índices antigos");
            }
        } catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível remover os índices anteriores");
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("[INFO] -> Procurando base de dados...");

        //identificar base de dados
        File dbFile = new File("./src/resources/db_Output/gamesDB.db");
        if (dbFile.exists()){
            //abrir arquivo no modo de leitura
            try (RandomAccessFile arquivo = new RandomAccessFile("./src/resources/db_Output/gamesDB.db","r")) {
                System.out.println("[INFO] -> Foi encontrada uma base de dados");
                
                //contar a quantidade de jogos ativos e inativos (deletados) na base de dados
                DB_Services.countGames(arquivo);

                //identificar a indexação atual
                System.out.println("[INFO] -> Procurando arquivos de indexação...");

                File indexMetadados = new File("./src/resources/db_Index/indexMetadata.db");
                if (indexMetadados.exists()){
                    try (RandomAccessFile metadados = new RandomAccessFile("./src/resources/db_Index/indexMetadata.db", "r")){
                        indexStatus = metadados.readByte();
                        int config =  metadados.readInt();

                        //verificar compatibilidade com a quantidade de elementos
                        try {
                            //ler e inicializar indexação
                            File indexArvore = new File("./src/resources/db_Index/arvoreBMais.db");
                            File indexHash = new File("./src/resources/db_Index/hash_diretorio.db");
                            File indexLista = new File("./src/resources/db_Index/lista_dicionario.db");
            
                            if (indexArvore.exists()) {

                                //inicializar arvore
                                arvore = new Index_ArvoreBMais<>(ArvoreElemento.class.getConstructor(), config, "./src/resources/db_Index/arvoreBMais.db");
                                System.out.println("[INFO] -> Indexação por Árvore B+ detectada " + "[Ordem: " + config +"]");
                            } else if (indexHash.exists()) {

                                //inicializar hash
                                hash = new Index_HashExtensivel<>(HashElemento.class.getConstructor(), config, "./src/resources/db_Index/hash_diretorio.db","./src/resources/db_Index/hash_cestos.db");  
                                System.out.println("[INFO] -> Indexação por Hash Extensível detectada " + "[Tamanho do Cesto: " + config +"]");
                            } else if (indexLista.exists()) {
                                //inicializar lista invertida
                                lista = new Index_ListaInvertida(config, "./src/resources/db_Index/lista_dicionario.db","./src/resources/db_Index/lista_blocos.db");  
                                System.out.println("[INFO] -> Indexação por Lista Invertida detectada " + "[Tamanho do Bloco: " + config +"]");
                            } else{
                                indexStatus = 0;
                                System.out.println("[INFO] -> Nenhum arquivo de indexação foi encontrado");
                            }   
                        } catch (Exception e) {
                            System.out.println("[ERRO] -> Não foi possível recuperar o arquivo de indexação");
                            System.out.println(e);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERRO] -> Não foi possível ler o arquivo de metadados da indexação");
                    }
                } else {
                    System.out.println("[INFO] -> Nenhum arquivo de metadados de indexação foi encontrado");
                    indexStatus = 0;
                }
            } catch (IOException e) {
                System.out.println("[ERRO] -> Não foi possível abrir o arquivo da base de dados");
                System.out.println(e);
            }

            hasData = true;
        } else{
            System.out.println("[INFO] -> Não foi encontrada uma base de dados em (./src/resources/db_Output/gamesDB.db)");
            indexStatus = 0;
            hasData = false;
        }

        //exibir interface de menu com opções
        UI.menu(indexStatus,totalGames,totalDeleted);
        try (Scanner leitor = new Scanner(System.in)) {
            int choice = -1;

            while (choice != 0) {
                //bloco try-catch para evitar que o programa encerre caso o scanner receba um valor inválido
                try {
                    // 0 = encerrar programa
                    System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                    choice = leitor.nextInt();
                    switch (choice) {
                        case 1 -> {
                            DB_Load.csvExtractAll();
                        }
                        case 2 -> {
                            System.out.print("\n[CsvExtract] -> Digite o número de registros que deseja extrair do CSV: ");
                            int num = leitor.nextInt();
                            DB_Load.csvExtractNum(num);
                        }
                        case 3 -> {
                            if (hasData){
                                int tipo = 1;
                                switch (indexStatus) {
                                    case 0 -> {
                                        while(tipo != 0){
                                            UI.search();
                                            tipo = leitor.nextInt();
                                            if (tipo != 0 && tipo < 4){
                                                System.out.print("\n[Search] -> Digite o valor do atributo que deseja procurar: ");
                                                leitor.nextLine(); //descartar caractere \n
                                                String valor = leitor.nextLine();
                                                DB_CRUD.searchGame(valor, tipo);
                                                System.out.println("[Search] -> Pesquisa finalizada. Deseja realizar outra pesquisa?");
                                                System.out.println(" \n               [1] - SIM                   [0] - NÃO");
                                                System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                                                tipo = leitor.nextInt();
                                            }
                                            //casamento de padrão
                                            else if (tipo == 4 || tipo == 5){
                                                System.out.print("\n[Search] -> Digite o padrão que deseja procurar: ");
                                                leitor.nextLine(); //descartar caractere \n
                                                String valor = leitor.nextLine();
                                                DB_CRUD.searchGame(valor, tipo);
                                            }
                                            else{
                                                System.out.println("[Search] -> Opção inválida.");
                                            }
                                        }
                                    }
                                    case 1 -> {
                                        try {
                                            System.out.print("\n[Search] -> Digite o ID do registro que deseja procurar na Árvore B+: ");
                                            int id = leitor.nextInt();
                                            // Ao passar o segundo valor como -1, ele funciona como um coringa
                                            // de acordo com a implementação do método compareTo na classe
                                            // ArvoreElemento
                                            ArrayList<ArvoreElemento> listaArvore = arvore.read(new ArvoreElemento(id, -1));
                                            if (!listaArvore.isEmpty() ){
                                                System.out.println("[Index] -> Registro encontrado com sucesso na Árvore B+: ");
                                                for (int i = 0; i < listaArvore.size(); i++)
                                                    DB_CRUD.readGame_Address(listaArvore.get(i).getAddress()).printAll();
                                            }
                                            else{
                                                System.out.println("Não foi possível encontrar o registro na Árvore.");
                                            }
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }
                                    }
                                    case 2 -> {
                                        try {
                                            System.out.print("\n[Search] -> Digite o ID do registro que deseja procurar na Hash: ");
                                            int id = leitor.nextInt();
                                            HashElemento registro = hash.read(HashElemento.hash(id));
                                            if (registro != null ){
                                                System.out.println("[Index] -> Registro encontrado com sucesso na Hash Extensível: ");
                                                DB_CRUD.readGame_Address(registro.getAddress()).printAll();
                                            }
                                            else{
                                                System.out.println("Não foi possível encontrar o registro na Hash.");
                                            }
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }
                                    }
                                    case 3 -> {
                                        try {
                                            System.out.print("\n[Search] -> Digite o ID do registro que deseja procurar na Lista Invertida: ");
                                            int id = leitor.nextInt();
                                            leitor.nextLine(); // limpar buffer
                                            System.out.print("\n[Search] -> Digite a Categoria do registro que deseja procurar na Lista Invertida: ");
                                            String categoria = leitor.nextLine();
                                            
                                            //pegar todos os elementos da categoria
                                            ListaElemento[] resultado = lista.read(categoria);

                                            //procura o ID exato dentro dessa lista
                                            ListaElemento registro = null;
                                            for (ListaElemento e : resultado) {
                                                if (e.getId() == id) {
                                                    registro = e;
                                                    break;
                                                }
                                            }

                                            if (registro != null) {
                                                System.out.println("\n[Search] -> Registro encontrado na Lista Invertida:");
                                                // A partir daqui, você já tem o endereco em registro.getEndereco()
                                                // e pode chamar seu CRUD para carregar e imprimir o SteamGame completo:
                                                DB_CRUD.readGame_Address(registro.getAddress()).printAll();
                                            }
                                            else {
                                                System.out.println("\n[Search] -> Não foi possível encontrar o registro ID: " + id + " na categoria: '" + categoria + "'");
                                            }
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }
                                    }
                                    default -> {
                                    }
                                }
                            } else{
                                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados");
                            }
                        }
                        case 4 -> {
                            if (hasData){
                                int tipo = 1;
                                while(tipo != 0){
                                    UI.create();
                                    tipo = leitor.nextInt();
                                    if (tipo != 0){
                                        if (DB_CRUD.createGame(tipo)){
                                            System.out.println("[Create] -> Registro criado e gravado com sucesso");
                                        }
                                        else{
                                            System.out.println("[Create] -> Não foi possível criar e gravar o registro");
                                        }
                                    }
                                }
                            } else{
                                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados");
                            }
                        }
                        case 5 -> {
                            if (hasData){
                                System.out.print("[Delete] -> Insira o ID do jogo que deseja remover: ");
                                int deleteID = leitor.nextInt();

                                String categoria = null;

                                //se for lista invertida, obter categoria
                                if (indexStatus == 3){
                                    leitor.nextLine(); //limpar buffer

                                    System.out.print("[Delete] -> Insira a categoria do jogo que deseja excluir: ");
                                    categoria = leitor.nextLine();
                                }
                                if (DB_CRUD.deleteGame(deleteID,categoria)){
                                    System.out.println("[Delete] -> Registro excluído com sucesso");
                                }
                            } else{
                                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados");
                            }
                        }
                        case 6 -> {
                            if (hasData){
                                System.out.print("[Update] -> Insira o ID do jogo que deseja atualizar: ");
                                int updateID = leitor.nextInt();
                                String categoria = null;

                                //se for lista invertida, obter categoria
                                if (indexStatus == 3){
                                    leitor.nextLine(); //limpar buffer

                                    System.out.print("[Update] -> Insira a categoria do jogo que deseja atualizar: ");
                                    categoria = leitor.nextLine();
                                }
                                if (DB_CRUD.updateGame(updateID, categoria)){
                                    System.out.println("[Update] -> Registro atualizado com sucesso");
                                }
                            } else{
                                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados");
                            }
                        }
                        case 7 -> {
                            if (hasData){
                                try {                                    
                                    System.out.println("[Sort] -> Digite o atributo a ser ordenado:\n");
                                    System.out.println("                [1] - ID                   [2] - NOME");
                                    System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                                    int ordenacao = leitor.nextInt();
                                    if (ordenacao == 1 || ordenacao == 2){
                                        System.out.print("[Sort] -> Digite o número de caminhos a serem usados na ordenação: ");
                                        int caminhos = leitor.nextInt();
                                        System.out.print("[Sort] -> Digite a capacidade máxima da heap utilizada na memória primária: ");
                                        int heapSize = leitor.nextInt();
                                        if (heapSize < 1){
                                            System.out.println("[INFO] -> Valor digitado inválido ou muito baixo. O tamano da heap foi definido como 7.");
                                            heapSize = 7;
                                        }
                                        DB_Sort.externalSort(caminhos,heapSize,ordenacao);
                                    }
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            } else{
                                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados");
                            }
                        }
                        case 8 ->{
                            DB_Services.backupDatabase();
                        }
                        case 9 ->{
                            DB_Services.restoreDatabase();
                        }
                        case 10 ->{
                            DB_Services.printDataBase();
                        }
                        case 11 ->{
                            if (hasData){
                                UI.indexar(indexStatus);
                                int tipo = leitor.nextInt();

                                //limpar indices anteriores
                                if (tipo > 0 && tipo < 4)
                                    clearIndex();

                                switch (tipo) {
                                    case 1 -> {
                                        System.out.print("[Index] -> Digite a ordem da Árvore B+: ");
                                        int ordem = leitor.nextInt();
                                        if (ordem < 1){
                                            System.out.println("[ERRO] -> Ordem muito pequena ou inválida");
                                        }
                                        else{
                                            System.out.println("[Index] -> Iniciando indexação por Árvore B+...");
                                            arvore = Index_ArvoreBMais.IndexDataBase(arvore,ordem);
                                        }
                                    }
                                    case 2 -> {
                                        System.out.print("[Index] -> Digite a quantidade de registros por cesto na Hash Extensível: ");
                                        int quantDados = leitor.nextInt();
                                        if (quantDados < 1){
                                            System.out.println("[ERRO] -> Quantidade de registros por cesto muito pequena ou inválida");
                                        }
                                        else{
                                            System.out.println("[Index] -> Iniciando indexação por Hash Extensível...");
                                            hash = Index_HashExtensivel.IndexDataBase(hash,quantDados);
                                        }
                                    }
                                    case 3 -> {
                                        System.out.print("[Index] -> Digite a quantidade de registros por bloco (por categoria) na Lista Invertida: ");
                                        int quantDados = leitor.nextInt();
                                        if (quantDados < 1){
                                            System.out.println("[ERRO] -> Quantidade de registros por bloco muito pequena ou inválida");
                                        }
                                        else{
                                            System.out.println("[Index] -> Iniciando indexação por Lista Invertida...");
                                            lista = Index_ListaInvertida.IndexDataBase(lista, quantDados);
                                        }
                                    }
                                    default -> {
                                        System.out.println("[ERRO] -> Opção inválida");
                                    }
                                }
                            } else{
                                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados");
                            }
                        }
                        case 12 -> {                                  
                            System.out.println("[Comp] -> Escolha uma das opções de compressão:\n");
                            System.out.println("       [1] - LZW       [2] - HUFFMAN");
                            System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                            int compressao = leitor.nextInt();
                            if (compressao == 1){
                                System.out.println("[Comp] -> Preparando para comprimir usando LZW...");
                                LZW.compress_DataBase();
                            }
                            if (compressao == 2){
                                System.out.println("[Comp] -> Preparando para comprimir usando Huffman...");
                                Huffman.compress_DataBase();
                            }
                        }
                        case 13 -> {                                  
                            System.out.println("[Comp] -> Escolha uma das opções de descompressão:\n");
                            System.out.println("       [1] - LZW       [2] - HUFFMAN");
                            System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                            int compressao = leitor.nextInt();
                            if (compressao == 1){
                                System.out.println("[DEComp] -> Preparando para descomprimir usando LZW...");
                                LZW.decompress_DataBase();
                            }
                            if (compressao == 2){
                                System.out.println("[DEComp] -> Preparando para descomprimir usando Huffman...");
                                Huffman.decompress_DataBase();
                            }
                        }
                        case 14 -> { 
                            System.out.println("[Encrypt] -> Escolha uma das opções de criptofrafia:\n");
                            System.out.println("       [1] - Simples (offset)       [2] - RSA");
                            System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                            int criptografia = leitor.nextInt();
                            if (criptografia == 1){//SIMPLES
                                System.out.print("[Encrypt] -> Digite o offset: ");
                                int offset = leitor.nextInt();
                                if (offset > 0){
                                    System.out.println("[Encrypt] -> Preparando para criptografar usando offset [" + offset +"]...");
                                    EncryptSimple.EncryptDataBase(offset);
                                }
                            }
                            if (criptografia == 2){//RSA
                                System.out.println("[Encrypt] -> Preparando para criptografar usando RSA...");
                                EncryptRSA.EncryptDataBase();
                            }                                 
                        }
                        case 15 -> {
                            System.out.println("[Decrypt] -> Escolha uma das opções de descriptofrafia:\n");
                            System.out.println("       [1] - Simples (offset)       [2] - RSA");
                            System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                            int descriptografia = leitor.nextInt();
                            if (descriptografia == 1){//SIMPLES
                                System.out.print("[Decrypt] -> Digite o offset: ");
                                int offset = leitor.nextInt();
                                if (offset > 0){
                                    System.out.println("[Decrypt] -> Preparando para descriptografar usando offset [" + offset +"]...");
                                    EncryptSimple.DecryptDataBase(offset);
                                }
                            }
                            if (descriptografia == 2){//RSA
                                System.out.print("[Decrypt] -> Digite a chave privada: ");
                                BigInteger d = leitor.nextBigInteger();
                                System.out.println("[Decrypt] -> Preparando para descriptografar usando RSA...");
                                EncryptRSA.DecryptDataBase(d);
                            }                                   
                        }
                        case 101 -> {
                            DB_Debug.csvExtractAll();
                        }
                        case 102 -> {
                            System.out.print("\n[DEBUG_CsvExtract] Digite o número de registros que deseja extrair do CSV: ");
                            int num = leitor.nextInt();
                            DB_Debug.csvExtractNum(num);
                        }
                        case 107 -> {
                            if (hasData){
                                try {                                    
                                        System.out.print("[DEBUG_Sort] -> Digite o número de caminhos a serem usados na ordenação: ");
                                        int caminhos = leitor.nextInt();
                                        System.out.print("[DEBUG_Sort] -> Digite a capacidade máxima da heap utilizada na memória primária: ");
                                        int heapSize = leitor.nextInt();
                                        if (heapSize < 1){
                                            System.out.println("[INFO] -> Valor digitado inválido ou muito baixo. O tamano da heap foi definido como 7.");
                                            heapSize = 7;
                                        }
                                        DB_Debug.externalSort(caminhos, heapSize, 2);
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            } else{
                                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados");
                            }
                        }
                        default -> {
                            if (choice !=0)
                                System.out.println("[ERRO] -> Número inválido. Por favor, digite o número de uma das opções acima.");
                        }
                    }
                    
                    //mostrar MENU até que escolha seja 0
                    if (choice !=0){
                        UI.menu(indexStatus,totalGames,totalDeleted);
                    }


                } catch (Exception e){
                    System.out.println("[ERRO] -> [" + e + "]");

                    //limpar buffer do scanner
                    leitor.nextLine();

                    //exibir novamente o menu
                    UI.menu(indexStatus,totalGames,totalDeleted);
                }
                
            }
        }catch(Exception e){
            System.out.println(e);
        }

        System.out.println("[INFO] -> Programa encerrado.");
    }
 
}
