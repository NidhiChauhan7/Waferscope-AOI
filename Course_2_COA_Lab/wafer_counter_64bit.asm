; =========================================================================
; Course 2: Computer Organization & Architecture Lab (2310221L)
; CO2: 64-bit Assembly-Based Application Processing
; Target: NASM / x86-64 Linux & Windows 64-bit ABI
; =========================================================================
; Description:
; Scans a 17x17 semiconductor wafer ternary grid stored in memory.
; Demonstrates:
; - 64-bit registers: RAX, RBX, RCX, RDX, RSI, RDI
; - Arithmetic instructions: ADD, INC, DEC, SUB
; - Comparison & conditional branching: CMP, JE, JNE, JL
; - Memory addressing: [rsi + rcx]
; =========================================================================

section .data
    ; Sample 17x17 Wafer Map Ternary Grid (289 bytes)
    ; 0 = Good Die, 1 = Defective Die, -1 (0xFF) = Substrate / Outside
    wafer_map db 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0xFF, 0xFF, 0xFF
    map_len   equ $ - wafer_map

    msg_done  db "64-bit Assembly Wafer Die Scan Complete.", 0x0A, 0
    msg_len   equ $ - msg_done

section .bss
    good_count    resq 1    ; 64-bit quadword
    defect_count  resq 1    ; 64-bit quadword

section .text
    global _start

_start:
    ; 1. Initialize 64-bit Registers
    mov rax, 0              ; RAX = Good die counter (Accumulator)
    mov rbx, 0              ; RBX = Defect die counter (Base register)
    mov rdx, 0              ; RDX = Outside substrate counter
    mov rcx, map_len        ; RCX = Loop counter
    lea rsi, [wafer_map]    ; RSI = Pointer to wafer grid array (Source Index)

scan_loop:
    cmp rcx, 0              ; Compare loop counter to zero
    je  scan_complete       ; Jump to completion if finished

    movzx r8, byte [rsi]    ; Fetch die value into 64-bit register R8
    inc   rsi               ; Advance memory pointer

    ; 2. ALU Comparison & Conditional Branching
    cmp r8, 1               ; Is die defective (value == 1)?
    je  is_defect

    cmp r8, 0               ; Is die good (value == 0)?
    je  is_good

    ; Else: Outside wafer substrate
    inc rdx                 ; Increment substrate count
    jmp next_iteration

is_defect:
    inc rbx                 ; Increment defect counter (RBX)
    jmp next_iteration

is_good:
    inc rax                 ; Increment good die counter (RAX)

next_iteration:
    dec rcx                 ; Decrement remaining die count
    jmp scan_loop

scan_complete:
    ; Store final quadword counts into memory
    mov [good_count], rax
    mov [defect_count], rbx

    ; Exit program via syscall (sys_exit = 60 on Linux / ExitProcess on Windows)
    mov rax, 60             ; syscall: sys_exit
    xor rdi, rdi            ; return code 0
    syscall

