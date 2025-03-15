package services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import main.DataBase;
import models.SteamGame;

public class DB_Load {

    public static void csvExtractAll(){
        System.out.println("[CsvExtract] -> Extraindo todos os registros do arquivo csv. Isso deve demorar um pouco...");
        csvExtractNum(27075);
    }

    public static void csvExtractNum(int max){
        //fazer um backup da base de dados, se existir
        DB_Services.backupDatabase();

        //resetar variáveis globais de número de registros na base de dados
        DataBase.totalGames = 0;
        DataBase.totalDeleted = 0;

        //ajustar o número de registros para +2 (ignorar primeira linha do csv);
        max += 2;
        try (RandomAccessFile entrada = new RandomAccessFile("./src/resources/csv_input/steamgames.csv","r");){ //obter os objetos da base de dados CSV (steamgames.csv)
            
            //excluir o arquivo existente, se existir
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
                
                System.out.println("[CsvExtract] -> Criando registros...");
                
                //cronometrar tempo total
                long tempo_inicio = System.currentTimeMillis();
                
                //iniciando o loop de leitura completa do arquivo CSV
                while (entrada.getFilePointer() < entrada.length() && contador < max){
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
                                    jogo.setReleaseDate(DB_Services.convertString_Unix(content[pos_vet]));
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
                        DB_CRUD.writeGame(saida, jogo);
                    }

                    //imprimir barra de progresso
                    UI.progressBar(contador - 1, max - 1,"[CsvExtract]",1,0);

                }
                
                //cronometrar tempo
                long tempo_fim = System.currentTimeMillis();
                long tempo = tempo_fim - tempo_inicio; //tempo

                DataBase.hasData = true;
                
                System.out.println("\n[CsvExtract] -> Carga do banco de dados realizada com sucesso.     Tempo decorrido: " + tempo/1000.0 + "s     Total de registros: " + (contador - 2) + "     Velocidade Média: " + String.format("%.1f",contador/(tempo/1000.0)) + " registros/s \n");
                entrada.close();
                saida.close();
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Ocorreu um erro durante o processamento do arquivo :(");
            System.out.println(e);
        }
    }

}
