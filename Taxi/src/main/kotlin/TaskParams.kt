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
        val ridesByStartTime: MutableMap<Int, MutableSet<Ride>> = mutableMapOf(),
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

        return bestRideInfo
    }


    fun getBestNextRide(realEndTime: Int, ride: Ride, currentRowIndex: Int, currentColumnIndex: Int): RideRatingInfo? {
        if (ridesComplete.size == ridesCount) {
            return null
        }

        val rideInfo = getRideRatingInfo(ride, currentRowIndex, currentColumnIndex, realEndTime)

        val x2 = ride.rowIndex2
        val y2 = ride.columnIndex2

        var correctRides: Set<Ride> = ride.getCorrectRides()
        correctRides = getCorrectRides(correctRides, x2, y2, realEndTime)

        ride.addCorrectRides(correctRides)
        val bestRideInfo = getBestRideInfo(correctRides, x2, y2, realEndTime)
        ride.bestNextRideInfo = bestRideInfo

        return bestRideInfo
    }


    private fun getBestRideInfo(correctRides: Set<Ride>, x2: Int, y2: Int, currentTime: Int): RideRatingInfo? {
        var ratingInfo: RideRatingInfo? = null

        for (correctRide in correctRides) {
            val rideRatingInfo = getRideRatingInfo(correctRide, x2, y2, currentTime)
            if (ratingInfo == null || ratingInfo.kpd < rideRatingInfo.kpd) {
                ratingInfo = rideRatingInfo
            }
        }
        return ratingInfo
    }

    private fun getCorrectRides(cachedRides: Set<Ride>, x2: Int, y2: Int, currentTime: Int): Set<Ride> {
        var correctRides = cachedRides
        if (correctRides.isEmpty()) {

            val squareOfMap = getSquareOfMapByCoordinates(x2, y2)
            val nearSquares: MutableSet<SquareOfMap> = mutableSetOf(squareOfMap)

            var newSquares: Set<SquareOfMap> = mutableSetOf(squareOfMap)

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
                startTime, realEndTime, realScore, waiting)
    }

    private fun getRidesByTimeWithShift(currentTime: Int, currentRowIndex: Int, currentColumnIndex: Int, timeShift: Int): MutableSet<Ride> {
        val timeWithShift = currentTime + timeShift
        var time = 0
        val rides = mutableSetOf<Ride>()

        while (time <= timeWithShift) {
            ridesByStartTime[time]?.filter {
                !it.complete && getRideRatingInfo(it, currentRowIndex, currentColumnIndex, currentTime).rating > 0
            }?.let { rides.addAll(it) }
            time++
        }
        return rides
    }

    fun getDriversFromTimeShift(maxTime: Int): List<Driver> {
        return drivers.filter { it.currentTime <= maxTime }
    }
}

