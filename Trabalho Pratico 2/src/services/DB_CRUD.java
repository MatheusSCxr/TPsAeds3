package services;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import main.DataBase;
import models.ArvoreElemento;
import models.HashElemento;
import models.ListaElemento;
import models.SteamGame;

public class DB_CRUD {
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
                    DataBase.totalGames++;
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

    public static SteamGame readGame(RandomAccessFile arquivo){
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

        return jogo;
    }

    public static SteamGame readGame_Address(long endereco){
        SteamGame jogo =  new SteamGame();

        try(RandomAccessFile arquivo = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "r")){
            arquivo.seek(endereco);

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

        return jogo;
    }

    public static SteamGame searchGame(String valor, int tipo){
        //tipo 1 -> pesquisa por ID
        //tipo 2 -> pesquisa por appId
        //tipo 3 -> pesquisa por name

        //inicializar variáveis de pesquisa
        boolean achou = false;
        SteamGame jogo = new SteamGame();

        try (RandomAccessFile arquivo = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "r")){
            //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N

            //mover ponteiro para início do arquivo
            arquivo.seek(0);

            //pular ultimo id inserido
            arquivo.skipBytes(4);
            int atual = 2;

            while (arquivo.getFilePointer() < arquivo.length() && !achou){
                //mostrar barra de progresso
                UI.progressBar(atual, (DataBase.totalGames + 1),"[Search]",2,0);
                
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
        try (RandomAccessFile arquivo = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "rw")){

            SteamGame jogo = new SteamGame();

            if (tipo == 1){
                //obter o endereço do ponteiro no final do arquivo sequencial
                long endereco = arquivo.length() + 5; //+5 pois 1 byte para a lápide e 4 bytes para o tamanho do endereço

                if (writeGame(arquivo, jogo)){
                    jogo.printAll();
                    resp = true;

                     //criar os índices
                     if (DataBase.indexStatus > 0){
                        System.out.println("[Index] -> Adicionando endereço do registro no arquivo de índices...");

                        try {
                            switch (DataBase.indexStatus) {
                                case 1 -> {
                                    //criar na árvore b+
                                    if (!DataBase.arvore.create(new ArvoreElemento(jogo.getId(),endereco)))
                                        System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices da Árvore B+");
                                }
                                case 2 -> {
                                    //criar na hash extensível
                                    if (!DataBase.hash.create(new HashElemento(jogo.getId(),endereco)))
                                        System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices da Hash Extensível");
                                }
                                case 3 -> {
                                    //abrir scanner
                                    Scanner leitor = new Scanner(System.in);
                                    //criar na categoria da Lista Invertida
                                    System.out.print("\n[Search] -> Digite a Categoria do registro que deseja adicionar na Lista Invertida: ");
                                    String categoria = leitor.nextLine();
                                    if (!DataBase.lista.create(categoria, new ListaElemento(jogo.getId(), endereco))) //adiciona jogo na lista
                                        System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices da categoria '" + categoria + "' da Lista Invertida");
                                    else{
                                        //incrementar entidades
                                        DataBase.lista.incrementaEntidades();
                                    } 
                                }
                                default -> {
                                    System.out.println("[ERRO Crítico!!!] -> Indexação atual inválida");
                                    DataBase.indexStatus = 0; //medida preventiva
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices");
                            System.out.println(e);
                        }
                    }
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
                    jogo.setReleaseDate(DB_Services.convertString_Unix(valor));
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

                    int num;
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

                    //obter o endereço do ponteiro no final do arquivo sequencial
                    long endereco = arquivo.length() + 5; //+5 pois 1 byte para a lápide e 4 bytes para o tamanho do endereço

                    //escrever jogo no arquivo
                    if (writeGame(arquivo, jogo)){
                        jogo.printAll();
                        resp = true;
    
                        //criar os índices
                        if (DataBase.indexStatus > 0){
                            System.out.println("[Index] -> Adicionando endereço do registro no arquivo de índices...");
    
                            try {
                                switch (DataBase.indexStatus) {
                                    case 1 -> {
                                        //criar na árvore b+
                                        if (!DataBase.arvore.create(new ArvoreElemento(jogo.getId(),endereco)))
                                            System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices da Árvore B+");
                                    }
                                    case 2 -> {
                                        //criar na hash extensível
                                        if (!DataBase.hash.create(new HashElemento(jogo.getId(),endereco)))
                                            System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices da Hash Extensível");
                                    }
                                    case 3 -> {
                                        //criar na categoria da Lista Invertida
                                        System.out.print("\n[Search] -> Digite a Categoria do registro que deseja adicionar na Lista Invertida: ");
                                        String categoria = leitor.nextLine();
                                        if (!DataBase.lista.create(categoria, new ListaElemento(jogo.getId(), endereco))) //adiciona jogo na lista
                                            System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices da categoria '" + categoria + "' da Lista Invertida");
                                        else{
                                            //incrementar entidades
                                            DataBase.lista.incrementaEntidades();
                                        } 
                                    }
                                    default -> {
                                        System.out.println("[ERRO Crítico!!!] -> Indexação atual inválida");
                                        DataBase.indexStatus = 0; //medida preventiva
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("[ERRO] -> Não foi possível criar o registro no arquivo de índices");
                                System.out.println(e);
                            }
                        }
                    }
                    else {
                        resp = false;
                    }
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

    public static boolean deleteGame(int delete_id, String categoria){
        //inicializar variáveis de pesquisa/deletar
        boolean achou = false;
        boolean resp = false;
        SteamGame jogo = new SteamGame();
        long pos_registro = -1;

        try (RandomAccessFile arquivo = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "rw")){
            switch (DataBase.indexStatus) {
                case 0 -> {
                    //pesquisa sequencial
                    //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N
                    //mover ponteiro para início do arquivo
                    arquivo.seek(0);

                    //pular ultimo id inserido
                    arquivo.skipBytes(4);
                    int atual = 2;

                    while (arquivo.getFilePointer() < arquivo.length() && !achou){
                        //mostrar barra de progresso
                        UI.progressBar(atual, (DataBase.totalGames + 1),"[Search]",4,0);
                        
                        //ler se a lápide está ativa
                        int lapide = arquivo.readUnsignedByte();
                        if (lapide == 0xFF){
                            //ler e pular o tamanho do registro a seguir
                            arquivo.skipBytes(arquivo.readInt());
                        }
                        else{
                            arquivo.skipBytes(4); //ignorar o tamanho e começar a leitura do registro
                            pos_registro = arquivo.getFilePointer();
                            jogo = readGame(arquivo);
                            if (jogo.getId() == delete_id){
                                achou = true; //indicar que o registro foi encontrado
                                System.out.println("[Search] -> Registro encontrado com sucesso! ");
                            }
                        }
                        atual++;
                    }
                }
                case 1 -> {
                    //pesquisa na árvore B+
                    try {
                        ArrayList<ArvoreElemento> lista = DataBase.arvore.read(new ArvoreElemento(delete_id, -1));
                        if (!lista.isEmpty() ){
                            System.out.println("[Index] -> Registro encontrado com sucesso na Árvore B+: ");
                            for (int i = 0; i < lista.size(); i++){
                                jogo = DB_CRUD.readGame_Address(lista.get(i).getAddress()); //obter o jogo
                                pos_registro = lista.get(i).getAddress(); //obter endereço do registro

                                achou = true;
                            }
                        }
                        else{
                            System.out.println("Não foi possível encontrar o registro na Árvore.");
                            achou = false;
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                case 2 -> {
                    //pesquisa na hash extensível
                    try {
                        HashElemento registro = DataBase.hash.read(HashElemento.hash(delete_id));
                        if (registro != null ){
                            System.out.println("[Index] -> Registro encontrado com sucesso na Hash Extensível: ");
                            jogo = DB_CRUD.readGame_Address(registro.getAddress()); //obter jogo
                            pos_registro = registro.getAddress(); //obter endereço do registro

                            achou = true;
                        }
                        else{
                            System.out.println("Não foi possível encontrar o registro na Hash.");
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                case 3 ->{
                    //pesquisa na lista invertida
                    try {
                        //pegar todos os elementos da categoria
                        ListaElemento[] resultado = DataBase.lista.read(categoria);

                        //procura o ID exato dentro dessa lista
                        ListaElemento registro = null;
                        for (ListaElemento e : resultado) {
                            if (e.getId() == delete_id) {
                                registro = e;
                                break;
                            }
                        }

                        if (registro != null) {
                            System.out.println("\n[Search] -> Registro encontrado na Lista Invertida:");
                            jogo = DB_CRUD.readGame_Address(registro.getAddress()); //obter jogo
                            pos_registro = registro.getAddress(); //obter endereço do registro
                            achou = true;
                        }
                        else{
                            System.out.println("Não foi possível encontrar o registro na categoria: '" + categoria + "' da Lista Invertida");
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                default -> {
                    System.out.println("[ERRO Crítico!!!] -> Indexação atual inválida");
                    DataBase.indexStatus = 0; //medida preventiva
                }
            }

            //caso tenha encontrado o registro
            if (achou){
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
                        arquivo.seek(pos_registro - 5); //posição do registro - 4 bytes (tamanho do registro) - 1 byte (onde está a lápide)
                        
                        //atualizar lápide como "inativa"
                        arquivo.writeByte(0xFF);
                        
                        //remover o registro da indexação, se existir
                        switch (DataBase.indexStatus) {
                            case 1 -> {
                                if (DataBase.arvore.delete(new ArvoreElemento(delete_id, -1)))
                                    System.out.println("[Delete] -> Removido da Árvore B+ com sucesso");
                            }
                            case 2 ->{
                                if (DataBase.hash.delete(delete_id))
                                    System.out.println("[Delete] -> Removido da Hash Extensivel com sucesso");
                            }
                            case 3 ->{
                                if (DataBase.lista.delete(categoria, delete_id))
                                    System.out.println("[Delete] -> Removido na categoria da Lista Invertida com sucesso");
                            }
                            default -> {}
                        }

                        //indicar que o registro foi removido com sucesso
                        resp = true;
                        
                        //decrementar numero de jogos ativos na base de dados
                        DataBase.totalGames--;
                        
                        //incrementar numero de jogos inativos na base de dados
                        DataBase.totalDeleted++;
                    } else {
                        System.out.println("[Delete] -> Remoção cancelada.");
                    }
                }catch (InputMismatchException e){
                    System.out.println("[ERRO] -> Não foi possível ler o valor digitado");
                    System.out.println(e);
                }
            } else {
                System.out.println("\n[Delete] -> Não foi possível localizar o registro a ser excluído.");
            }
        }
        catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível realizar a exclusão do registro.");
            System.out.println(e);
        }

        return resp;
    }
    
    public static boolean updateGame(int update_id, String categoria) {
        //inicializar variáveis de pesquisa/deletar
        boolean achou = false;
        boolean atualizado = false;

        try (RandomAccessFile arquivo = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "rw")){
            System.out.println("[Update] -> Procurando registro com o ID especificado...");

            //inicializar objeto jogo
            SteamGame jogo = new SteamGame();
            int old_tamanho = 0; //variável de controle para atualização da posição do registro
            long pos_registro = -1; //variável de controle para gravar a posição atual do registro
            boolean resp = false; //variável de controle para indicar se existe alguma atualização


            switch (DataBase.indexStatus) {
                case 0 -> {
                    //pesquisa sequencial

                    //estrutura útlimoId -> (lápide, tamanho do registro, dados)x N
                    //mover ponteiro para início do arquivo
                    arquivo.seek(0);

                    //pular ultimo id inserido
                    arquivo.skipBytes(4);
                    int atual = 2;

                    while (arquivo.getFilePointer() < arquivo.length() && !achou){
                        //mostrar barra de progresso
                        UI.progressBar(atual, (DataBase.totalGames + 1),"[Search]",4,0);
                        
                        //gravar a posição do registro atual
                        pos_registro = arquivo.getFilePointer();
        
                        //ler se a lápide está ativa
                        int lapide = arquivo.readUnsignedByte();
                        if (lapide == 0xFF){
                            //ler e pular o tamanho do registro a seguir
                            arquivo.skipBytes(arquivo.readInt());
                        }
                        else{
                            old_tamanho = arquivo.readInt(); //ler o tamanho do registro para tomar a decisão correta no momento da atualização
                            jogo = readGame(arquivo);
                            if (jogo.getId() == update_id){
                                achou = true; //indicar que o registro foi encontrado
                                System.out.println("[Search] -> Registro com sucesso! ");
                            }
                        }
                        atual++;
                    }
                }
                case 1 -> {
                    //pesquisa na árvore B+
                    try {
                        ArrayList<ArvoreElemento> lista = DataBase.arvore.read(new ArvoreElemento(update_id, -1));
                        if (!lista.isEmpty() ){
                            System.out.println("[Index] -> Registro encontrado com sucesso na Árvore B+: ");
                            for (int i = 0; i < lista.size(); i++){
                                jogo = DB_CRUD.readGame_Address(lista.get(i).getAddress()); //obter o jogo
                                pos_registro = lista.get(i).getAddress() - 1; //obter endereço do registro

                                //obter tamanho do registro
                                arquivo.seek(pos_registro - 4);
                                old_tamanho = arquivo.readInt();
                                achou = true;
                            }
                        }
                        else{
                            System.out.println("Não foi possível encontrar o registro na Árvore.");
                            achou = false;
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                case 2 -> {
                    //pesquisa na hash extensível
                    try {
                        HashElemento registro = DataBase.hash.read(HashElemento.hash(update_id));
                        if (registro != null ){
                            System.out.println("[Index] -> Registro encontrado com sucesso na Hash Extensível: ");
                            jogo = DB_CRUD.readGame_Address(registro.getAddress()); //obter jogo
                            pos_registro = registro.getAddress() - 1; //obter endereço do registro
                            //obter tamanho do registro
                            arquivo.seek(pos_registro - 4);
                            old_tamanho = arquivo.readInt();
                            achou = true;
                        }
                        else{
                            System.out.println("Não foi possível encontrar o registro na Hash.");
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                case 3 ->{
                    //pesquisa na lista invertida
                    try {
                        //pegar todos os elementos da categoria
                        ListaElemento[] resultado = DataBase.lista.read(categoria);

                        //procura o ID exato dentro dessa lista
                        ListaElemento registro = null;
                        for (ListaElemento e : resultado) {
                            if (e.getId() == update_id) {
                                registro = e;
                                break;
                            }
                        }

                        if (registro != null) {
                            System.out.println("\n[Search] -> Registro encontrado na Lista Invertida:");
                            jogo = DB_CRUD.readGame_Address(registro.getAddress()); //obter jogo
                            pos_registro = registro.getAddress() - 1; //obter endereço do registro
                            //obter tamanho do registro
                            arquivo.seek(pos_registro - 4);
                            old_tamanho = arquivo.readInt();
                            achou = true;
                        }
                        else{
                            System.out.println("Não foi possível encontrar o registro na categoria: '" + categoria + "' da Lista Invertida");
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                default -> {
                    System.out.println("[ERRO Crítico!!!] -> Indexação atual inválida");
                    DataBase.indexStatus = 0; //medida preventiva
                }
            }

            //se um jogo foi encontrado na pesquisa
            if (achou){
                jogo.printAll();

                //variável de controle do loop
                boolean stop = false;
                try {
                    //inicializar o scanner para ler a opção
                    Scanner leitor = new Scanner(System.in);

                    //loop menu de atualização
                    while (!stop){
                        //exibir interface de opções para atualizar
                        UI.update(jogo);

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
                                    leitor.nextLine();
                                    String nome = leitor.nextLine();
                                    jogo.setName(nome);
                                    resp = true; //indicar que o registro foi atualizado
                                }
                                case 3 -> {
                                    System.out.println("[Update] -> Valor atual da data de lançamento: " + jogo.getReleaseDateString());
                                    System.out.print("[Update] -> Digite a nova data de lançamento (AAAA-MM-DD): ");
                                    String data = leitor.next();
                                    //converter data e atualizar no registro
                                    jogo.setReleaseDate(DB_Services.convertString_Unix(data));
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

                            //se algum elemento foi atualizado
                            if (resp){
                                try {
                                    System.out.println("[Update] -> Atualizando o registro no arquivo...");

                                    //mover o ponteiro para onde o registro estava antes de ser atualizado
                                    arquivo.seek(pos_registro);

                                    //identificar se o registro atualizado está maior (em BYTES) que o registro antigo
                                    if (jogo.measureSize() > old_tamanho){
                                        //mudar a lápide para indicar que o antigo registro deve ser desconsiderado
                                        arquivo.writeByte(0xFF); //lapide inativa

                                        //atualizar número de registros inativos
                                        DataBase.totalDeleted++;

                                        //obter endereço do final do arquivo
                                        long endereco = (arquivo.length() + 5);//endereço final + lápide(byte) + tamanho(int -> 4 bytes) do registro

                                        //gravar o registro atualizado no final do arquivo
                                        if (writeGame(arquivo, jogo)){

                                            //atualizar os índices
                                            if (DataBase.indexStatus > 0){
                                                System.out.println("[Index] -> Atualizando endereço do registro no arquivo de índices...");
                                                switch (DataBase.indexStatus) {
                                                    case 1 -> {
                                                        //atualizar na árvore b+
                                                        if (!DataBase.arvore.update(new ArvoreElemento(jogo.getId(),endereco)))
                                                            System.out.println("[ERRO] -> Não foi possível atualizar o registro no arquivo de índices da Árvore B+");
                                                    }
                                                    case 2 -> {
                                                        //atualizar na hash extensível
                                                        if (!DataBase.hash.update(new HashElemento(jogo.getId(),endereco)))
                                                            System.out.println("[ERRO] -> Não foi possível atualizar o registro no arquivo de índices da Hash Extensível");
                                                    }
                                                    case 3 -> {
                                                        //atualizar na categoria da Lista Invertida
                                                        if (!DataBase.lista.update(categoria,jogo.getId(),endereco))
                                                            System.out.println("[ERRO] -> Não foi possível atualizar o registro no arquivo de índices da categoria '" + categoria + "' da Lista Invertida");
                                                    }
                                                    default -> {
                                                        System.out.println("[ERRO Crítico!!!] -> Indexação atual inválida");
                                                        DataBase.indexStatus = 0; //medida preventiva
                                                    }
                                                }
                                            }
                                            atualizado = true;
                                            System.out.println("[Update] -> Registro atualizado com sucesso");
                                        }
                                    } else{ //se o registro ter o tamanho igual ou menor, atualizar na mesma posição
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
                                            arquivo.write(buffer.toByteArray());
                                        }
                                    }
                                    
                                } catch (IOException e) {
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
            } else{
                System.out.println("\n[Update] -> Não foi possível localizar a ser atualizado.");
            }
        }
        catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível realizar a atualização do registro.");
            System.out.println(e);
        }

        return atualizado;
    }
    
}
