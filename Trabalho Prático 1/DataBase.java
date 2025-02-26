import java.io.RandomAccessFile;

public class DataBase {
    public static void main(String[] args) {
        try {
            //obter os objetos da base de dados CSV (steamgames.csv)
            RandomAccessFile entrada = new RandomAccessFile("./database/steamgames.csv","r");
            //definir o local de saída com os dados extraídos
            RandomAccessFile saida = new RandomAccessFile("./output/games_ds.db","rw");

            //ler a primeira linha, mas ela será desconsiderada assim que entrar no loop de leitura
            String linha = entrada.readLine();

            //contador de linhas lidas
            int contador = 2;
            System.out.println("Contando linhas...");

            //variável para debug
            Boolean stop = false;
            //iniciando o loop de leitura completa do arquivo CSV
            while (entrada.getFilePointer() < entrada.length() && stop == false && contador < 415){
                //obter liha atual do CSV
                linha = entrada.readLine();

                //mensagens para debug
                System.out.println("\n--------------- Processando Linha ["+ contador + "] -------------------");
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
                            //escrever conteúdo...
                            saida.writeUTF(elemento.toString());
                        }
                        switch (pos_val) {
                            case 1 -> {
                                System.out.println("Nome -> " + content[pos_vet]);
                                saida.writeUTF(content[pos_vet]);
                            }
                            case 2 -> {
                                System.out.println("Release -> " + content[pos_vet]);
                                saida.writeUTF(content[pos_vet]);
                            }
                            case 3 -> {
                                System.out.println("English (boolean) -> " + content[pos_vet]);
                                if (content[pos_vet].startsWith("1"))
                                    saida.writeBoolean(true);
                                else
                                    saida.writeBoolean(false);
                            }
                            case 4 -> {
                                System.out.println("Developer -> " + content[pos_vet]);
                                saida.writeUTF(content[pos_vet]);
                            }
                            case 5 -> {
                                System.out.println("Publisher -> " + content[pos_vet]);
                                saida.writeUTF(content[pos_vet]);
                            }
                            default -> System.out.println("pos_val INVÁLIDA ou não acessível código = " + pos_val);
                        }
                        pos_vet++;
                        pos_val++;
                        System.out.println("POS VAL -> " + pos_val);
                        System.out.println("PROXIMA -> " + content[pos_vet]);
                    }

                    //pos vet = 6
                    System.out.println("Length plat = " + content[pos_vet].length());
                    System.out.println("Plataforms -> " + content[pos_vet]);
                    if (content[pos_vet].length() > 7){ //string de tamanho fixo
                        System.out.println("Plataforms PROCESSADA -> " + content[pos_vet].substring(0,content[pos_vet].indexOf(";")));
                        saida.writeUTF(content[pos_vet].substring(0,content[pos_vet].indexOf(";")));
                    }
                    else{
                        saida.writeUTF("windows"); 
                    }

                    System.out.println("Required Age -> " + content[pos_vet + 1]);
                    saida.writeInt(Integer.parseInt(content[pos_vet + 1]));

                    String[] categories = content[pos_vet + 2].split(";");
                    System.out.println("Categories ORIGINAL -> " + content[pos_vet + 2]);
                    if (categories.length > 1){
                        System.out.println("Detectadas " + categories.length + " categorias. Gravando valor...");
                        saida.writeInt(categories.length);
                        for (int i = 0; i < categories.length; i++){
                            System.out.println("Categories Processada -> " + categories[i]);
                            saida.writeUTF(categories[i]);
                        }
                    }
                    else
                        System.out.println("Categories Processada -> " + categories[0]);

                    String[] genres = content[pos_vet + 3].split(";");
                    System.out.println("Genres ORIGINAL -> " + content[pos_vet + 3]);
                    if (genres.length > 1){
                        System.out.println("Detectados " + genres.length + " genres. Gravando valor...");
                        saida.writeInt(genres.length);
                        for (int i = 0; i < genres.length; i++){
                            System.out.println("Genres Processada -> " + genres[i]);
                            saida.writeUTF(genres[i]);
                        }
                    }
                    else
                        System.out.println("Genres Processada -> " + genres[0]);

                    String[] spytag = content[pos_vet + 4].split(";");
                    System.out.println("Spytag ORIGINAL -> " + content[pos_vet + 4]);
                    if (spytag.length > 1){
                        System.out.println("Detectados " + spytag.length + " spytags. Gravando valor...");
                        saida.writeInt(spytag.length);
                        for (int i = 0; i < spytag.length; i++){
                            System.out.println("Spytag Processada -> " + spytag[i]);
                            saida.writeUTF(spytag[i]);
                        }
                    }
                    else
                        System.out.println("Genres Processada -> " + genres[0]);

                    System.out.println("Achievements -> " + content[pos_vet + 5]);
                    saida.writeInt(Integer.parseInt(content[pos_vet + 5]));

                    System.out.println("Positive Ratings -> " + content[pos_vet + 6]);
                    saida.writeInt(Integer.parseInt(content[pos_vet + 6]));

                    System.out.println("Negative Ratings -> " + content[pos_vet + 7]);
                    saida.writeInt(Integer.parseInt(content[pos_vet + 7]));

                    System.out.println("Avarage Playtime -> " + content[pos_vet + 8]);
                    saida.writeInt(Integer.parseInt(content[pos_vet + 8]));

                    System.out.println("Median Playtime -> " + content[pos_vet + 9]);
                    saida.writeInt(Integer.parseInt(content[pos_vet + 9]));

                    System.out.println("Owners -> " + content[pos_vet + 10]);
                    saida.writeUTF(content[pos_vet + 10]);

                    System.out.println("Price -> " + content[pos_vet + 11]);
                    saida.writeFloat(Float.parseFloat(content[pos_vet + 11]));

                    jogo.printAll();
                }
            }
            System.out.println("Linhas processadas = " + (contador + 1) + "\n");
            entrada.close();
            saida.close();
        } catch (Exception e) {
            System.out.println("Erro durante o processamento do arquivo.");
        }
    }
}
