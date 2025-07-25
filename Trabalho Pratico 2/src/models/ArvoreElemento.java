/*
Esta classe representa um objeto para uma entidade
que será armazenado em uma árvore B+

Neste caso em particular, este objeto é representado
por dois números inteiros para que possa conter
relacionamentos entre dois IDs de entidades quaisquer

Modificação da implementação feita pelo Prof. Marcos Kutova
v2.0 - 2025
*/

package models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ArvoreElemento implements models.RegistroArvoreBMais<ArvoreElemento> {

  private int num_id;
  private long num_endereco;
  private short TAMANHO = 12; //  int + long

  public ArvoreElemento() {
    this(-1, -1);
  }

  public ArvoreElemento(int id) {
    this(id, -1);
  }

  public ArvoreElemento(int id, long endereco) {
    try {
      this.num_id = id; // ID do Usuário
      this.num_endereco = endereco; // ID da Pergunta
    } catch (Exception ec) {
      ec.printStackTrace();
    }
  }

  @Override
  public ArvoreElemento clone() {
    return new ArvoreElemento(this.num_id, this.num_endereco);
  }

  public short size() {
    return this.TAMANHO;
  }

  public int compareTo(ArvoreElemento a) {
    if (this.num_id != a.num_id)
      return this.num_id - a.num_id;
    else
      return 0;
  }

  public String toString() {
    return "ID -> " + this.num_id + " Endereço -> " + this.num_endereco;
  }

  public int getId(){
    return this.num_id;
  }

  public long getAddress(){
    return this.num_endereco;
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(this.num_id);
    dos.writeLong(this.num_endereco);
    return baos.toByteArray();
  }

  public void fromByteArray(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    this.num_id = dis.readInt();
    this.num_endereco = dis.readLong();
  }

}