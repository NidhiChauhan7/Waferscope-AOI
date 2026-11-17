package waferscope.datastructures;

/**
 * Course 1: Topic 3 - Stack and Queue.
 * Stack: LIFO (Last-In-First-Out) for inspection rollback and audit undo.
 * Queue: FIFO (First-In-First-Out) for automated fab wafer dispatch queue.
 */
public class WaferStackQueue {

    // LIFO Stack Implementation
    public static class InspectionStack {
        private final String[] stack;
        private int top;
        private final int capacity;

        public InspectionStack(int capacity) {
            this.capacity = capacity;
            this.stack = new String[capacity];
            this.top = -1;
        }

        public boolean push(String waferId) {
            if (top >= capacity - 1) return false;
            stack[++top] = waferId;
            return true;
        }

        public String pop() {
            if (top < 0) return null;
            return stack[top--];
        }

        public String peek() {
            if (top < 0) return null;
            return stack[top];
        }

        public int size() {
            return top + 1;
        }

        public String toStackString() {
            if (top < 0) return "[EMPTY STACK]";
            StringBuilder sb = new StringBuilder();
            for (int i = top; i >= 0; i--) {
                sb.append(stack[i]);
                if (i == top) sb.append(" [TOP]");
                if (i > 0) sb.append(" | ");
            }
            return sb.toString();
        }
    }

    // FIFO Queue Implementation
    public static class LotQueue {
        private final String[] queue;
        private int front;
        private int rear;
        private int count;
        private final int capacity;

        public LotQueue(int capacity) {
            this.capacity = capacity;
            this.queue = new String[capacity];
            this.front = 0;
            this.rear = -1;
            this.count = 0;
        }

        public boolean enqueue(String waferId) {
            if (count >= capacity) return false;
            rear = (rear + 1) % capacity;
            queue[rear] = waferId;
            count++;
            return true;
        }

        public String dequeue() {
            if (count == 0) return null;
            String item = queue[front];
            front = (front + 1) % capacity;
            count--;
            return item;
        }

        public int size() {
            return count;
        }

        public String toQueueString() {
            if (count == 0) return "[EMPTY QUEUE]";
            StringBuilder sb = new StringBuilder();
            sb.append("FRONT -> ");
            for (int i = 0; i < count; i++) {
                int idx = (front + i) % capacity;
                sb.append(queue[idx]);
                if (i < count - 1) sb.append(" -> ");
            }
            sb.append(" -> REAR");
            return sb.toString();
        }
    }
}
