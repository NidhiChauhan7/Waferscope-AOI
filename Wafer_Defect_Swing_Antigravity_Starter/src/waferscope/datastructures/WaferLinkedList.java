package waferscope.datastructures;

import java.util.Locale;

/**
 * Course 1: Topic 2 - Singly Linked List.
 * Dynamic heap memory node allocation, head/tail insertion, deletion, and pointer traversal.
 */
public class WaferLinkedList {

    public static class Node {
        public String waferId;
        public String defectLabel;
        public double defectDensity;
        public Node next;

        public Node(String id, String label, double density) {
            this.waferId = id;
            this.defectLabel = label;
            this.defectDensity = density;
            this.next = null;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "[%s | %s | %.1f%%]", waferId, defectLabel, defectDensity);
        }
    }

    private Node head;
    private int size;

    public WaferLinkedList() {
        this.head = null;
        this.size = 0;
    }

    public void insertHead(String id, String label, double density) {
        Node newNode = new Node(id, label, density);
        newNode.next = head;
        head = newNode;
        size++;
    }

    public void insertTail(String id, String label, double density) {
        Node newNode = new Node(id, label, density);
        if (head == null) {
            head = newNode;
        } else {
            Node temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next = newNode;
        }
        size++;
    }

    public boolean delete(String id) {
        if (head == null) return false;
        if (head.waferId.equalsIgnoreCase(id)) {
            head = head.next;
            size--;
            return true;
        }
        Node curr = head;
        while (curr.next != null && !curr.next.waferId.equalsIgnoreCase(id)) {
            curr = curr.next;
        }
        if (curr.next != null) {
            curr.next = curr.next.next;
            size--;
            return true;
        }
        return false;
    }

    public Node getHead() {
        return head;
    }

    public int getSize() {
        return size;
    }

    public String toTraversalString() {
        StringBuilder sb = new StringBuilder();
        Node temp = head;
        while (temp != null) {
            sb.append(temp.toString());
            if (temp.next != null) sb.append(" -> ");
            temp = temp.next;
        }
        if (sb.length() == 0) sb.append("EMPTY");
        else sb.append(" -> NULL");
        return sb.toString();
    }
}
