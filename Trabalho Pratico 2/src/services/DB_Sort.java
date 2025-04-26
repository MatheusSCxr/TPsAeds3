package services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import main.DataBase;
import static main.DataBase.indexStatus;
import models.HeapGame;
import models.SteamGame;

public class DB_Sort {
    public static int totalSegmentos;   //variável de controle para indicar o progresso da ordenação externa
    public static int ultimoSegmento;   //variável para cálculo do tempo restante para ordenar
    public static double velocidade;   //variável para cálculo do tempo restante para ordenar
    public static int totalIntercalações;   //variável de controle para indicar quantas intercalações foram feitas

    public static void externalSort(int caminho_num, int heapSize, int tipo) throws IOException{
        //limpar pasta de ordenação, caso tenha sobrado algum caminho decorrente de um erro
        File folder = new File("./src/resources/db_Sort/");
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }

        //comparator para ordenar por ID (crescente) ou nome (decrescente), priorizando o menor peso
        Comparator<HeapGame> comparator;

        System.out.println("[Sort] -> Distribuindo registros nos caminhos (Etapa 1/2)...");
        if (tipo == 1){ //ordenar por ID
            comparator = (a, b) -> {
                //se ambos têm peso, comparar pelo peso
                if (a.getPeso() != 0 && b.getPeso() != 0) {
                    //priorizar pelo peso (menor peso primeiro)
                    int comparaPeso = Integer.compare(a.getPeso(), b.getPeso());
                    if (comparaPeso != 0) {
                        return comparaPeso;
                    }
                } else if (a.getPeso() == 0 && b.getPeso() != 0) {
                    return -1; // 'a' sem peso vem primeiro
                } else if (a.getPeso() != 0 && b.getPeso() == 0) {
                    return 1; // 'b' sem peso vem primeiro
                }
    
                //se os pesos são iguais comparar pelo ID
                return Integer.compare(a.getId(), b.getId());
            };
        }else{ //ordenar por nome
            comparator = (a, b) -> {
                //se ambos têm peso, comparar pelo peso
                if (a.getPeso() != 0 && b.getPeso() != 0) {
                    //priorizar pelo peso (menor peso primeiro)
                    int comparaPeso = Integer.compare(a.getPeso(), b.getPeso());
                    if (comparaPeso != 0) {
                        return comparaPeso;
                    }
                } else if (a.getPeso() == 0 && b.getPeso() != 0) {
                    return -1; // 'a' sem peso vem primeiro
                } else if (a.getPeso() != 0 && b.getPeso() == 0) {
                    return 1; // 'b' sem peso vem primeiro
                }
    
                //se os pesos são iguais comparar pelo nome
                return a.getName().compareTo(b.getName());
            };
        }

        //ordenação
        try (RandomAccessFile entrada = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "r")) {
            //contar tempo total da ordenação
            long tempo_inicio = System.currentTimeMillis();
              
            //posicionar ponteiro da entrada na pos 0
            entrada.seek(0);
            int lastID = entrada.readInt(); //id usado como referência na criação da nova base de dados

            //heap usada para ordenar
            PriorityQueue<HeapGame> heap = new PriorityQueue<>(comparator);

            //criar arquivos que servirão de caminhos para a ordenação externa
            RandomAccessFile caminhos[] = new RandomAccessFile[caminho_num];

            for (int i = 0; i < caminho_num; i++){
                caminhos[i] = new RandomAccessFile("./src/resources/db_Sort/caminho_" + i + ".db","rw");
                caminhos[i].writeInt(lastID); //transcrever o ultimo ID inserido para a nova base de dados
            }

            //variável de controle do tamanho da heap
            int elementosHeap = 0;

            //variável de controle do número de elementos processados
            int total_registros = 0;

            //preencher a heap até a capacidade máxima (heapSize)
            while (elementosHeap < heapSize && (entrada.getFilePointer() < entrada.length())){
                //ler se a lápide está ativa
                int lapide = entrada.readUnsignedByte();
                if (lapide == 0xFF){
                    //ler e pular o tamanho do registro a seguir
                    entrada.skipBytes(entrada.readInt());
                }
                else{
                    entrada.skipBytes(4);
                    heap.add(new HeapGame(DB_CRUD.readGame(entrada),0));
                    elementosHeap++;
                    total_registros++;
                }
            }

            //heap preenchida com peso 0

            //esvaziar heap e preencher novamente até o final do arquivo de entrada
            String ultimo = "";
            int ultimoId = -1;
            int peso = 0;
            int caminho_atual = 0;

            while (entrada.getFilePointer() < entrada.length()){
                //mostrar barra de progresso
                UI.progressBar((int)entrada.getFilePointer(),(int)entrada.length(),"[Sort]",5,0);
                
                //puxar elemento da heap
                HeapGame heapgame = heap.poll();
                SteamGame jogo = heapgame.getGame();

                boolean condicao;
                if (tipo == 1) {
                    //comparar por ID
                    condicao = jogo.getId() > ultimoId;
                }else{
                    //comparar por nome
                    condicao = jogo.getName().compareTo(ultimo) > 0;
                }
                
                if (condicao) {
                    // escrever no caminho atual
                    DB_CRUD.writeGame(caminhos[caminho_atual], jogo);
                    total_registros++;
                    

                    //atualizar referência de ultimo
                    if (tipo == 1) {
                        ultimoId = jogo.getId();
                    } else {
                        ultimo = jogo.getName();
                    }

                    //ler próximo registro e coloca-lo na heap
                    boolean inseriu = false;
                    while (!inseriu){
                        //ler se a lápide está ativa
                        int lapide = entrada.readUnsignedByte();
                        if (lapide == 0xFF){
                            //ler e pular o tamanho do registro a seguir
                            entrada.skipBytes(entrada.readInt());
                        }
                        else{
                            entrada.skipBytes(4);
                            heap.add(new HeapGame(DB_CRUD.readGame(entrada),peso));
                            inseriu = true;
                        }
                    }
                }
                else{
                    //se a heap estiver cheia, com elementos de peso maior
                    if (heapgame.getPeso() > peso){
                        //incrementar peso para os próximos elementos
                        peso++;

                        //adicionar elemento mais pesado na heap
                        heap.add(new HeapGame(jogo, peso));
    
                        //trocar caminho ("ciclando" entre 0 até o numero máximo)
                        caminho_atual = (caminho_atual + 1) % caminho_num;

                        //iniciar um novo segmento e resetar referencias
                        ultimo = "";
                        ultimoId = -1;
                    }
                    else{
                        //preencher a heap até que todos tenham o mesmo peso superior
                        heap.add(new HeapGame(jogo, peso + 1));
                    }
                }
            }

            //mostrar 100% de progresso
            UI.progressBar((int)entrada.getFilePointer(),(int)entrada.length(),"[Sort]",5,0);

            int ref_peso = peso;

            //gravar os elementos restantes do heap nos caminhos
            while (!heap.isEmpty()){
                HeapGame heapgame = heap.poll();
                SteamGame jogo = heapgame.getGame();
                if (heapgame.getPeso() != ref_peso){
                    //trocar caminho ("ciclando" entre 0 até o numero máximo)
                    caminho_atual = (caminho_atual + 1) % caminho_num;
                    ref_peso = heapgame.getPeso();
                    DB_CRUD.writeGame(caminhos[caminho_atual], jogo);
                }
                else{
                    ref_peso = heapgame.getPeso();
                    DB_CRUD.writeGame(caminhos[caminho_atual], jogo);
                }
            }


            System.out.println("\n[Sort] -> Registros distribuidos com sucesso");
            //intercalação dos arquivos

            //variável de controle de caminhos com algum valor
            int caminhos_validos = 0;

            //detectar quais arquivos possuem registros
            for (int i = 0; i < caminho_num; i++){
                if (caminhos[i].length() > 4) //4 pois ele armazena o ultimo id inserido na base de dados
                    caminhos_validos++;
            }

            System.out.println("[Sort] -> Caminhos devidamente utilizados: " + caminhos_validos);

            //contar quantidade de intercalações
            int intercalacoes = 0;

            if (caminhos_validos > 1){
                //intercalação própriamente dita
                System.out.println("[Sort] -> Iniciando intercalação (Etapa 2/2)...");

                //atualizar variável de controle para mostrar o progresso
                totalSegmentos = 0;

                //criar arquivos que servirão de caminhos para a ordenação externa
                RandomAccessFile intercalados[] = new RandomAccessFile[caminhos_validos]; //reduz a quantidade de acordo com os arquivos válidos, caso necessário

                //iniciar arquivos que serão intercalados
                for (int i = 0; i < caminhos_validos; i++){
                    intercalados[i] = new RandomAccessFile("./src/resources/db_Sort/caminho_" + (i + caminho_num) + ".db","rw");
                    intercalados[i].writeInt(lastID); //transcrever o ultimo ID inserido para a nova base de dados
                }

                //atualizar a variável de controle para medir o delta de segmentos ordenados durante a intercalação, para calcular o tempo restante
                ultimoSegmento = 0;

                //intercalar até sobrar 1 arquivo de caminho (que será o arquivo ordenado)
                while (caminhos_validos > 1){
                    //intercalar os arquivos dos caminhos
                    intercalados = intercalar(caminhos, intercalados, caminhos_validos,tipo);

                    //resetar arquivos de caminhos anteriores para reaproveitar os arquivos
                    for (RandomAccessFile i : caminhos){
                        i.setLength(4); //4 para preservar o ultimo id inserido na base de dados
                        i.skipBytes(4);
                    }

                    //intercalar novamente, agora os novos arquivos intercalados, aproveitando os arquivos anteriores de caminhos
                    caminhos = intercalar(intercalados, caminhos, caminhos_validos,tipo);

                    //resetar arquivos de caminhos anteriores (que eram os intercalados anteriorermente) para reaproveitar os arquivos
                    for (RandomAccessFile i : intercalados){
                        i.setLength(4); //4 para preservar o ultimo id inserido na base de dados
                        i.skipBytes(4);
                    }

                    //contar novamente número de caminhos válidos (com registros)
                    caminhos_validos = 0;

                    //detectar quais arquivos possuem registros
                    for (int i = 0; i < caminho_num; i++){
                        if (caminhos[i].length() > 4)
                            caminhos_validos++;
                    }

                    //verificação dupla para contar as intercalações
                    if (caminhos_validos > 1){
                        intercalacoes += 2;
                    }
                }
                //deletar por completo os vetores auxiliares (intercalados)
                for (int k = 0; k < intercalados.length; k++){
                    if (intercalados[k] != null) {
                        intercalados[k].close();

                        //deletar os arquivos intercalados
                        File deletar = new File("./src/resources/db_Sort/caminho_" + (k + caminho_num) + ".db");
                        //verificação dupla
                        if (deletar.exists()){
                            deletar.delete();
                        }
                    }
                } 
            }

            //deletar por completo os vetores auxiliares (caminhos)
            for (int j = 1; j < caminhos.length; j++){ //começar por 1 pois 0 está com a base de dados ordenada
                if (caminhos[j] != null) {
                    caminhos[j].close();

                    //deletar os arquivos intercalados
                    File deletar = new File("./src/resources/db_Sort/caminho_" + j + ".db");
                    //verificação dupla
                    if (deletar.exists()) {
                        deletar.delete();
                    }
                }
            }
            //terminou a ordenação

            //fechar arquivo ordenado
            caminhos[0].close();

            //contar tempo total da ordenação
            long tempo_fim = System.currentTimeMillis();
            long tempo = (tempo_fim - tempo_inicio); // dividir por 1000 para contar em segundos
            
            //exibir estatísticas
            System.out.println("\n[Sort] -> A Base de dados foi ordenada com sucesso.");
            System.out.println("[Sort] -> Intercalações registradas: " + intercalacoes + " \t Tempo decorrido: " + tempo/1000.0 + "s \tTotal de Segmentos: " + totalSegmentos + " \t Velocidade Média: " +String.format("%.1f",totalSegmentos/(tempo/1000.0)) + " segmentos/s \n");              

            //atualizar número de registros
            DataBase.totalGames = total_registros;
            DataBase.totalDeleted = 0;

            //remover indexação anterior ou indexar novamente
            try {
                Scanner leitor = new Scanner(System.in);
                if (DataBase.indexStatus > 0){
                    System.out.println("[INFO] -> Indexação detectada. Localizando arquivo de metadados...");
                    File indexMetadados = new File("./src/resources/db_Index/indexMetadata.db");
                    if (indexMetadados.exists()){
                        System.out.println("[INFO] -> Arquivo de metadados da indexação encontrado");
                        System.out.println("[INFO] -> Deseja realizar a indexação novamente?");
                        System.out.println(" \n               [1] - SIM                   [0] - NÃO");
                        System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                        int option = Integer.parseInt(leitor.nextLine());
                        if (option == 1){
                            try (RandomAccessFile metadados = new RandomAccessFile("./src/resources/db_Index/indexMetadata.db", "r")){
                                metadados.seek(1);//pular byte to tipo de indexação
                                int config =  metadados.readInt();
                                //indexar novamente, de acordo com a indexação anterior
                                switch (indexStatus) {
                                    case 1 -> {
                                        System.out.println("[Index] -> Indexando novamente para Árvore B+ [Ordem: " + config + " ]...");
                                        DataBase.arvore = Index_ArvoreBMais.IndexDataBase(DataBase.arvore,config);
                                    }
                                    case 2 -> {
                                        System.out.println("[Index] -> Indexando novamente para Hash Extensível [Tam. Cesto: " + config + " ]...");
                                        DataBase.hash = Index_HashExtensivel.IndexDataBase(DataBase.hash, config);
                                    }
                                    case 3 -> {
                                        
                                    }
                                    default -> System.out.println("[ERRO] -> Opção inválida");
                                }
                            }
                        } else{
                            System.out.println("[INFO] -> Removendo indexação anterior...");

                            //remover arquivo de metadados e indexação
                            indexMetadados.delete();
                            DataBase.clearIndex();

                            DataBase.indexStatus = 0;
                        }
                    }
                    else{
                        System.out.println("[ERRO] -> Não foi possível localizar o arquivo de metadados da indexação anterior");
                        DataBase.indexStatus = 0;
                    }
                }
            } catch (Exception e) {
                System.out.println("[ERRO] -> Opção inválida");
            }
            //fechar arquivo de entrada (antiga base de dados)
            entrada.close();

            //fazer backup e substituir pelo arquivo ordenado
            DB_Services.backupDatabase();
            
        } catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível realizar a ordenação dos registros.");
        }
    }

    public static RandomAccessFile[] intercalar(RandomAccessFile[] caminhos, RandomAccessFile[] intercalados, int caminhos_validos, int tipo) throws IOException{
        try {
            //variável para a contagem de segmentos
            int segmentos = 0;

            //cronometrar tempo da intercalação
            double tempo_inicio = System.currentTimeMillis();

            //vetor de jogos para armazenar o próximo jogo de cada caminho
            SteamGame jogos_intercalados[] = new SteamGame[caminhos_validos];

            //obter primeiro jogo de cada caminho
            for (int i = 0; i < caminhos_validos; i++){
                //posicionar ponteiros no inicio do registro de cada um dos caminhos
                if (caminhos[i].length() > 4){ //verificar se o caminho possui um registro
                    caminhos[i].seek(9); //pular ultimo id (4) + lapide (1) + tamanho do registro (4)
                    
                    //ler primeiro jogo de cada caminho
                    jogos_intercalados[i] = DB_CRUD.readGame(caminhos[i]);
                }
                else{
                    jogos_intercalados[i] = null;
                }
            }
            
            //controle do índice do arquivo intercalado
            int arquivo_intercalado_atual = 0;

            //variável para controlar o último jogo inserido no arquivo intercalado
            SteamGame ultimoInserido = null;

            //iniciar a intercalação de caminhos
            while (!FilesEmpty(caminhos_validos, caminhos) || !gamesEmpty(jogos_intercalados)) {
                //encontrar o menor jogo entre os jogos dos caminhos
                int menorIndice = -1;
                SteamGame menorJogo = null;

                //percorrer todos os N arquivos validos
                for (int i = 0; i < caminhos_validos; i++) {
                    if (jogos_intercalados[i] != null && (menorJogo == null || (tipo == 1 && jogos_intercalados[i].getId() < menorJogo.getId()) || (tipo == 2 && jogos_intercalados[i].getName().compareTo(menorJogo.getName()) < 0))) {
                        menorJogo = jogos_intercalados[i];
                        menorIndice = i;
                    }
                }

                //se houver um jogo a ser escrito no arquivo intercalado
                if (menorJogo != null) {
                    //verifica se o jogo atual é menor que o último inserido
                    if (ultimoInserido != null && ((tipo == 2 && menorJogo.getName().compareTo(ultimoInserido.getName()) < 0) || (tipo == 1 && menorJogo.getId() < ultimoInserido.getId()))) {
                        //se o último inserido for maior, muda o arquivo intercalado
                        if (caminhos_validos != 1){
                            segmentos++;
                            arquivo_intercalado_atual = (arquivo_intercalado_atual + 1) % caminhos_validos;
                        }            
                    }

                    //escrever o menor jogo no arquivo intercalado atual
                    DB_CRUD.writeGame(intercalados[arquivo_intercalado_atual],menorJogo);

                    //atualizar o último jogo inserido
                    ultimoInserido = menorJogo;

                    //mover o ponteiro do arquivo de onde o jogo foi retirado
                    if (caminhos[menorIndice].getFilePointer() < caminhos[menorIndice].length()) {
                        //mover ponteiro para o próximo registro
                        caminhos[menorIndice].skipBytes(5);
                        jogos_intercalados[menorIndice] = DB_CRUD.readGame(caminhos[menorIndice]);
                    } else {
                        //se terminar a leitura, então colocar null
                        jogos_intercalados[menorIndice] = null;
                    }
                }
            }

            //atualizar variável de controlo do total de segmentos, se for maior (casos em que na segunda intercalação surgem mais segmentos)
            if (segmentos > totalSegmentos) 
                totalSegmentos = segmentos; //segmentos totais restantes


            double tempo_fim = System.currentTimeMillis();
            double tempo = (tempo_fim - tempo_inicio)/1_000.0;

            //calcular velocidade (para descobrir o tempo necessário para ordenar)
            double velocidade_intercalação = (ultimoSegmento - segmentos)/tempo; //delta (segmentos ordenados) sobre o tempo  (tempo gasto em segundos)

            //atualizar referência do ultimos segmentos e da velocidade (constante)
            ultimoSegmento = segmentos;
            
            //exibir progresso estimado
            UI.progressBar(segmentos, totalSegmentos, "[Sort]",6,(int)((double)segmentos/velocidade_intercalação));
        } catch (IOException e) {
            System.out.println(e);
        }
        return intercalados;
    }

    public static boolean gamesEmpty(SteamGame[] jogos_intercalados) {
        for (SteamGame jogo : jogos_intercalados) {
            if (jogo != null) {
                return false;  //se encontrar pelo menos 1, então não está vazio
            }
        }
        return true;  //se todos os elementos forem null, então está vazio
    }

    public static boolean FilesEmpty(int caminhos_validos, RandomAccessFile[] caminhos) throws IOException {
        for (int i = 0; i < caminhos_validos; i++) {
            if (caminhos[i].getFilePointer() < caminhos[i].length()) {
                return false;
            }
        }
        return true;
    }

}
