; =========================================================================
; Course 2: Computer Organization & Architecture Lab (2310221L)
; CO3: Arithmetic Operations & Algorithms Implementation
; =========================================================================
; Demonstrates two computer arithmetic algorithms:
; 1. Successive Addition Algorithm (Multiplication via repeated addition)
; 2. Shift-and-Add Binary Multiplier Algorithm (ALU hardware simulation)
; Used to compute wafer yield percentage: (Good_Dies * 100)
; =========================================================================

section .text
    global successive_addition
    global shift_and_add_multiply

; =========================================================================
; Algorithm 1: Successive Addition
; Inputs:  RDI = Multiplicand (e.g. Good_Dies = 185)
;          RSI = Multiplier   (e.g. Scale_Factor = 100)
; Output:  RAX = Product      (185 * 100 = 18500)
; =========================================================================
successive_addition:
    mov rax, 0              ; Initialize accumulator product to 0
    mov rcx, rsi            ; Set loop counter to Multiplier

add_loop:
    cmp rcx, 0
    je  done_successive
    add rax, rdi            ; Repeatedly add Multiplicand to accumulator
    dec rcx                 ; Decrement counter
    jmp add_loop

done_successive:
    ret

; =========================================================================
; Algorithm 2: Shift-and-Add Binary Multiplier (Booth / ALU Hardware Logic)
; Inputs:  RDI = Multiplicand (A)
;          RSI = Multiplier   (B)
; Output:  RAX = Product      (A * B)
; Principle:
; If LSB of B is 1, add A to Product.
; Then shift A left (SHL) and shift B right (SHR) each cycle.
; =========================================================================
shift_and_add_multiply:
    mov rax, 0              ; Accumulator Product = 0
    mov r8,  rdi            ; R8 = Multiplicand
    mov r9,  rsi            ; R9 = Multiplier

shift_loop:
    cmp r9, 0               ; Check if Multiplier is zero
    je  done_shift_add

    test r9, 1              ; Test Least Significant Bit (LSB)
    jz   skip_add           ; If LSB == 0, skip addition

    add rax, r8             ; If LSB == 1, add shifted multiplicand

skip_add:
    shl r8, 1               ; Multiplicand << 1 (multiply by 2)
    shr r9, 1               ; Multiplier >> 1 (divide by 2)
    jmp shift_loop

done_shift_add:
    ret

