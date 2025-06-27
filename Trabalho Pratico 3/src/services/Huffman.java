package services;

import java.io.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import models.VetorDeBits;

/**
 * Essa classe é uma alteração da implementação pelo prof. Marcos Kutova
 * Agora ela é capaz de codificar/decoficar uma sequência de blocos de bytes de um arquivo, não estando limitada apenas a strings.
 **/

class HuffmanNode implements Comparable<HuffmanNode> {
    byte b;
    int frequencia;
    HuffmanNode esquerdo, direito;

    public HuffmanNode(byte b, int f) {
        this.b = b;
        this.frequencia = f;
        esquerdo = direito = null;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return this.frequencia - o.frequencia;
    }
}

public class Huffman {
    private static final int BLOCK_SIZE = 8192;

    public static void compress_DataBase() throws IOException {
        File file = new File("./src/resources/db_Output/gamesDB.db");
        File file2 = new File("./src/resources/db_compress/HuffmanCompressed_DB.db");

        //calcular frequências
        Map<Byte, Integer> freqMap = new HashMap<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesLidos;
            while ((bytesLidos = raf.read(buffer)) != -1) {
                for (int i = 0; i < bytesLidos; i++) {
                    byte b = buffer[i];
                    freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
                }
            }
        }

        //construir árvore e códigos
        HuffmanNode raiz = construirArvore(freqMap);
        Map<Byte, String> codigos = new HashMap<>();
        constroiCodigos(raiz, "", codigos);

        //contar tempo total da compressão
        long tempo_inicio = System.currentTimeMillis();

        //escrever cabeçalho e dados compactados
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"); DataOutputStream dos = new DataOutputStream(new FileOutputStream(file2))) {
            int processedBytes = 0;//variável de controle

            //escrever tamanho original e dicionário
            dos.writeLong(raf.length());
            dos.writeInt(freqMap.size());
            for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
                dos.writeByte(entry.getKey());
                dos.writeInt(entry.getValue());
            }

            BitSet tempBitSet = new BitSet(); //BitSet temporário para acumular bits
            int bitCount = 0;

            //ler o arquivo novamente para codificar
            raf.seek(0);
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesLidos;

            //codificação
            
            while ((bytesLidos = raf.read(buffer)) != -1) {
                for (int i = 0; i < bytesLidos; i++) {
                    String codigo = codigos.get(buffer[i]);
                    for (char c : codigo.toCharArray()) {
                        if (c == '1') {
                            tempBitSet.set(bitCount);
                        }
                        bitCount++;
                    }
                }
                
                processedBytes += bytesLidos;//incrementar contador
                UI.progressBar((int)processedBytes,(int)raf.length(),"[Comp]",5,0);//barra de progresso
            }

            //converter BitSet para array de bytes
            byte[] dadosCompactados = tempBitSet.toByteArray();

            //escrever número de bits válidos e tamanho do array
            dos.writeInt(bitCount);
            dos.writeInt(dadosCompactados.length);
            dos.write(dadosCompactados);
        }

        //contar tempo total da compressão
        long tempo_fim = System.currentTimeMillis();
        long tempo = (tempo_fim - tempo_inicio); // dividir por 1000 para contar em segundos

        System.out.println("\n[Comp] -> Comprimido com sucesso! (\"./src/resources/db_compress/LZWCompressed_DB.db\")");
        System.out.println("[Comp] -> Tamanho arquivo original: " + file.length() + "B \t Tamanho arquivo comprimido: " + file2.length() +"B");
        double taxaCompressao = 100 * ((double) file2.length() / file.length());//taxa de compressão
        System.out.println("[Comp] -> Taxa de compressão: " + String.format("%.2f", taxaCompressao) + "% \t Tempo decorrido: " + tempo/1000.0 + "s");
    }

public static void decompress_DataBase() throws IOException {
        File file = new File("./src/resources/db_Output/HuffmanDECompressed_DB.db");
        File file2 = new File("./src/resources/db_compress/HuffmanCompressed_DB.db");

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file2)); RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            //obter o cabeçalho
            long tamanhoOriginal = dis.readLong();
            int numEntradas = dis.readInt();
            Map<Byte, Integer> freqMap = new HashMap<>();
            for (int i = 0; i < numEntradas; i++) {
                byte b = dis.readByte();
                int freq = dis.readInt();
                freqMap.put(b, freq);
            }

            //ler número de bits válidos e tamanho do array
            int bitCount = dis.readInt();
            int numBytes = dis.readInt();
            byte[] dadosCompactados = new byte[numBytes];
            dis.readFully(dadosCompactados);

            //criar VetorDeBits a partir dos dados compactados
            VetorDeBits vetorBits = new VetorDeBits(dadosCompactados);

            //reconstruir árvore
            HuffmanNode raiz = construirArvore(freqMap);

            //decodificar usando a árvore
            HuffmanNode noAtual = raiz;
            long bytesEscritos = 0;
            raf.setLength(0); // Limpar o arquivo

            for (int i = 0; i < bitCount && bytesEscritos < tamanhoOriginal; i++) {
                boolean bit = vetorBits.get(i);
                noAtual = bit ? noAtual.direito : noAtual.esquerdo;

                if (noAtual.esquerdo == null && noAtual.direito == null) {
                    raf.write(noAtual.b);
                    bytesEscritos++;
                    noAtual = raiz;
                }
            }
            UI.progressBar((int)bytesEscritos, (int)tamanhoOriginal, "[DEComp]", 5, 0);
        }
    }

    private static HuffmanNode construirArvore(Map<Byte, Integer> freqMap) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            HuffmanNode esquerdo = pq.poll();
            HuffmanNode direito = pq.poll();

            HuffmanNode pai = new HuffmanNode((byte) 0, esquerdo.frequencia + direito.frequencia);
            pai.esquerdo = esquerdo;
            pai.direito = direito;
            pq.add(pai);
        }
        return pq.poll();
    }

    private static void constroiCodigos(HuffmanNode no, String codigo, Map<Byte, String> codigos) {
        //percorrer a árvore pós-ordem
        if (no == null) return;
        if (no.esquerdo == null && no.direito == null) {
            codigos.put(no.b, codigo);
            return;
        }
        constroiCodigos(no.esquerdo, codigo + "0", codigos);
        constroiCodigos(no.direito, codigo + "1", codigos);
    }
}