import java.util.*
import kotlin.collections.LinkedHashSet

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
        var drivers: MutableList<Driver> = mutableListOf()) {

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

    fun getBestFirstRide(currentTime: Int, currentRowIndex: Int, currentColumnIndex: Int): RideRatingInfo? {
        if (ridesComplete.size == ridesCount) {
            return null
        }

        val correctRides = getCorrectRides(Collections.emptySet(), currentRowIndex, currentColumnIndex, currentTime)
        val bestRideInfo = getBestRideInfo(correctRides, currentRowIndex, currentColumnIndex, currentTime)

        return bestRideInfo.firstOrNull()
    }

    fun getBestNextRideDeep(realEndTime: Int, ride: Ride): RideRatingInfo? {
        val bestNextRides = getBestNextRide(realEndTime, ride)
        if (bestNextRides.isEmpty()) {
            return null
        }

        if (bestNextRides.size == 1) {
            return bestNextRides.first()
        }

        val futureBestRides: MutableMap<Ride, RideRatingInfo?> = mutableMapOf()

        bestNextRides.forEach {
            futureBestRides[it.ride] = getBestNextRide(realEndTime + it.totalTime, it.ride).firstOrNull()
        }
        var bestRideRatingInfo: RideRatingInfo? = null
        bestNextRides.forEach {
            if (bestRideRatingInfo == null) {
                bestRideRatingInfo = it
            } else {
                val futureKpdBest: Double = futureBestRides[bestRideRatingInfo!!.ride]?.kpd ?: 0.0
                val futureKpdCurrent: Double = futureBestRides[it.ride]?.kpd ?: 0.0
                if (bestRideRatingInfo!!.kpd + futureKpdBest < it.kpd + futureKpdCurrent) {
                    bestRideRatingInfo = it
                }
            }
        }
        return bestRideRatingInfo
    }


    fun getBestNextRide(realEndTime: Int, ride: Ride): List<RideRatingInfo> {
        if (ridesComplete.size == ridesCount) {
            return Collections.emptyList()
        }

        val x2 = ride.rowIndex2
        val y2 = ride.columnIndex2
        var correctRides: Set<Ride> = ride.getCorrectRides()

        correctRides = getCorrectRides(correctRides, x2, y2, realEndTime)

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

    private fun getCorrectRides(cachedRides: Set<Ride>, x2: Int, y2: Int, currentTime: Int): Set<Ride> {
        var correctRides = cachedRides
        if (correctRides.isEmpty()) {

            val squareOfMap = getSquareOfMapByCoordinates(x2, y2)
            val nearSquares: MutableSet<SquareOfMap> = LinkedHashSet(getNearestSquares(squareOfMap))

            var newSquares: Set<SquareOfMap> = LinkedHashSet(nearSquares)

            var timeShift = InputConfig.getTimeWindowShift(totalTime)
            var ridesByTime = getRidesByTimeWithShift(currentTime, x2, y2, timeShift)

            while (correctRides.isEmpty()) {
                correctRides = getRidesFromSquares(nearSquares).intersect(ridesByTime)

                if (correctRides.isNotEmpty()) {
                    break
                }
                val ridesByTimeFinished = currentTime + timeShift > totalTime && ridesByTime.isEmpty()

                if (nearSquares.size == squareOfElements.size && ridesByTimeFinished) {
                    break
                }

                if (correctRides.isEmpty()) {
                    newSquares = getNearestSquares(newSquares).minus(nearSquares)
                    val ridesFromSquares = getRidesFromSquares(newSquares)
                    correctRides = ridesFromSquares.intersect(ridesByTime)
                    nearSquares.addAll(newSquares)
                } else {
                    break
                }

                if (ridesByTimeFinished) {
                    continue
                }

                if (currentTime + timeShift <= totalTime && correctRides.isEmpty()) {
                    timeShift += InputConfig.getTimeWindowShift(totalTime)
                    ridesByTime = getRidesByTimeWithShift(currentTime, x2, y2, timeShift)
                    correctRides = getRidesFromSquares(nearSquares).intersect(ridesByTime)
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
            waiting = ride.getWaiting(currentTime)
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

        return RideRatingInfo(totalTime, rating, ride, rating.toDouble() / totalTime,
                startTime, realEndTime, realScore, waiting, distanceToPointA)
    }

    private fun getRidesByTimeWithShift(currentTime: Int, currentRowIndex: Int, currentColumnIndex: Int, timeShift: Int): MutableSet<Ride> {
        val timeWithShift = currentTime + timeShift
        val rides = mutableSetOf<Ride>()

        val timesIterator = ridesByStartTime.keys.iterator()
        var timeKey = timesIterator.next()

        while (timesIterator.hasNext() && timeKey <= timeWithShift) {
            ridesByStartTime[timeKey]?.filter {
                !it.complete && getRideRatingInfo(it, currentRowIndex, currentColumnIndex, currentTime).rating > 0
            }?.let { rides.addAll(it) }
            timeKey = timesIterator.next()
        }
        return rides
    }

    fun getDriversFromTimeShift(maxTime: Int): List<Driver> {
        return drivers.filter { it.currentTime <= maxTime }
    }
}

