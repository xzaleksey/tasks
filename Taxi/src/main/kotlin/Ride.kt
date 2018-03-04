data class Ride(
        val id: Int,

        val rowIndex1: Int,
        val columnIndex1: Int,

        val rowIndex2: Int,
        val columnIndex2: Int,

        val startTime: Int,
        val endTime: Int,

        var complete: Boolean = false,
        private val correctRides: MutableSet<Ride> = mutableSetOf(),
        var previousRide: Ride? = null,
        var bestNextRideInfo: RideRatingInfo? = null) {

    fun getCorrectRides(): Set<Ride> {
        return correctRides.filter { !it.complete }.toSet()
    }

    fun addCorrectRides(rides: Set<Ride>) {
        correctRides.addAll(rides)
    }

    fun calculateDistance(): Int {
        return Math.abs(rowIndex1 - rowIndex2) + Math.abs(columnIndex2 - columnIndex1)
    }

    fun getWaiting(currentTime: Int): Int {
        return Math.max(startTime - currentTime, 0)
    }

    fun getDistanceToPointA(rowIndex: Int, columnIndex: Int): Int {
        return Math.abs(rowIndex1 - rowIndex) + Math.abs(columnIndex - columnIndex1)
    }

    override fun toString(): String {
        return "Ride(rowIndex1=$rowIndex1, columnIndex1=$columnIndex1, rowIndex2=$rowIndex2, columnIndex2=$columnIndex2, startTime=$startTime, endTime=$endTime, complete=$complete)"
    }


}

/**

● a – the row of the start intersection (0 ≤ a < R)
● b – the column of the start intersection (0 ≤ b < C)
● x – the row of the finish intersection (0 ≤ x < R)
● y – the column of the finish intersection (0 ≤ y < C)
● s – the earliest start(0 ≤ s < T)
● f – the latest finish (0 ≤ f ≤ T) , (f ≥ s + |x − a| + |y − b|)
○ note that f can be equal to T – this makes the latest finish equal to the end of the simulation
 */