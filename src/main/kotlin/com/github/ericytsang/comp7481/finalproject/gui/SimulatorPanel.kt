package com.github.ericytsang.comp7481.finalproject.gui

import com.github.ericytsang.comp7481.finalproject.model.CodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.DataBlockGenerator
import com.github.ericytsang.comp7481.finalproject.model.ErrorInducer
import com.github.ericytsang.comp7481.finalproject.model.SimpleHammingCodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.Transformer
import com.github.ericytsang.comp7481.finalproject.model.TransformerFlattener
import com.github.ericytsang.comp7481.finalproject.model.bits
import com.github.ericytsang.comp7481.finalproject.model.monitored
import com.github.ericytsang.comp7481.finalproject.model.next
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
    @FXML lateinit var dataBlocksEncodedLabel:SubstituteLabel
    @FXML lateinit var codeBlocksCorruptedLabel:SubstituteLabel
    @FXML lateinit var dataBlocksCorruptedLabel:SubstituteLabel
    @FXML lateinit var dataBlocksAcceptedLabel:SubstituteLabel
    @FXML lateinit var dataBlocksRecoveredLabel:SubstituteLabel
    @FXML lateinit var dataBlocksDiscardedLabel:SubstituteLabel
    @FXML lateinit var corruptDataBlocksAcceptedLabel:SubstituteLabel
    @FXML lateinit var pieChart:PieChart

    private var efficiency:Byte = 0
    private var dataBlocksEncoded:Long = 0
    private var codeBlocksCorrupted:Long = 0
    private var dataBlocksCorrupted:Long = 0
    private var dataBlocksAccepted:Long = 0
    private var dataBlocksRecovered:Long = 0
    private var dataBlocksDiscarded:Long = 0
    private var corruptDataBlocksAccepted:Long = 0
    private val statsUpdater = object:UiComponent()
    {
        private val dataBlocksAcceptedPieData = PieChart.Data("Data blocks accepted",0.0)
        private val dataBlocksRecoveredPieData = PieChart.Data("Data blocks recovered",0.0)
        private val dataBlocksDiscardedPieData = PieChart.Data("Data blocks discarded",0.0)
        private val corruptDataBlocksAcceptedPieData = PieChart.Data("Corrupt data blocks accepted",0.0)
        private val pieChartData = ObservableListWrapper(listOf(
            dataBlocksAcceptedPieData,
            dataBlocksRecoveredPieData,
            dataBlocksDiscardedPieData,
            corruptDataBlocksAcceptedPieData))

        fun update() = publishUpdate()
        {
            efficiencyLabel.substitute(efficiency.toString())
            dataBlocksEncodedLabel.substitute(dataBlocksEncoded.toString())
            codeBlocksCorruptedLabel.substitute(codeBlocksCorrupted.toString())
            dataBlocksCorruptedLabel.substitute(dataBlocksCorrupted.toString())
            dataBlocksAcceptedLabel.substitute(dataBlocksAccepted.toString())
            dataBlocksRecoveredLabel.substitute(dataBlocksRecovered.toString())
            dataBlocksDiscardedLabel.substitute(dataBlocksDiscarded.toString())
            corruptDataBlocksAcceptedLabel.substitute(corruptDataBlocksAccepted.toString())
            dataBlocksAcceptedPieData.pieValue = dataBlocksAccepted.toDouble()
            dataBlocksRecoveredPieData.pieValue = dataBlocksRecovered.toDouble()
            dataBlocksDiscardedPieData.pieValue = dataBlocksDiscarded.toDouble()
            corruptDataBlocksAcceptedPieData.pieValue = corruptDataBlocksAccepted.toDouble()
            if (pieChart.data !== pieChartData)
            {
                pieChart.data = pieChartData
            }
        }
    }

    private val controlsUpdater = object:UiComponent()
    {
        private val decimalFormat = DecimalFormat("0.00")
        fun update() = publishUpdate()
        {
            dataBlockSizeLabel.substitute(dataBlockSizeSlider.value.toInt().toString())
            burstErrorFrequencyLabel.substitute(burstErrorFrequencySlider.value.let {decimalFormat.format(it)})
            minBurstErrorSizeLabel.substitute(minBurstErrorSizeSlider.value.toInt().toString())
            maxBurstErrorSizeLabel.substitute(maxBurstErrorSizeSlider.value.toInt().toString())
            resetStats()
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
        efficiency = 0
        dataBlocksEncoded = 0
        codeBlocksCorrupted = 0
        dataBlocksCorrupted = 0
        dataBlocksAccepted = 0
        dataBlocksRecovered = 0
        dataBlocksDiscarded = 0
        corruptDataBlocksAccepted = 0
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
        val worker = Worker(
            minBurstErrorSizeSlider.value.toInt(),
            maxBurstErrorSizeSlider.value.toInt(),
            burstErrorFrequencySlider.value/100,
            dataBlockSizeSlider.value.toInt(),
            workerListener,
            emptyList()/*listOf(SimpleHammingCodingStrategy())*//*todo: uncomment codePanel.items.map {it.codingStrategy}*/)
        workerExecutor.submitTask()
        {
            this.worker = worker
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

    private val workerListener = object:Worker.Listener
    {
        override fun onCycle(dataBlocks:List<List<Byte>>,codeBlock:List<Byte>,noisyCodeBlock:List<Byte>,decodedBlocks:List<List<Byte>?>)
        {
            val isCorrupted = codeBlock != noisyCodeBlock
            efficiency = (dataBlocks.sumBy {it.size}.toDouble()/codeBlock.size).times(100).toByte()
            dataBlocksEncoded += dataBlocks.size
            val zipped = dataBlocks.zip(decodedBlocks)
            if (isCorrupted)
            {
                codeBlocksCorrupted += 1
                dataBlocksCorrupted += dataBlocks.size
                dataBlocksRecovered += zipped.count {it.first == it.second}
            }
            else
            {
                dataBlocksAccepted += zipped.count {it.first == it.second}
            }
            dataBlocksDiscarded += decodedBlocks.count {it == null}
            corruptDataBlocksAccepted += zipped.count {it.second != null && it.first != it.second}
//            zipped.forEach {
//                if (it.second != null && it.first != it.second) println("corrupt&accepted dataBlocks: $dataBlocks, codeBlock: $codeBlock, noisyCodeBlock: $noisyCodeBlock ,decodedBlocks: $decodedBlocks")
//                if (isCorrupted && it.first == it.second) println("corrupt&recovered dataBlocks: $dataBlocks, codeBlock: $codeBlock, noisyCodeBlock: $noisyCodeBlock ,decodedBlocks: $decodedBlocks")
//            }
            statsUpdater.update()
        }
    }

    private class Worker(
        val minBurstErrorSize:Int,
        val maxBurstErrorSize:Int,
        val burstErrorFrequency:Double,
        val dataBlockSize:Int,
        val listener:Listener,
        val codingStrategies:List<CodingStrategy>):Thread()
    {
        private val dataGenerator = DataBlockGenerator(dataBlockSize).monitored()
        private val encoder = TransformerFlattener(codingStrategies.map {it.encoder})
            .transform(dataGenerator).monitored()
        private val errorInducer = ErrorInducer()
            .apply {
                minBurstLength = minBurstErrorSize
                maxBurstLength = maxBurstErrorSize
                targetBurstErrorFrequency = burstErrorFrequency
            }
        private val noisyChannel = errorInducer.transform(encoder).monitored()
        private val decoder = TransformerFlattener(codingStrategies.asReversed().map {it.decoder})
            .transform(noisyChannel)

        override fun run()
        {
            // simulate & collect statistics
            while (!isInterrupted)
            {
                val decoded = decoder.next()?.toList()
                val rawdata = dataGenerator.elements.map {it.toList()}
                val encoded = encoder.elements.flatMap {it!!.toList()}
                val allDecodedBlocks = listOf(decoded)+decoder.next(rawdata.size-1).map {it?.toList()}
                val postError = noisyChannel.elements.flatMap {it!!.toList()}
                listener.onCycle(rawdata,encoded,postError,allDecodedBlocks)
            }
        }

        interface Listener
        {
            fun onCycle(dataBlocks:List<List<Byte>>,codeBlock:List<Byte>,noisyCodeBlock:List<Byte>,decodedBlocks:List<List<Byte>?>)
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

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("window_simulator.fxml")!!)
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }
}
