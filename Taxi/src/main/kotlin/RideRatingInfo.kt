class RideRatingInfo(val totalTime: Int,
                     val rating: Int,
                     val ride: Ride,
                     val kpd: Double = 0.0,
                     val realStartTime: Int,
                     val realEndTime: Int,
                     val realScore: Int,
                     val waiting: Int)