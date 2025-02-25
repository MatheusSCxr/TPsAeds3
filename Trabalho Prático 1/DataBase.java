import java.io.RandomAccessFile;

public class DataBase {

    public static void main(String[] args) {
        //obter os objetos da base de dados csv (steam.csv)

        try {
            RandomAccessFile entrada = new RandomAccessFile("./steamgames.csv","r");
            RandomAccessFile saida = new RandomAccessFile("./games_ds.db","rw");
            String linha = entrada.readLine();//ignorar primeira linha
            int contador = 0;
            System.out.println("Contando linhas...");
            Boolean stop = false;
            while (entrada.getFilePointer() < entrada.length() && stop == false){ //percorrer todas as linhas
                linha = entrada.readLine();
                System.out.println("\nLINHA = "+ contador);
                System.out.println("LINHA REGISTRADA -> " + linha);
                contador++;
                if (linha != null){
                        String[] content = linha.split(",");
                        System.out.println("String obtidas -> " + content.length + "\nlendo as string individualmente...");
                        System.out.println("AppId -> " + content[0]);
                        saida.writeInt(Integer.parseInt(content[0]));
                        int pos_vet = 1; //posição no vetor
                        int pos_val = 1; //representa a posição do elemento correspondente no csv (1 até 18)
                        if (content.length > 18){
                            System.out.println("Vírgula detectada. Ativando modo interpretativo seguro...");
                            while (pos_val < 7){
                                if (content[pos_vet].startsWith("\"")){
                                    if (content[pos_vet + 1].endsWith("\"")){ //apenas 2 vírgulas entre aspas
                                        System.out.println("Escrevendo -> " + content[pos_vet] + "," + content[pos_vet + 1]);
                                        saida.writeUTF(content[pos_vet] + "," + content[pos_vet + 1]);
                                        pos_vet += 2;
                                    }
                                    else if (content[pos_vet + 2].endsWith("\"")){ //3 vírgulas entre aspas
                                        System.out.println("Escrevendo -> " + content[pos_vet] + "," + content[pos_vet + 1] + "," + content[pos_vet + 2]);
                                        saida.writeUTF(content[pos_vet] + "," + content[pos_vet + 1] + "," + content[pos_vet + 2]);
                                        pos_vet += 3;
                                    }
                                }
                                else{
                                    switch (pos_val) {
                                        case 1:
                                            System.out.println("Nome -> " + content[pos_vet]);
                                            saida.writeUTF(content[pos_vet]);
                                            break;
                                        case 2:
                                            System.out.println("Release -> " + content[pos_vet]);
                                            saida.writeUTF(content[pos_vet]);
                                            break;
                                        case 3:
                                            System.out.println("English (boolean) -> " + content[pos_vet]);
                                            if (content[pos_vet].startsWith("1"))
                                                saida.writeBoolean(true);
                                            else
                                                saida.writeBoolean(false);
                                            break;
                                        case 4:
                                            System.out.println("Developer -> " + content[pos_vet]);
                                            saida.writeUTF(content[pos_vet]);
                                            break;
                                        case 5:
                                            System.out.println("Publisher -> " + content[pos_vet]);
                                            saida.writeUTF(content[pos_vet]);
                                            break;
                                        case 6:
                                            System.out.println("Length plat = " + content[pos_vet].length());
                                            System.out.println("Plataforms -> " + content[pos_vet]);
                                            if (content[pos_vet].length() > 7){
                                                System.out.println("Plataforms -> " + content[pos_vet].substring(0,content[pos_vet].indexOf(";")));
                                                saida.writeUTF(content[pos_vet].substring(0,content[pos_vet].indexOf(";")));
                                            }
                                            else{
                                                saida.writeUTF("windows"); 
                                            }
                                            break;
                                        default:
                                            System.out.println("pos_val INVÁLIDA ou não acessível código = " + pos_val);;
                                    }
                                    pos_vet++;
                                }
                                pos_val++;
                                System.out.println("PROXIMA -> " + content[pos_vet]);
                            }
                        }
                    else{
                        System.out.println("Nome -> " + content[1]);
                        saida.writeUTF(content[1]);
            
                        System.out.println("Release Date -> " + content[2]);
                        saida.writeUTF(content[2]);
            
                        System.out.println("English (boolean) -> " + content[3]);
                        if (content[3].startsWith("1"))
                            saida.writeBoolean(true);
                        else
                            saida.writeBoolean(false);
            
                        System.out.println("Developer -> " + content[4]);
                        saida.writeUTF(content[4]);
            
                        System.out.println("Publisher -> " + content[5]);
                        saida.writeUTF(content[5]);
                        System.out.println("Length plat = " + content[6].length());
                        System.out.println("Plataforms -> " + content[6]);
                        if (content[6].length() > 7){ //string de tamanho fixo
                            System.out.println("Plataforms PROCESSADA -> " + content[6].substring(0,content[6].indexOf(";")));
                            saida.writeUTF(content[6].substring(0,content[6].indexOf(";")));
                        }
                        else{
                            saida.writeUTF("windows"); 
                        }

                        System.out.println("Required Age -> " + content[7]);
                        saida.writeInt(Integer.parseInt(content[7]));

                        String[] categories = content[8].split(";");
                        System.out.println("Categories ORIGINAL -> " + content[8]);
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

                        String[] genres = content[9].split(";");
                        System.out.println("Genres ORIGINAL -> " + content[9]);
                        if (genres.length > 1){
                            System.out.println("Detectados " + genres.length + " genres. Gravando valor...");
                            saida.writeInt(genres.length);
                            for (int i = 0; i < genres.length; i++){
                                System.out.println("Genres Processada -> " + genres[i]);
                                saida.writeUTF(genres[i]);
                            }
                            stop = true;
                        }
                        else
                            System.out.println("Genres Processada -> " + genres[0]);

                        String[] spytag = content[10].split(";");
                        System.out.println("Spytag ORIGINAL -> " + content[10]);
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


                        System.out.println("Achievements -> " + content[11]);
                        saida.writeInt(Integer.parseInt(content[11]));

                        System.out.println("Positive Ratings -> " + content[12]);
                        saida.writeInt(Integer.parseInt(content[12]));

                        System.out.println("Negative Ratings -> " + content[13]);
                        saida.writeInt(Integer.parseInt(content[13]));

                        System.out.println("Avarage Playtime -> " + content[14]);
                        saida.writeInt(Integer.parseInt(content[14]));

                        System.out.println("Median Playtime -> " + content[15]);
                        saida.writeInt(Integer.parseInt(content[15]));

                        System.out.println("Owners -> " + content[16]);
                        saida.writeUTF(content[16]);

                        System.out.println("Price -> " + content[17]);
                        saida.writeFloat(Float.parseFloat(content[17]));
                    }
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
