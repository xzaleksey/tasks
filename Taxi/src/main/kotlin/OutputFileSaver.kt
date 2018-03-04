import java.io.File
import java.io.IOException


class OutputFileSaver {

    fun saveResult(drivers: List<Driver>, path: String) {
        val stringBuilder = StringBuilder()
        for (driver in drivers) {
            val rides = driver.rides

            stringBuilder.append(rides.size)
            rides.forEach {
                stringBuilder.append(" ").append(it.ride.id)
            }
            stringBuilder.append("\n")
        }
        writeFile(path, stringBuilder.toString())
    }

    @Throws(IOException::class)
    private fun writeFile(path: String, text: String) {
        File(path).writeText(text)
    }
}
