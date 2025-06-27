package services;

import java.util.ArrayList;

//classe KMP responsável por realizar a busca por casamento de padrões
//foi usado o site www.geeksforgeeks.org como base para a implementação
public class KMP {

    public static void vetorDeslocamentos(String padrao, int[] vetDeslocamentos) {
        int maior = 0;//comprimento do maior prefixo que também é sufixo para o índice anterior

        //pos [0] é sempre 0 pois não há prefixo/sufixo para um único caractere
        vetDeslocamentos[0] = 0;

        int i = 1;
        //processar o padrão
        while (i < padrao.length()) {
            
            //se os caracteres coincidem, incrementamos o tamanho do vetDeslocamentos
            if (padrao.charAt(i) == padrao.charAt(maior)) {
                maior++;
                vetDeslocamentos[i] = maior;
                i++;
            }
            
            //se houver incompatibilidade de caracteres
            else {
                if (maior != 0) {
                    maior = vetDeslocamentos[maior - 1];//atualiza maior para o valor vetDeslocamentos anterior para evitar comparações redundantes
                } 
                else {
                    vetDeslocamentos[i] = 0;//se nenhum prefixo for encontrado usar 0
                    i++;
                }
            }
        }
    }

    //função de busca continua
    public static ArrayList<Integer> search(String padrao, String texto) {
        int totalSaltos = 0;

        int n = texto.length();
        int m = padrao.length();

        //criar vetDeslocamentos para o padrão
        int[] vetDeslocamentos = new int[m];
        ArrayList<Integer> ocorrencias = new ArrayList<>();

        vetorDeslocamentos(padrao, vetDeslocamentos);

        //i (para o texto) e j (para o padrão)
        int i = 0;
        int j = 0;

        while (i < n) {
            //se os caracteres baterem, então continuar
            if (texto.charAt(i) == padrao.charAt(j)) {
                i++;
                j++;

                //se todo o padrão foi encontrado
                if (j == m) {
                    ocorrencias.add(i - j); //adicionar o indice inicial da ocorrência

                    j = vetDeslocamentos[j - 1]; //saltar usando vetDeslocamentos do índice anterior
                    totalSaltos++;
                }
            }
            //se os caracteres forem diferentes
            else {
                //saltar usando vetDeslocamentos do índice anterior
                if (j != 0){
                    j = vetDeslocamentos[j - 1];
                    totalSaltos++;
                }
                else{
                    i++;
                }
            }
        }
        
        if (ocorrencias.size() > 0)
            System.out.println("\n[Search] -> Total de Saltos: " + totalSaltos);
        return ocorrencias; 
    }
}