import InputConfig.getSquareElementCount
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


class InputFileReader {

    fun readFile(fileName: String): TaskParams {
        val inputData = readFile(fileName, Charset.defaultCharset())

        val rows = inputData.split("\n").dropLastWhile { it == "" }

        val entryData = rows.first().split(" ")
        val rowsCount = entryData[0].toInt()
        val columnsCount = entryData[1].toInt()


        val squareOfMapRowElementsCount = getSquareElementCount(rowsCount)
        val squareOfMapColumnElementsCount = getSquareElementCount(columnsCount)

        val taskParams = TaskParams(rowsCount,
                columnsCount,
                entryData[2].toInt(),
                entryData[3].toInt(),
                entryData[4].toInt(),
                entryData[5].toInt(),
                squareOfMapRowElementsCount,
                squareOfMapColumnElementsCount)

        for (i in 0 until rowsCount / squareOfMapRowElementsCount) {
            for (j in 0 until columnsCount / squareOfMapColumnElementsCount) {
                val rectangle = Rectangle(i * squareOfMapRowElementsCount,
                        j * squareOfMapColumnElementsCount,
                        (i + 1) * squareOfMapRowElementsCount - 1,
                        (j + 1) * squareOfMapColumnElementsCount - 1)
                val squareOfMap = SquareOfMap(rectangle)
                taskParams.squareOfElements.put(rectangle, squareOfMap)
            }
        }

        val minRowIndex = 1
        for (i in minRowIndex until rows.size) {
            val row = rows[i]
            val rideInfo = row.split(" ")
            val ride = Ride(i - 1, rideInfo[0].toInt(), rideInfo[1].toInt(), rideInfo[2].toInt(), rideInfo[3].toInt(), rideInfo[4].toInt(), rideInfo[5].toInt())
            val ridesByStartTime = taskParams.ridesByStartTime

            if (!ridesByStartTime.containsKey(ride.startTime)) {
                ridesByStartTime[ride.startTime] = mutableSetOf()
            }

            val squareOfMapByCoordinates = taskParams.getSquareOfMapByCoordinates(ride.rowIndex1, ride.columnIndex1)
            squareOfMapByCoordinates.rides.add(ride)


            ridesByStartTime[ride.startTime]!!.add(ride)
        }

        for (i in 0 until taskParams.carsCount) {
            taskParams.drivers.add(Driver())
        }

        return taskParams
    }

    @Throws(IOException::class)
    private fun readFile(path: String, encoding: Charset): String {
        val encoded = Files.readAllBytes(Paths.get(path))
        return String(encoded, encoding)
    }
}
