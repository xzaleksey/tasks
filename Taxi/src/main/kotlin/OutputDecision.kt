package selfdrive

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.*

object OutputDecision {

    @JvmStatic
    fun main(args: Array<String>) {
        val test = readFile("Taxi/b_should_be_easy.in")
        val testDecision = readFile("Taxi/b_should_be_easy.indecision")

        val data = test.split("\n").dropLastWhile { it.isEmpty() }.toTypedArray()
        val firstRow = data[0]


        val entryData = firstRow.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()

        val rowCount = entryData[0].toInt()
        val columnCount = entryData[1].toInt()
        val numberOfVehicles = entryData[2].toInt()
        val ridesCount = entryData[3].toInt()
        val startBonus = entryData[4].toInt()
        val endTime = entryData[5].toInt()

        val minRowIndex = 1
        val rides = LinkedHashMap<Int, Ride>()

        for (i in minRowIndex until data.size) {
            val row = data[i]
            val list = row.split(" ")
            rides[i - 1] = Ride(list[0].toInt(), list[1].toInt(), list[2].toInt(), list[3].toInt(), list[4].toInt(), list[5].toInt())
        }

        val dataDecision = testDecision.split("\n").dropLastWhile { it.isEmpty() }.toTypedArray()

        val minRowDecisionIndex = 0
        var totalWaiting = 0
        var totalScoring = 0
        var totalCompleteRides = 0
        var totalDistanceToPointA = 0

        val waitings = ArrayList<Int>()
        for (i in minRowDecisionIndex until dataDecision.size) {
            val row = dataDecision[i].split(" ")

            var time = 0
            var currentX = 0
            var currentY = 0

            for (rowIndex in minRowIndex until row.size) {
                val indexOfRide = row[rowIndex].toInt()
                val ride = rides[indexOfRide]!!
                ride.vehicleId = i
                val distanceToPointA = Math.abs(currentX - ride.x1) + Math.abs(currentY - ride.y1)
                totalDistanceToPointA += distanceToPointA

                val getToStartPointTime = time + Math.abs(currentX - ride.x1) + Math.abs(currentY - ride.y1)

                if (getToStartPointTime < ride.startTime) {
                    ride.waitingTime = ride.startTime - getToStartPointTime
                }

                waitings.add(ride.waitingTime)

                totalWaiting += ride.waitingTime
                ride.realStartTime = getToStartPointTime + ride.waitingTime

                time = getToStartPointTime + Math.abs(ride.x1 - ride.x2) + Math.abs(ride.y1 - ride.y2) + ride.waitingTime

                ride.realEndTime = time

                currentX = ride.x2
                currentY = ride.y2

                ride.startBonus = if (ride.realStartTime <= ride.startTime) startBonus else 0
                ride.finishBonus = if (ride.realEndTime <= ride.finishTime) ride.getDistance() else 0

                totalScoring += ride.startBonus + ride.finishBonus
                totalCompleteRides++
            }
        }

        val totalMissedRides = ridesCount - totalCompleteRides

        var totalMissedScore = 0
        for (rideEntry in rides) {
            val value = rideEntry.value
            if (value.vehicleId == UNDEFINED) {
                totalMissedScore += value.getDistance() + startBonus
            } else {
                totalMissedScore += startBonus - value.startBonus + value.getDistance() - value.finishBonus
            }
        }


        Collections.sort(waitings)

        val index = ((waitings.size - 1) * 0.95).toInt()

        println("total score $totalScoring missed rides " + " $totalMissedRides " +
                "totalMissedScore $totalMissedScore total waiting" + " $totalWaiting avgWaiting "
                + totalWaiting / totalCompleteRides + " totalDistanceToPointA " +
                "$totalDistanceToPointA avgDistancePerRideToPointA ${totalDistanceToPointA / totalCompleteRides}" +
                " waiting of 95 ${waitings[index]}")

    }

    @Throws(IOException::class)
    @JvmStatic
    private fun readFile(file: String): String {
        val reader = BufferedReader(FileReader(file))
        val stringBuilder = StringBuilder()
        val ls = System.getProperty("line.separator")

        try {
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                stringBuilder.append(ls)
                line = reader.readLine()
            }

            return stringBuilder.toString()
        } finally {
            reader.close()
        }
    }

}
