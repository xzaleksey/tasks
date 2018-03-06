internal object StartSolvingTask {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputFileReader = InputFileReader()
        val fileName = "Taxi/c_no_hurry.in"
        val taskParams = inputFileReader.readFile(fileName)
        TaskSolver().solveTask(taskParams)
        OutputFileSaver().saveResult(taskParams.drivers, fileName + "decision")
    }
}
