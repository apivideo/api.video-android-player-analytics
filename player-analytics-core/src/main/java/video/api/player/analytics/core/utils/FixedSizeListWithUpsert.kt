package video.api.player.analytics.core.utils

/**
 * A list that upserts elements and has a maximum size.
 * @param maxSize The maximum size of the list.
 */
internal class FixedSizeListWithUpsert<T>(
    private val maxSize: Int,
    private val elements: MutableList<T> = mutableListOf()
) : MutableList<T> by elements {
    /**
     * Adds an element to the list.
     *
     * It will upsert the element if it is already in the list.
     * If the list is full, it will remove the first element.
     */
    override fun add(element: T): Boolean {
        // Upsert element
        if (elements.isNotEmpty()) {
            if (elements.last() == element) {
                elements.removeLast()
                return elements.add(element)
            }
        }

        // Shift elements if list is full
        if (elements.size >= maxSize) {
            elements.removeFirst()
        }
        return elements.add(element)
    }
}