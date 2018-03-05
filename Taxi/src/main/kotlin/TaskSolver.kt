class TaskSolver {

    fun solveTask(taskParams: TaskParams) {
        while (taskParams.drivers.any { !it.complete }) {
            for (driver in taskParams.drivers.filter { !it.complete }) {
                val nextBestRide = getNextBestRide(driver, taskParams)
                nextBestRide?.let {
                    driver.currentTime += it.totalTime
                    val ride = it.ride
                    ride.complete = true
                    driver.rides.add(it)
                    driver.currentRowIndex = ride.rowIndex2
                    driver.currentColumnIndex = ride.columnIndex2
                    taskParams.ridesComplete.add(ride)
                    taskParams.ridesUncomplete.remove(ride)
//                    println(ride)
                }
            }
            for (driver in taskParams.drivers.filter { !it.complete }) {
                val nextBestRide = getNextBestRide(driver, taskParams)
                nextBestRide?.let {
                    driver.currentRide = it
                    driver.currentTime += it.totalTime
                    val ride = it.ride
                    ride.complete = true
                    driver.rides.add(it)
                    driver.currentRowIndex = ride.rowIndex2
                    driver.currentColumnIndex = ride.columnIndex2
                    taskParams.ridesComplete.add(ride)
                    taskParams.ridesUncomplete.remove(ride)
//                    println(ride)
                }
                if (nextBestRide == null) {
                    driver.complete = true
                }
            }
        }
        outputDebugInfo(taskParams)
    }


    fun outputDebugInfo(taskParams: TaskParams) {
        var score = 0
        var totalRides = 0
        var totalWaiting = 0
        var totalDistanceToPointA = 0
        var totalKpd = 0.0

        val drivers = taskParams.drivers
        for (driver in drivers) {
            for (ride in driver.rides) {
                score += ride.realScore
                totalRides++
                totalWaiting += ride.waiting
                totalDistanceToPointA += ride.distanceToPintA
                totalKpd += ride.kpd
            }
        }
        val missedRides = totalRides - taskParams.ridesCount
        println("total score $score missedRides $missedRides avgWaiting ${totalWaiting / totalRides}" +
                " avgDistanceToPointA ${totalDistanceToPointA / totalRides} totalKpd ${totalKpd / totalRides}")
    }

    private fun getNextBestRide(driver: Driver, taskParams: TaskParams): RideRatingInfo? {
        var bestRide: RideRatingInfo?
        val currentRide = driver.currentRide

        if (currentRide == null) {
            bestRide = taskParams.getBestFirstRide(driver.currentTime, driver.currentRowIndex, driver.currentColumnIndex)
        } else {
            bestRide = taskParams.getBestNextRideDeep(driver.currentTime, currentRide.ride)
        }
        return bestRide
    }
}