package services;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

//classe responsável por realizar a criptografia e descriptografia por RSA
//foi usado o site www.geeksforgeeks.org como base para a implementação
public class EncryptRSA {
    private static final int KEY_SIZE = 1024; // tamanho máximo em bits da chave

    // Geração de chaves com primos (prováveis) grandes e aleatórios
    public static BigInteger[] generateRandomKeys() {
        SecureRandom random = new SecureRandom();

        //calculos inicias das chaves
        BigInteger p = BigInteger.probablePrime(KEY_SIZE / 2, random); //gera um numero primo grande de 512 bits
        BigInteger q = BigInteger.probablePrime(KEY_SIZE / 2, random); //gera um numero primo grande de 512 bits
        BigInteger[] chaves = new BigInteger[3];
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Calcular e, onde 1 < e < phi(n) e gcd(e, phi(n)) == 1
        BigInteger e = BigInteger.ZERO;
        for (e = new BigInteger("2"); e.compareTo(phi) < 0; e = e.add(BigInteger.ONE)) {
            if (e.gcd(phi).equals(BigInteger.ONE)) {
                break;
            }
        }

        // Calcular d -> e * d ≡ 1 (mod phi(n))
        BigInteger d = e.modInverse(phi);
        chaves[0] = e;
        chaves[1] = d;
        chaves[2] = n;

        return chaves;
    }

    // Desabilitado por ser pouco seguro. P e Q precisam ser números muito grandes.
    // // Geração de chaves com primos passados por parâmetro
    // public static BigInteger[] generateKeys(BigInteger p, BigInteger q) {
    // BigInteger[] chaves = new BigInteger[3];

    // if (p.bitLength() > KEY_SIZE/2 || q.bitLength()> KEY_SIZE/2){
    // SecureRandom random = new SecureRandom();
    // p = BigInteger.probablePrime(KEY_SIZE / 2, random);
    // q = BigInteger.probablePrime(KEY_SIZE / 2, random);
    // }
    // BigInteger n = p.multiply(q);
    // BigInteger phi =
    // p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

    // // Calcular e, onde 1 < e < phi(n) e gcd(e, phi(n)) == 1
    // BigInteger e = BigInteger.ZERO;
    // for (e = new BigInteger("2"); e.compareTo(phi) < 0; e =
    // e.add(BigInteger.ONE)) {
    // if (e.gcd(phi).equals(BigInteger.ONE)) {
    // break;
    // }
    // }

    // // Calcular d -> e * d ≡ 1 (mod phi(n))
    // BigInteger d = e.modInverse(phi);
    // chaves[0] = e;
    // chaves[1] = d;
    // chaves[2] = n;

    // return chaves;
    // }

    // Criptografa o arquivo
    public static void EncryptDataBase() throws IOException { // Parâmetros redundantes removidos
        File inputFile = new File("./src/resources/db_Output/gamesDB.db");
        File outputFile = new File("./src/resources/db_encrypted/EncryptedRSA.db");

        if (outputFile.exists())
            outputFile.delete();

        try (InputStream in = new FileInputStream(inputFile);
                DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile))) {

            BigInteger[] chaves = generateRandomKeys();
            BigInteger e = chaves[0];
            BigInteger n = chaves[2];

            // Escreve o módulo n no início do arquivo
            byte[] nBytes = n.toByteArray();
            out.writeInt(nBytes.length);
            out.write(nBytes);

            // Tamanho do bloco: tamanho da chave menos overhead de padding
            int BLOCK_SIZE = (KEY_SIZE / 8) - 11;
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;

            // Variáveis da barra de progresso
            long totalBytes = inputFile.length();
            long processedBytes = 0;

            // Processa o arquivo em blocos
            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] block = Arrays.copyOf(buffer, bytesRead); // Bloco exato do tamanho lido
                BigInteger plain = new BigInteger(1, block); // Converte para BigInteger
                BigInteger encrypted = plain.modPow(e, n); // Criptografa: plain^e mod n

                byte[] encryptedBytes = encrypted.toByteArray();
                out.writeInt(encryptedBytes.length); // Tamanho do bloco criptografado
                out.write(encryptedBytes);
                out.writeInt(bytesRead); // Armazena tamanho original para descriptografia

                processedBytes += BLOCK_SIZE;
                UI.progressBar((int)processedBytes,(int)totalBytes,"[Encrypt]",5,0);//barra de progresso
            }

            // Log das chaves geradas (APENAS PARA DEMONSTRAÇÃO)
            System.out.println("\n[Encrypt] -> Chaves geradas:");
            System.out.println("[Encrypt] -> Chave Pública (e): " + chaves[0]);
            System.out.println("[Encrypt] -> Chave Privada (d): " + chaves[1]);
            System.out.println("[Encrypt] -> Módulo (n): " + chaves[2]);
        }
    }

    // Descriptografa o arquivo usando chave privada no parâmetro
    public static void DecryptDataBase(BigInteger d) throws IOException {
        File inputFile = new File("./src/resources/db_encrypted/EncryptedRSA.db");
        File outputFile = new File("./src/resources/db_Output/DecryptedRSA.db");

        try (DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
                OutputStream out = new FileOutputStream(outputFile)) {

            // Lê o módulo n do início do arquivo
            int nLen = in.readInt();
            byte[] nBytes = new byte[nLen];
            in.readFully(nBytes);
            BigInteger n = new BigInteger(nBytes);

            // Variáveis da barra de progresso
            long totalBytes = inputFile.length();
            long processedBytes = 0;

            // Processa cada bloco criptografado
            while (in.available() > 0) {
                int encryptedSize = in.readInt(); // Tamanho do bloco criptografado
                byte[] encryptedBytes = new byte[encryptedSize];
                in.readFully(encryptedBytes);
                int originalSize = in.readInt(); // Tamanho original do bloco

                BigInteger encrypted = new BigInteger(1, encryptedBytes);
                BigInteger decrypted = encrypted.modPow(d, n); // Descriptografa: encrypted^d mod n
                byte[] decryptedBytes = decrypted.toByteArray();

                // Remove byte de padding zero se necessário
                if (decryptedBytes.length > originalSize && decryptedBytes[0] == 0) {
                    decryptedBytes = Arrays.copyOfRange(decryptedBytes, 1, decryptedBytes.length);
                }

                // Ajusta para tamanho original com padding à esquerda
                byte[] finalBlock = new byte[originalSize];
                int startPos = Math.max(0, originalSize - decryptedBytes.length);
                System.arraycopy(
                        decryptedBytes, 0,
                        finalBlock, startPos,
                        Math.min(decryptedBytes.length, originalSize));

                out.write(finalBlock);

                processedBytes += (4 + encryptedSize + 4);
                UI.progressBar((int)processedBytes,(int)totalBytes,"[Decrypt]",5,0);//barra de progresso
            }
        }
    }
}