// FIX: Replace 'if' expression with safe access expression
// WITH_STDLIB
// HIGHLIGHT: INFORMATION
fun convert(x: String, y: Int) = ""

fun foo(a: String?, it: Int) {
    <caret>if (a != null) convert(a, it) else null
}
