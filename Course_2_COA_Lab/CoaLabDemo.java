/**
 * Course 2: Computer Organization & Architecture Lab (2310221L)
 * Master Demonstration for COA Examination Panel
 *
 * Demonstrates:
 * CO1: Processor-Level Modeling (80386 Registers, ALU, Control Unit)
 * CO2: 64-bit Assembly Language Processing Logic
 * CO3: Arithmetic Algorithms (Successive Addition vs Shift-and-Add)
 * CO4: Memory Hierarchy, 64-Byte Cache Line Locality, and Instruction Pipelining
 */
public class CoaLabDemo {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   COURSE 2: COMPUTER ORGANIZATION & ARCHITECTURE LAB (2310221L)");
        System.out.println("   MIT Academy of Engineering, Alandi, Pune | Dept of CE");
        System.out.println("==========================================================================\n");

        // -------------------------------------------------------------
        // CO1 & CO4: PROCESSOR MODELING & CACHE HIERARCHY
        // -------------------------------------------------------------
        System.out.println(">>> [CO1 & CO4] PROCESSOR REGISTERS, ALU & CACHE HIERARCHY");

        // Create synthetic 17x17 wafer grid (289 bytes)
        byte[] waferDies = new byte[289];
        // Populate sample dies: 185 good (0), 25 defect (1), 79 outside (-1)
        for (int i = 0; i < 289; i++) {
            if (i < 79) waferDies[i] = -1;
            else if (i < 79 + 25) waferDies[i] = 1;
            else waferDies[i] = 0;
        }

        WaferProcessorSimulator sim = new WaferProcessorSimulator();
        sim.simulateWaferDieScan(waferDies);

        // -------------------------------------------------------------
        // CO2: 64-BIT ASSEMBLY LANGUAGE LOGIC
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO2] 64-BIT ASSEMBLY LANGUAGE EXECUTION MAPPING");
        System.out.println("Mapping application die scanning to x86-64 CPU registers:");
        System.out.println("  1. RAX (64-bit Accumulator) : Holds Good Die Count");
        System.out.println("  2. RBX (64-bit Base)        : Holds Defective Die Count");
        System.out.println("  3. RCX (64-bit Counter)     : Loop counter (decremented via DEC RCX)");
        System.out.println("  4. RSI (64-bit Source Index): Memory base pointer to wafer matrix");
        System.out.println("  5. FLAGS (ZF, CF, SF)       : Updated by CMP and TEST instructions");

        // -------------------------------------------------------------
        // CO3: COMPUTER ARITHMETIC ALGORITHMS
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO3] ARITHMETIC ALGORITHMS: SUCCESSIVE ADDITION VS SHIFT-AND-ADD");
        long goodDies = 185;
        long multiplier = 100;

        // Algorithm 1: Successive Addition
        long t1 = System.nanoTime();
        long product1 = 0;
        for (int i = 0; i < multiplier; i++) {
            product1 += goodDies;
        }
        long timeSuccessive = System.nanoTime() - t1;

        // Algorithm 2: Shift-and-Add Binary Multiplication (Hardware ALU Booth logic)
        long t2 = System.nanoTime();
        long product2 = 0;
        long a = goodDies;
        long b = multiplier;
        while (b > 0) {
            if ((b & 1) == 1) {
                product2 += a; // Add shifted multiplicand
            }
            a <<= 1; // Shift multiplicand left (multiply by 2)
            b >>= 1; // Shift multiplier right (divide by 2)
        }
        long timeShiftAdd = System.nanoTime() - t2;

        System.out.println(String.format("Multiplicand = %d, Multiplier = %d", goodDies, multiplier));
        System.out.println(String.format("1. Successive Addition Result : %d (%d ns)", product1, timeSuccessive));
        System.out.println(String.format("2. Shift-and-Add ALU Result    : %d (%d ns)", product2, timeShiftAdd));
        System.out.println("Architectural Conclusion: Shift-and-Add completes in logarithmic O(log2 B) cycles,");
        System.out.println("whereas Successive Addition requires linear O(B) clock cycles.");

        System.out.println("\n==========================================================================");
        System.out.println("   [SUCCESS] ALL COA LAB OUTCOMES DEMONSTRATED SUCCESSFULLY!");
        System.out.println("==========================================================================");
    }
}

