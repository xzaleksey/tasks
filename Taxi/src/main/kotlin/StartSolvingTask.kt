internal object StartSolvingTask {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputFileReader = InputFileReader()
        val fileName = "Taxi/b_should_be_easy.in"
        val taskParams = inputFileReader.readFile(fileName)
        TaskSolver().solveTask(taskParams)
        OutputFileSaver().saveResult(taskParams.drivers, fileName + "decision")
    }
}
