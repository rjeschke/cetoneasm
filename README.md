cetoneasm - A C64 (Cross) Macro Assembler
=========================================

TODO:
--

* `.BINCLUDE`
* `.INFO`, `.WARN`, `.ERROR`
* configuration file
* server mode
* endless loop detection (detection implemented, now handle it)
* `.CONST` ?

Number literals:
--
`cetoneasm` uses a `long` number type (i.e. int64_t) as the basic number type.

* `123` - decimal
* `$12` - hexadecimal
* `0x12`, `0X34` - hexadecimal
* `0o12`, `0O34` - octal
* `0b1101`, `0B1011` - binary


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
* `JMP @` - endless loop


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
* `.DB` *exprs/strings* 
* `.DW` *exprs/strings* 
* `.REPB` *count-expr*, *exprs/strings* 
* `.REPW` *count-expr*, *exprs/strings* 
* `.IF` *expr*, `.ELIF` *expr*, `.ELSE`, `.ENDIF`
* `.MACRO` *name-ascii-str*, *vars-ascii-str*, `.ENDMACRO`
* `.CALL` *name-ascii-str*, *exprs*
* `.REP` *count-expr*, `.ENDREP`
* `.WHILE` *expr*, `.ENDWHILE`
* `.LABEL` *identifier*
* `.GOTO` *identifier*
* `.INCLUDE` *name-ascii-str*
* `.BINCLUDE` *name-ascii-str*[, *skip-bytes-number*, *length-number*]
* `.INFO`/`.INFOF` *strings/exprs*
* `.WARN`/`.WARNF` *strings/exprs*
* `.ERROR`/`.ERRORF` *strings/exprs*

Common pitfalls and quirks:
---

* `LDA $00` and `LDA $0000` both are the same opcode in the end. If you *need*
  word width addressing use another placeholder (e.g. `LDA -1`, or `LDA @` (when
  `@` > $FF))
* `LABEL: @ = $1000` will not work as you might expect, as the label assignment
  happens *before* the expression after it gets evaluated. 
* `.CALL` inside of `.MACRO` is forbidden
* `.MACRO` inside any type of block element is forbidden
* it is not possible to use mangled labels or variables directly *(this is intended)*
* labels and variables should not be used inside of `.REP`, `.ENDREP` (of course)
* `X` and `Y` can be used as variables and labels, but it's usage is discouraged
  to improve source code readability
* `.GOTO` can be used safely to exit any kind of control flow block (even `.REP`) 
* having labels and variables of the same name is forbidden *(makes sense somehow^^)* 
* `.LABEL` does not get mangled in `.MACRO` so I recommend *not* to use it inside
  of macros
* it is possible to read/modify local macro variables after macro expansion, as those
  do not contain unusable identifier characters and therefore leak
* there is no need to use local variables or labels inside of `.MACRO` as everything
  is mangled into locals anyway
* `.MACRO` names have their own namespace and can therefore be identical to already
  defines variable or label names
* `.INCLUDE` is evaluated in the very first pass, there's no other evaluation going
  on. This means that you can not do conditional includes. Includes *always* get
  included.

Internals:
--
**Name mangling:**

Error example:

    [ 0.030] [E] : testing.casm:36: Duplicate label 'LOOP$$_LOOP'

Local variables and local labels are (in the end) a concatenation of the parent
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
into the foot (the assembler does not detect *empty* endless loops, i.e. loops that
do not emit bytes ... I should put this on my TODO list).

**cetoneasm is generating too much output!**

I am German, I love statistics and flashing logs and stuff.

**Why so many passes?**

> My main goal was to keep the assembler simple but versatile.

The assembler works internally using lists of actions. Those actions simply get
interpreted in the assembly passes. Separating the whole assembly process into
simple passes aids the overall design.

The first pass only expands `.INCLUDE` directives, checks for circular includes
and *injects* the list of actions generated by include parsing into the main
list. 

The next pass first translates `.MACRO` and after that `.CALL` actions. This
results in having all `.MACRO` calls removed and all `.CALL` actions replaced
by their outcome.

The third pass now gathers all defined labels and variables and creates local
variable/label mangling.

After the third pass we end up with a neat, (nearly) flat list of actions that
just can be interpreted.

**Why 3(!) assembly passes?**

Mostly because I was quite lazy when writing the initial version of the assembler,
but also because of that variables and labels might be used before initialization,
and therefore some opcodes might vary in their width (which would shift all labels
defined after that by one byte up/down). I could just check if uninitialized 
variables or labels were used and then skip the last two passes, also the third
assembly pass is just for my paranoid self (and should only be needed for very,
very few situations (e.g. compiling into the zeropage while crossing page one's
border). 

  