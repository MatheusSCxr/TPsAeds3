import java.io.IOException;
import java.io.RandomAccessFile;
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

    public static void csvExtractNum(int max){
        max += 2;
        try {
            //obter os objetos da base de dados CSV (steamgames.csv)
            RandomAccessFile entrada = new RandomAccessFile("./CSV_Input/steamgames.csv","r");
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

                    jogo.setId(contador - 1);

                    //filtrar e separar os conteúdos do CSV, dividos por ','
                    String[] content = linha.split(",");
                    System.out.println("Strings obtidas -> " + content.length + "\nLendo as strings individualmente...");
                    
                    //detectar se a linha lida está de acordo com o padrão, caso esteja maior, significa que existe pelo menos mais de 1 elemento que contém vírgula na string
                    if (content.length > 18){
                        System.out.println("Linha com vírgulas detectada!");
                    }

                    System.out.println("AppId -> " + content[0]);

                
                    //gravar no arquivo e no objeto
                    saida.writeInt(Integer.parseInt(content[0]));
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
                                saida.writeUTF(content[pos_vet]);
                            }
                            case 2 -> {
                                System.out.println("Release -> " + content[pos_vet]);
                                jogo.setReleaseDate(content[pos_vet]);
                                saida.writeUTF(content[pos_vet]);
                            }
                            case 3 -> {
                                System.out.println("English (boolean) -> " + content[pos_vet]);
                                if (content[pos_vet].startsWith("1")){
                                    jogo.setEnglish(true);
                                    saida.writeBoolean(true);
                                }
                                else{
                                    jogo.setEnglish(false);
                                    saida.writeBoolean(false);
                                }
                            }
                            case 4 -> {
                                System.out.println("Developer -> " + content[pos_vet]);
                                jogo.setDeveloper(content[pos_vet]);
                                saida.writeUTF(content[pos_vet]);
                            }
                            case 5 -> {
                                System.out.println("Publisher -> " + content[pos_vet]);
                                jogo.setPublisher(content[pos_vet]);
                                saida.writeUTF(content[pos_vet]);
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
                        saida.writeUTF(process);
                    }
                    else{
                        jogo.setPlatforms("windows");
                        saida.writeUTF("windows"); 
                    }

                    //pos_vet = 7
                    System.out.println("Required Age -> " + content[pos_vet + 1]);
                    jogo.setRequiredAge(Integer.parseInt(content[pos_vet + 1]));
                    saida.writeInt(Integer.parseInt(content[pos_vet + 1]));

                    String[] categories = content[pos_vet + 2].split(";");
                    ArrayList<String> categoriesList = new ArrayList<>();
                    
                    System.out.println("Categories ORIGINAL -> " + content[pos_vet + 2]);
                    if (categories.length > 1){
                        System.out.println("Detectadas " + categories.length + " categorias. Gravando valor...");
                        saida.writeInt(categories.length);
                        for (String i : categories){
                            System.out.println("Categories Processada -> " + i);
                            categoriesList.add(i);
                            saida.writeUTF(i);
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
                        saida.writeInt(genres.length);
                        for (String i : genres){
                            System.out.println("Genre Processado -> " + i);
                            genreList.add(i);
                            saida.writeUTF(i);
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
                        saida.writeInt(spytag.length);
                        for (String i : spytag){
                            System.out.println("Spytag Processada -> " + i);
                            spytagList.add(i);
                            saida.writeUTF(i);
                        }
                    }
                    else{
                        System.out.println("Spytag Processada -> " + genres[0]);
                        spytagList.add(spytag[0]);
                    }
                    jogo.setSteamspyTags(spytagList);

                    System.out.println("Achievements -> " + content[pos_vet + 5]);
                    jogo.setAchievements(Integer.parseInt(content[pos_vet + 5]));
                    saida.writeInt(Integer.parseInt(content[pos_vet + 5]));

                    System.out.println("Positive Ratings -> " + content[pos_vet + 6]);
                    jogo.setPositiveRatings(Integer.parseInt(content[pos_vet + 6]));
                    saida.writeInt(Integer.parseInt(content[pos_vet + 6]));

                    System.out.println("Negative Ratings -> " + content[pos_vet + 7]);
                    jogo.setNegativeRatings(Integer.parseInt(content[pos_vet + 7]));
                    saida.writeInt(Integer.parseInt(content[pos_vet + 7]));

                    System.out.println("Avarage Playtime -> " + content[pos_vet + 8]);
                    jogo.setAveragePlaytime(Integer.parseInt(content[pos_vet + 8]));
                    saida.writeInt(Integer.parseInt(content[pos_vet + 8]));

                    System.out.println("Median Playtime -> " + content[pos_vet + 9]);
                    jogo.setMedianPlaytime(Integer.parseInt(content[pos_vet + 9]));
                    saida.writeInt(Integer.parseInt(content[pos_vet + 9]));

                    System.out.println("Owners -> " + content[pos_vet + 10]);
                    jogo.setOwners(content[pos_vet + 10]);
                    saida.writeUTF(content[pos_vet + 10]);

                    System.out.println("Price -> " + content[pos_vet + 11]);
                    jogo.setPrice(Float.parseFloat(content[pos_vet + 11]));
                    saida.writeFloat(Float.parseFloat(content[pos_vet + 11]));

                    jogo.printAll();
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
    public static void csvExtractAll(){
        csvExtractNum(27077);
    }
}
