import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Scanner;

public class DataBase {
    public static void main(String[] args) {
        //exibir interface de menu com opções
        UI_menu();
        Scanner leitor = new Scanner(System.in);
        int choice = -1;

        while (choice != 0){ // 0 = encerrar programa
            choice = leitor.nextInt();
            switch (choice) {
                case 1 -> {
                    csvExtractAll();
                }
                case 2 -> {
                    System.out.print("\nDigite o número de registros que deseja extrair do CSV: ");
                    int num = leitor.nextInt();
                    csvExtractNum(num);
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
                    System.out.println("Número inválido. Por favor, digite o número de uma das opções acima.");
                }
            }
            UI_menu();
        }

        //fechar scanner
        leitor.close();

        System.out.println("\n\n[Escolha] -> Programa Encerrado");
    }

    public static void UI_menu(){
        System.out.println("----------------------- [ MENU ] -----------------------");
        System.out.println("[1] - Criar Arquivo com todos os registros do CSV");
        System.out.println("[2] - Criar Arquivo com um número N de registros do CSV (primeiro -> último)");
        System.out.println("[101][ DEBUG ] - Criar Arquivo com todos os registros do CSV [Aviso: LENTO]");
        System.out.println("[102][ DEBUG ] - Criar Arquivo com um número N de registros do CSV (primeiro -> último) [Aviso: LENTO]");
        System.out.println("[0] - Encerrar o programa");
        System.out.println("--------------------------------------------------------");

        System.out.print("\n[Escolha] -> Por favor, digite o número de uma das opções acima: ");
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
            System.out.println("Erro ao gravar o registro no arquivo!");
            System.out.println(e);
        }
    }

    public static void csvExtractAll(){
        System.out.println("[CsvExtract] -> Iniciando processo de extração de todos os registros do csv. Isso deve demorar um pouco...");
        csvExtractNum(27075);
    }

    public static void DEBUG_csvExtractAll(){
        DEBUG_csvExtractNum(27077);
    }

    public static void progressBar(int atual, int total){
        //definir propoção/tamanho da barra
        int ratio = 50;

        //calcular o progresso na proproção
        float progress = (float)(((double)atual/total) * ratio); // (total / total) = 1 * proproção = proporção

        StringBuilder barra = new StringBuilder("[CsvExtract] -> PROGRESSO ATUAL:  ["); //criar barra

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
        barra.append("   Registro número: ").append(atual - 1).append(" de ").append(total - 1).append("\t");

        //imprimir a barra
        System.out.print("\r" + barra.toString());
    }

    public static void csvExtractNum(int max){
            //ajustar o número de registros para +2 (ignorar primeira linha do csv);
            max += 2;
            try {
                //obter os objetos da base de dados CSV (steamgames.csv)
                RandomAccessFile entrada = new RandomAccessFile("./CSV_Input/steamgames.csv","r");

                //excluir o arquivo existente, se necessário
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
                    progressBar(contador, max - 1);

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
                                    String[] calendario = content[pos_vet].split("-");
                                    LocalDate data = LocalDate.of(Integer.parseInt(calendario[0]), Integer.parseInt(calendario[1]), Integer.parseInt(calendario[2]));
                                    
                                    //converter para Unix timestamp
                                    long timestamp = data.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
                                    //gravar no objeto o unix
                                    jogo.setReleaseDate(timestamp);
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
                            for (String i : categories){
                                categoriesList.add(i);
                            }
                        }
                        else{
                            categoriesList.add(categories[0]);
                        }
                        jogo.setCategories(categoriesList);
                        
                        String[] genres = content[pos_vet + 3].split(";");
                        ArrayList<String> genreList = new ArrayList<>();

                        if (genres.length > 1){
                            for (String i : genres){
                                genreList.add(i);
                            }
                        }
                        else{
                            genreList.add(genres[0]);
                        }
                        jogo.setGenres(genreList);

                        String[] spytag = content[pos_vet + 4].split(";");
                        ArrayList<String> spytagList = new ArrayList<>();
                        
                        if (spytag.length > 1){
                            for (String i : spytag){
                                spytagList.add(i);
                            }
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
