package waferscope.datastructures;

/**
 * Course 1: Topic 5 - Hashing Techniques.
 * Direct addressing hash table with separate chaining for collision resolution.
 * Provides O(1) average lookup time.
 */
public class WaferHashTable {

    public static class HashNode {
        public String key;
        public String value;
        public HashNode next;

        public HashNode(String key, String value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private final HashNode[] buckets;
    private final int numBuckets;
    private int size;

    public WaferHashTable(int capacity) {
        this.numBuckets = capacity;
        this.buckets = new HashNode[capacity];
        this.size = 0;
    }

    public int hash(String key) {
        if (key == null) return 0;
        int h = 0;
        for (int i = 0; i < key.length(); i++) {
            h = (31 * h + key.charAt(i)) & 0x7fffffff;
        }
        return h % numBuckets;
    }

    public void put(String key, String value) {
        int bucketIndex = hash(key);
        HashNode head = buckets[bucketIndex];

        // Update if key exists
        HashNode curr = head;
        while (curr != null) {
            if (curr.key.equalsIgnoreCase(key)) {
                curr.value = value;
                return;
            }
            curr = curr.next;
        }

        // Insert at head of bucket chain
        HashNode newNode = new HashNode(key, value);
        newNode.next = head;
        buckets[bucketIndex] = newNode;
        size++;
    }

    public String get(String key) {
        int bucketIndex = hash(key);
        HashNode curr = buckets[bucketIndex];
        while (curr != null) {
            if (curr.key.equalsIgnoreCase(key)) {
                return curr.value;
            }
            curr = curr.next;
        }
        return null; // Not found
    }

    public int getBucketFor(String key) {
        return hash(key);
    }

    public int getSize() {
        return size;
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    public String getBucketChainString(int bucketIndex) {
        if (bucketIndex < 0 || bucketIndex >= numBuckets) return "INVALID";
        HashNode curr = buckets[bucketIndex];
        if (curr == null) return "EMPTY";
        StringBuilder sb = new StringBuilder();
        while (curr != null) {
            sb.append("(").append(curr.key).append(" -> ").append(curr.value).append(")");
            if (curr.next != null) sb.append(" -> ");
            curr = curr.next;
        }
        return sb.toString();
    }
}
