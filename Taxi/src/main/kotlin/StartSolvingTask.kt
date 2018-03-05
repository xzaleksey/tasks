internal object StartSolvingTask {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputFileReader = InputFileReader()
        val fileName = "Taxi/d_metropolis.in"
        val taskParams = inputFileReader.readFile(fileName)
        TaskSolver().solveTask(taskParams)
        OutputFileSaver().saveResult(taskParams.drivers, fileName + "decision")
    }
}
