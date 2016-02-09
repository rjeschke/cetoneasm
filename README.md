cetoneasm - A C64 (Cross) Macro Assembler
=========================================

TODO:
--

* .INCLUDE
* .BINCLUDE
* .WHILE/ENDWHILE
* .REP/.ENDREP
* command line arguments
* configuration file
* server mode
* endless loop detection


Number literals:
--
`cetoneasm` uses a `long` number type (i.e. int64_t) as the basic number type.

* `123` - decimal
* `$12` - hexadecimal
* `0x12` - hexadecimal
* `0o12` - octal
* `0b1101` - binary


String literals:
--
Strings literals are only support as arguments to (some) meta commands.

* `"ASCII STRING"` - ASCII
* `a"ASCII STRING"` - ASCII
* `s"SCREEN CODE STRING"` - Screen codes (`& $3F`)


Identifier:
--
Identifiers must start with a letter or `_` and are allowed to contain `_`, 
letters and digits. Identifiers starting with `_` are local to the last
defined label, more on that later. The program counter can be read/modified
using the variable name `@`. Identifiers are case insensitive.

* `LOOP: STA $D020` - Label
* `_LOOP1: NOP` - local label
* `HUND = 42` - variable   
* `_HUND = 42` - local variable


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


Internals:
--
**Name mangling:**

Error example:

    [ 0.030] [E] : testing.casm:36: Duplicate label 'LOOP$$_LOOP2'

Local variables and local variables are (in the end) a concatenation of the parent
label, `$$` and the local label, e.g. `LOOP$$_LOOP`.

Labels and variables (also arguments) defined in `.MACRO` are converted to local
labels/variables by prepending `__`, e.g. `__ADDR`. A label, consisting of the
macro's name and an index is created as the parent label, e.g. `MY_MACRO$1:`.

**Why .GOTO and .LABEL?**

My main goal was to keep the assembler simple but versatile. In the beginning I
thought about omitting loops completely, but I changed my mind about this quite
early. Then I was thinking about what kind of loops to support and if `continue`
and `break` would be needed. These thought pointed into a direction that would
make the assembler way too bloated so I decided to only use `.WHILE`/`.ENDWHILE`.

But, as there are situations where some more control over execution flow might be
needed I added `.GOTO` and `.LABEL` *(we're developing for the C64 anyways so
we're not scared of `GOTO`)*. This way everybody is free to shoot himself/herself
into the foot (the assembler does not detect *empty* endless loops, loops that do
not emit bytes ... I should put this on my TODO list).

