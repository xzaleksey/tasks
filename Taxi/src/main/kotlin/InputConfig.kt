object InputConfig {

    fun getTimeWindowShift(totalTime: Int): Int {
        return totalTime / 10
    }

    fun getSquareElementCount(rowsCount: Int): Int {
        var counter = 6

        return Math.max(Math.ceil((rowsCount / counter).toDouble()).toInt(), 1)
    }

    fun getBestRidesCount(): Int {
        return 5
    }
}