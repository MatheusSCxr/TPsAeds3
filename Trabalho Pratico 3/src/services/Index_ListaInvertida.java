/*********
 * LISTA INVERTIDA
 * String chave, int dado
 * 
 * Os nomes dos métodos foram mantidos em inglês
 * apenas para manter a coerência com o resto da
 * disciplina:
 * - boolean create(String chave, int dado)
 * - int[] read(int chave)
 * - boolean delete(String chave, int dado)
 * 
 * Implementado pelo Prof. Marcos Kutova
 * v1.0 - 2020
 */
package services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import main.DataBase;
import models.ListaElemento;
import models.SteamGame;

public class Index_ListaInvertida {
  //lista de categorias válidas do CSV
  private static final String[] VALID_CATEGORIES = {
      "Single-player", "Multi-player", "Online Multi-Player", "Local Multi-Player",
      "Co-op", "Online Co-op", "Local Co-op", "Cross-Platform Multiplayer","Shared/Split Screen",
      "Steam Achievements", "Captions available", "Steam Workshop",
      "Partial Controller Support", "Full controller support","Steam Trading Cards","In-App Purchases","Valve Anti-Cheat enabled", "Stats",
      "Steam Leaderboards", "Includes level editor", "Includes Source SDK"
  };
  String nomeArquivoDicionario;
  String nomeArquivoBlocos;
  public RandomAccessFile arqDicionario;
  public RandomAccessFile arqBlocos;
  int quantidadeDadosPorBloco;

  class Bloco {

    short quantidade; // quantidade de dados presentes na lista
    short quantidadeMaxima; // quantidade máxima de dados que a lista pode conter
    ListaElemento[] elementos; // sequência de dados armazenados no bloco
    long proximo; // ponteiro para o bloco sequinte da mesma chave
    short bytesPorBloco; // size fixo do cesto em bytes

    public Bloco(int qtdmax) throws Exception {
      quantidade = 0;
      quantidadeMaxima = (short) qtdmax;
      elementos = new ListaElemento[quantidadeMaxima];
      proximo = -1;
      bytesPorBloco = (short) (2 + (4+8) * quantidadeMaxima + 8);  // 4 do INT e 8 do LONG
    }

    public byte[] toByteArray() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeShort(quantidade);
      int i = 0;
      while (i < quantidade) {
        dos.writeInt(elementos[i].getId());
        dos.writeLong(elementos[i].getAddress());
        i++;
      }
      while (i < quantidadeMaxima) {
        dos.writeInt(-1);
        dos.writeLong(-1);
        i++;
      }
      dos.writeLong(proximo);
      return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(ba);
      DataInputStream dis = new DataInputStream(bais);
      quantidade = dis.readShort();
      int i = 0;
      while (i < quantidadeMaxima) {
        elementos[i] = new ListaElemento(dis.readInt(), dis.readLong());
        i++;
      }
      proximo = dis.readLong();
    }

    // Insere um valor no bloco
    public boolean create(ListaElemento e) {
      if (full())
        return false;
      int i = quantidade - 1;
      while (i >= 0 && e.getId() < elementos[i].getId()) {
        elementos[i + 1] = elementos[i];
        i--;
      }
      i++;
      elementos[i] = e.clone();
      quantidade++;
      return true;
    }

    // Lê um valor no bloco
    public boolean test(int id) {
      if (empty())
        return false;
      int i = 0;
      while (i < quantidade && id > elementos[i].getId())
        i++;
      if (i < quantidade && id == elementos[i].getId())
        return true;
      else
        return false;
    }

    // Remove um valor do bloco
    public boolean delete(int id) {
      if (empty())
        return false;
      int i = 0;
      while (i < quantidade && id > elementos[i].getId())
        i++;
      if (id == elementos[i].getId()) {
        while (i < quantidade - 1) {
          elementos[i] = elementos[i + 1];
          i++;
        }
        quantidade--;
        return true;
      } else
        return false;
    }

    public ListaElemento last() {
      return elementos[quantidade - 1];
    }

    public ListaElemento[] list() {
      ListaElemento[] lista = new ListaElemento[quantidade];
      for (int i = 0; i < quantidade; i++)
        lista[i] = elementos[i].clone();
      return lista;
    }

    public boolean empty() {
      return quantidade == 0;
    }

    public boolean full() {
      return quantidade == quantidadeMaxima;
    }

    public String toString() {
      String s = "\nQuantidade: " + quantidade + "\n| ";
      int i = 0;
      while (i < quantidade) {
        s += elementos[i] + " | ";
        i++;
      }
      while (i < quantidadeMaxima) {
        s += "- | ";
        i++;
      }
      return s;
    }

    public long next() {
      return proximo;
    }

    public void setNext(long p) {
      proximo = p;
    }

    public int size() {
      return bytesPorBloco;
    }

  }

  public Index_ListaInvertida(int n, String nd, String nc) throws Exception {
    quantidadeDadosPorBloco = n;
    nomeArquivoDicionario = nd;
    nomeArquivoBlocos = nc;

    if (n > 2){
      arqDicionario = new RandomAccessFile(nomeArquivoDicionario, "rw");
      if(arqDicionario.length()<4) {    // cabeçalho do arquivo com número de entidades
        arqDicionario.seek(0);
        arqDicionario.writeInt(0);
      }
      arqBlocos = new RandomAccessFile(nomeArquivoBlocos, "rw");
    }
  }

  // Incrementa o número de entidades
  public void incrementaEntidades() throws Exception {
    arqDicionario.seek(0);
    int n = arqDicionario.readInt();
    arqDicionario.seek(0);
    arqDicionario.writeInt(n+1);    
  }

  // Decrementa o número de entidades
  public void decrementaEntidades() throws Exception {
    arqDicionario.seek(0);
    int n = arqDicionario.readInt();
    arqDicionario.seek(0);
    arqDicionario.writeInt(n-1);    
  }

  // Retorna o número de entidades
  public int numeroEntidades() throws Exception {
    arqDicionario.seek(0);
    return arqDicionario.readInt();
  }

  // Insere um dado na lista da chave de forma NÃO ORDENADA
  public boolean create(String c, ListaElemento e) throws Exception {

    // Percorre toda a lista testando se já não existe
    // o dado associado a essa chave
    ListaElemento[] lista = read(c);
    for (int i = 0; i < lista.length; i++)
      if (lista[i].getId() == e.getId())
        return false;

    String chave = "";
    long endereco = -1;
    boolean jaExiste = false;

    // localiza a chave no dicionário
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      chave = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (chave.compareTo(c) == 0) {
        jaExiste = true;
        break;
      }
    }

    // Se não encontrou, cria um novo bloco para essa chave
    if (!jaExiste) {
      // Cria um novo bloco
      Bloco b = new Bloco(quantidadeDadosPorBloco);
      endereco = arqBlocos.length();
      arqBlocos.seek(endereco);
      arqBlocos.write(b.toByteArray());

      // Insere a nova chave no dicionário
      arqDicionario.seek(arqDicionario.length());
      arqDicionario.writeUTF(c);
      arqDicionario.writeLong(endereco);
    }

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {
      long proximo = -1;

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Testa se o dado cabe nesse bloco
      if (!b.full()) {
        b.create(e);
      } else {
        // Avança para o próximo bloco
        proximo = b.next();
        if (proximo == -1) {
          // Se não existir um novo bloco, cria esse novo bloco
          Bloco b1 = new Bloco(quantidadeDadosPorBloco);
          proximo = arqBlocos.length();
          arqBlocos.seek(proximo);
          arqBlocos.write(b1.toByteArray());

          // Atualiza o ponteiro do bloco anterior
          b.setNext(proximo);
        }
      }

      // Atualiza o bloco atual
      arqBlocos.seek(endereco);
      arqBlocos.write(b.toByteArray());
      endereco = proximo;
    }
    return true;
  }

  // Retorna a lista de dados de uma determinada chave
  public ListaElemento[] read(String c) throws Exception {

    ArrayList<ListaElemento> lista = new ArrayList<>();

    String chave = "";
    long endereco = -1;
    boolean jaExiste = false;

    // localiza a chave no dicionário
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      chave = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (chave.compareTo(c) == 0) {
        jaExiste = true;
        break;
      }
    }
    if (!jaExiste)
      return new ListaElemento[0];

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Acrescenta cada valor à lista
      ListaElemento[] lb = b.list();
      for (int i = 0; i < lb.length; i++)
        lista.add(lb[i]);

      // Avança para o próximo bloco
      endereco = b.next();

    }

    // Constrói o vetor de respostas
    lista.sort(null);
    ListaElemento[] resposta = new ListaElemento[lista.size()];
    for (int j = 0; j < lista.size(); j++)
      resposta[j] = (ListaElemento) lista.get(j);
    return resposta;
  }

  // Remove o dado de uma chave (mas não apaga a chave nem apaga blocos)
  public boolean delete(String c, int id) throws Exception {

    String chave = "";
    long endereco = -1;
    boolean jaExiste = false;

    // localiza a chave no dicionário
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      chave = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (chave.compareTo(c) == 0) {
        jaExiste = true;
        break;
      }
    }
    if (!jaExiste)
      return false;

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Testa se o valor está neste bloco e sai do laço
      if (b.test(id)) {
        b.delete(id);
        arqBlocos.seek(endereco);
        arqBlocos.write(b.toByteArray());
        return true;
      }

      // Avança para o próximo bloco
      endereco = b.next();
    }

    // chave não encontrada
    return false;

  }

  //atualiza o campo 'endereco' do ElementoLista de id e na chave c.
  public boolean update(String c, int id, long novoEndereco) throws Exception {
    //localiza a chave no dicionário
    arqDicionario.seek(4);
    long enderecoBloco = -1;
    boolean achouChave = false;
    while (arqDicionario.getFilePointer() < arqDicionario.length()) {
        String chave = arqDicionario.readUTF();
        long ponteiro = arqDicionario.readLong();
        if (chave.equals(c)) {
            enderecoBloco = ponteiro;
            achouChave = true;
            break;
        }
    }
    if (!achouChave)
      return false;

    //percorre os blocos encadeados em busca do id
    Bloco bloco = new Bloco(quantidadeDadosPorBloco);
    byte[] buffer;
    long atual = enderecoBloco;
    while (atual != -1) {
        //obter bloco
        arqBlocos.seek(atual);
        buffer = new byte[bloco.size()];
        arqBlocos.read(buffer);
        bloco.fromByteArray(buffer);

        //tenta encontrar e atualizar o elemento
        for (int i = 0; i < bloco.quantidade; i++) {
            if (bloco.elementos[i].getId() == id) {
                bloco.elementos[i].setAddress(novoEndereco);
                // grava o bloco atualizado
                arqBlocos.seek(atual);
                arqBlocos.write(bloco.toByteArray());
                return true; //encerrar update
            }
        }
        //avança para o próximo
        atual = bloco.next();
    }

    //não achou o id em nenhum bloco
    return false;
  }

  public static Index_ListaInvertida IndexDataBase(Index_ListaInvertida oldLista, int qntDados){
    System.out.println("[Index] -> Indexando registros...");
    try (RandomAccessFile database = new RandomAccessFile("./src/resources/db_Output/gamesDB.db","r")) {
        //excluir o arquivo antigo, se existir
        File file = new File("./src/resources/db_Index/lista_dicionario.db");
        if (file.exists()) {
            if (oldLista != null)
                oldLista.arqDicionario.close();
            file.delete();
        }
        //excluir o arquivo antigo, se existir
        File file2 = new File("./src/resources/db_Index/lista_blocos.db");
        if (file2.exists()) {
            if (oldLista != null)
                oldLista.arqBlocos.close();
            file2.delete();
        }
        
        //criar nova arvore
        Index_ListaInvertida lista;
        lista = new Index_ListaInvertida( qntDados, "./src/resources/db_Index/lista_dicionario.db", "./src/resources/db_Index/lista_blocos.db");

        int conta = 0;
        if (database.length() != 0){
            database.seek(0);
            database.skipBytes(4); //pular ultimo id inserido
            while (database.getFilePointer() < database.length()){
                try {
                    //ler se a lápide está ativa
                    int lapide = database.readUnsignedByte();
                    if (lapide != 0xFF){
                        database.skipBytes(4);  //pular tamanho do registro
                        long endereco = database.getFilePointer();
                        
                        SteamGame jogo = DB_CRUD.readGame(database);

                        //adicionar elemento na lista para Para cada categoria válida
                        List<String> cats = jogo.getCategories();
                        for (String cat : cats) {
                            //só indexa se for uma categoria da lista definida na classe
                            for (String valid : VALID_CATEGORIES) {
                                if (valid.equals(cat)) {
                                    lista.create(cat, new ListaElemento(jogo.getId(), endereco)); //adiciona jogo na lista
                                    lista.incrementaEntidades();
                                    break;
                                }
                            }
                        }
                    }
                    else{
                        //pular o registro inativo
                        database.skipBytes(database.readInt());
                    }
                    conta++;
                    UI.progressBar(conta, DataBase.totalGames + DataBase.totalDeleted, "[Index]", 8, 0);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            //atualizar status de indexação do banco de dados
            try (RandomAccessFile metadados = new RandomAccessFile("./src/resources/db_Index/indexMetadata.db", "rw")){
                DataBase.indexStatus = 3;
                metadados.writeByte(3);
                metadados.writeInt(qntDados);
            } catch (Exception e){
                System.out.println("[ERRO] Não foi possível escrever os metadados da indexação atual");
            }
            
            System.out.println("\n[Index] -> Base indexada com sucesso para Lista Invertida em (./src/resources/db_Index/lista_dicionario.db) e (./src/resources/db_Index/lista_blocos.db)");
        }
        else{
            System.out.println("[INFO] -> Não foi detectada um banco de dados em (./src/resources/db_Output/gamesDB.db)");
        }

        //retornar nova lista
        return lista;

    } catch (Exception e) {
            System.out.println("[ERRO] -> Não foi possível indexar o banco de dados para Lista Invertida");
            System.out.println(e);
    }

    //retornar null se falhar
    return null;
  }

  public void print() throws Exception {

    System.out.println("\nLISTAS INVERTIDAS:");

    // Percorre todas as chaves
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {

      String chave = arqDicionario.readUTF();
      long endereco = arqDicionario.readLong();

      // Percorre a lista desta chave
      ArrayList<ListaElemento> lista = new ArrayList<>();
      Bloco b = new Bloco(quantidadeDadosPorBloco);
      byte[] bd;
      while (endereco != -1) {

        // Carrega o bloco
        arqBlocos.seek(endereco);
        bd = new byte[b.size()];
        arqBlocos.read(bd);
        b.fromByteArray(bd);

        // Acrescenta cada valor à lista
        ListaElemento[] lb = b.list();
        for (int i = 0; i < lb.length; i++)
          lista.add(lb[i]);

        // Avança para o próximo bloco
        endereco = b.next();
      }

      // Imprime a chave e sua lista
      System.out.print(chave + ": ");
      lista.sort(null);
      for (int j = 0; j < lista.size(); j++)
        System.out.print(lista.get(j) + " ");
      System.out.println();
    }
  }
}
