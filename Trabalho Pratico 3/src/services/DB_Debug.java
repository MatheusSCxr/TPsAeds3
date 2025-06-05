package services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import models.HeapGame;
import models.SteamGame;

public class DB_Debug {

    public static void csvExtractAll(){
        csvExtractNum(27077);
    }

    public static void csvExtractNum(int max){
        max += 2;
        try (RandomAccessFile entrada = new RandomAccessFile("./src/resources/CSV_Input/steamgames.csv","r")){ //obter os objetos da base de dados CSV (steamgames.csv)

            //Excluir o arquivo existente, se necessário
            File file = new File("./src/resources/db_Output/gamesDB.db");
            if (file.exists()) {
                file.delete();
            }

            //definir o local de saída com os dados extraídos
            try (RandomAccessFile saida = new RandomAccessFile("./src/resources/db_Output/gamesDB.db","rw")) {
                //ler a primeira linha, mas ela será desconsiderada assim que entrar no loop de leitura
                String linha = entrada.readLine();
                
                //contador de linhas lidas
                int contador = 2;
                System.out.println("Contando linhas...");
                
                //variável para debug
                Boolean stop = false;
                //iniciando o loop de leitura completa do arquivo CSV
                while (entrada.getFilePointer() < entrada.length() && stop == false && contador < max){
                    //obter liha atual do CSV
                    linha = entrada.readLine();
                    
                    //mensagens para debug
                    System.out.println("\n--------------- Processando Linha [" + contador + "] -------------------");
                    System.out.println("LINHA EXTRAÍDA -> " + linha);
                    
                    //incrementar contador de linhas lidas
                    contador++;
                    
                    //se a linha for válida, continuar a leitura
                    if (linha != null){
                        //criar objeto do tipo SteamGame, para registrar o objeto inteiro como 1 registro
                        SteamGame jogo = new SteamGame();
                        
                        //filtrar e separar os conteúdos do CSV, dividos por ','
                        String[] content = linha.split(",");
                        System.out.println("Strings obtidas -> " + content.length + "\nLendo as strings individualmente...");
                        
                        //detectar se a linha lida está de acordo com o padrão, caso esteja maior, significa que existe pelo menos mais de 1 elemento que contém vírgula na string
                        if (content.length > 18){
                            System.out.println("Linha com vírgulas detectada!");
                        }
                        
                        System.out.println("AppId -> " + content[0]);
                        
                        
                        //gravar no obejto
                        jogo.setAppid(Integer.parseInt(content[0]));
                        
                        int pos_vet = 1; //posição no vetor
                        int pos_val = 1; //representa a posição do elemento (appId,name,data etc) correspondente no csv (1 até 18)
                        
                        while (pos_val < 6){
                            //detectar se o conteúdo tem aspas no ínicio (pois indica que o conteúdo está entre aspas)
                            if ((pos_val !=2 && pos_val != 3) && content[pos_vet].startsWith("\"")){
                                System.out.println("Vírgula detectada!");
                                StringBuilder elemento = new StringBuilder();
                                int offset = 0;
                                while(!(content[pos_vet + offset].endsWith("\""))){
                                    elemento.append(content[pos_vet + offset]);
                                    elemento.append(",");
                                    offset++;
                                }
                                elemento.append(content[pos_vet + offset]);
                                System.out.println("Vírgulas = " + offset);
                                System.out.println("Escrevendo -> " + elemento.toString());
                                
                                pos_vet += offset;
                                
                                //substituir na string
                                content[pos_vet] = elemento.toString();
                            }
                            switch (pos_val) {
                                case 1 -> {
                                    System.out.println("Nome -> " + content[pos_vet]);
                                    jogo.setName(content[pos_vet]);
                                }
                                case 2 -> {
                                    System.out.println("Release extraído -> " + content[pos_vet]);
                                    String[] calendario = content[pos_vet].split("-");
                                    LocalDate data = LocalDate.of(Integer.parseInt(calendario[0]), Integer.parseInt(calendario[1]), Integer.parseInt(calendario[2]));
                                    
                                    //converter para Unix timestamp
                                    long timestamp = data.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
                                    
                                    System.out.println("Release extraída -> " + data + " UNIX = " + timestamp);
                                    jogo.setReleaseDate(timestamp);
                                }
                                case 3 -> {
                                    System.out.println("English (boolean) -> " + content[pos_vet]);
                                    if (content[pos_vet].startsWith("1")){
                                        jogo.setEnglish(true);
                                    }
                                    else{
                                        jogo.setEnglish(false);
                                    }
                                }
                                case 4 -> {
                                    System.out.println("Developer -> " + content[pos_vet]);
                                    jogo.setDeveloper(content[pos_vet]);
                                }
                                case 5 -> {
                                    System.out.println("Publisher -> " + content[pos_vet]);
                                    jogo.setPublisher(content[pos_vet]);
                                }
                                default -> System.out.println("pos_val INVÁLIDA ou não acessível código = " + pos_val);
                            }
                            pos_vet++;
                            pos_val++;
                            System.out.println("POS VAL -> " + pos_val);
                            System.out.println("PROXIMA -> " + content[pos_vet]);
                        }
                        
                        //gora pos_vet é constante e será apenas incrmentado 1. atual = 6
                        System.out.println("Length plat = " + content[pos_vet].length());
                        System.out.println("Plataforms -> " + content[pos_vet]);
                        if (content[pos_vet].length() > 7){ //string de tamanho fixo
                            String process = content[pos_vet].substring(0,content[pos_vet].indexOf(";"));
                            System.out.println("Plataforms PROCESSADA -> " + process);
                            jogo.setPlatforms(process);
                        }
                        else{
                            jogo.setPlatforms("windows");
                        }
                        
                        //pos_vet = 7
                        System.out.println("Required Age -> " + content[pos_vet + 1]);
                        jogo.setRequiredAge(Integer.parseInt(content[pos_vet + 1]));
                        
                        String[] categories = content[pos_vet + 2].split(";");
                        ArrayList<String> categoriesList = new ArrayList<>();
                        
                        System.out.println("Categories ORIGINAL -> " + content[pos_vet + 2]);
                        if (categories.length > 1){
                            System.out.println("Detectadas " + categories.length + " categorias. Gravando valor...");
                            for (String i : categories){
                                System.out.println("Categories Processada -> " + i);
                                categoriesList.add(i);
                            }
                        }
                        else{
                            System.out.println("Categories Processada -> " + categories[0]);
                            categoriesList.add(categories[0]);
                        }
                        jogo.setCategories(categoriesList);
                        
                        String[] genres = content[pos_vet + 3].split(";");
                        ArrayList<String> genreList = new ArrayList<>();
                        
                        System.out.println("Genres ORIGINAL -> " + content[pos_vet + 3]);
                        if (genres.length > 1){
                            System.out.println("Detectados " + genres.length + " gêneros. Gravando valor...");
                            for (String i : genres){
                                System.out.println("Genre Processado -> " + i);
                                genreList.add(i);
                            }
                        }
                        else{
                            System.out.println("Genre Processado -> " + genres[0]);
                            genreList.add(genres[0]);
                        }
                        jogo.setGenres(genreList);
                        
                        String[] spytag = content[pos_vet + 4].split(";");
                        ArrayList<String> spytagList = new ArrayList<>();
                        
                        System.out.println("Spytag ORIGINAL -> " + content[pos_vet + 4]);
                        if (spytag.length > 1){
                            System.out.println("Detectadas " + spytag.length + " spytags. Gravando valor...");
                            for (String i : spytag){
                                System.out.println("Spytag Processada -> " + i);
                                spytagList.add(i);
                            }
                        }
                        else{
                            System.out.println("Spytag Processada -> " + genres[0]);
                            spytagList.add(spytag[0]);
                        }
                        jogo.setSteamspyTags(spytagList);
                        
                        System.out.println("Achievements -> " + content[pos_vet + 5]);
                        jogo.setAchievements(Integer.parseInt(content[pos_vet + 5]));
                        
                        System.out.println("Positive Ratings -> " + content[pos_vet + 6]);
                        jogo.setPositiveRatings(Integer.parseInt(content[pos_vet + 6]));
                        
                        System.out.println("Negative Ratings -> " + content[pos_vet + 7]);
                        jogo.setNegativeRatings(Integer.parseInt(content[pos_vet + 7]));
                        
                        System.out.println("Avarage Playtime -> " + content[pos_vet + 8]);
                        jogo.setAveragePlaytime(Integer.parseInt(content[pos_vet + 8]));
                        
                        System.out.println("Median Playtime -> " + content[pos_vet + 9]);
                        jogo.setMedianPlaytime(Integer.parseInt(content[pos_vet + 9]));
                        
                        System.out.println("Owners -> " + content[pos_vet + 10]);
                        jogo.setOwners(content[pos_vet + 10]);
                        
                        System.out.println("Price -> " + content[pos_vet + 11]);
                        jogo.setPrice(Float.parseFloat(content[pos_vet + 11]));
                        
                        //gravar as informações do objeto no arquivo como um registro
                        DB_CRUD.writeGame(saida, jogo);
                    }
                }
                System.out.println("Total de linhas processadas = " + (contador - 2) + "\n");
                entrada.close();
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Um erro ocorreu durante o processamento do arquivo :(");
            System.out.println(e);
        }
    }
    
    //ordena apenas por nome tipo == 2
    public static void externalSort(int caminho_num, int heapSize, int tipo) throws IOException{
        try (RandomAccessFile entrada = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "r")) {
            //primerio -> retirar da heap
            //segundo -> comparar com o úlltimo inserido
            //terceiro -> se for maior, retirar da heap e inserir (Se for menor, inserir no heap com peso + 1)
            //quarto -> atualizar ultimo_arquivo
            //quinto -> obter novo registro


            //comparator para ordenar por ID (crescente), priorizando o menor peso
            Comparator<HeapGame> comparator = (a, b) -> {
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
                }
            }

            //heap preenchida com peso 0

            //esvaziar heap e preencher novamente até o final do arquivo de entrada
            String ultimo = "";
            int peso = 0;
            int caminho_atual = 0;

            while (entrada.getFilePointer() < entrada.length()){
                System.out.println("PONTEIRO -> " + entrada.getFilePointer());
                HeapGame heapgame = heap.poll();
                SteamGame jogo = heapgame.getGame();
                System.out.println("jogo atual " + jogo.getName());
                System.out.println("peso do jogo ->" + heapgame.getPeso());
                if (jogo.getName().compareTo(ultimo) > 0){
                    System.out.println("é maior");
                    //escrever no caminho atual
                    DB_CRUD.writeGame(caminhos[caminho_atual], jogo);

                    //atualizar referência de ultimo
                    ultimo = jogo.getName();

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
                            System.out.println("novo jogo inserido");
                        }
                    }
                }
                else{
                    System.out.println("não é maior");
                    //se a heap estiver cheia, com elementos de peso maior
                    if (heapgame.getPeso() > peso){
                        System.out.println("heap cheio, com peso anterior");
                        //incrementar peso para os próximos elementos
                        peso++;

                        System.out.println("aumentou peso");
                        heap.add(new HeapGame(jogo, peso));
    
                        //trocar caminho ("ciclando" entre 0 até o numero máximo)
                        caminho_atual = (caminho_atual + 1) % caminho_num;

                        //iniciar um novo segmento
                        System.out.println("Caminho trocado para " + caminho_atual);
                        ultimo = "";
                        System.out.println("novo segmento");
                    }
                    else{
                        System.out.println("inseriu com peso maior");
                        heap.add(new HeapGame(jogo, peso + 1));
                    }
                }
            }


            System.out.println("tam heap = " + heap.size());

            int ref_peso = peso;

            //gravar os elementos restantes do heap nos caminhos
            while (!heap.isEmpty()){
                HeapGame heapgame = heap.poll();
                SteamGame jogo = heapgame.getGame();
                if (heapgame.getPeso() != ref_peso){
                    //trocar caminho ("ciclando" entre 0 até o numero máximo)
                    System.out.println("Trocou caminho - escrita final");
                    caminho_atual = (caminho_atual + 1) % caminho_num;
                    ref_peso = heapgame.getPeso();
                    System.out.println("escreveu outro caminho");
                    DB_CRUD.writeGame(caminhos[caminho_atual], jogo);
                }
                else{
                    System.out.println("escreveu caminho atual");
                    ref_peso = heapgame.getPeso();
                    DB_CRUD.writeGame(caminhos[caminho_atual], jogo);
                }
            }


            System.out.println("Caminhos carregados");

            readIntercalado(caminhos);
            
            
            //intercalação dos arquivos
            int caminhos_validos = 0;

            //detectar quais arquivos possuem registros
            for (int i = 0; i < caminho_num; i++){
                if (caminhos[i].length() > 4){  //4 pois ele armazena o ultimo id inserido na base de dados
                    caminhos_validos++;
                }
                else{
                    System.out.println("caminho " + i + " vazio");
                }
            }

            System.out.println("Caminhos detectados " + caminhos_validos);

            if (caminhos_validos > 1){
                //criar arquivos que servirão de caminhos para a ordenação externa
                RandomAccessFile intercalados[] = new RandomAccessFile[caminhos_validos]; //reduz a quantidade de acordo com os arquivos válidos, caso necessário

                //iniciar arquivos que serão intercalados
                for (int i = 0; i < caminhos_validos; i++){
                    intercalados[i] = new RandomAccessFile("./src/resources/db_Sort/caminho_" + (i + caminho_num) + ".db","rw");
                    intercalados[i].writeInt(lastID); //transcrever o ultimo ID inserido para a nova base de dados
                }

                //intercalar até sobrar 1 arquivo de caminho (que será o arquivo ordenado)
                while (caminhos_validos > 1){
                    //intercalar os arquivos dos caminhos
                    intercalados = DB_Sort.intercalar(caminhos, intercalados, caminhos_validos,tipo);

                    //resetar arquivos de caminhos anteriores para reaproveitar os arquivos
                    for (RandomAccessFile i : caminhos){
                        i.setLength(4); //4 para preservar o ultimo id inserido na base de dados
                        i.skipBytes(4);
                    }

                    //intercalar novamente, agora os novos arquivos intercalados, aproveitando os arquivos anteriores de caminhos
                    caminhos = DB_Sort.intercalar(intercalados, caminhos, caminhos_validos,tipo);

                    //resetar arquivos de caminhos anteriores (que eram os intercalados anteriorermente) para reaproveitar os arquivos
                    for (RandomAccessFile i : intercalados){
                        i.setLength(4); //4 para preservar o ultimo id inserido na base de dados
                        i.skipBytes(4);
                    }

                    //contar novamente número de caminhos válidos (com registros)
                    caminhos_validos = 0;

                    //detectar quais arquivos possuem registros
                    for (int i = 0; i < caminho_num; i++){
                        if (caminhos[i].length() > 4){ //4 pois ele armazena o ultimo id inserido na base de dados
                            caminhos_validos++;
                        }
                        else{
                            System.out.println("caminho " + i + " vazio");
                        }
                    }
                    
                }
                //deletar por completo os vetores auxiliares (intercalados)
                for (int k = 0; k < intercalados.length; k++){
                    if (intercalados[k] != null) {
                        intercalados[k].close();
                        System.out.println("deletando caminho " + k);

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
                    System.out.println("deletando caminho " + j);
                    
                    //deletar os arquivos intercalados
                    File deletar = new File("./src/resources/db_Sort/caminho_" + j + ".db");
                    //verificação dupla
                    if (deletar.exists()) {
                        deletar.delete();
                    }
                }
            }

            //imprimir em um arquivo txt, o ID e o Nome, sequencialmente do arquivo gerado (para verificar a ordenação)
            DB_Services.printDataBase();

            //fechar arquivo ordenado
            caminhos[0].close();
            
            //terminou a ordenação
            System.out.println("[Sort] -> Base de dados ordenada com sucesso");

            //fechar arquivo de entrada (antiga base de dados)
            entrada.close();

            //fazer backup e substituir pelo arquivo ordenado
            DB_Services.backupDatabase();
            
        } catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível realizar a ordenação dos registros.");
        }
    }

    //lê um caminho intercalado durante a ordenação externa
    public static void readIntercalado(RandomAccessFile[] arquivo) throws IOException{
        //posicionar ponteiro no inico
        for (RandomAccessFile i : arquivo){
            System.out.println("\n\n\nIMPRIMINDO NOVO ARQUIVO:");
            i.seek(4);
            while (i.getFilePointer() < i.length()){
                i.skipBytes(5);
                SteamGame jogo = DB_CRUD.readGame(i);
                System.out.println("Jogo: " + jogo.getName() + "    ID: " + jogo.getId());
            }
        }
    }
}
