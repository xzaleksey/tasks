package selfdrive

const val UNDEFINED = -1

class Ride(val x1: Int, val y1: Int,
           val x2: Int, val y2: Int,
           val startTime: Int,
           val finishTime: Int,
           var vehicleId: Int = UNDEFINED,
           var realStartTime: Int = UNDEFINED,
           var realEndTime: Int = UNDEFINED,
           var waitingTime: Int = 0,
           var startBonus: Int = 0,
           var finishBonus: Int = 0) {

    fun getDistance(): Int {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1)
    }
}
