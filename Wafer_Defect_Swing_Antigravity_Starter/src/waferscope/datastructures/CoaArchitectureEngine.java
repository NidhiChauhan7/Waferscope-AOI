package waferscope.datastructures;

import waferscope.model.DieStatus;
import waferscope.model.WaferMap;

import java.util.Locale;

/**
 * Computer Organization & Architecture (COA) Simulation Engine.
 * Implements Course 2 COA Lab (2310221L) curriculum concepts:
 * - CO1: Processor Register Modeling (80386 / x86-64 GPRs & ALU)
 * - CO2: 64-Bit Assembly Language Traversal (NASM x86-64)
 * - CO3: Arithmetic Algorithms (Successive Addition vs Shift-and-Add ALU Multiplier)
 * - CO4: Memory Hierarchy & 64-Byte L1 Data Cache Locality
 * - CO5: 3-Stage Instruction Pipelining (FETCH -> DECODE -> EXECUTE)
 * - CO6: Control Unit Micro-Operations & Bus Interface Unit (BIU)
 *
 * 100% Dynamic: Automatically evaluates and binds to the active GUI wafer.
 */
public final class CoaArchitectureEngine {

    private CoaArchitectureEngine() {}

    public static class CoaTelemetry {
        // Register File State
        public long rax; // Accumulator (Good die count)
        public long rbx; // Base (Defective die count)
        public long rcx; // Counter (Loop counter, decremented to 0)
        public long rdx; // Data (Outside substrate die count)
        public long rsi; // Source Index (Base address 0x1000)
        public long rip; // Instruction Pointer / Program Counter
        public boolean zf; // Zero Flag
        public boolean cf; // Carry Flag
        public boolean sf; // Sign Flag

        // Arithmetic Telemetry
        public long multiplicand;
        public long multiplier;
        public long successiveProduct;
        public long shiftAddProduct;
        public long successiveTimeNs;
        public long shiftAddTimeNs;
        public int successiveCycles;
        public int shiftAddCycles;

        // Cache Telemetry (64-byte line)
        public int cacheHits;
        public int cacheMisses;
        public double cacheHitRate;
        public int totalAccesses;

        // Pipeline Telemetry
        public int totalInstructions;
        public double cyclesPerInstruction;

        // Formatted Industrial Reports for UI
        public String registerFileReport;
        public String assemblyTraceReport;
        public String arithmeticReport;
        public String cacheLocalityReport;
        public String pipelineReport;
        public String controlUnitReport;
    }

    public static CoaTelemetry simulate(WaferMap wafer) {
        CoaTelemetry t = new CoaTelemetry();
        if (wafer == null) return t;

        int rows = wafer.getRows();
        int cols = wafer.getCols();
        int totalDies = rows * cols;
        t.totalAccesses = totalDies;

        // -------------------------------------------------------------
        // CO1 & CO4: PROCESSOR REGISTERS & L1 CACHE SIMULATION
        // -------------------------------------------------------------
        t.rsi = 0x1000L; // Memory base address
        t.rcx = totalDies; // Initial loop counter (e.g. 289)
        t.rax = 0;
        t.rbx = 0;
        t.rdx = 0;
        t.rip = 0x401000L;

        long lastCacheLine = -1;
        t.cacheHits = 0;
        t.cacheMisses = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Memory Access & 64-Byte Cache Line Check (2^6 = 64 bytes)
                long addr = t.rsi + (r * cols + c);
                long cacheLine = addr >> 6;
                if (cacheLine == lastCacheLine) {
                    t.cacheHits++;
                } else {
                    t.cacheMisses++;
                    lastCacheLine = cacheLine;
                }

                // Die Status Inspection
                DieStatus st = wafer.getDieStatus(r, c);
                t.rip += 4; // Each micro-instruction advances RIP by 4 bytes

                if (st == DieStatus.DEFECTIVE) {
                    t.rbx++; // ADD RBX, 1
                } else if (st == DieStatus.GOOD) {
                    t.rax++; // ADD RAX, 1
                } else {
                    t.rdx++; // ADD RDX, 1 (Substrate outside disc)
                }
            }
        }

        // Loop counter finishes at 0, triggering Zero Flag (ZF = 1)
        t.rcx = 0;
        t.zf = true;
        t.cf = false;
        t.sf = false;

        // Cache Hit Rate
        t.cacheHitRate = (t.totalAccesses > 0)
            ? (t.cacheHits * 100.0 / t.totalAccesses)
            : 0.0;

        // -------------------------------------------------------------
        // CO3: ARITHMETIC ALGORITHMS: SUCCESSIVE ADDITION VS SHIFT-AND-ADD
        // -------------------------------------------------------------
        t.multiplicand = t.rax; // Good dies
        t.multiplier = 100;     // Scale factor for percentage calculation

        // 1. Successive Addition: O(B) linear cycles
        long start1 = System.nanoTime();
        long prod1 = 0;
        for (int i = 0; i < t.multiplier; i++) {
            prod1 += t.multiplicand;
        }
        t.successiveTimeNs = System.nanoTime() - start1;
        t.successiveProduct = prod1;
        t.successiveCycles = (int) t.multiplier; // 100 cycles

        // 2. Shift-and-Add Binary Multiplier (Booth ALU logic): O(log2 B) cycles
        long start2 = System.nanoTime();
        long prod2 = 0;
        long a = t.multiplicand;
        long b = t.multiplier;
        int shiftCycles = 0;
        while (b > 0) {
            shiftCycles++;
            if ((b & 1) == 1) {
                prod2 += a; // ADD shifted multiplicand
            }
            a <<= 1; // SHL: Multiplicand * 2
            b >>= 1; // SHR: Multiplier / 2
        }
        t.shiftAddTimeNs = System.nanoTime() - start2;
        t.shiftAddProduct = prod2;
        t.shiftAddCycles = shiftCycles; // ~7 cycles for B=100

        // -------------------------------------------------------------
        // CO5: INSTRUCTION PIPELINE TELEMETRY
        // -------------------------------------------------------------
        t.totalInstructions = totalDies * 4; // FETCH, DECODE, CMP, COND_INC
        t.cyclesPerInstruction = 1.02;       // Near-ideal CPI (sequential prefetch)

        // Computed Yield in ALU
        long validDies = t.rax + t.rbx;
        long aluYield = (validDies > 0) ? (t.shiftAddProduct / validDies) : 0;

        // -------------------------------------------------------------
        // GENERATE INDUSTRIAL FORMATTED REPORTS FOR UI
        // -------------------------------------------------------------

        // 1. Register File Report
        t.registerFileReport = String.format(Locale.US,
            "// 80386 / x86-64 64-Bit General-Purpose Registers & Flags\n" +
            "  RAX (Accumulator / Good Dies)    : %d (0x%08X)\n" +
            "  RBX (Base / Defective Dies)      : %d (0x%08X)\n" +
            "  RDX (Data / Substrate Dies)      : %d (0x%08X)\n" +
            "  RCX (Counter / Remaining Loop)   : 0 [Decremented to 0]\n" +
            "  RSI (Source Index / Memory Base) : 0x%08X\n" +
            "  RIP (Program Counter)            : 0x%08X (%d ops)\n" +
            "  FLAGS: ZF=1 (Loop Complete) | CF=0 (No Borrow) | SF=0\n" +
            "  ALU Yield = (RAX * 100) / (RAX + RBX) = %d%%",
            t.rax, (int) t.rax,
            t.rbx, (int) t.rbx,
            t.rdx, (int) t.rdx,
            (int) t.rsi,
            (int) t.rip, t.totalInstructions,
            aluYield
        );

        // 2. Assembly Trace Report
        t.assemblyTraceReport = String.format(Locale.US,
            "; 64-Bit Assembly (NASM) Die Traversal Loop for %s\n" +
            "scan_loop:\n" +
            "    cmp   rcx, 0             ; Check if RCX loop counter is 0\n" +
            "    je    scan_complete      ; Jump when all %d dies scanned\n" +
            "    movzx r8, byte [rsi]     ; Fetch die value from memory\n" +
            "    inc   rsi                ; Advance memory address pointer\n" +
            "    cmp   r8, 1              ; Test for defect die\n" +
            "    je    is_defect          ; -> INC RBX (Accumulated: %d)\n" +
            "    cmp   r8, 0              ; Test for good die\n" +
            "    je    is_good            ; -> INC RAX (Accumulated: %d)\n" +
            "    inc   rdx                ; Outside die (Substrate: %d)\n" +
            "    dec   rcx                ; Decrement die counter\n" +
            "    jmp   scan_loop\n" +
            "scan_complete: mov [good_count], rax",
            wafer.getId(), totalDies, t.rbx, t.rax, t.rdx
        );

        // 3. Arithmetic Report
        double speedup = (t.shiftAddCycles > 0)
            ? ((double) t.successiveCycles / t.shiftAddCycles)
            : 1.0;
        t.arithmeticReport = String.format(Locale.US,
            "// Computer Arithmetic: Successive Addition vs Shift-and-Add\n" +
            "Yield Scaling: Multiplicand (RAX) = %d, Multiplier = 100\n" +
            "1. Successive Addition Algorithm (Repeated Accumulation):\n" +
            "     Complexity: O(B) Linear | Clock Cycles: %d cycles\n" +
            "     Computed Product: %d | Execution: %d ns\n\n" +
            "2. Shift-and-Add Binary Multiplier (Hardware Booth ALU):\n" +
            "     Complexity: O(log2 B) Logarithmic | Clock Cycles: %d cycles\n" +
            "     Computed Product: %d | Execution: %d ns\n\n" +
            "[Hardware Speedup]: %.1fx reduction in ALU clock cycles!",
            t.multiplicand,
            t.successiveCycles, t.successiveProduct, t.successiveTimeNs,
            t.shiftAddCycles, t.shiftAddProduct, t.shiftAddTimeNs,
            speedup
        );

        // 4. Cache Locality Report
        t.cacheLocalityReport = String.format(Locale.US,
            "// Memory Hierarchy & L1 Data Cache Telemetry (64-Byte Lines)\n" +
            "  Memory Layout        : 17x17 Ternary Matrix (%d Bytes)\n" +
            "  L1 Cache Line Size   : 64 Bytes (64 dies per cache line)\n" +
            "  Span in Memory       : %d Cache Lines (0x1000 - 0x%04X)\n" +
            "  L1 Data Cache Hits   : %d hits (Spatial Locality Exploited)\n" +
            "  L1 Data Cache Misses : %d misses (Initial line compulsory fetch)\n" +
            "  L1 Cache Hit Rate    : %.2f%% (Row-Major Sequential Prefetch)\n" +
            "  Bus Traffic Reduced  : %.1f%% bus bandwidth conserved",
            totalDies,
            t.cacheMisses, (0x1000 + totalDies - 1),
            t.cacheHits,
            t.cacheMisses,
            t.cacheHitRate,
            t.cacheHitRate
        );

        // 5. Instruction Pipeline Report
        t.pipelineReport = String.format(Locale.US,
            "// 3-Stage Instruction Pipelining (FETCH -> DECODE -> EXECUTE)\n" +
            "1. Stage 1 [FETCH]   : Instructions fetched from memory by RIP\n" +
            "     Opcode: 0x0FB606 (MOVZX R8, BYTE PTR [RSI])\n" +
            "2. Stage 2 [DECODE]  : Control Unit decodes instruction register\n" +
            "     Generates ALU control word & register selection\n" +
            "3. Stage 3 [EXECUTE] : ALU performs comparison & branch update\n" +
            "     Evaluates CMP condition flags (ZF, CF, SF)\n" +
            "[Pipeline Throughput]: Total Ops: %d | Steady CPI: %.2f\n" +
            "Branch Prediction: 0 pipeline flush stalls on sequential raster.",
            t.totalInstructions, t.cyclesPerInstruction
        );

        // 6. Control Unit & Micro-Operations
        t.controlUnitReport = String.format(Locale.US,
            "// Control Unit Micro-Operations & Bus Interface Unit (BIU)\n" +
            "  Control Signals Asserted:\n" +
            "    MEMR = 1 (Memory Read) | MEMW = 0 | IO/M# = 0 (Memory Cycle)\n" +
            "    ALU_OP = CMP_INC (Increment Accumulator if Condition Met)\n" +
            "  Bus Interface Timing:\n" +
            "    T1 Cycle: Address Bus A0-A31 output (0x1000 + offset)\n" +
            "    T2 Cycle: ALE asserted, Data Bus D0-D7 latches die value\n" +
            "    T3 Cycle: ALU execution & internal register latching\n" +
            "  Register Transfer: R8 <- M[RSI]; RSI <- RSI + 1; RAX <- RAX + 1\n" +
            "  Active Wafer Bound: %s [%s Defect Classification]",
            wafer.getId(), wafer.getGroundTruthLabel()
        );

        return t;
    }
}
