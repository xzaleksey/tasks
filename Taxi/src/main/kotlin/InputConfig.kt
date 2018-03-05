object InputConfig {

    fun getTimeWindowShift(totalTime: Int): Int {
        return totalTime / 100
    }

    fun getSquareElementCount(rowsCount: Int): Int {
        var counter = 10
        while (rowsCount % counter != 0 && counter > 0) {
            counter--
        }

        return Math.max(Math.ceil((rowsCount / counter).toDouble()).toInt(), 1)
    }

    fun getBestRidesCount(): Int {
        return 5
    }
}