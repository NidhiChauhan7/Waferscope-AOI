/**
 * Course 1: Programming Lab (2310214L)
 * CO3: Scenario Processing - Stack & Queue
 *
 * Scenarios:
 * 1. Stack (LIFO): Manages an "Undo / Inspection Rollback Stack".
 *    The most recently inspected wafer can be popped to revert verification.
 * 2. Queue (FIFO): Manages an "Automated Wafer Ingestion Queue".
 *    Wafers entering from robotic transfer lots are queued and inspected in First-In, First-Out order.
 */
public class WaferStackQueue {

    // ==========================================
    // 1. STACK (LIFO: Last-In, First-Out)
    // ==========================================
    public static class InspectionStack {
        private String[] stack;
        private int top;
        private int capacity;

        public InspectionStack(int capacity) {
            this.capacity = capacity;
            this.stack = new String[capacity];
            this.top = -1;
        }

        public boolean push(String waferId) {
            if (top >= capacity - 1) {
                System.out.println("[Stack Overflow] Inspection rollback stack is full.");
                return false;
            }
            stack[++top] = waferId;
            return true;
        }

        public String pop() {
            if (top < 0) {
                System.out.println("[Stack Underflow] No inspections to roll back.");
                return null;
            }
            return stack[top--];
        }

        public String peek() {
            if (top < 0) return null;
            return stack[top];
        }

        public void display() {
            System.out.println("\n--- RECENT INSPECTION ROLLBACK STACK (LIFO) ---");
            if (top < 0) {
                System.out.println("(Stack Empty)");
                return;
            }
            for (int i = top; i >= 0; i--) {
                System.out.println("  | " + stack[i] + (i == top ? "  <-- TOP" : ""));
            }
        }
    }

    // ==========================================
    // 2. QUEUE (FIFO: First-In, First-Out)
    // ==========================================
    public static class LotQueue {
        private String[] queue;
        private int front;
        private int rear;
        private int count;
        private int capacity;

        public LotQueue(int capacity) {
            this.capacity = capacity;
            this.queue = new String[capacity];
            this.front = 0;
            this.rear = -1;
            this.count = 0;
        }

        public boolean enqueue(String waferId) {
            if (count >= capacity) {
                System.out.println("[Queue Overflow] Ingestion buffer queue is full.");
                return false;
            }
            rear = (rear + 1) % capacity;
            queue[rear] = waferId;
            count++;
            return true;
        }

        public String dequeue() {
            if (count == 0) {
                System.out.println("[Queue Underflow] Ingestion queue is empty.");
                return null;
            }
            String item = queue[front];
            front = (front + 1) % capacity;
            count--;
            return item;
        }

        public void display() {
            System.out.println("\n--- AUTOMATED LOT INGESTION QUEUE (FIFO, Count: " + count + ") ---");
            if (count == 0) {
                System.out.println("(Queue Empty)");
                return;
            }
            System.out.print("FRONT -> ");
            for (int i = 0; i < count; i++) {
                int idx = (front + i) % capacity;
                System.out.print(queue[idx] + " -> ");
            }
            System.out.println("REAR");
        }
    }
}

