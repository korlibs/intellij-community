// FIR_COMPARISON
// FIR_IDENTICAL
import dependency.xxx
import A.xxx

object A {
    fun xxx() {}
}

fun test() {
    xx<caret>
}
// ELEMENT: xxx
// TAIL_TEXT: "() (dependency)"