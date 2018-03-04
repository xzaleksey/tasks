class Driver(
        val rides: MutableSet<RideRatingInfo> = mutableSetOf(),
        var currentRowIndex: Int = 0,
        var currentColumnIndex: Int = 0,
        var currentTime: Int = 0,
        var currentRide: RideRatingInfo? = null,
        var complete: Boolean = false)