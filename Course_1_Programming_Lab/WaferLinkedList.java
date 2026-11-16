/**
 * Course 1: Programming Lab (2310214L)
 * CO2: Extend the Application - Linked List
 *
 * Demonstrates:
 * 1. Node structure with data and next pointer
 * 2. Dynamic memory allocation (no fixed array limits)
 * 3. Insert at Head (O(1)) and Insert at Tail (O(n))
 * 4. Delete by Wafer ID
 * 5. Traversal and display
 *
 * Why Linked List is Appropriate:
 * In semiconductor fabrication, wafers are inspected continuously from a production line.
 * Fixed-size arrays risk memory overflow or wasted preallocated RAM. A Linked List
 * provides dynamic growth and O(1) constant-time insertion at the head.
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
            return String.format("[%s | %s | %.1f%%]", waferId, defectLabel, defectDensity);
        }
    }

    private Node head;
    private int count;

    public WaferLinkedList() {
        this.head = null;
        this.count = 0;
    }

    // Insert at Head (O(1))
    public void insertHead(String id, String label, double density) {
        Node newNode = new Node(id, label, density);
        newNode.next = head;
        head = newNode;
        count++;
    }

    // Insert at Tail (O(n))
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
        count++;
    }

    // Delete by Wafer ID
    public boolean delete(String waferId) {
        if (head == null) return false;

        if (head.waferId.equalsIgnoreCase(waferId)) {
            head = head.next;
            count--;
            return true;
        }

        Node current = head;
        while (current.next != null && !current.next.waferId.equalsIgnoreCase(waferId)) {
            current = current.next;
        }

        if (current.next != null) {
            current.next = current.next.next;
            count--;
            return true;
        }
        return false;
    }

    // Traversal and Display
    public void display() {
        System.out.println("\n--- WAFER LINKED LIST TRAVERSAL (Total Nodes: " + count + ") ---");
        Node temp = head;
        while (temp != null) {
            System.out.print(temp + " -> ");
            temp = temp.next;
        }
        System.out.println("NULL");
    }

    public int getCount() {
        return count;
    }
}

