package services;

import models.SteamGame;

public class UI {
    public static void menu(int totalGames, int totalDeleted){
        System.out.println("\n----------------------- [ MENU ] -----------------------");
        System.out.println("Registros ativos -> [" + totalGames + "]        Registros inativos -> [" + totalDeleted + "]");
        System.out.println("[1] - Criar Arquivo com todos os registros do CSV (primeiro -> último; Já ordenado por appId)");
        System.out.println("[2] - Criar Arquivo com um número N de registros do CSV (primeiro -> último; Já ordenado por appId)");
        System.out.println("[3] - Procurar por um atributo nos registros [ID, appID, Nome]");
        System.out.println("[4] - Criar um registro no arquivo de banco de dados");
        System.out.println("[5] - Remover um registro no arquivo de banco de dados [por ID]");
        System.out.println("[6] - Atualizar um registro no arquivo de banco de dados [por ID]");
        System.out.println("[7] - Ordenar arquivo de registros (ID ou Nome)");
        System.out.println("[8] - Criar um backup da base de dados atual");
        System.out.println("[9] - Restaurar um backup para a base de dados atual");
        System.out.println("[10] - Imprimir em um arquivo todos os registros (ativos e inativos) da base de dados (ID e Nome)");
        System.out.println("[101][ DEBUG ] - Criar Arquivo com todos os registros do CSV [Aviso: LENTO]");
        System.out.println("[102][ DEBUG ] - Criar Arquivo com um número N de registros do CSV (primeiro -> último) [Aviso: LENTO]");
        System.out.println("[107][ DEBUG ] - Ordenar a base de dados por Nome [Aviso: LENTO]");
        System.out.println("[0] - Encerrar o programa");
        System.out.println("--------------------------------------------------------");
    }

    public static void search(){
        System.out.println("\n----------------------- [ Pesquisar ] -----------------------");
        System.out.println("[1] - Pesquisar pelo ID");
        System.out.println("[2] - Pesquisar pelo appId");
        System.out.println("[3] - Pesquisar pelo nome");
        System.out.println("\n[0] - Voltar ao menu principal");
        System.out.println("--------------------------------------------------------");

        System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
    }

    public static void create(){
        System.out.println("\n----------------------- [ Criar ] -----------------------");
        System.out.println("[1] - Criar e adicionar registro com construtor padrão");
        System.out.println("[2] - Criar e adicionar registro customizado");
        System.out.println("\n[0] - Voltar ao menu principal");
        System.out.println("--------------------------------------------------------");

        System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
    }

    public static void update(SteamGame jogo) {
        System.out.println("\n----------------------- [ Atualizar ] -----------------------");
        System.out.println("[1] - [Int] Atualizar appId -> " + jogo.getAppid());
        System.out.println("[2] - [String] Atualizar nome -> " + jogo.getName());
        System.out.println("[3] - [String (AAAA-MM-DD)] Atualizar data de lançamento -> " + jogo.getReleaseDateString());
        System.out.println("[4] - [Boolean] Atualizar se está em inglês -> " + jogo.getEnglish());
        System.out.println("[5] - [String] Atualizar desenvolvedor -> " + jogo.getDeveloper());
        System.out.println("[6] - [String] Atualizar publisher -> " + jogo.getPublisher());
        System.out.println("[7] - [String] Atualizar plataformas -> " + jogo.getPlatforms());
        System.out.println("[8] - [Int] Atualizar idade requerida -> " + jogo.getRequiredAge());
        System.out.println("[9] - [List<String>] Atualizar categorias -> " + jogo.getCategories());
        System.out.println("[10] - [List<String>] Atualizar gêneros -> " + jogo.getGenres());
        System.out.println("[11] - [List<String>] Atualizar tags do SteamSpy -> " + jogo.getSteamspyTags());
        System.out.println("[12] - [Int] Atualizar conquistas -> " + jogo.getAchievements());
        System.out.println("[13] - [Int] Atualizar avaliações positivas -> " + jogo.getPositiveRatings());
        System.out.println("[14] - [Int] Atualizar avaliações negativas -> " + jogo.getNegativeRatings());
        System.out.println("[15] - [Int] Atualizar tempo médio de jogo -> " + jogo.getAveragePlaytime());
        System.out.println("[16] - [Int] Atualizar tempo mediano de jogo -> " + jogo.getMedianPlaytime());
        System.out.println("[17] - [String] Atualizar proprietários -> " + jogo.getOwners());
        System.out.println("[18] - [Float] Atualizar preço -> " + jogo.getPrice());
        System.out.println("\n[0] - Voltar ao menu principal");
        System.out.println("--------------------------------------------------------");
        System.out.print("\n[Escolha] -> Digite o número de uma das opções acima: ");
    }
   
    public static void progressBar(int atual, int total, String operador, int tipo, int tempo_restante){
        //tipos:
        //csvExtract -> 1
        //Search -> 2
        //Delete -> 3
        //Update -> 4
        //Sort -> 5
        //Intercalação -> 6

        //definir propoção/tamanho da barra
        int ratio = 50;

        //calcular o progresso na proproção
        float progress;

        if (tipo != 6)
            progress = (float)(((double)atual/total) * ratio); // (total / total) = 1 * proproção = proporção 0 a 100%
        else{   //se for intercalação, a contagem é inversa
            progress = (float)(((1 - (double)atual/total)) * ratio); // de 100 a 0%
        }
        
        //criar barra
        StringBuilder barra = new StringBuilder(operador + " -> [");

        //preencher barra de acordo com o progresso
        for (int i = 0; i < ratio; i++){
            if (i < progress){
                barra.append(":");
            }
            else{
                barra.append(" ");
            }
        }
        barra.append("]");

        //adicionar porcentagem
        barra.append("   [").append(String.format("%.1f", ((progress/ratio)*100))).append("%]");

        //exibir registro atual
        switch (tipo) {
            case 1 -> {
                barra.append("   Escrevendo registro: ").append(atual - 1).append(" de ").append(total - 1).append("\t");
            }
            case 2,3,4 -> {
                barra.append("   Procurando registro: ").append(atual - 1).append(" de ").append(total - 1).append("\t");
            }
            case 5 -> {
                barra.append("   Dividindo Registros -> Byte: ").append(atual).append(" de ").append(total);
            }
            case 6 ->{
                if (tempo_restante < 0)
                    tempo_restante = 0;
                barra.append("   Intercalando -> Segmentos restantes: ").append(atual).append(" de ").append(total).append("\t Tempo restante: ").append(tempo_restante).append("s \t");
            }
            case 7 ->{
                barra.append("   Imprimindo registro: ").append(atual).append(" de ").append(total);
            }
            default -> {
                System.out.println("[ERRO] -> Não foi possível identificar o tipo de barra de progresso");
            }
        }

        //imprimir a barra
        System.out.print("\r" + barra.toString());
    }
}
