import java.util.Comparator

fun <U, V> createComparator(unused: U, keyExtractor: (V) -> String = { it.toString() }): Comparator<V> = TODO()

// U - inferrable from 'unused' => (U)
// (V) -> String - V is not in (U) => (U)
// V - cannot be inferred from (U)

fun testInsertion() {
    val comp: Comparator<Int> = createComparator(10).<caret>
}

// ELEMENT: reversed
// TAIL_TEXT: "()"