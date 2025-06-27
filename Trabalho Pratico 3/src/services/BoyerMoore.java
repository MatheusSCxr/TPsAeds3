package services;

import java.util.ArrayList;

//classe BoyerMoore responsável por realizar a busca por casamento de padrões
//foi usado o site www.geeksforgeeks.org como base para a implementação
public class BoyerMoore {

    public static final int num_caracteres = 256; //caracteres pertencem ao conjunto ASCII

    //obter o máximo entre dois inteiros
    private static int max(int a, int b) { 
        return (a > b) ? a : b; 
    }

    //pré-processamento para o vetor de caractere ruim
    
    public static void caractereRuim(char[] texto, int tam, int caractereRuim[]) {
        //inicializa todas as ocorrências como -1
        for (int i = 0; i < num_caracteres; i++)
            caractereRuim[i] = -1;

        //preenche o valor real da última ocorrência de cada caractere
        for (int i = 0; i < tam; i++)
            caractereRuim[(int) texto[i]] = i;
    }

    //pré-processamento para o vetor de sufixo bom
    public static int[] ProcessarSufixoBom(char pat[]) {
        int m = pat.length;
        int[] sufixosBons = new int[m];
        int[] suff = new int[m];
        
        //calcular o array de sufixos
        suff[m - 1] = m;
        for (int i = m - 2; i >= 0; i--) {
            int k = i;
            while (k >= 0 && pat[k] == pat[m - 1 - (i - k)]) {
                k--;
            }
            suff[i] = i - k;
        }

        //inicializar vetor de sufixos bons
        for (int i = 0; i < m; i++) {
            sufixosBons[i] = m;
        }
        
        //caso 2: sufixo que coincide com prefixo
        for (int i = m - 1, j = 0; i >= 0; i--) {
            if (suff[i] == i + 1) {
                for (; j < m - 1 - i; j++) {
                    if (sufixosBons[j] == m) {
                        sufixosBons[j] = m - 1 - i;
                    }
                }
            }
        }
        
        //caso 1: Sufixo dentro do padrão
        for (int i = 0; i < m - 1; i++) {
            sufixosBons[m - 1 - suff[i]] = m - 1 - i;
        }
        
        return sufixosBons;
    }

    //função de busca por caractere ruim e sufixo bom
    public static ArrayList<Integer> search(String padrao, String texto) {
        //variáveis de controle
        int totalDeslocamento_CR = 0;//deslocamentos por Caractere Ruim
        int totalDeslocamento_SB = 0;//deslocamentos por Sufixo Bom

        //converter as strings para vetor de caracteres
        char[] texto_vet = texto.toCharArray();
        char[] padrao_vet = padrao.toCharArray();

        ArrayList<Integer> resp = new ArrayList<>();
        int m = padrao_vet.length;
        int n = texto_vet.length;

        int charRuim[] = new int[num_caracteres];
        caractereRuim(padrao_vet, m, charRuim); //calcular deslocamento para caracteres ruins

        int[] sufixosBons = ProcessarSufixoBom(padrao_vet); //calcular deslocamento para sufixos bons

        int s = 0; //deslocamento do padrão
        while (s <= (n - m)) {
            int j = m - 1;

            //reduz j enquanto os caracteres coincidirem, percorrendo o padrão
            while (j >= 0 && padrao_vet[j] == texto_vet[s + j])
                j--;

            if (j < 0) {//se encontrar padrão
                resp.add(s);//adicionar pos no arraylist de respostas
                //deslocar pelo maior entre sufixosBons[0] e deslocamento do caractere ruim
                int shift = Math.max(sufixosBons[0], (s + m < n) ? m - charRuim[texto_vet[s + m]] : 1);
                s += shift; //atualizar posição
            } else {
                //calcular ambos deslocamentos
                int deslocamentoCR = max(1, j - charRuim[texto_vet[s + j]]); //deslocamento por caractere ruim (CR)
                int deslocamentoSB = sufixosBons[j]; //deslocamento por sufixo bom (SB)
                int maiorDeslocamento = max(deslocamentoCR, deslocamentoSB);
                if (maiorDeslocamento == deslocamentoCR){
                    totalDeslocamento_CR++;//registrar deslocamento
                }
                else{
                    totalDeslocamento_SB++;//registrar deslocamento
                }
                s += maiorDeslocamento; //obter o maior deslocamento entre os dois
            }
        }
        if (resp.size() > 0){
            System.out.println("\n[Search] -> Total de deslocamentos por Caractere Ruim: " + totalDeslocamento_CR);
            System.out.println("[Search] -> Total de deslocamentos por Sufixo Bom: " + totalDeslocamento_SB);
        }
        return resp;
    }
}