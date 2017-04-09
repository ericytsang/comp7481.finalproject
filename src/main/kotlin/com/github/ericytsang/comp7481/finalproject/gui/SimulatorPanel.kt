package com.github.ericytsang.comp7481.finalproject.gui

import com.github.ericytsang.comp7481.finalproject.model.CodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.DataBlockGenerator
import com.github.ericytsang.comp7481.finalproject.model.ErrorInducer
import com.github.ericytsang.comp7481.finalproject.model.SimpleHammingCodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.Transformer
import com.github.ericytsang.comp7481.finalproject.model.TransformerFlattener
import com.github.ericytsang.comp7481.finalproject.model.bits
import com.github.ericytsang.lib.concurrent.future
import com.sun.javafx.collections.ObservableListWrapper
import javafx.application.Platform
import javafx.beans.Observable
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.chart.PieChart
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import java.io.Closeable
import java.net.URL
import java.util.ResourceBundle
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SimulatorPanel:VBox(),Initializable,Closeable
{
    @FXML lateinit var codePanel:CodePanel
    @FXML lateinit var dataBlockSizeLabel:SubstituteLabel
    @FXML lateinit var dataBlockSizeSlider:Slider
    @FXML lateinit var minBurstErrorSizeLabel:SubstituteLabel
    @FXML lateinit var minBurstErrorSizeSlider:Slider
    @FXML lateinit var maxBurstErrorSizeLabel:SubstituteLabel
    @FXML lateinit var maxBurstErrorSizeSlider:Slider
    @FXML lateinit var burstErrorFrequencyLabel:SubstituteLabel
    @FXML lateinit var burstErrorFrequencySlider:Slider
    @FXML lateinit var efficiencyLabel:SubstituteLabel
    @FXML lateinit var totalDataTxLabel:SubstituteLabel
    @FXML lateinit var totalCodeLabel:SubstituteLabel
    @FXML lateinit var bitErrorCountLabel:SubstituteLabel
    @FXML lateinit var burstErrorCountLabel:SubstituteLabel
    @FXML lateinit var errorsDiscardedLabel:SubstituteLabel
    @FXML lateinit var errorsAcceptedLabel:SubstituteLabel
    @FXML lateinit var normalAcceptedLabel:SubstituteLabel
    @FXML lateinit var pieChart:PieChart

    private var totalDataBitsEncoded:Long = 0
    private var totalCodeBits:Long = 0
    private var bitErrorCount:Long = 0
    private var burstErrorCount:Long = 0
    private var badBitsAcceptedCount:Long = 0
    private var badBitsRejectedCount:Long = 0
    private var goodBitsAcceptedCount:Long = 0
    private val statsUpdater = object:UiComponent()
    {
        private val goodBitsAcceptedPieData = PieChart.Data("correct bits accepted",0.0)
        private val correctedBitsAcceptedPieData = PieChart.Data("corrected bits accepted",0.0)
        private val badBitsAcceptedPieData = PieChart.Data("erroneous bits accepted",0.0)
        private val badBitsRejectedPieData = PieChart.Data("bits discarded",0.0)
        private val pieChartData = ObservableListWrapper(listOf(
            goodBitsAcceptedPieData,
            correctedBitsAcceptedPieData,
            badBitsAcceptedPieData,
            badBitsRejectedPieData))

        fun update() = publishUpdate()
        {
            efficiencyLabel.substitute((totalDataBitsEncoded.toDouble()/(totalCodeBits.takeIf {it != 0.toLong()}?:1)*100).toString())
            totalDataTxLabel.substitute(totalDataBitsEncoded.toString())
            totalCodeLabel.substitute(totalCodeBits.toString())
            bitErrorCountLabel.substitute(bitErrorCount.toString())
            burstErrorCountLabel.substitute(burstErrorCount.toString())
            errorsDiscardedLabel.substitute(badBitsRejectedCount.toString())
            errorsAcceptedLabel.substitute(badBitsAcceptedCount.toString())
            normalAcceptedLabel.substitute(goodBitsAcceptedCount.toString())
            goodBitsAcceptedPieData.pieValue = (goodBitsAcceptedCount-(bitErrorCount-badBitsAcceptedCount)).toDouble()
            correctedBitsAcceptedPieData.pieValue = (bitErrorCount-badBitsAcceptedCount).toDouble()
            badBitsAcceptedPieData.pieValue = badBitsAcceptedCount.toDouble()
            badBitsRejectedPieData.pieValue = badBitsRejectedCount.toDouble()
            if (pieChart.data !== pieChartData)
            {
                pieChart.data = pieChartData
            }
        }
    }

    private val controlsUpdater = object:UiComponent()
    {
        fun update() = publishUpdate()
        {
            dataBlockSizeLabel.substitute(dataBlockSizeSlider.value.let {Math.round(it)}.toString())
            burstErrorFrequencyLabel.substitute(burstErrorFrequencySlider.value.let {Math.round(it)}.toString())
            minBurstErrorSizeLabel.substitute(minBurstErrorSizeSlider.value.let {Math.round(it)}.toString())
            maxBurstErrorSizeLabel.substitute(maxBurstErrorSizeSlider.value.let {Math.round(it)}.toString())
        }
    }

    private val workerExecutor = object
    {
        private val executorService = Executors.newSingleThreadExecutor()
        private var lastFuture:FutureTask<*>? = null
        fun submitTask(task:()->Unit)
        {
            lastFuture?.cancel(false)
            lastFuture = future(false,block = task)
            executorService.execute(lastFuture)
        }
        fun close()
        {
            executorService.shutdown()
        }
    }

    private var worker:Worker? = null
        set(value)
        {
            field?.interrupt()
            field?.join()
            field = value
            field?.start()
        }

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("window_simulator.fxml")!!)
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }

    override fun initialize(location:URL,resources:ResourceBundle?)
    {
        onInputsChanged()
        dataBlockSizeSlider.valueProperty().addListener {_:Observable -> onInputsChanged()}
        minBurstErrorSizeSlider.valueProperty().addListener {_:Observable -> onInputsChanged()}
        maxBurstErrorSizeSlider.valueProperty().addListener {_:Observable -> onInputsChanged()}
        burstErrorFrequencySlider.valueProperty().addListener {_:Observable -> onInputsChanged()}
        codePanel.items.addListener {_:Observable -> onInputsChanged()}
    }

    override fun close()
    {
        workerExecutor.close()
        worker?.interrupt()
    }

    @FXML fun resetStats()
    {
        totalDataBitsEncoded = 0
        totalCodeBits = 0
        bitErrorCount = 0
        burstErrorCount = 0
        badBitsAcceptedCount = 0
        badBitsRejectedCount = 0
        goodBitsAcceptedCount = 0
    }

    @FXML fun onInputsChanged()
    {
        // enforce input constraints
        if (!minBurstErrorSizeSlider.isValueChanging)
        {
            minBurstErrorSizeSlider.value = Math.min(minBurstErrorSizeSlider.value,maxBurstErrorSizeSlider.value)
        }
        if (!maxBurstErrorSizeSlider.isValueChanging)
        {
            maxBurstErrorSizeSlider.value = Math.max(minBurstErrorSizeSlider.value,maxBurstErrorSizeSlider.value)
        }

        // update UI
        controlsUpdater.update()

        // restart worker
        val worker = Worker(listOf(SimpleHammingCodingStrategy())/*todo: uncomment codePanel.items.map {it.codingStrategy}*/)
        workerExecutor.submitTask()
        {
            this.worker = worker
        }
    }

    private inner class Worker(val codingStrategies:List<CodingStrategy>):Thread()
    {
        override fun run()
        {
            val dataGenerator = DataBlockGenerator(dataBlockSizeSlider.value.toInt())
            val encoderObserver = object:Transformer.Observer
            {
                val rawInput = mutableListOf<ByteArray?>()
                val rawOutput = mutableListOf<ByteArray?>()
                override fun onTransform(input:List<ByteArray?>,output:ByteArray?)
                {
                    rawInput += input
                    rawOutput += output
                }
            }
            val encoder = TransformerFlattener(codingStrategies.map {it.encoder})
                .apply {transformObservers += encoderObserver}
                .transform(dataGenerator)
            val errorInducerObserver = object:Transformer.Observer
            {
                val rawInput = mutableListOf<ByteArray?>()
                val rawOutput = mutableListOf<ByteArray?>()
                override fun onTransform(input:List<ByteArray?>,output:ByteArray?)
                {
                    rawInput += input
                    rawOutput += output
                }
            }
            val errorInducer = ErrorInducer()
                .apply {transformObservers += errorInducerObserver}
            val noisyChannel = errorInducer.transform(encoder)
            val decoder = TransformerFlattener(codingStrategies.asReversed().map {it.decoder})
                .transform(noisyChannel)

            // update error inducer and data block generator as per inputs
            dataGenerator.dataBlockSize = dataBlockSizeSlider.value.toInt()
            errorInducer.minBurstLength = minBurstErrorSizeSlider.value.toInt()
            errorInducer.maxBurstLength = maxBurstErrorSizeSlider.value.toInt()
            errorInducer.targetBurstErrorFrequency = burstErrorFrequencySlider.value/100

            // simulate & collect statistics
            while (!isInterrupted)
            {
                // todo: do statistics based on blocks not bits!
                val decoded = decoder.next()?.toList()
                val rawdata = encoderObserver.rawInput.map {it?.toList()}
                val encoded = encoderObserver.rawOutput.map {it?.toList()}
                encoderObserver.rawInput.clear()
                encoderObserver.rawOutput.clear()
                val preError = errorInducerObserver.rawInput.map {it?.toList()}
                val postError = errorInducerObserver.rawOutput.map {it?.toList()}
                errorInducerObserver.rawInput.clear()
                errorInducerObserver.rawOutput.clear()
                totalDataBitsEncoded += rawdata.flatMap {it?:emptyList()}.size
                totalCodeBits += encoded.flatMap {it?:emptyList()}.size
                bitErrorCount += (preError.flatMap {it!!}.flatMap {it.bits} zip postError.flatMap {it!!}.flatMap {it.bits}).count {it.first != it.second}
                burstErrorCount = errorInducer.burstErrorCounter
                val (badBitsAccepted,goodBitsAccepted) = (rawdata.flatMap {it!!}.flatMap {it.bits} zip (decoded?:emptyList()).flatMap {it.bits}).partition {it.first != it.second}
                badBitsAcceptedCount += badBitsAccepted.size
                badBitsRejectedCount += if (decoded == null) dataGenerator.dataBlockSize else 0
                goodBitsAcceptedCount += goodBitsAccepted.size
                statsUpdater.update()
            }
        }
    }

    open class UiComponent
    {
        private var futureUpdate:(()->Unit)? = null
        private var futureUpdateMutex = ReentrantLock()
        protected fun publishUpdate(update:()->Unit)
        {
            futureUpdateMutex.withLock()
            {
                // if the previous update has already been executed, publish
                // another one to the UI thread
                if (futureUpdate == null)
                {
                    Platform.runLater {
                        futureUpdateMutex.withLock()
                        {
                            futureUpdate?.invoke()
                            futureUpdate = null
                        }
                    }
                }

                // set the reference to the update lambda
                futureUpdate = update
            }
        }
    }
}
