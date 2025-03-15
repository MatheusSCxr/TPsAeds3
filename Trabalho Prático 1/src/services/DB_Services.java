package services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import main.DataBase;
import models.SteamGame;

public class DB_Services {
    
    public static void backupDatabase(){
        try {
            //criar arquivo de backup
            File dbOutputFile = new File("./src/resources/db_Output/gamesDB.db");
            if (dbOutputFile.exists()){
                File dbBackupFile = new File("./src/resources/db_Backup/gamesDB_backup.db");

                System.out.println("[Backup] -> Iniciando backup...");
                //fazer uma copia do banco de dados atual no arquivo de backup
                Files.copy(dbOutputFile.toPath(), dbBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[INFO] -> Um backup da base de dados foi criado com sucesso em (./src/resources/db_Backup/gamesDB_backup.db)");
    
                //substituir gamesDB.db por caminho_0.db
                File dbSortFile = new File("./src/resources/db_Sort/caminho_0.db");
                if (dbSortFile.exists()) {
                    //deletar a base de dados anterior
                    Files.delete(dbOutputFile.toPath());
    
                    System.out.println("[Backup] -> Substituindo arquivo do banco de dados...");
                    //renomear caminho_0 para gamesDB (copiar, mudando de nome e deletar antigo.)
                    Files.copy(Paths.get("./src/resources/db_Sort/caminho_0.db"), Paths.get("./src/resources/db_Output/gamesDB.db"), StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(Paths.get("./src/resources/db_Sort/caminho_0.db"));
    
                    System.out.println("[Backup] -> Base de dados substituída com sucesso");
                }
            }
            else{
                System.out.println("[INFO] -> Não foi encontrada uma base de dados para fazer um backup.");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void restoreDatabase(){
        try {
            //criar arquivo de substituição
            File dbOutputFile = new File("./src/resources/db_Output/gamesDB.db");
            File dbBackupFile = new File("./src/resources/db_Backup/gamesDB_backup.db");

            if (dbBackupFile.exists()) {
                System.out.println("[Restore] -> Iniciando restauração...");

                if (dbOutputFile.exists()){
                    //deletar a base de dados anterior
                    Files.delete(dbOutputFile.toPath());
                }
    
                // Fazer uma cópia do banco de dados do backup no arquivo atual de banco de dados
                Files.copy(dbBackupFile.toPath(), dbOutputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[Restore] -> Um backup da base de dados foi restaurado com sucesso em (db_Output/gamesDB.db)");
            } else {
                System.out.println("[INFO] -> Não foi possível encontrar um backup para restaurar em (db_Backup/gamesDB_backup.db)");
            }

        } catch (IOException e) {
            System.out.println("[ERRO] -> Não foi possível restaurar um backup.");
            System.out.println(e);
        }
    }
    //imprime os atributos ID e Nome de todos os elementos, ativos e inativos, de uma base de dados
    public static void printDataBase(){
        System.out.println("[Print] -> Imprimindo registros...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/resources/db_Output/printDataBase.txt")); RandomAccessFile arquivo = new RandomAccessFile("./src/resources/db_Output/gamesDB.db","r")) {
            int conta = 0;
            if (arquivo.length() != 0){
                arquivo.seek(0);
                arquivo.skipBytes(4); //pular ultimo id inserido
                while (arquivo.getFilePointer() < arquivo.length()){
                    arquivo.skipBytes(5); //pular lápides e tamanhos dos registros
                    SteamGame jogo = DB_CRUD.readGame(arquivo);
                    writer.write("[ID:" + jogo.getId() + "]\t\t[" + jogo.getName() + "]\n");
                    conta++;
                    UI.progressBar(conta, DataBase.totalGames, "[Print]", 7, 0);
                }
                writer.write("\n\nNúmero de registros -> " + conta);
                System.out.println("\n[Print] -> Arquivo com IDs e Nomes da base de dados criado com sucesso em (./src/resources/db_Output/printDataBase.txt)");   
            }
            else{
                System.out.println("[INFO] -> Não foi detectada uma base de dados em (./src/resources/db_Output/gamesDB.db)");
            }
       } catch (IOException e) {
            System.out.println("[ERRO] -> Não foi possível criar um arquivo com IDs e Nomes da base de dados.");
        }
    }

    //converte uma string de data para UNIX
    public static long convertString_Unix(String valor){
        long timestamp;
        String[] calendario = valor.split("-");

        //formatar no tipo LocalDate
        LocalDate data = LocalDate.of(Integer.parseInt(calendario[0]), Integer.parseInt(calendario[1]), Integer.parseInt(calendario[2]));
        
        //converter para Unix timestamp
        timestamp = data.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        return timestamp;
    }

    public static int countGames(RandomAccessFile arquivo){
        //método para contabilizar todos os jogos no arquivo de registros
        int totalActive = 0;
        int totalInactive = 0;

        System.out.println("[INFO] -> Iniciando contagem de registros... Por favor, aguarde.");
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
        DataBase.totalGames = totalActive;
        DataBase.totalDeleted = totalInactive;

        //retornar o número de registros ativos
        return totalActive;
    }

}
