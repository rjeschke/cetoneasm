cetoneasm - A C64 (Cross) Macro Assembler
=========================================

Number literals:
--
`cetoneasm` uses a `long` number type (i.e. int64_t) as the basic number type.

`123` - decimal
`$12` - hexadecimal
`0x12` - hexadecimal
`0o12` - octal
`0b1101` - binary


String literals:
--
Strings literals are only support as arguments to (some) meta commands.

`"ASCII STRING"` - ASCII
`a"ASCII STRING"` - ASCII
`s"SCREEN CODE STRING"` - Screen codes (`& $3F`)


Identifier:
--
Identifiers must start with a letter or `_` and are allowed to contain `_`, 
letters and digits. Identifiers starting with `_` are local to the last
defined label, more on that later. The program counter can be read/modified
using the variable name `@`. Identifiers are case insensitive.

`LOOP: STA $D020` - Label
`_LOOP1: NOP` - local label
`HUND = 42` - variable   
`_HUND = 42` - local variable


Expressions:
--
**Unary operators:**

* `!` - logical not (i.e. 0 -> 1, !=0 -> 0)
* `~` - binary not (`^ -1`)
* `-` - negation
* `<` - low byte
* `>` - high byte

**Binary operators:**
 
* `+`, `-`, `*`. `/` - basic arithmetic
* `<<`, `>>` - (arithmetic) left/right shift
* `&`, `|`, `^` - binary and/or/xor
* `==`. `!=`. `<`. `<=`. `>`. `>=` - comparison  

**Operator precedence:**

*(Given from highest to lowest)*

1. Unary (`!`, `~`, `-`, `<`, `>`)
2. `*`, `/`
3. `+`, `-`
4. `<<`, `>>`
5. `<`, `<=`, `>=`, `>`
6. `==`, `!=`
7. `&`
8. `^`
9. `|`
 
**Remarks for using `(` and `)` for grouping expressions:**
Keep in mind that addressing mode detection happens *before* expression parsing,
i.e. `JMP (HUND + KATZE) + 1` will be recognized as `JMP` with indirect 
addressing and then throw a syntax error because of ` + 1`. The same applies
to all indirect addressing modes. If you really need braces, rearrange the
operands so that the expression does not start with a `(` or prepend something
 like `0 |` or `0 +` to avoid mistakes.

Opcode format:
--
`cetoneasm` does not support explicitly specifying `A` for opcodes that operate
on the accumulator, e.g. `ASL A`, instead just write `ASL`. Opcode names are
case insensitive.
Otherwise `cetoneasm` uses the following table (by Graham/Oxyron) for opcode naming 
and addressing modes: http://www.oxyron.de/html/opcodes02.html

Operands are of type *expression*.

Meta commands:
--
* `.DB`, `.DW` *expr*[,*expr*,*expr*,...] - store (B)ytes or (W)words
* `.REPB`, `.REPW`  *expr*,*expr*[,*expr*,*expr*,...] - write repeated (B)ytes
  or (W)words. First expression is the amount, following expressions are data
 