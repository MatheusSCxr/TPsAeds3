package services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

//classe responsável por realizar a criptografia e descriptografia simples
public class EncryptSimple {

    private static final int BLOCK_SIZE = 8192; //tamanho do chunk a ser lido e criptografado

    public static byte[] EncryptChunk(int offset, byte[] chunk){
        for (int i = 0; i < chunk.length; i++) {
        chunk[i] = (byte)(chunk[i] + offset);
        }
        return chunk;
    }

    public static byte[] DecryptChunk(int offset, byte[] chunk){
        for (int i = 0; i < chunk.length; i++) {
            chunk[i] = (byte)(chunk[i] - offset);
        }
        return chunk;
    }

    public static void DecryptDataBase(int offset) throws FileNotFoundException, IOException{
        File encrypted = new File("./src/resources/db_encrypted/EncryptedSimple.db");
        if (encrypted.exists()){
            //ler a base de dados e iniciar descriptografia
            try (RandomAccessFile decrypt = new RandomAccessFile("./src/resources/db_Output/DecryptedSimple.db", "rw"); RandomAccessFile encryptedDatabase = new RandomAccessFile("./src/resources/db_encrypted/EncryptedSimple.db", "r")) {

                long totalBytes = encryptedDatabase.length();
                long processedBytes = 0;

                while (encryptedDatabase.getFilePointer() < totalBytes) {
                    //obter tamanho do bloco a ser extraido. Será BLOCK_SIZE ou o restante caso seja menor que BLOCK_SIZE
                    int blockSize = (int) Math.min(BLOCK_SIZE, totalBytes - encryptedDatabase.getFilePointer());
                    byte[] blockData = new byte[blockSize];//criar vetor de bytes
                    encryptedDatabase.readFully(blockData);//preencher o vetor de bytes com um bloco da base de dados

                    byte[] decryptedData = DecryptChunk(offset, blockData);//aplica a descriptografia simples no bloco lido

                    decrypt.write(decryptedData);//escrever o bloco criptografado

                    processedBytes += blockSize;//contabilizar bytes processados
                    UI.progressBar((int)processedBytes,(int)totalBytes,"[Decrypt]",5,0);//barra de progresso
                }
                
                System.out.println();
                DB_Services.countGames(decrypt);
            }
        }
        else{
            System.out.println("[Decrypt] -> Não foi encontrada um base de dados criptografada com offsets");
        }
    }


    public static void EncryptDataBase(int offset) throws FileNotFoundException, IOException{
        File encrypted = new File("./src/resources/db_encrypted/EncryptedSimple.db");
        if (encrypted.exists())
            encrypted.delete();

        //ler a base de dados e iniciar criptografia
        try (RandomAccessFile encrypt = new RandomAccessFile("./src/resources/db_encrypted/EncryptedSimple.db", "rw"); RandomAccessFile database = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "r")) {

            long totalBytes = database.length();
            long processedBytes = 0;

            while (database.getFilePointer() < totalBytes) {
                //obter tamanho do bloco a ser extraido. Será BLOCK_SIZE ou o restante caso seja menor que BLOCK_SIZE
                int blockSize = (int) Math.min(BLOCK_SIZE, totalBytes - database.getFilePointer());
                byte[] blockData = new byte[blockSize];//criar vetor de bytes
                database.readFully(blockData);//preencher o vetor de bytes com um bloco da base de dados

                byte[] encryptedData = EncryptChunk(offset, blockData);//aplica a criptografia simples no bloco lido

                encrypt.write(encryptedData);//escrever o bloco comprimido

                processedBytes += blockSize;//contabilizar bytes processados
                UI.progressBar((int)processedBytes,(int)totalBytes,"[Encrypt]",5,0);//barra de progresso
            }
        }
    }
}
