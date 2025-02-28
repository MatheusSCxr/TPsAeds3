import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Scanner;

public class DataBase {
    public static void main(String[] args) {
        System.out.println("----------------------- [ MENU ] -----------------------");
        System.out.println("[1] - Criar Arquivo com todos os registros do CSV");
        System.out.println("[2] - Criar Arquivo com um número N de registros do CSV (primeiro -> último)");
        System.out.print("Por favor, digite o número de uma das opções acima: ");
        Scanner leitor = new Scanner(System.in);
        int choice = leitor.nextInt();

        switch (choice) {
            case 1 -> {
                csvExtractAll();
            }
            case 2 -> {
                System.out.print("\nDigite o número de registros que deseja extrair do CSV: ");
                int num = leitor.nextInt();
                csvExtractNum(num);
            }
            default -> {
                System.out.println("Número inválido. Por favor, digite o número de uma das opções acima.");
                choice = leitor.nextInt();
            }
        }

        leitor.close();
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
                System.out.println("Nenhum registro encontrado. Escrevendo primeiro Id = 1");
            }
            else{
                //ler id do ultimo registro adicinado e acrescentar 1
                id = saida.readInt();
                System.out.println("Ultimo id registrado: " + id);
                id++;
            }

            //gravar o id no objeto
            jogo.setId(id);

            //debug
            jogo.printAll();

            //mover ponteiro para o final
            saida.seek(saida.length());


            //gravar no registro as informações do objeto (metadados e dados)
            //metadados
            saida.writeByte(0x00); //lápide para indicar que registro está ativo (0xFF indica que está inativo)
            saida.writeInt(jogo.measureSize());
            
            //debug
            System.out.println("Tamanho do registro : " + jogo.measureSize());
            
            //dados
            saida.writeInt(jogo.getId());
            saida.writeInt(jogo.getAppid());
            saida.writeUTF(jogo.getName());
            saida.writeLong(jogo.getReleaseDate());
            saida.writeBoolean(jogo.getEnglish());
            saida.writeUTF(jogo.getDeveloper());
            saida.writeUTF(jogo.getPublisher());
            saida.writeUTF(jogo.getPlatforms());
            saida.writeInt(jogo.getRequiredAge());

            //escrever lista de categorias
            saida.writeInt(jogo.getCategories().size());//indicar tamanho da lista
            for (String category : jogo.getCategories()) {
                saida.writeUTF(category);//elementos da lista
            }

            //escrever lista de gêneros
            saida.writeInt(jogo.getGenres().size());
            for (String genre : jogo.getGenres()) {
                saida.writeUTF(genre);
            }

            //ecrever lista de spytags
            saida.writeInt(jogo.getSteamspyTags().size());
            for (String tag : jogo.getSteamspyTags()) {
                saida.writeUTF(tag);
            }

            saida.writeInt(jogo.getAchievements());
            saida.writeInt(jogo.getPositiveRatings());
            saida.writeInt(jogo.getNegativeRatings());
            saida.writeInt(jogo.getAveragePlaytime());
            saida.writeInt(jogo.getMedianPlaytime());
            saida.writeUTF(jogo.getOwners());
            saida.writeFloat(jogo.getPrice());

            //atualizar o id do início dos registros
            saida.seek(0);
            saida.writeInt(id);

        } catch (Exception e) {
            System.out.println("Erro ao gravar o registro no arquivo!");
            System.out.println(e);
        }
    }

    public static void csvExtractAll(){
        csvExtractNum(27077);
    }

    public static void csvExtractNum(int max){
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
