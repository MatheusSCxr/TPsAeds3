package models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class HashElemento implements RegistroHashExtensivel<HashElemento> {
    private Integer id;
    private long endereco;
    private short TAMANHO = 12; // int + long

    public HashElemento() {
        this(-1, -1);
    }

    public HashElemento(Integer id, long endereco) {
        this.id = id;
        this.endereco = endereco;
    }

    public int getID(){
        return this.id;
    }

    public long getAddress(){
        return this.endereco;
    }

    @Override
    public int hashCode() {
        return Math.abs(this.id.hashCode());
    }

    public short size() {
        return this.TAMANHO;
    }

    public String toString() {
        return "ID -> " + this.id + " Endereço -> " + this.endereco;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeLong(endereco);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.endereco = dis.readLong();
    }

    public static int hash(Integer id) {
        return Math.abs(id.hashCode());
    }
    
}
