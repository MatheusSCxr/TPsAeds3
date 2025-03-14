import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.PriorityQueue;
import java.util.Scanner;

public class DataBase {
    public static int totalGames; //variável de controle do número de registros ativos no banco de dados
    public static int totalDeleted; //variável de controle do número de registros inativos no banco de dados
    public static boolean hasData; //variável que indica se existe ou não um banco de dados
    public static void main(String[] args) {
        System.out.println("[INFO] -> Procurando base de dados...");

        //identificar base de dados
        File dbFile = new File("./db_Output/gamesDB.db");
        if (dbFile.exists()){
            hasData = true;
            //abrir arquivo no modo de leitura
            try (RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db","r")) {
                System.out.println("[INFO] -> Foi encontrada uma base de dados");
                
                //contar a quantidade de jogos ativos e inativos (deletados) na base de dados
                countGames(arquivo);
            } catch (IOException e) {
                System.out.println("[ERRO] -> Não foi possível abrir o arquivo da base de dados");
                System.out.println(e);
            }
        }
        else{
            System.out.println("[INFO] -> Nenhuma base de dados foi encontrada na pasta \"db_Output\"");
            hasData = false;
        }

        //exibir interface de menu com opções
        UI_menu();
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
                            csvExtractAll();
                        }
                        case 2 -> {
                            System.out.print("\n[CsvExtract] -> Digite o número de registros que deseja extrair do CSV: ");
                            int num = leitor.nextInt();
                            csvExtractNum(num);
                        }
                        case 3 -> {
                            int tipo = 1;
                            while(tipo != 0){
                                UI_search();
                                tipo = leitor.nextInt();
                                if (tipo != 0 && tipo < 4){
                                    System.out.print("\n[Search] -> Digite o valor do atributo que deseja procurar nos registros: ");
                                    leitor.nextLine(); //descartar caractere \n
                                    String valor = leitor.nextLine();
                                    searchGame(valor, tipo);
                                    System.out.println("[Search] -> Pesquisa finalizada. Deseja realizar outra pesquisa?");
                                    System.out.println("                [1] - SIM                   [0] - NÃO");
                                    System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                                    tipo = leitor.nextInt();
                                }
                                else if (tipo > 3){
                                    System.out.println("[Search] -> Opção inválida.");
                                }
                            }
                        }
                        case 4 -> {
                            int tipo = 1;
                            while(tipo != 0){
                                UI_create();
                                tipo = leitor.nextInt();
                                if (tipo != 0){
                                    if (createGame(tipo)){
                                        System.out.println("[Create] -> Registro criado e gravado com sucesso");
                                    }
                                    else{
                                        System.out.println("[Create] -> Não foi possível criar e gravar o registro");
                                    }
                                }
                            }
                        }
                        case 5 -> {
                            System.out.print("[Delete] -> Insira o ID do jogo que deseja remover: ");
                            int deleteID = leitor.nextInt();
                            if (deleteGame(deleteID)){
                                System.out.println("[Delete] -> Registro excluído com sucesso");
                            }
                        }
                        case 6 -> {
                            System.out.print("[Update] -> Insira o ID do jogo que deseja atualizar: ");
                            int updateID = leitor.nextInt();
                            if (updateGame(updateID)){
                                System.out.println("[Update] -> Registro atualizado com sucesso");
                            }
                        }
                        case 7 -> {
                            try {
                                System.out.print("[Sort] -> Digite o número de caminhos a serem usados na ordenação: ");
                                int caminhos = leitor.nextInt();
                                System.out.print("[Sort] -> Digite a capacidade máxima da heap utilizada na memória primária: ");
                                int heapSize = leitor.nextInt();
                                if (heapSize < 1){
                                    System.out.println("[INFO] -> Valor digitado inválido ou muito baixo. O tamano da heap foi definido como 7.");
                                    heapSize = 7;
                                }
                                externalSort(caminhos,heapSize);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                        case 101 -> {
                            DEBUG_csvExtractAll();
                        }
                        case 102 -> {
                            System.out.print("\n[DEBUG_CsvExtract] Digite o número de registros que deseja extrair do CSV: ");
                            int num = leitor.nextInt();
                            DEBUG_csvExtractNum(num);
                        }
                        default -> {
                            if (choice !=0)
                                System.out.println("[ERRO] -> Número inválido. Por favor, digite o número de uma das opções acima.");
                        }
                    }
                    
                    //mostrar MENU até que escolha seja 0
                    if (choice !=0){
                        UI_menu();
                    }


                } catch (InputMismatchException e){
                    System.out.println("[ERRO] -> Não foi possível ler a opção digitada [" + e + "]");

                    //limpar buffer do scanner
                    leitor.nextLine();

                    //exibir novamente o menu
                    UI_menu();
                }
                
            }
        }

        System.out.println("[INFO] -> Programa encerrado.");
    }

    public static void UI_menu(){
        System.out.println("\n----------------------- [ MENU ] -----------------------");
        System.out.println("Registros ativos -> [" + totalGames + "]        Registros inativos -> [" + totalDeleted + "]");
        System.out.println("[1] - Criar Arquivo com todos os registros do CSV");
        System.out.println("[2] - Criar Arquivo com um número N de registros do CSV (primeiro -> último)");
        System.out.println("[3] - Procurar por um atributo nos registros [ID, appID, Nome]");
        System.out.println("[4] - Criar um registro no arquivo de banco de dados");
        System.out.println("[5] - Remover um registro no arquivo de banco de dados [por ID]");
        System.out.println("[6] - Atualizar um registro no arquivo de banco de dados [por ID]");
        System.out.println("[7] - Ordenar arquivo de registros");
        System.out.println("[101][ DEBUG ] - Criar Arquivo com todos os registros do CSV [Aviso: LENTO]");
        System.out.println("[102][ DEBUG ] - Criar Arquivo com um número N de registros do CSV (primeiro -> último) [Aviso: LENTO]");
        System.out.println("[0] - Encerrar o programa");
        System.out.println("--------------------------------------------------------");
    }

    public static void UI_search(){
        System.out.println("\n----------------------- [ Pesquisar ] -----------------------");
        System.out.println("[1] - Pesquisar pelo ID");
        System.out.println("[2] - Pesquisar pelo appId");
        System.out.println("[3] - Pesquisar pelo nome");
        System.out.println("\n[0] - Voltar ao menu principal");
        System.out.println("--------------------------------------------------------");

        System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
    }

    public static void UI_create(){
        System.out.println("\n----------------------- [ Criar ] -----------------------");
        System.out.println("[1] - Criar e adicionar registro com construtor padrão");
        System.out.println("[2] - Criar e adicionar registro customizado");
        System.out.println("\n[0] - Voltar ao menu principal");
        System.out.println("--------------------------------------------------------");

        System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
    }

    public static void UI_update(SteamGame jogo) {
        System.out.println("\n----------------------- [ Atualizar ] -----------------------");
        System.out.println("[1] - [Int] Atualizar appId -> " + jogo.getAppid());
        System.out.println("[2] - [String] Atualizar nome -> " + jogo.getName());
        System.out.println("[3] - [String (AAAA-MM-DD)] Atualizar data de lançamento -> " + jogo.getReleaseDateString());
        System.out.println("[4] - [Boolean] Atualizar se está em inglês -> " + jogo.getEnglish());
        System.out.println("[5] - [String] Atualizar desenvolvedor -> " + jogo.getDeveloper());
        System.out.println("[6] - [String] Atualizar publisher -> " + jogo.getPublisher());
        System.out.println("[7] - [String] Atualizar plataformas -> " + jogo.getPlatforms());
        System.out.println("[8] - [Int] Atualizar idade requerida -> " + jogo.getRequiredAge());
        System.out.println("[9] - [List<String>] Atualizar categorias -> " + jogo.getCategories());
        System.out.println("[10] - [List<String>] Atualizar gêneros -> " + jogo.getGenres());
        System.out.println("[11] - [List<String>] Atualizar tags do SteamSpy -> " + jogo.getSteamspyTags());
        System.out.println("[12] - [Int] Atualizar conquistas -> " + jogo.getAchievements());
        System.out.println("[13] - [Int] Atualizar avaliações positivas -> " + jogo.getPositiveRatings());
        System.out.println("[14] - [Int] Atualizar avaliações negativas -> " + jogo.getNegativeRatings());
        System.out.println("[15] - [Int] Atualizar tempo médio de jogo -> " + jogo.getAveragePlaytime());
        System.out.println("[16] - [Int] Atualizar tempo mediano de jogo -> " + jogo.getMedianPlaytime());
        System.out.println("[17] - [String] Atualizar proprietários -> " + jogo.getOwners());
        System.out.println("[18] - [Float] Atualizar preço -> " + jogo.getPrice());
        System.out.println("\n[0] - Voltar ao menu principal");
        System.out.println("--------------------------------------------------------");
        System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
    }
    
    public static boolean writeGame(RandomAccessFile saida, SteamGame jogo){
        boolean resp = false;
        try {
            //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N

            //definir o id do registro
            int id = -2; //-2 não será gravado como um ID, a não ser que ocorra um erro.

            //mover ponteiro para início do arquivo
            saida.seek(0);

            //se for -1, indica que é um registro que está sendo gravado pela primeira vez. Caso contrário, indica um registro sendo atualizado par ao final do arquivo.
            int novo = jogo.getId();
            
            //se não houver registros e for um jogo novo (e não um jogo sendo ordenado)
            if (saida.length() == 0 && jogo.getId() == -1){
                //escrever o primeiro Id como 1
                id = 1;
                saida.writeInt(id);
                //System.out.println("Nenhum registro encontrado. Escrevendo primeiro Id = 1");
            }
            else{
                //identificar se o registro ja estava gravado e está sendo atualizado para o final do arquivo.
                if (novo == -1){
                    //ler id do ultimo registro adicinado e acrescentar 1
                    id = saida.readInt();
                    //System.out.println("Ultimo id registrado: " + id);
                    id++;
                }
            }
            
            //se for um jogo novo, atribuir um ID
            if (novo == -1){
                //gravar o id no objeto
                jogo.setId(id);
            }

            //mover ponteiro para o final
            saida.seek(saida.length());

            //gravar no buffer as informações do objeto (metadados e dados)
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(); DataOutputStream bufferData = new DataOutputStream(buffer)){

                //metadados
                bufferData.writeByte(0x00); //lápide para indicar que registro está ativo (0xFF indica que está inativo)
                bufferData.writeInt(jogo.measureSize());
                
                //dados
                bufferData.writeInt(jogo.getId());
                bufferData.writeInt(jogo.getAppid());
                bufferData.writeUTF(jogo.getName());
                bufferData.writeLong(jogo.getReleaseDateUnix());
                bufferData.writeBoolean(jogo.getEnglish());
                bufferData.writeUTF(jogo.getDeveloper());
                bufferData.writeUTF(jogo.getPublisher());
                bufferData.writeUTF(jogo.getPlatforms());
                bufferData.writeInt(jogo.getRequiredAge());

                //escrever lista de categorias
                bufferData.writeInt(jogo.getCategories().size());//indicar tamanho da lista
                for (String category : jogo.getCategories()) {
                    bufferData.writeUTF(category);//elementos da lista
                }

                //escrever lista de gêneros
                bufferData.writeInt(jogo.getGenres().size());
                for (String genre : jogo.getGenres()) {
                    bufferData.writeUTF(genre);
                }

                //ecrever lista de spytags
                bufferData.writeInt(jogo.getSteamspyTags().size());
                for (String tag : jogo.getSteamspyTags()) {
                    bufferData.writeUTF(tag);
                }

                bufferData.writeInt(jogo.getAchievements());
                bufferData.writeInt(jogo.getPositiveRatings());
                bufferData.writeInt(jogo.getNegativeRatings());
                bufferData.writeInt(jogo.getAveragePlaytime());
                bufferData.writeInt(jogo.getMedianPlaytime());
                bufferData.writeUTF(jogo.getOwners());
                bufferData.writeFloat(jogo.getPrice());

                //escrever no arquivo os dados do buffer
                saida.write(buffer.toByteArray());

                //sinalizar que o registro foi escrito com sucesso no arquivo
                resp = true;

                //se não for um registro sendo atualizado, então gravar o id dele para o inicio do arquivo.
                if (novo == -1){
                    //incrementar quantidade total de registros
                    totalGames++;
                    //atualizar o id do início dos registros
                    saida.seek(0);
                    saida.writeInt(id);
                }
            }

        } catch (IOException e) {
            System.out.println("[ERRO] -> Não foi possível escrever o registro no arquivo");
            System.out.println(e);
        }

        //retornar resposta se foi possível escrever o registro
        return resp;
    }

    public static SteamGame readGame (RandomAccessFile arquivo){
        SteamGame jogo =  new SteamGame();

        try{
            jogo.setId(arquivo.readInt());
            jogo.setAppid(arquivo.readInt());
            jogo.setName(arquivo.readUTF());
            jogo.setReleaseDate(arquivo.readLong());
            jogo.setEnglish(arquivo.readBoolean());
            jogo.setDeveloper(arquivo.readUTF());
            jogo.setPublisher(arquivo.readUTF());
            jogo.setPlatforms(arquivo.readUTF());
            jogo.setRequiredAge(arquivo.readInt());

            //quantidade de elementos na lista a seguir
            int i = arquivo.readInt();
            //criar lista
            ArrayList<String> categories = new ArrayList<>();
            while (i > 0){
                categories.add(arquivo.readUTF());
                i--;
            }
            jogo.setCategories(categories);
            
            //quantidade de elementos na lista a seguir
            i = arquivo.readInt();
            //criar lista
            ArrayList<String> genres = new ArrayList<>();
            while (i > 0){
                genres.add(arquivo.readUTF());
                i--;
            }
            jogo.setGenres(genres);
            
            //quantidade de elementos na lista a seguir
            i = arquivo.readInt();
            //criar lista
            ArrayList<String> spytags = new ArrayList<>();
            while (i > 0){
                spytags.add(arquivo.readUTF());
                i--;
            }
            jogo.setSteamspyTags(spytags);

            jogo.setAchievements(arquivo.readInt());
            jogo.setPositiveRatings(arquivo.readInt());
            jogo.setNegativeRatings(arquivo.readInt());
            jogo.setAveragePlaytime(arquivo.readInt());
            jogo.setMedianPlaytime(arquivo.readInt());

            jogo.setOwners(arquivo.readUTF());
            jogo.setPrice(arquivo.readFloat());

        } catch (IOException e){
            System.out.println("[ERRO] -> Não foi possível resgatar o registro no arquivo");
            System.out.println(e);
        }
;
        return jogo;
    }

    public static SteamGame searchGame(String valor, int tipo){
        //tipo 1 -> pesquisa por ID
        //tipo 2 -> pesquisa por appId
        //tipo 3 -> pesquisa por name

        //inicializar variáveis de pesquisa
        boolean achou = false;
        SteamGame jogo = new SteamGame();

        try (RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db", "r")){
            //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N

            //mover ponteiro para início do arquivo
            arquivo.seek(0);

            //pular ultimo id inserido
            arquivo.skipBytes(4);
            int atual = 2;

            while (arquivo.getFilePointer() < arquivo.length() && !achou){
                //mostrar barra de progresso
                progressBar(atual, (totalGames + 1),"[Search]");
                
                //ler se a lápide está ativa
                int lapide = arquivo.readUnsignedByte();
                if (lapide == 0xFF){
                    //ler e pular o tamanho do registro a seguir
                    arquivo.skipBytes(arquivo.readInt());
                }
                else{
                    arquivo.skipBytes(4); //ignorar o tamanho e começar a leitura do registro
                    jogo = readGame(arquivo);
                    switch (tipo) {
                        case 1 -> {
                            if (jogo.getId() == Integer.parseInt(valor)){
                                achou = true;
                                System.out.println("Registro com o ID encontrado!");
                            }
                        }
                        case 2 -> {
                            if (jogo.getAppid() == Integer.parseInt(valor)){
                                achou = true;
                                System.out.println("Registro com o appId encontrado!");
                            }
                        }
                        case 3 -> {
                            if (jogo.getName().toLowerCase().compareTo(valor.toLowerCase()) == 0){
                                achou = true;
                                System.out.println("Registro com o nome encontrado!");
                            }
                        }
                        default -> System.out.println("[ERRO] -> Opção de pesquisa inválida.");
                    }
                }
                atual++;
            }

            //imprimir informações do jogo encontrado
            if (achou){
                jogo.printAll();
                System.out.println("[Search] -> Registro encontrado!");
            }
            else{
                System.out.println("\n[Search] -> Não foi possível localizar o registro.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao pesquisar o registro!");
            System.out.println(e);
        }

        //devolver o jogo, com ou sem as informações
        return jogo;
    }

    public static boolean createGame(int tipo) {
        boolean resp = false;
        try (RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db", "rw")){

            SteamGame jogo = new SteamGame();

            if (tipo == 1){
                if (writeGame(arquivo, jogo)){
                    jogo.printAll();
                    resp = true;
                }
            }
            else{
                try {
                    //abrir scanner
                    Scanner leitor = new Scanner(System.in);
                    //appId
                    System.out.print("\n[Create] -> Digite o appId do jogo: ");
                    String valor = leitor.nextLine();
                    jogo.setAppid(Integer.parseInt(valor));
                    //name
                    System.out.print("\n[Create] -> Digite o nome do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setName(valor);
                    //release-date
                    System.out.print("\n[Create] -> Digite a data de lançamento do jogo (\"Ano\"-\"Mês\"-\"Dia\"): ");
                    valor = leitor.nextLine();
                    jogo.setReleaseDate(convertString_Unix(valor));
                    //english
                    System.out.print("\n[Create] -> Digite o booleano English: \"true\"/1 ou \"false\"/0: ");
                    valor = leitor.nextLine();
                    jogo.setEnglish(Boolean.valueOf(valor));
                    //developer
                    System.out.print("\n[Create] -> Digite o desenvolvedor do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setDeveloper(valor);
                    //publisher
                    System.out.print("\n[Create] -> Digite o distribuidor do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setPublisher(valor);
                    //platforms
                    System.out.print("\n[Create] -> Plataforma = \"windows\" (valor fixo) \n");
                    //required-age
                    System.out.print("\n[Create] -> Digite a idade necessária: ");
                    valor = leitor.nextLine();
                    if (Integer.parseInt(valor) < 0){
                        System.out.println("[Create] -> Valor inválido. Escrevendo 0...");
                        jogo.setRequiredAge(0);
                    }
                    else{
                        jogo.setRequiredAge(Integer.parseInt(valor));
                    }   //contador de elementos na lista

                    int num = 0;
                    //lista categories
                    System.out.print("\n[Create] -> Digite o número de categorias: ");
                    num = Integer.parseInt(leitor.nextLine());
                    ArrayList<String> categories = new ArrayList<>();
                    for (int i = 0; i < num; i++){
                        System.out.print("\n[Create] -> Digite a categoria [" + i + "]: ");
                        valor = leitor.nextLine();
                        categories.add(valor);
                    }   jogo.setCategories(categories);
                    //lista genres
                    System.out.print("\n[Create] -> Digite o número de gêneros: ");
                    num = Integer.parseInt(leitor.nextLine());
                    ArrayList<String> genres = new ArrayList<>();
                    for (int i = 0; i < num; i++){
                        System.out.print("\n[Create] -> Digite o gênero [" + i + "]: ");
                        valor = leitor.nextLine();
                        genres.add(valor);
                    }   jogo.setGenres(genres);
                    //lista spytags
                    System.out.print("\n[Create] -> Digite o número de spytags: ");
                    num = Integer.parseInt(leitor.nextLine());
                    ArrayList<String> spytags = new ArrayList<>();
                    for (int i = 0; i < num; i++){
                        System.out.print("\n[Create] -> Digite a spytag [" + i + "]: ");
                        valor = leitor.nextLine();
                        spytags.add(valor);
                    }   jogo.setSteamspyTags(spytags);
                    //achievements
                    System.out.print("\n[Create] -> Digite o número de conquistas do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setAchievements(Integer.parseInt(valor));
                    //positive-ratings
                    System.out.print("\n[Create] -> Digite o número de avaliações positivas do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setPositiveRatings(Integer.parseInt(valor));
                    //negative-ratings
                    System.out.print("\n[Create] -> Digite o número de avaliações negativas do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setNegativeRatings(Integer.parseInt(valor));
                    //avarage-playtime
                    System.out.print("\n[Create] -> Digite o tempo de jogo médio do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setAveragePlaytime(Integer.parseInt(valor));
                    //median-playtime
                    System.out.print("\n[Create] -> Digite o tempo de jogo mediano do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setMedianPlaytime(Integer.parseInt(valor));
                    //owners
                    System.out.print("\n[Create] -> Digite número aproximado de proprietários do jogo (\"min\"-\"max\"): ");
                    valor = leitor.nextLine();
                    jogo.setOwners(valor);
                    //preço
                    System.out.print("\n[Create] -> Digite o preço do jogo: ");
                    valor = leitor.nextLine();
                    jogo.setPrice(Float.parseFloat(valor));
                    //escrever jogo no arquivo
                    writeGame(arquivo, jogo);
                    jogo.printAll();
                    resp = true;
                } catch (InputMismatchException e){
                    System.out.println("[ERRO] -> Não foi possível ler o valor digitado");
                    System.out.println(e);
                }
            }
        } catch (IOException e) {
            System.out.println("[ERRO] -> Não foi possível criar o registro");
            System.out.println(e);
        }
        return resp;
    }

    public static boolean deleteGame(int delete_id){
        //inicializar variáveis de pesquisa/deletar
        boolean achou = false;
        boolean resp = false;

        try (RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db", "rw")){
            //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N

            //mover ponteiro para início do arquivo
            arquivo.seek(0);

            //pular ultimo id inserido
            arquivo.skipBytes(4);
            int atual = 2;

            System.out.println("[Delete] -> Procurando registro com o ID especificado...");

            while (arquivo.getFilePointer() < arquivo.length() && !achou){
                //mostrar barra de progresso
                progressBar(atual, (totalGames + 1),"[Search]");
                
                //ler se a lápide está ativa
                int lapide = arquivo.readUnsignedByte();
                if (lapide == 0xFF){
                    //ler e pular o tamanho do registro a seguir
                    arquivo.skipBytes(arquivo.readInt());
                }
                else{
                    arquivo.skipBytes(4); //ignorar o tamanho e começar a leitura do registro
                    SteamGame jogo = readGame(arquivo);
                    if (jogo.getId() == delete_id){
                        achou = true;
                        System.out.println("[Search] -> Registro com o ID encontrado!");
                        jogo.printAll();
                        System.out.println("[Delete] -> Tem certeza que deseja deletar esse registro? Digite \"CONFIRMAR\" para deletar, ou qualquer outra tecla para cancelar");
                        System.out.print("[Delete] -> Resposta: ");

                        try {
                            //inicializar o scanner para ler a confirmação
                            Scanner leitor = new Scanner(System.in);
                            String confirm = leitor.nextLine();
                            if (confirm.toLowerCase().compareTo("confirmar") == 0) {
                                System.out.println("[Delete] -> Removendo registro de ID [" + delete_id + "]");
                                
                                //ir para posição da lápide desse registro
                                arquivo.seek(arquivo.getFilePointer() - jogo.measureSize() - 5); //posição atual - tamanho do registro -4 bytes (para o incluir o tamanho do registro) - 1 byte (onde está a lápide)
                                
                                //atualizar lápide como "inativa"
                                arquivo.writeByte(0xFF);
                                
                                //indicar que o registro foi removido com sucesso
                                resp = true;
                                
                                //decrementar numero de jogos ativos na base de dados
                                totalGames--;
                                
                                //incrementar numero de jogos inativos na base de dados
                                totalDeleted++;
                            } else {
                                System.out.println("[Delete] -> Remoção cancelada.");
                            }
                        }catch (InputMismatchException e){
                            System.out.println("[ERRO] -> Não foi possível ler o valor digitado");
                            System.out.println(e);
                        }
                    }

                }

                atual++;
    
            }
            if (!achou){
                System.out.println("\n[Delete] -> Não foi possível localizar a ser excluído.");
            }
        }
        catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível realizar a exclusão do registro.");
            System.out.println(e);
        }

        return resp;
    }
    
    public static boolean updateGame(int update_id) {
        //inicializar variáveis de pesquisa/deletar
        boolean achou = false;
        boolean atualizado = false;

        try (RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db", "rw")){
            //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N

            //mover ponteiro para início do arquivo
            arquivo.seek(0);

            //pular ultimo id inserido
            arquivo.skipBytes(4);
            int atual = 2;

            System.out.println("[Update] -> Procurando registro com o ID especificado...");

            while (arquivo.getFilePointer() < arquivo.length() && !achou){
                //mostrar barra de progresso
                progressBar(atual, (totalGames + 1),"[Search]");
                
                //gravar a posição do registro atual
                long pos_registro = arquivo.getFilePointer();

                //ler se a lápide está ativa
                int lapide = arquivo.readUnsignedByte();
                if (lapide == 0xFF){
                    //ler e pular o tamanho do registro a seguir
                    arquivo.skipBytes(arquivo.readInt());
                }
                else{
                    int old_tamanho = arquivo.readInt(); //ler o tamanho do registro para tomar a decisão correta no momento da atualização
                    SteamGame jogo = readGame(arquivo);
                    boolean resp = false;
                    if (jogo.getId() == update_id){
                        achou = true; //indicar que o registro foi encontrado
                        System.out.println("[Search] -> Registro com o ID encontrado!");
                        jogo.printAll();

                        //variável de controle do loop
                        boolean stop = false;
                        try {
                            //inicializar o scanner para ler a opção
                            Scanner leitor = new Scanner(System.in);

                            //loop menu de atualização
                            while (!stop){
                                //exibir interface de opções para atualizar
                                UI_update(jogo);

                                try {
                                    int option = leitor.nextInt();
                                    switch (option) {
                                        case 1 -> {
                                            System.out.println("[Update] -> Valor atual do appId: " + jogo.getAppid());
                                            System.out.print("[Update] -> Digite o novo appId: ");
                                            int appId = leitor.nextInt();
                                            jogo.setAppid(appId);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 2 -> {
                                            System.out.println("[Update] -> Valor atual do nome: " + jogo.getName());
                                            System.out.print("[Update] -> Digite o novo nome: ");
                                            String nome = leitor.next();
                                            jogo.setName(nome);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 3 -> {
                                            System.out.println("[Update] -> Valor atual da data de lançamento: " + jogo.getReleaseDateString());
                                            System.out.print("[Update] -> Digite a nova data de lançamento (AAAA-MM-DD): ");
                                            String data = leitor.next();
                                            //converter data e atualizar no registro
                                            jogo.setReleaseDate(convertString_Unix(data));
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 4 -> {
                                            System.out.println("[Update] -> Valor atual se está em inglês: " + jogo.getEnglish());
                                            System.out.print("[Update] -> Está em inglês? (true (1)/false (0) ): ");
                                            boolean emIngles = leitor.nextBoolean();
                                            jogo.setEnglish(emIngles);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 5 -> {
                                            System.out.println("[Update] -> Valor atual do desenvolvedor: " + jogo.getDeveloper());
                                            System.out.print("[Update] -> Digite o novo desenvolvedor: ");
                                            String desenvolvedor = leitor.next();
                                            jogo.setDeveloper(desenvolvedor);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 6 -> {
                                            System.out.println("[Update] -> Valor atual do distribuidor: " + jogo.getPublisher());
                                            System.out.print("[Update] -> Digite o novo disitribuidor: ");
                                            String publisher = leitor.next();
                                            jogo.setPublisher(publisher);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 7 -> {
                                            System.out.println("[Update] -> Valor atual das plataformas: " + jogo.getPlatforms());
                                            System.out.print("[Update] -> Digite as novas plataformas (tamanho fixo de " + jogo.getPlatformsLenght() + " caracteres): ");
                                            String plataformas = leitor.next();
                                            if (jogo.setPlatforms(plataformas))
                                                resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 8 -> {
                                            System.out.println("[Update] -> Valor atual da idade requerida: " + jogo.getRequiredAge());
                                            System.out.print("[Update] -> Digite a nova idade requerida: ");
                                            int idade = leitor.nextInt();
                                            jogo.setRequiredAge(idade);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 9 -> {
                                            //obter numero de elementos na lista
                                            int max = jogo.getCategories().size();

                                            //inicializar e copiar esses elementos para uma nova lista
                                            ArrayList<String> categories_list = new ArrayList<>();
                                            categories_list.addAll(jogo.getCategories());

                                            //atualizar, ou não, cada elemento da lista
                                            System.out.println("[Update] -> Exibindo categorias atuais");

                                            //percorrer a lista inteira
                                            for (int i = 0; i < max; i++) {
                                                System.out.println("[Update] -> Categoria [" + i +"] atual: " + categories_list.get(i));
                                                System.out.print("[Update] -> Digite [1] para alterar ou [0] para a próxima categoria: ");
                                                int alterar = leitor.nextInt();
                                                
                                                //confirmar a atualização
                                                if (alterar == 1) {
                                                    System.out.print("[Update] -> Digite o novo valor: ");
                                                    String valor = leitor.next();

                                                    //atualizar o elemento em questão
                                                    categories_list.set(i, valor);
                                                }
                                            }
                                            //atualizar lista do objeto
                                            jogo.setCategories(categories_list);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 10 -> {
                                            //obter o tamanho da lista e criar uma cópia
                                            int max = jogo.getGenres().size();
                                            ArrayList<String> generos_list = new ArrayList<>();
                                            generos_list.addAll(jogo.getGenres());
                                            System.out.println("[Update] -> Exibindo gêneros atuais");

                                            //atualizar, ou não, individualmente os elementos da lista
                                            for (int i = 0; i < max; i++) {
                                                System.out.println("[Update] -> Gênero [" + i +"] atual: " + generos_list.get(i));
                                                System.out.print("[Update] -> Digite [1] para alterar ou [0] para o próximo gênero: ");
                                                int alterar = leitor.nextInt();

                                                //confirmar a atualização
                                                if (alterar == 1) {
                                                    System.out.print("[Update] -> Digite o novo valor: ");
                                                    String valor = leitor.next();
                                                    //atualizar o elemento em questão
                                                    generos_list.set(i, valor);
                                                }
                                            }
                                            //atualizar lista do objeto
                                            jogo.setGenres(generos_list);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 11 -> {
                                            //obter tamanho da lista e criar uma cópia
                                            int max = jogo.getSteamspyTags().size();
                                            ArrayList<String> tags_list = new ArrayList<>();
                                            tags_list.addAll(jogo.getSteamspyTags());

                                            System.out.println("[Update] -> Exibindo tags atuais");
                                            //atualizar, ou não, individualmente os elementos da lista
                                            for (int i = 0; i < max; i++) {
                                                System.out.println("[Update] -> Tag [" + i +"] atual: " + tags_list.get(i));
                                                System.out.print("[Update] -> Digite [1] para alterar ou [0] para a próxima spytag: ");
                                                int alterar = leitor.nextInt();

                                                //confirmar atualização
                                                if (alterar == 1) {
                                                    System.out.print("[Update] -> Digite o novo valor: ");
                                                    String valor = leitor.next();
                                                    //atualizar o elemento em questão
                                                    tags_list.set(i, valor);
                                                }
                                            }
                                            //atualizar lista do objeto
                                            jogo.setSteamspyTags(tags_list);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 12 -> {
                                            System.out.println("[Update] -> Valor atual de conquistas: " + jogo.getAchievements());
                                            System.out.print("[Update] -> Digite o novo número de conquistas: ");
                                            int conquistas = leitor.nextInt();
                                            jogo.setAchievements(conquistas);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 13 -> {
                                            System.out.println("[Update] -> Valor atual de avaliações positivas: " + jogo.getPositiveRatings());
                                            System.out.print("[Update] -> Digite o novo número de avaliações positivas: ");
                                            int avaliacoesPositivas = leitor.nextInt();
                                            jogo.setPositiveRatings(avaliacoesPositivas);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 14 -> {
                                            System.out.println("[Update] -> Valor atual de avaliações negativas: " + jogo.getNegativeRatings());
                                            System.out.print("[Update] -> Digite o novo número de avaliações negativas: ");
                                            int avaliacoesNegativas = leitor.nextInt();
                                            jogo.setNegativeRatings(avaliacoesNegativas);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 15 -> {
                                            System.out.println("[Update] -> Valor atual do tempo médio de jogo: " + jogo.getAveragePlaytime());
                                            System.out.print("[Update] -> Digite o novo tempo médio de jogo: ");
                                            int tempoMedio = leitor.nextInt();
                                            jogo.setAveragePlaytime(tempoMedio);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 16 -> {
                                            System.out.println("[Update] -> Valor atual do tempo mediano de jogo: " + jogo.getMedianPlaytime());
                                            System.out.print("[Update] -> Digite o novo tempo mediano de jogo: ");
                                            int tempoMediano = leitor.nextInt();
                                            jogo.setMedianPlaytime(tempoMediano);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 17 -> {
                                            System.out.println("[Update] -> Valor atual dos proprietários: " + jogo.getOwners());
                                            System.out.print("[Update] -> Digite o novo número de proprietários (\"min\"-\"max\"): ");
                                            String proprietarios = leitor.next();
                                            jogo.setOwners(proprietarios);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 18 -> {
                                            System.out.println("[Update] -> Valor atual do preço: " + jogo.getPrice());
                                            System.out.print("[Update] -> Digite o novo preço: ");
                                            float preco = leitor.nextFloat();
                                            jogo.setPrice(preco);
                                            resp = true; //indicar que o registro foi atualizado
                                        }
                                        case 0 -> {
                                            System.out.println("[Update] -> Voltando ao menu inicial...");
                                            stop = true; //parar loop
                                        }
                                        default -> System.out.println("[Update] -> Opção inválida!");
                                    }

                                    if (resp){
                                        try {
                                            System.out.println("[Update] -> Atualizando o registro no arquivo...");

                                            //mover o ponteiro para onde o registro estava antes de ser atualizado
                                            arquivo.seek(pos_registro);

                                            //identificar se o registro atualizado está maior (em BYTES) que o registro antigo

                                            if (jogo.measureSize() > old_tamanho){
                                                //mudar a lápide para indicar que o antigo registro deve ser desconsiderado
                                                arquivo.writeByte(0xFF); //lapide inativa

                                                //gravar o registro atualizado no final do arquivo
                                                if (writeGame(arquivo, jogo)){
                                                    atualizado = true;
                                                    System.out.println("[Update] -> Registro atualizado com sucesso");
                                                }
                                            } else{ //se o registro ter o tamanho igual ou menor, atualizar na mesma posição
                                                try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(); DataOutputStream bufferData = new DataOutputStream(buffer)){

                                                    //metadados
                                                    bufferData.writeByte(0x00); //lápide para indicar que registro está ativo (0xFF indica que está inativo)

                                                    //atualizar número de registros inativos
                                                    totalDeleted++;
                                                    
                                                    bufferData.writeInt(jogo.measureSize());
                                                    
                                                    //dados
                                                    bufferData.writeInt(jogo.getId());
                                                    bufferData.writeInt(jogo.getAppid());
                                                    bufferData.writeUTF(jogo.getName());
                                                    bufferData.writeLong(jogo.getReleaseDateUnix());
                                                    bufferData.writeBoolean(jogo.getEnglish());
                                                    bufferData.writeUTF(jogo.getDeveloper());
                                                    bufferData.writeUTF(jogo.getPublisher());
                                                    bufferData.writeUTF(jogo.getPlatforms());
                                                    bufferData.writeInt(jogo.getRequiredAge());
                                    
                                                    //escrever lista de categorias
                                                    bufferData.writeInt(jogo.getCategories().size());//indicar tamanho da lista
                                                    for (String category : jogo.getCategories()) {
                                                        bufferData.writeUTF(category);//elementos da lista
                                                    }
                                    
                                                    //escrever lista de gêneros
                                                    bufferData.writeInt(jogo.getGenres().size());
                                                    for (String genre : jogo.getGenres()) {
                                                        bufferData.writeUTF(genre);
                                                    }
                                    
                                                    //ecrever lista de spytags
                                                    bufferData.writeInt(jogo.getSteamspyTags().size());
                                                    for (String tag : jogo.getSteamspyTags()) {
                                                        bufferData.writeUTF(tag);
                                                    }
                                    
                                                    bufferData.writeInt(jogo.getAchievements());
                                                    bufferData.writeInt(jogo.getPositiveRatings());
                                                    bufferData.writeInt(jogo.getNegativeRatings());
                                                    bufferData.writeInt(jogo.getAveragePlaytime());
                                                    bufferData.writeInt(jogo.getMedianPlaytime());
                                                    bufferData.writeUTF(jogo.getOwners());
                                                    bufferData.writeFloat(jogo.getPrice());
                                    
                                                    //escrever no arquivo os dados do buffer
                                                    arquivo.write(buffer.toByteArray());

                                                }
                                            }
                                            
                                        } catch (Exception e) {
                                            System.out.println("[ERRO] -> Não foi possível atualizar o registro do arquivo [" + e + "]");
                                        }
                                        resp = false;
                                    }

                                } catch (InputMismatchException e){
                                    System.out.println("[ERRO] -> Valor digitado inválido para o atributo [" + e + "]");
                                }
                            }
                        }catch (InputMismatchException e){
                            System.out.println("[ERRO] -> Não foi possível ler o valor digitado");
                            System.out.println(e);
                        }
                    }

                }

                atual++;
    
            }
            if (!achou){
                System.out.println("\n[Update] -> Não foi possível localizar a ser atualizado.");
            }
        }
        catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível realizar a atualização do registro.");
            System.out.println(e);
        }

        return atualizado;
    }
    
    public static long convertString_Unix(String valor){
        long timestamp;
        String[] calendario = valor.split("-");

        //formatar no tipo LocalDate
        LocalDate data = LocalDate.of(Integer.parseInt(calendario[0]), Integer.parseInt(calendario[1]), Integer.parseInt(calendario[2]));
        
        //converter para Unix timestamp
        timestamp = data.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        return timestamp;
    }

    public static void csvExtractAll(){
        System.out.println("[CsvExtract] -> Extraindo todos os registros do arquivo csv. Isso deve demorar um pouco...");
        csvExtractNum(27075);
    }

    public static void DEBUG_csvExtractAll(){
        DEBUG_csvExtractNum(27077);
    }

    public static void progressBar(int atual, int total, String tipo){
        //excluir registros inativos
        total -= totalDeleted;

        //definir propoção/tamanho da barra
        int ratio = 50;

        //calcular o progresso na proproção
        float progress = (float)(((double)atual/total) * ratio); // (total / total) = 1 * proproção = proporção

        StringBuilder barra = new StringBuilder(tipo + " -> ["); //criar barra

        //preencher barra de acordo com o progresso
        for (int i = 0; i < ratio; i++){
            if (i < progress){
                barra.append(":");
            }
            else{
                barra.append(" ");
            }
        }
        barra.append("]");

        //adicionar porcentagem
        barra.append("   [").append(String.format("%.1f", ((progress/ratio)*100))).append("%]");

        //exibir registro atual
        barra.append("   Registro: ").append(atual - 1).append(" de ").append(total - 1).append("\t");

        //imprimir a barra
        System.out.print("\r" + barra.toString());
    }

    public static int countGames(RandomAccessFile arquivo){
        //método para contabilizar todos os jogos no arquivo de registros
        int totalActive = 0;
        int totalInactive = 0;

        System.out.println("Iniciando contagem de registros... Por favor, aguarde.");
        try {                
            //mover ponteiro para início do arquivo
            arquivo.seek(0);

            //pular ultimo id inserido
            arquivo.skipBytes(4);

            while (arquivo.getFilePointer() < arquivo.length()){
                //ler se a lápide está ativa
                int lapide = arquivo.readUnsignedByte();
                if (lapide != 0xFF){
                    //contabilizar registro ativo
                    totalActive++;
                }
                else{
                    //contabilizar registro inativo (deletado)
                    totalInactive++;
                }

                //pular o tamanho do registro a seguir
                int num = arquivo.readInt();
                arquivo.skipBytes(num);

                //mostrar número de registros válidos encontrados
                System.out.print("\r" + "[INFO] -> Lendo registros... [" + totalActive + "]");
            }
            System.out.println("\n[INFO] -> Contagem finalizada.");
        } catch (IOException e) {
            System.out.println("[ERRO] -> Não foi possível contar o número de registros");
            System.out.println(e);
        }

        //atualizar variáveis globais da classe
        totalGames = totalActive;
        totalDeleted = totalInactive;

        //retornar o número de registros ativos
        return totalActive;
    }

    public static void csvExtractNum(int max){
        //resetar número de registros na base de dados
        totalGames = 0;
        totalDeleted = 0;

        //ajustar o número de registros para +2 (ignorar primeira linha do csv);
        max += 2;
        try (RandomAccessFile entrada = new RandomAccessFile("./CSV_Input/steamgames.csv","r");){ //obter os objetos da base de dados CSV (steamgames.csv)
            
            //excluir o arquivo existente, se existir
            File file = new File("./db_Output/gamesDB.db");
            if (file.exists()) {
                file.delete();
            }
            
            //definir o local de saída com os dados extraídos
            try (RandomAccessFile saida = new RandomAccessFile("./db_Output/gamesDB.db","rw")) {
                //ler a primeira linha, mas ela será desconsiderada assim que entrar no loop de leitura
                String linha = entrada.readLine();
                
                //contador de linhas lidas
                int contador = 2;
                
                System.out.println("[CsvExtract] -> Criando registros...");
                
                //cronometrar tempo
                long tempo_inicio = System.currentTimeMillis();
                
                //iniciando o loop de leitura completa do arquivo CSV
                while (entrada.getFilePointer() < entrada.length() && contador < max){
                    //imprimir barra de progresso
                    progressBar(contador, max - 1,"[CsvExtract]");
                    
                    //obter liha atual do CSV
                    linha = entrada.readLine();
                    
                    //incrementar contador de linhas lidas
                    contador++;
                    
                    //se a linha for válida, continuar a leitura
                    if (linha != null){
                        //criar objeto do tipo SteamGame, para registrar o objeto inteiro como 1 registro
                        SteamGame jogo = new SteamGame();
                        
                        //filtrar e separar os conteúdos do CSV, dividos por ','
                        String[] content = linha.split(",");
                        
                        //gravar no obejto
                        jogo.setAppid(Integer.parseInt(content[0]));
                        
                        int pos_vet = 1; //posição no vetor
                        int pos_val = 1; //representa a posição do elemento (appId,name,data etc) correspondente no csv (1 até 18)
                        
                        while (pos_val < 6){
                            //detectar se o conteúdo tem aspas no ínicio (pois indica que o conteúdo está entre aspas)
                            if ((pos_val !=2 && pos_val != 3) && content[pos_vet].startsWith("\"")){
                                StringBuilder elemento = new StringBuilder();
                                int offset = 0;
                                while(!(content[pos_vet + offset].endsWith("\""))){
                                    elemento.append(content[pos_vet + offset]);
                                    elemento.append(",");
                                    offset++;
                                }
                                elemento.append(content[pos_vet + offset]);
                                //adicionar o offset das vírgulas no pos_vet
                                pos_vet += offset;
                                
                                //substituir na string
                                content[pos_vet] = elemento.toString();
                            }
                            switch (pos_val) {
                                case 1 -> {
                                    jogo.setName(content[pos_vet]);
                                }
                                case 2 -> {
                                    jogo.setReleaseDate(convertString_Unix(content[pos_vet]));
                                }
                                case 3 -> {
                                    if (content[pos_vet].startsWith("1")){
                                        jogo.setEnglish(true);
                                    }
                                    else{
                                        jogo.setEnglish(false);
                                    }
                                }
                                case 4 -> {
                                    jogo.setDeveloper(content[pos_vet]);
                                }
                                case 5 -> {
                                    jogo.setPublisher(content[pos_vet]);
                                }
                                default -> System.out.println("ERRO! pos_val INVÁLIDA ou não acessível código = " + pos_val);
                            }
                            pos_vet++;
                            pos_val++;
                        }
                        
                        //agora pos_vet é constante e será apenas incrmentado 1. atual = 6
                        if (content[pos_vet].length() > 7){ //string de tamanho fixo
                            String process = content[pos_vet].substring(0,content[pos_vet].indexOf(";"));
                            jogo.setPlatforms(process);
                        }
                        else{
                            jogo.setPlatforms("windows");
                        }
                        
                        //pos_vet = 7
                        jogo.setRequiredAge(Integer.parseInt(content[pos_vet + 1]));
                        
                        String[] categories = content[pos_vet + 2].split(";");
                        ArrayList<String> categoriesList = new ArrayList<>();
                        
                        if (categories.length > 1){
                            categoriesList.addAll(Arrays.asList(categories));
                        }
                        else{
                            categoriesList.add(categories[0]);
                        }
                        jogo.setCategories(categoriesList);
                        
                        String[] genres = content[pos_vet + 3].split(";");
                        ArrayList<String> genreList = new ArrayList<>();
                        
                        if (genres.length > 1){
                            genreList.addAll(Arrays.asList(genres));
                        }
                        else{
                            genreList.add(genres[0]);
                        }
                        jogo.setGenres(genreList);
                        
                        String[] spytag = content[pos_vet + 4].split(";");
                        ArrayList<String> spytagList = new ArrayList<>();
                        
                        if (spytag.length > 1){
                            spytagList.addAll(Arrays.asList(spytag));
                        }
                        else{
                            spytagList.add(spytag[0]);
                        }
                        jogo.setSteamspyTags(spytagList);
                        
                        jogo.setAchievements(Integer.parseInt(content[pos_vet + 5]));
                        
                        jogo.setPositiveRatings(Integer.parseInt(content[pos_vet + 6]));
                        
                        jogo.setNegativeRatings(Integer.parseInt(content[pos_vet + 7]));
                        
                        jogo.setAveragePlaytime(Integer.parseInt(content[pos_vet + 8]));
                        
                        jogo.setMedianPlaytime(Integer.parseInt(content[pos_vet + 9]));
                        
                        jogo.setOwners(content[pos_vet + 10]);
                        
                        jogo.setPrice(Float.parseFloat(content[pos_vet + 11]));
                        
                        //gravar as informações do objeto no arquivo como um registro
                        writeGame(saida, jogo);
                    }
                }
                
                //cronometrar tempo
                long tempo_fim = System.currentTimeMillis();
                long tempo = tempo_fim - tempo_inicio; //tempo em segundos
                
                System.out.println("\n[CsvExtract] -> Operação concluída com sucesso.     Tempo decorrido: " + tempo/1000.0 + "s     Total de registros: " + (contador - 2) + "     [" + String.format("%.1f",contador/(tempo/1000.0)) + " registros/s] \n");
                entrada.close();
                saida.close();
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Ocorreu um erro durante o processamento do arquivo :(");
            System.out.println(e);
        }
    }

    public static void DEBUG_csvExtractNum(int max){
        max += 2;
        try (RandomAccessFile entrada = new RandomAccessFile("./CSV_Input/steamgames.csv","r")){ //obter os objetos da base de dados CSV (steamgames.csv)

            //Excluir o arquivo existente, se necessário
            File file = new File("./db_Output/gamesDB.db");
            if (file.exists()) {
                file.delete();
            }

            //definir o local de saída com os dados extraídos
            try (RandomAccessFile saida = new RandomAccessFile("./db_Output/gamesDB.db","rw")) {
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
                        writeGame(saida, jogo);
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

    public static void externalSort(int caminho_num, int heapSize) throws IOException{
        try (RandomAccessFile entrada = new RandomAccessFile("./db_Output/gamesDB.db", "r")) {
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
                caminhos[i] = new RandomAccessFile("./db_Sort/caminho_" + i + ".db","rw");
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
                    heap.add(new HeapGame(readGame(entrada),0));
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
                    writeGame(caminhos[caminho_atual], jogo);

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
                            heap.add(new HeapGame(readGame(entrada),peso));
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
                    writeGame(caminhos[caminho_atual], jogo);
                }
                else{
                    System.out.println("escreveu caminho atual");
                    ref_peso = heapgame.getPeso();
                    writeGame(caminhos[caminho_atual], jogo);
                }
            }


            System.out.println("Caminhos carregados");

            //readIntercalado(caminhos)
            
            
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
                    intercalados[i] = new RandomAccessFile("./db_Sort/caminho_" + (i + caminho_num) + ".db","rw");
                    intercalados[i].writeInt(lastID); //transcrever o ultimo ID inserido para a nova base de dados
                }

                //intercalar até sobrar 1 arquivo de caminho (que será o arquivo ordenado)
                while (caminhos_validos > 1){
                    //intercalar os arquivos dos caminhos
                    intercalados = intercalar(caminhos, intercalados, caminhos_validos);

                    //resetar arquivos de caminhos anteriores para reaproveitar os arquivos
                    for (RandomAccessFile i : caminhos){
                        i.setLength(4); //4 para preservar o ultimo id inserido na base de dados
                        i.skipBytes(4);
                    }

                    //intercalar novamente, agora os novos arquivos intercalados, aproveitando os arquivos anteriores de caminhos
                    caminhos = intercalar(intercalados, caminhos, caminhos_validos);

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
                        File deletar = new File("./db_Sort/caminho_" + (k + caminho_num) + ".db");
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
                    File deletar = new File("./db_Sort/caminho_" + j + ".db");
                    //verificação dupla
                    if (deletar.exists()) {
                        deletar.delete();
                    }
                }
            }

            //imprimir em um arquivo txt, o ID e o Nome, sequencialmente do arquivo gerado (para verificar a ordenação)
            printDataBase(caminhos[0]);

            //fechar arquivo ordenado
            caminhos[0].close();
            
            //terminou a ordenação
            System.out.println("[Sort] -> Base de dados ordenada com sucesso");

            //fechar arquivo de entrada (antiga base de dados)
            entrada.close();

            //fazer backup e substituir pelo arquivo ordenado
            backupDatabase();
            
        } catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível realizar a ordenação dos registros.");
        }
    }

    public static void backupDatabase(){
        try {
            //criar arquivo de backup
            File dbOutputFile = new File("./db_Output/gamesDB.db");
            File dbBackupFile = new File("./db_Backup/gamesDB_backup.db");

            System.out.println("[Backup] -> Iniciando backup...");
            //fazer uma copia do banco de dados atual no arquivo de backup
            Files.copy(dbOutputFile.toPath(), dbBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[Backup] -> Um backup do banco de dados foi criado com sucesso");

            //substituir gamesDB.db por caminho_0.db
            File dbSortFile = new File("./db_Sort/caminho_0.db");
            if (dbSortFile.exists()) {
                //deletar a base de dados anterior
                Files.delete(dbOutputFile.toPath());

                System.out.println("[Backup] -> Substituindo arquivo do banco de dados...");
                //renomear caminho_0 para gamesDB (copiar, mudando de nome e deletar antigo.)
                Files.copy(Paths.get("./db_Sort/caminho_0.db"), Paths.get("./db_Output/gamesDB.db"), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(Paths.get("./db_Sort/caminho_0.db"));

                System.out.println("[Backup] -> Base de dados substituída com sucesso");

                //contar jogos na nova base de dados
                try (RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db","r")) {
                    //contar a quantidade de jogos ativos e inativos (deletados) na base de dados
                    countGames(arquivo);
                } catch (IOException e) {
                    System.out.println("[ERRO] -> Não foi possível abrir o arquivo da base de dados");
                    System.out.println(e);
                }

            } else {
                System.out.println("[ERRO] -> Não foi possível encontrar uma base de dados ordenada");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    //imprime os atributos ID e Nome de todos os elementos, ativos e inativos, de uma base de dados
    public static void printDataBase(RandomAccessFile arquivo){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./db_Output/printDataBase.txt"))) {    
            arquivo.seek(0);
            arquivo.skipBytes(4); //pular ultimo id inserido
            while (arquivo.getFilePointer() < arquivo.length()){
                arquivo.skipBytes(5); //pular lápides e tamanhos dos registros
                SteamGame jogo = readGame(arquivo);
                writer.write("[ID:" + jogo.getId() + "]\n[" + jogo.getName() + "]\n");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static RandomAccessFile[] intercalar(RandomAccessFile[] caminhos, RandomAccessFile[] intercalados, int caminhos_validos) throws IOException{
        //vetor de jogos para armazenar o próximo jogo de cada caminho
        SteamGame jogos_intercalados[] = new SteamGame[caminhos_validos];

        //obter primeiro jogo de cada caminho
        for (int i = 0; i < caminhos_validos; i++){
            //posicionar ponteiros no inicio do registro de cada um dos caminhos
            if (caminhos[i].length() > 4){ //verificar se o caminho possui um registro
                caminhos[i].seek(9); //pular ultimo id (4) + lapide (1) + tamanho do registro (4)
                
                //ler primeiro jogo de cada caminho
                jogos_intercalados[i] = readGame(caminhos[i]);
            }
            else{
                jogos_intercalados[i] = null;
            }
        }
        
        //controle do índice do arquivo intercalado
        int arquivo_intercalado_atual = 0;

        //variável para controlar o último jogo inserido no arquivo intercalado
        SteamGame ultimoInserido = null;

        System.out.println("Iniciando intercalação");

        //iniciar a intercalação de caminhos
        while (!FilesEmpty(caminhos_validos, caminhos) || !gamesEmpty(jogos_intercalados)) {
            System.out.println("entrou while");
            //encontrar o menor jogo entre os jogos dos caminhos
            int menorIndice = -1;
            SteamGame menorJogo = null;

            //percorrer todos os N arquivos validos
            for (int i = 0; i < caminhos_validos; i++) {
                if (jogos_intercalados[i] != null && (menorJogo == null || jogos_intercalados[i].getName().compareTo(menorJogo.getName()) < 0)) {
                    menorJogo = jogos_intercalados[i];
                    menorIndice = i;
                }
            }

            if (menorJogo != null)
                System.out.println("menor -> " + menorJogo.getName());

            //se houver um jogo a ser escrito no arquivo intercalado
            if (menorJogo != null) {
                //verifica se o jogo atual é menor que o último inserido
                if (ultimoInserido != null && menorJogo.getName().compareTo(ultimoInserido.getName()) < 0) {
                    //se o último inserido for maior, muda o arquivo intercalado
                    System.out.println("mudando arquivo");
                    if (caminhos_validos != 1){
                        arquivo_intercalado_atual = (arquivo_intercalado_atual + 1) % caminhos_validos;
                    }            
                    System.out.println("intercalando no arquivo " + arquivo_intercalado_atual);        
                }

                //escrever o menor jogo no arquivo intercalado atual
                writeGame(intercalados[arquivo_intercalado_atual],menorJogo);
                System.out.println("Encreveu " + menorJogo.getName());

                //atualizar o último jogo inserido
                ultimoInserido = menorJogo;

                //mover o ponteiro do arquivo de onde o jogo foi retirado
                if (caminhos[menorIndice].getFilePointer() < caminhos[menorIndice].length()) {
                    //mover ponteiro para o próximo registro
                    caminhos[menorIndice].skipBytes(5);
                    System.out.println("moveu ponteiro do arq " + menorIndice);
                    jogos_intercalados[menorIndice] = readGame(caminhos[menorIndice]);
                } else {
                    System.out.println("terminou leitura no arquivo " + menorIndice);
                    //se terminar a leitura, então colocar null
                    jogos_intercalados[menorIndice] = null;
                }
            }
        }


        
        System.out.println("Intercalados com sucesso!");
        //readIntercalado(intercalados);
        //System.out.println("\n\nleitura feita");
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

    public static void readIntercalado(RandomAccessFile[] arquivo) throws IOException{
        //posicionar ponteiro no inico
        for (RandomAccessFile i : arquivo){
            System.out.println("\n\n\nIMPRIMINDO NOVO ARQUIVO:");
            i.seek(4);
            while (i.getFilePointer() < i.length()){
                i.skipBytes(5);
                SteamGame jogo = readGame(i);
                System.out.println("Jogo: " + jogo.getName());
            }
        }
    }
}
