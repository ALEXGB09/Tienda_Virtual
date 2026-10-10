package model;

public class Node<T> {
    private T data;
    private Node<T> nextPte;

    public Node() {
        this.data = null;
        this.nextPte = null;
    }

    public Node(T data) {
        this.data = data;
        this.nextPte = null;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Node<T> getNextPte() {
        return nextPte;
    }

    public void setNextPte(Node<T> nextPte) {
        this.nextPte = nextPte;
    }
}
