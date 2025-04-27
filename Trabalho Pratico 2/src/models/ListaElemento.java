package models;

public class ListaElemento implements Comparable<ListaElemento>, Cloneable {
    
    private int id;
    private long endereco;

    public ListaElemento(int i, long e) {
        this.id = i;
        this.endereco = e;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAddress() {
        return endereco;
    }

    public void setAddress(long endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return "("+this.id+";"+this.endereco+")";
    }

    @Override
    public ListaElemento clone() {
        try {
            return (ListaElemento) super.clone();
        } catch (CloneNotSupportedException e) {
            // Tratamento de exceção se a clonagem falhar
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int compareTo(ListaElemento outro) {
        return Integer.compare(this.id, outro.id);
    }
}
