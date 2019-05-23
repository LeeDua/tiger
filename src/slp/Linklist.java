package slp;

import java.util.LinkedList;
import java.util.ListIterator;


public class Linklist {
    LinkedList list;

    public Linklist(){
        this.list = new LinkedList();
    }

    public void update(String id, int value){
        Node node  = new Node(id, value);
        this.list.addFirst(node);
    }

    public int lookup(String id){
        ListIterator<Node> iter = this.list.listIterator();
        while(iter.hasNext()){
            Node node = iter.next();
            //System.out.println(node.id);
            //System.out.println(node.value);
            if(node.id == id){
                return node.value;
            }
        }
        throw new java.lang.Error("Error: use var before declare!");

    }

    private class Node{
        String id;
        int value;
        public Node(String id, int value){
            this.id = id;
            this.value = value;
        }
    }


}
