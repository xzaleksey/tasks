class SquareOfMap(val rectangle: Rectangle) {
    val rides: MutableSet<Ride> = mutableSetOf()

    fun getUncompleteRides(): List<Ride> {
        return rides.filter { !it.complete }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SquareOfMap

        if (rectangle != other.rectangle) return false

        return true
    }

    override fun hashCode(): Int {
        return rectangle.hashCode()
    }

}