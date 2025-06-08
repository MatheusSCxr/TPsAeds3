package services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import models.VetorDeBits;

/**
 * Essa classe é uma alteração da implementação pelo prof. Marcos Kutova
 * Agora ela é capaz de codificar/decoficar uma sequência de blocos de bytes de um arquivo, não estando limitada apenas a strings.
 **/

public class LZW {
    public static final int BITS_POR_INDICE = 11; // Mínimo de 9 bits por índice (512 itens no dicionário). Atualemente, 11 é o mais adequado para a base de dados usada nesse projeto.
    public static final int BLOCK_SIZE = 8192; // Tamanho fixo dos blocos que serão comprimidos
    public static ArrayList<ArrayList<Byte>> dicionarioCompressao;
    public static ArrayList<ArrayList<Byte>> dicionarioDESCompressao;

    //método experimental mais eficiente para leitura completa da base de dados.
    public static void compress_DataBase() throws Exception {
        //deletar base de dados comprimida, se existir
        File compressedFile = new File("./src/resources/db_compress/LZWCompressed_DB.db");
        if (compressedFile.exists())
            compressedFile.delete();

        //ler a base de dados e iniciar compresssão
        try (RandomAccessFile compressed = new RandomAccessFile(compressedFile, "rw"); RandomAccessFile database = new RandomAccessFile("./src/resources/db_Output/gamesDB.db", "r")) {

            //copiar último ID
            compressed.writeInt(database.readInt());

            long totalBytes = database.length();
            long processedBytes = 4; //contabilizar

            //inicializar dicionário da compressão
            //nota: apenas 1 dicionário é usado para a compressão inteira
            dicionarioCompressao = criarDicionarioInicial();
            while (database.getFilePointer() < totalBytes) {
                //obter tamanho do bloco a ser extraido. Será BLOCK_SIZE ou o restante caso seja menor que BLOCK_SIZE
                int blockSize = (int) Math.min(BLOCK_SIZE, totalBytes - database.getFilePointer());
                byte[] blockData = new byte[blockSize];//criar vetor de bytes
                database.readFully(blockData);//preencher o vetor de bytes com um bloco da base de dados

                byte[] compressedData = codifica(blockData);//codificar o bloco lido

                compressed.writeShort(compressedData.length);//escrever o tamanho do bloco comprimido
                compressed.write(compressedData);//escrever o bloco comprimido

                processedBytes += blockSize + 2;//contabilizar
                UI.progressBar((int)processedBytes,(int)totalBytes,"[Compress]",5,0);//barra de progresso
            }
        }
    }

    public static void decompress_DataBase() throws Exception {
        //obter base de dados comprimida, se existir
        File compressedFile = new File("./src/resources/db_compress/LZWCompressed_DB.db");
        if (!compressedFile.exists()) {
            throw new FileNotFoundException("Compressed file not found!");
        }

        //deletar base de dados descomprimida, se existir
        File decompressedFile = new File("./src/resources/db_Output/LZWDECompressed_DB.db");
        if (decompressedFile.exists())
            decompressedFile.delete();

        try (RandomAccessFile compressed = new RandomAccessFile(compressedFile, "r");
                RandomAccessFile decompressed = new RandomAccessFile(decompressedFile, "rw")) {

            //copiar último ID
            decompressed.writeInt(compressed.readInt());

            long totalBytes = compressed.length();
            long processedBytes = 4;//contabilizar

            //inicializar dicionário da descompressão
            dicionarioDESCompressao = criarDicionarioInicial();

            //percorrer arquivo comprimido, desfazendo a compressão
            while (compressed.getFilePointer() < compressed.length()) {
                int compressedSize = compressed.readShort();//obter tamanho do bloco comprimido
                processedBytes += 2;//contabilizar

                byte[] compressedData = new byte[compressedSize];
                compressed.readFully(compressedData);//obter bloco comprimido

                byte[] originalData = decodifica(compressedData);//descomprimir o bloco
                decompressed.write(originalData);//escrever bloco descomprimido
                processedBytes += compressedSize;//contabilizar

                UI.progressBar((int)processedBytes,(int)totalBytes,"[DeCompress]",5,0);//barra de progresso
            }
        }
    }

    // CODIFICAÇÃO POR LZW
    // Usa a mensagem na forma de um vetor de bytes, para
    // eliminar a variação da quantidade de bytes por caráter do UTF-8
    // Os valores de bytes variarão entre -128 e 127, considerando que,
    // em Java, não existe o tipo Unsigned Byte
    private static ArrayList<ArrayList<Byte>> criarDicionarioInicial() {
        //cria um dicionário com bytes entre -128 e 127
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
        for (int j = -128; j < 128; j++) {
            byte b = (byte) j;
            ArrayList<Byte> vetorBytes = new ArrayList<>();
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }
        return dicionario;
    }

    //processo de codificação
    public static byte[] codifica(byte[] mensagem) throws Exception {
        ArrayList<Integer> saida = new ArrayList<>();//criar vetor de saída
        int i = 0;

        //percorrer a mensagem
        while (i < mensagem.length) {
            //criar vetor de bytes para a mensagem
            ArrayList<Byte> vetorBytes = new ArrayList<>();
            byte b = mensagem[i];
            vetorBytes.add(b);
            int indice = dicionarioCompressao.indexOf(vetorBytes);//adicionar ao dicionário
            int ultimoIndice = indice;

            //enquanto encontrar no dicionário e não chegar no final
            while (indice != -1 && i < mensagem.length - 1) {
                i++;
                b = mensagem[i];
                vetorBytes.add(b);
                indice = dicionarioCompressao.indexOf(vetorBytes);
                if (indice != -1)
                    ultimoIndice = indice;
            }

            saida.add(ultimoIndice);

            //adiciona ao dicionário se houver espaço
            if (dicionarioCompressao.size() < (1 << BITS_POR_INDICE)) {
                dicionarioCompressao.add(vetorBytes);
            }

            //se chegar ao final da mensagem
            if (indice != -1 && i == mensagem.length - 1)
                break;
        }

        //escrever bits na ordem MSB first
        VetorDeBits bits = new VetorDeBits(saida.size() * BITS_POR_INDICE);
        int bitIndex = 0;

        //converte cada índice para bits
        for (int idx : saida) {
            for (int j = BITS_POR_INDICE - 1; j >= 0; j--) {
                if ((idx & (1 << j)) != 0) {
                    bits.set(bitIndex);
                }
                bitIndex++;
            }
        }

        return bits.toByteArray();
    }

    //processo de decodificação
    public static byte[] decodifica(byte[] msgCodificada) throws Exception {
        VetorDeBits bits = new VetorDeBits(msgCodificada);
        int totalBits = bits.length();
        int numIndices = totalBits / BITS_POR_INDICE; //calcular o número de bits por indice da mensagem

        ArrayList<Integer> indices = new ArrayList<>();
        int bitIndex = 0;

        //ler os bits e reconstruir os índices
        for (int i = 0; i < numIndices; i++) {
            int code = 0;
            for (int j = 0; j < BITS_POR_INDICE; j++) {
                code = (code << 1) | (bits.get(bitIndex++) ? 1 : 0);
            }
            indices.add(code);
        }

        ArrayList<Byte> msgBytes = new ArrayList<>();
        ArrayList<Byte> previous = new ArrayList<>();

        //reconstruir a mensagem original
        for (int code : indices) {
            ArrayList<Byte> sequence;

            //trata códigos conhecidos, novos e inválidos
            if (code < dicionarioDESCompressao.size()) {
                sequence = new ArrayList<>(dicionarioDESCompressao.get(code));
            } else if (code == dicionarioDESCompressao.size()) {
                sequence = new ArrayList<>(previous);
                if (!previous.isEmpty()) {
                    sequence.add(previous.get(0));
                }
            } else {
                throw new IOException("Código inválido: " + code);
            }

            //adiciona bytes à mensagem
            for (Byte b : sequence) {
                msgBytes.add(b);
            }

            //atualiza dicionário durante a descompressão
            if (!previous.isEmpty() && dicionarioDESCompressao.size() < (1 << BITS_POR_INDICE)) {
                ArrayList<Byte> newEntry = new ArrayList<>(previous);
                newEntry.add(sequence.get(0));
                dicionarioDESCompressao.add(newEntry);
            }

            previous = sequence;
        }

        //converte para array de bytes
        byte[] result = new byte[msgBytes.size()];
        for (int i = 0; i < msgBytes.size(); i++) {
            result[i] = msgBytes.get(i);
        }
        return result;
    }
}