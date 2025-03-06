import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class DataBase {
    public static void main(String[] args) {
        //exibir interface de menu com opções
        UI_menu();
        Scanner leitor = new Scanner(System.in);
        int choice = -1;

        while (choice != 0){ // 0 = encerrar programa
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
                        if (tipo != 0){
                            System.out.print("\n[Search] -> Digite o valor do atributo que deseja procurar nos registros: ");
                            leitor.nextLine(); //descartar caractere \n
                            String valor = leitor.nextLine();
                            searchGame(valor, tipo);
                            System.out.println("[Search] -> Pesquisa finalizada. Deseja realizar outra pesquisa?");
                            System.out.println("                [1] - SIM                   [0] - NÃO");
                            System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
                            tipo = leitor.nextInt();
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
        }

        //fechar scanner
        leitor.close();

        System.out.println("---------[ Programa Encerrado ]---------");
    }

    public static void UI_menu(){
        System.out.println("\n----------------------- [ MENU ] -----------------------");
        System.out.println("[1] - Criar Arquivo com todos os registros do CSV");
        System.out.println("[2] - Criar Arquivo com um número N de registros do CSV (primeiro -> último)");
        System.out.println("[3] - Procurar por um atributo nos registros [ID, appID, Nome]");
        System.out.println("[4] - Criar um registro no arquivo de banco de dados");
        System.out.println("[5] - Remover um registro no arquivo de banco de dados [por ID]");
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

    public static void writeGame(RandomAccessFile saida, SteamGame jogo){
        try {
            //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N

            //definir o id do registro
            int id;

            //mover ponteiro para início do arquivo
            saida.seek(0);

            //se não houver registros
            if (saida.length() == 0){
                //escrever o primeiro Id como 1
                id = 1;
                saida.writeInt(id);
                //System.out.println("Nenhum registro encontrado. Escrevendo primeiro Id = 1");
            }
            else{
                //ler id do ultimo registro adicinado e acrescentar 1
                id = saida.readInt();
                //System.out.println("Ultimo id registrado: " + id);
                id++;
            }

            //gravar o id no objeto
            jogo.setId(id);

            //mover ponteiro para o final
            saida.seek(saida.length());

            //buffer de saída para escrever no arquivo
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream bufferData = new DataOutputStream(buffer);


            //gravar no buffer as informações do objeto (metadados e dados)
            //metadados
            bufferData.writeByte(0x00); //lápide para indicar que registro está ativo (0xFF indica que está inativo)
            bufferData.writeInt(jogo.measureSize());
            
            //debug
            //System.out.println("Tamanho do registro : " + jogo.measureSize());
            
            //dados
            bufferData.writeInt(jogo.getId());
            bufferData.writeInt(jogo.getAppid());
            bufferData.writeUTF(jogo.getName());
            bufferData.writeLong(jogo.getReleaseDate());
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

            //atualizar o id do início dos registros
            saida.seek(0);
            saida.writeInt(id);

        } catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível escrever o registro no arquivo");
            System.out.println(e);
        }
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

        } catch (Exception e){
            System.out.println(e);
        }

        return jogo;
    }

    public static SteamGame searchGame(String valor, int tipo){
        //tipo 1 -> pesquisa por ID
        //tipo 2 -> pesquisa por appId
        //tipo 3 -> pesquisa por name

        //inicializar variáveis de pesquisa
        boolean achou = false;
        SteamGame jogo = new SteamGame();

        try {
            RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db", "r");
            //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N


            //mover ponteiro para início do arquivo
            arquivo.seek(0);

            //pular ultimo id inserido
            arquivo.skipBytes(4);
            int atual = 2;

            while (arquivo.getFilePointer() != arquivo.length() && !achou){
                //mostrar barra de progresso
                progressBar(atual, 27076,"[Search]");
                
                //ver se está com lápide ativa
                byte lapide = arquivo.readByte();

                if (lapide == 0xFF){//pular o tamanho do registro
                    arquivo.skipBytes(arquivo.readInt());
                }
                else{
                    arquivo.skipBytes(4); //ignorar o tamanho e começar a leitura do registro
                    jogo = readGame(arquivo);
                    switch (tipo) {
                        case 1:
                            if (jogo.getId() == Integer.parseInt(valor)){
                                achou = true;
                                System.out.println("Registro com o ID encontrado!");
                            }
                            break;
                        case 2:
                            if (jogo.getAppid() == Integer.parseInt(valor)){
                                achou = true;
                                System.out.println("Registro com o appId encontrado!");
                            }
                            break;
                        case 3:
                            if (jogo.getName().toLowerCase().compareTo(valor.toLowerCase()) == 0){
                                achou = true;
                                System.out.println("Registro com o nome encontrado!");
                            }
                            break;
                        default:
                            System.out.println("[ERRO] -> Opção de pesquisa inválida.");
                            break;
                    }

                    //devolver o jogo encontrado
                    if (achou){
                        jogo.printAll();
                        System.out.println("[Search] -> Registro encontrado!");
                    }
                    else{
                        System.out.println("\n[Search] -> Não foi possível localizar o registro.");
                    }
                }
                atual++;
            }

            arquivo.close();
        } catch (Exception e) {
            System.out.println("Erro ao pesquisar o registro!");
            System.out.println(e);
        }

        return jogo;
    }

    public static boolean createGame(int tipo) {
        boolean resp = false;
        try {
            RandomAccessFile arquivo = new RandomAccessFile("./db_Output/gamesDB.db", "rw");


            SteamGame jogo = new SteamGame();

            if (tipo == 1){
                writeGame(arquivo, jogo);
                jogo.printAll();
                resp = true;
            }
            else{
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
                }

                //contador de elementos na lista
                int num = 0;

                //lista categories
                System.out.print("\n[Create] -> Digite o número de categorias: ");
                num = Integer.parseInt(leitor.nextLine());
                ArrayList<String> categories = new ArrayList<>();
                for (int i = 0; i < num; i++){
                    System.out.print("\n[Create] -> Digite a categoria [" + i + "]: ");
                    valor = leitor.nextLine();
                    categories.add(valor);
                }
                jogo.setCategories(categories);

                //lista genres
                System.out.print("\n[Create] -> Digite o número de gêneros: ");
                num = Integer.parseInt(leitor.nextLine());
                ArrayList<String> genres = new ArrayList<>();
                for (int i = 0; i < num; i++){
                    System.out.print("\n[Create] -> Digite o gênero [" + i + "]: ");
                    valor = leitor.nextLine();
                    genres.add(valor);
                }
                jogo.setGenres(genres);

                //lista spytags
                System.out.print("\n[Create] -> Digite o número de spytags: ");
                num = Integer.parseInt(leitor.nextLine());
                ArrayList<String> spytags = new ArrayList<>();
                for (int i = 0; i < num; i++){
                    System.out.print("\n[Create] -> Digite a spytag [" + i + "]: ");
                    valor = leitor.nextLine();
                    spytags.add(valor);
                }
                jogo.setSteamspyTags(spytags);

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
            }
        } catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível criar o registro");
            System.out.println(e);
        }
        return resp;
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

    public static void csvExtractNum(int max){
            //ajustar o número de registros para +2 (ignorar primeira linha do csv);
            max += 2;
            try {
                //obter os objetos da base de dados CSV (steamgames.csv)
                RandomAccessFile entrada = new RandomAccessFile("./CSV_Input/steamgames.csv","r");

                //excluir o arquivo existente
                File file = new File("./db_Output/gamesDB.db");
                if (file.exists()) {
                    file.delete();
                }
                
                //definir o local de saída com os dados extraídos
                RandomAccessFile saida = new RandomAccessFile("./db_Output/gamesDB.db","rw");

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
            } catch (IOException | NumberFormatException e) {
                System.out.println("Ocorreu um erro durante o processamento do arquivo :(");
                System.out.println(e);
            }
        }

    public static void DEBUG_csvExtractNum(int max){
        max += 2;
        try {
            //obter os objetos da base de dados CSV (steamgames.csv)
            RandomAccessFile entrada = new RandomAccessFile("./CSV_Input/steamgames.csv","r");

            //Excluir o arquivo existente, se necessário
            File file = new File("./db_Output/gamesDB.db");
            if (file.exists()) {
                file.delete();
            }

            //definir o local de saída com os dados extraídos
            RandomAccessFile saida = new RandomAccessFile("./db_Output/gamesDB.db","rw");

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
            saida.close();
        } catch (IOException | NumberFormatException e) {
            System.out.println("Um erro ocorreu durante o processamento do arquivo :(");
            System.out.println(e);
        }
    }
}
