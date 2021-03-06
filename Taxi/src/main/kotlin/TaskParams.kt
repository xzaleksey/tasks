import java.util.*

class TaskParams(
        val rows: Int,
        val columns: Int,
        val carsCount: Int,
        val ridesCount: Int,
        val rideStartBonus: Int,
        val totalTime: Int,
        val squareOfMapRowElementsCount: Int,
        val squareOfMapColumnElementsCount: Int,
        val squareOfElements: MutableMap<Rectangle, SquareOfMap> = mutableMapOf(),
        val ridesByStartTime: MutableMap<Int, MutableSet<Ride>> = TreeMap(),
        val ridesComplete: MutableSet<Ride> = mutableSetOf(),
        var drivers: MutableList<Driver> = mutableListOf(),
        var ridesUncomplete: MutableSet<Ride> = mutableSetOf()) {

    fun getSquareOfMapByCoordinates(rowIndex: Int, columnIndex: Int): SquareOfMap {
        val leftRow = rowIndex - rowIndex % squareOfMapRowElementsCount
        val leftColumn = columnIndex - columnIndex % squareOfMapColumnElementsCount
        val rightRow = leftRow + squareOfMapRowElementsCount - 1
        val rightColumn = leftColumn + squareOfMapColumnElementsCount - 1

        return squareOfElements.get(Rectangle(leftRow, leftColumn, rightRow, rightColumn))!!
    }

    fun getNextSquare(squareOfMap: SquareOfMap, direction: Direction): SquareOfMap? {
        var horizontalShift = 0
        var verticalShift = 0

        when (direction) {
            Direction.LEfT -> horizontalShift = -squareOfMapRowElementsCount
            Direction.RIGHT -> horizontalShift = squareOfMapRowElementsCount
            Direction.UP -> verticalShift = -squareOfMapColumnElementsCount
            Direction.BOTTOM -> verticalShift = squareOfMapColumnElementsCount
            Direction.LEFT_BOTTOM -> {
                horizontalShift = -squareOfMapRowElementsCount
                verticalShift = squareOfMapColumnElementsCount
            }
            Direction.RIGHT_BOTTOM -> {
                horizontalShift = squareOfMapRowElementsCount
                verticalShift = squareOfMapColumnElementsCount
            }
            Direction.LEFT_UP -> {
                horizontalShift = -squareOfMapRowElementsCount
                verticalShift = -squareOfMapColumnElementsCount
            }
            Direction.RIGHT_UP -> {
                horizontalShift = squareOfMapRowElementsCount
                verticalShift = -squareOfMapColumnElementsCount
            }
        }

        val rectangle = squareOfMap.rectangle
        val newRectangle = Rectangle(rectangle.leftRowIndex + horizontalShift,
                rectangle.leftColumnIndex + verticalShift,
                rectangle.rightRowIndex + horizontalShift,
                rectangle.rightColumnIndex + verticalShift)
        return squareOfElements[newRectangle]
    }

    fun getRideScore(ride: Ride): Int {
        return ride.calculateDistance() + rideStartBonus
    }

    private fun getUncompleteRides(ride: Ride?): Set<Ride> {
        return ridesUncomplete.filter {
            !it.complete && (it.previousRide == null || it.previousRide == ride)
        }.toSet()
    }

    private fun getRideWithPrevious(ride: Ride): Ride? {
        return ridesUncomplete.filter {
            !it.complete && (it.previousRide == ride)
        }.firstOrNull()
    }

    fun getBestFirstRide(currentTime: Int, currentRowIndex: Int, currentColumnIndex: Int): RideRatingInfo? {
        if (ridesComplete.size == ridesCount) {
            return null
        }

        val correctRides = getCorrectRides(Collections.emptySet(), currentRowIndex, currentColumnIndex, currentTime, null)
        val bestNextRides = getBestRideInfo(correctRides, currentRowIndex, currentColumnIndex, currentTime)
        if (bestNextRides.isEmpty()) {
            return null
        }

        return bestNextRides.first()

//        val futureBestRides: MutableMap<Ride, RideRatingInfo?> = mutableMapOf()
//
//        for (rideRatingInfo in bestNextRides) {
//            futureBestRides[rideRatingInfo.ride] = getBestNextRide(rideRatingInfo.totalTime, rideRatingInfo.ride).firstOrNull()
//        }
//
//        var bestRideRatingInfo: RideRatingInfo? = null
//
//        bestNextRides.forEach {
//            if (bestRideRatingInfo == null) {
//                bestRideRatingInfo = it
//            } else {
//                val futureKpdBest: Double = futureBestRides[bestRideRatingInfo!!.ride]?.kpd ?: 0.0
//                val futureKpdCurrent: Double = futureBestRides[it.ride]?.kpd ?: 0.0
//                if (bestRideRatingInfo!!.kpd + futureKpdBest < it.kpd + futureKpdCurrent) {
//                    bestRideRatingInfo = it
//                }
//            }
//        }
//        return bestRideRatingInfo
    }

    fun getBestNextRideDeep(realEndTime: Int, ride: Ride): RideRatingInfo? {
//        getRideWithPrevious(ride)?.let {
//            val rideRatingInfo = getRideRatingInfo(it, ride.rowIndex2, ride.columnIndex2, realEndTime)
//            getBestNextRide(rideRatingInfo.realEndTime, rideRatingInfo.ride).firstOrNull()?.let {
//                it.ride.previousRide = rideRatingInfo.ride
//            }
//            return rideRatingInfo
//        }

        val bestNextRides = getBestNextRide(realEndTime, ride)


        if (bestNextRides.isEmpty()) {
            return null
        }

        return bestNextRides.first()

//        val futureBestRides: MutableMap<Ride, RideRatingInfo?> = mutableMapOf()
//
//        bestNextRides.forEach {
//            futureBestRides[it.ride] = getBestNextRide(realEndTime + it.totalTime, it.ride).firstOrNull()
//        }
//
//        var bestRideRatingInfo: RideRatingInfo? = null
//        bestNextRides.forEach {
//            if (bestRideRatingInfo == null) {
//                bestRideRatingInfo = it
//            } else {
//                val futureKpdBest: Double = futureBestRides[bestRideRatingInfo!!.ride]?.kpd ?: 0.0
//                val futureKpdCurrent: Double = futureBestRides[it.ride]?.kpd ?: 0.0
//                if (bestRideRatingInfo!!.kpd + futureKpdBest < it.kpd + futureKpdCurrent) {
//                    bestRideRatingInfo = it
//                }
//            }
//        }
//
//        futureBestRides[bestRideRatingInfo!!.ride]?.let {
//            it.ride.previousRide = bestRideRatingInfo!!.ride
//        }
//
//        return bestRideRatingInfo
    }


    fun getBestNextRide(realEndTime: Int, ride: Ride): List<RideRatingInfo> {
        if (ridesComplete.size == ridesCount) {
            return Collections.emptyList()
        }

        val x2 = ride.rowIndex2
        val y2 = ride.columnIndex2
        var correctRides: Set<Ride> = ride.getCorrectRides()

        correctRides = getCorrectRides(correctRides, x2, y2, realEndTime, ride)

        ride.addCorrectRides(correctRides)

        val bestRideInfo = getBestRideInfo(correctRides, x2, y2, realEndTime)
        ride.bestNextRideInfo = bestRideInfo.firstOrNull()

        return bestRideInfo
    }

    private fun getBestRideInfo(correctRides: Set<Ride>, x2: Int, y2: Int, currentTime: Int): List<RideRatingInfo> {
        val bestRatingInfos: MutableList<RideRatingInfo> = mutableListOf()

        for (correctRide in correctRides) {
            val rideRatingInfo = getRideRatingInfo(correctRide, x2, y2, currentTime)
            bestRatingInfos.add(rideRatingInfo)
        }

        return bestRatingInfos.sortedWith(kotlin.Comparator { o1, o2 ->
            if (o1.kpd < o2.kpd) return@Comparator 1
            if (o1.kpd > o2.kpd) return@Comparator -1

            return@Comparator 0
        }).take(InputConfig.getBestRidesCount())
    }

    private fun getCorrectRides(cachedRides: Set<Ride>, x2: Int, y2: Int, currentTime: Int, ride: Ride?): Set<Ride> {
        var correctRides = cachedRides
        if (correctRides.isEmpty()) {

            var timeShift = InputConfig.getTimeWindowShift(totalTime)
            var ridesByTime = getRidesByTimeWithShift(currentTime, x2, y2, timeShift)
            val uncompleteRides = getUncompleteRides(ride)

            while (correctRides.isEmpty()) {
                correctRides = ridesByTime.intersect(uncompleteRides)

                if (correctRides.isNotEmpty()) {
                    break
                }
                val ridesByTimeFinished = currentTime + timeShift > totalTime && correctRides.isEmpty()

                if (ridesByTimeFinished) {
                    break
                }

                if (currentTime + timeShift <= totalTime && correctRides.isEmpty()) {
                    timeShift += InputConfig.getTimeWindowShift(totalTime)
                    ridesByTime = getRidesByTimeWithShift(currentTime, x2, y2, timeShift)
                    correctRides = ridesByTime.intersect(uncompleteRides)
                }
            }
        }
        return correctRides
    }

    fun getNearestSquares(squares: Set<SquareOfMap>): Set<SquareOfMap> {
        val nearestSquares = mutableSetOf<SquareOfMap>()

        squares.forEach {
            nearestSquares.addAll(getNearestSquares(it))
        }

        return nearestSquares
    }

    private fun getNearestSquares(squareOfMap: SquareOfMap): Set<SquareOfMap> {
        val nearestSquares = mutableSetOf<SquareOfMap>()

        getNextSquare(squareOfMap, Direction.LEfT)?.let {
            nearestSquares.add(it)
        }
        getNextSquare(squareOfMap, Direction.RIGHT)?.let {
            nearestSquares.add(it)
        }
        getNextSquare(squareOfMap, Direction.UP)?.let {
            nearestSquares.add(it)
        }
        getNextSquare(squareOfMap, Direction.BOTTOM)?.let {
            nearestSquares.add(it)
        }
        getNextSquare(squareOfMap, Direction.LEFT_UP)?.let {
            nearestSquares.add(it)
        }
        getNextSquare(squareOfMap, Direction.LEFT_BOTTOM)?.let {
            nearestSquares.add(it)
        }
        getNextSquare(squareOfMap, Direction.RIGHT_UP)?.let {
            nearestSquares.add(it)
        }
        getNextSquare(squareOfMap, Direction.RIGHT_BOTTOM)?.let {
            nearestSquares.add(it)
        }

        return nearestSquares
    }

    private fun getRidesFromSquares(squares: Set<SquareOfMap>): Set<Ride> {
        val nearestRides = mutableSetOf<Ride>()
        squares.forEach { nearestRides.addAll(it.getUncompleteRides()) }
        return nearestRides
    }

    private fun getRideRatingInfo(ride: Ride, currentRowIndex: Int, currentColumnIndex: Int, currentTime: Int): RideRatingInfo {
        val distanceToPointA = ride.getDistanceToPointA(currentRowIndex, currentColumnIndex)
        val distance = ride.calculateDistance()

        val arrivalTime = currentTime + distanceToPointA
        var rating = 0
        var waiting = 0
        var realScore = 0

        if (arrivalTime <= ride.startTime) {
            rating += rideStartBonus
            waiting = ride.getWaiting(arrivalTime)
            rating -= waiting
            realScore += rideStartBonus
        }

        val startTime = arrivalTime + waiting
        val totalTime = distanceToPointA + distance + waiting

        val realEndTime = startTime + distance
        if (realEndTime <= ride.endTime) {
            rating += distance
            realScore += distance
        }

        return RideRatingInfo(totalTime, rating, ride, realScore.toDouble() / totalTime,
                startTime, realEndTime, realScore, waiting, distanceToPointA)
    }

    private fun getRidesByTimeWithShift(currentTime: Int, currentRowIndex: Int, currentColumnIndex: Int, timeShift: Int): MutableSet<Ride> {
        val timeWithShift = currentTime + timeShift
        val rides = mutableSetOf<Ride>()

        val timesIterator = ridesByStartTime.keys.iterator()

        while (timesIterator.hasNext()) {
            val timeKey = timesIterator.next()
            ridesByStartTime[timeKey]?.filter {
                val rideRatingInfo = getRideRatingInfo(it, currentRowIndex, currentColumnIndex, currentTime)
                !it.complete && rideRatingInfo.kpd > 0
            }?.let { rides.addAll(it) }
        }
        return rides
    }

    fun getDriversFromTimeShift(maxTime: Int): List<Driver> {
        return drivers.filter { it.currentTime <= maxTime }
    }
}

