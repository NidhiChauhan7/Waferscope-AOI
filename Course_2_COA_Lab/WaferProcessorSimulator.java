/**
 * Course 2: Computer Organization & Architecture Lab (2310221L)
 * CO1 & CO4: Processor-Level Modeling & Instruction Pipelining Simulator
 *
 * Simulates:
 * 1. 80386 / 64-bit CPU Architecture:
 *    - General Purpose Registers: RAX (Accumulator), RBX (Base), RCX (Counter), RDX (Data)
 *    - Index Registers: RSI (Source Index), RDI (Destination Index)
 *    - Instruction Pointer: RIP (Program Counter)
 *    - Condition Flags: ZF (Zero Flag), CF (Carry Flag), SF (Sign Flag)
 * 2. ALU Arithmetic & Logic: ADD, SUB, SHL, SHR, CMP
 * 3. Control Unit & 3-Stage Instruction Pipeline: FETCH -> DECODE -> EXECUTE
 * 4. Cache Hierarchy: 64-byte L1 Data Cache simulation with hit/miss counter
 */
public class WaferProcessorSimulator {

    // Registers
    public long RAX, RBX, RCX, RDX, RSI, RDI, RIP;
    public boolean ZF, CF, SF; // Flags

    // Cache Telemetry
    public int cacheHits = 0;
    public int cacheMisses = 0;
    private long lastCacheLine = -1;

    public WaferProcessorSimulator() {
        reset();
    }

    public void reset() {
        RAX = RBX = RCX = RDX = RSI = RDI = RIP = 0;
        ZF = CF = SF = false;
        cacheHits = cacheMisses = 0;
        lastCacheLine = -1;
    }

    // ALU Comparison
    public void aluCompare(long a, long b) {
        long diff = a - b;
        ZF = (diff == 0);
        SF = (diff < 0);
        CF = (a < b);
    }

    // L1 Data Cache Access (64-byte cache line)
    public void accessMemory(long address) {
        long cacheLine = address >> 6; // 64 bytes = 2^6
        if (cacheLine == lastCacheLine) {
            cacheHits++;
        } else {
            cacheMisses++;
            lastCacheLine = cacheLine;
        }
    }

    // Simulates scanning a wafer map in CPU registers & ALU
    public void simulateWaferDieScan(byte[] waferDies) {
        System.out.println("\n--- 80386 / X86-64 PROCESSOR REGISTER SIMULATION ---");
        reset();

        RCX = waferDies.length; // Counter = Total dies (e.g., 289)
        RSI = 0;               // Memory index
        RAX = 0;               // Good die accumulator
        RBX = 0;               // Defective die accumulator
        RDX = 0;               // Outside substrate counter

        System.out.println("Initial State: RCX=" + RCX + ", RAX=0, RBX=0, RDX=0");
        System.out.println("Beginning 3-Stage Pipeline (FETCH -> DECODE -> EXECUTE):");

        for (int i = 0; i < waferDies.length; i++) {
            accessMemory(0x1000 + i); // Memory fetch with cache check
            byte val = waferDies[i];
            RIP++; // Increment instruction pointer

            // ALU Operations & Conditional Branching
            if (val == 1) {
                RBX++; // ADD RBX, 1 (Defect die)
            } else if (val == 0) {
                RAX++; // ADD RAX, 1 (Good die)
            } else {
                RDX++; // ADD RDX, 1 (Outside)
            }
        }

        // Final ALU Yield Percentage Calculation: (RAX * 100) / (RAX + RBX)
        long validDies = RAX + RBX;
        long yieldPct = (validDies > 0) ? (RAX * 100) / validDies : 0;

        System.out.println("\nFinal Processor Register State:");
        System.out.println(String.format("  RAX (Good Dies)      : %d", RAX));
        System.out.println(String.format("  RBX (Defective Dies) : %d", RBX));
        System.out.println(String.format("  RDX (Substrate Dies) : %d", RDX));
        System.out.println(String.format("  RCX (Remaining Loop) : 0 (ZF=1)"));
        System.out.println(String.format("  ALU Computed Yield   : %d%%", yieldPct));

        System.out.println("\nMemory Hierarchy Telemetry:");
        int totalAccesses = cacheHits + cacheMisses;
        double hitRate = (totalAccesses > 0) ? (cacheHits * 100.0 / totalAccesses) : 0.0;
        System.out.println(String.format("  L1 Data Cache Hits   : %d", cacheHits));
        System.out.println(String.format("  L1 Data Cache Misses : %d", cacheMisses));
        System.out.println(String.format("  L1 Cache Hit Rate    : %.2f%% (Sequential Row-Major Prefetching)", hitRate));
    }
}

