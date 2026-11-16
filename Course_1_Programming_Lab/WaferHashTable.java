/**
 * Course 1: Programming Lab (2310214L)
 * CO5: Hashing Techniques & Collision Resolution
 *
 * Demonstrates:
 * 1. Hash function with modulo arithmetic (hash(key) % capacity)
 * 2. Collision resolution using Separate Chaining (Linked Nodes in buckets)
 * 3. O(1) average-time wafer lookup by Fab Lot ID
 */
public class WaferHashTable {

    private static class HashNode {
        String key; // Wafer ID
        String value; // Defect Pattern & Classification
        HashNode next;

        public HashNode(String key, String value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private HashNode[] buckets;
    private int capacity;
    private int size;

    public WaferHashTable(int capacity) {
        this.capacity = capacity;
        this.buckets = new HashNode[capacity];
        this.size = 0;
    }

    // Hash function
    private int hashFunction(String key) {
        int hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = (31 * hash + key.charAt(i)) % capacity;
        }
        return Math.abs(hash);
    }

    // Insert with collision handling (chaining)
    public void put(String key, String value) {
        int bucketIndex = hashFunction(key);
        HashNode head = buckets[bucketIndex];

        // Check if key already exists
        HashNode current = head;
        while (current != null) {
            if (current.key.equalsIgnoreCase(key)) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        // Insert new node at head of chain
        HashNode newNode = new HashNode(key, value);
        newNode.next = head;
        buckets[bucketIndex] = newNode;
        size++;
    }

    // Get (O(1) average)
    public String get(String key) {
        int bucketIndex = hashFunction(key);
        HashNode current = buckets[bucketIndex];
        while (current != null) {
            if (current.key.equalsIgnoreCase(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null; // Not found
    }

    // Display Hash Table Structure & Collision Chains
    public void display() {
        System.out.println("\n--- WAFER HASH TABLE (Buckets: " + capacity + ", Elements: " + size + ") ---");
        for (int i = 0; i < capacity; i++) {
            System.out.print("Bucket [" + i + "]: ");
            HashNode current = buckets[i];
            if (current == null) {
                System.out.println("EMPTY");
            } else {
                while (current != null) {
                    System.out.print("(" + current.key + " -> " + current.value + ") -> ");
                    current = current.next;
                }
                System.out.println("NULL");
            }
        }
    }
}

