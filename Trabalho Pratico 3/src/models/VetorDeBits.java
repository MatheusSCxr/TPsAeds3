package models;

import java.util.BitSet;

//classe auxiliar usada nos métodos de compressão
public class VetorDeBits {
    private BitSet bitSet;
    private int length;

    public VetorDeBits(int length) {
        this.bitSet = new BitSet(length);
        this.length = length;
    }

    public VetorDeBits(byte[] bytes) {
        this.bitSet = BitSet.valueOf(bytes);
        this.length = bytes.length * 8;
    }

    public void set(int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
        }
        bitSet.set(index);
    }

    public void clear(int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
        }
        bitSet.clear(index);
    }

    public boolean get(int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
        }
        return bitSet.get(index);
    }

    public int length() {
        return length;
    }

    //converter para array de bytes
    public byte[] toByteArray() {
        int numBytes = (length + 7) / 8; //garantir que seja multiplo de 8
        byte[] bytes = bitSet.toByteArray();
        if (bytes.length < numBytes) {
            byte[] newBytes = new byte[numBytes];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            return newBytes;
        }
        return bytes;
    }
}