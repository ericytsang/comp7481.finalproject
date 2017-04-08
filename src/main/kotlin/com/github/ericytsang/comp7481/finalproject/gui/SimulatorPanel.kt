package com.github.ericytsang.comp7481.finalproject.gui

import com.github.ericytsang.comp7481.finalproject.model.CodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.DataBlockGenerator
import com.github.ericytsang.comp7481.finalproject.model.Transformer
import com.github.ericytsang.comp7481.finalproject.model.TransformerFlattener
import javafx.application.Platform
import javafx.beans.Observable
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import java.io.Closeable
import java.net.URL
import java.util.ResourceBundle
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
    @FXML lateinit var totalDataRxLabel:SubstituteLabel
    @FXML lateinit var totalCodeLabel:SubstituteLabel
    @FXML lateinit var bitErrorCountLabel:SubstituteLabel
    @FXML lateinit var burstErrorCountLabel:SubstituteLabel
    @FXML lateinit var errorsDiscardedLabel:SubstituteLabel
    @FXML lateinit var errorsCorrectedLabel:SubstituteLabel
    @FXML lateinit var errorsAcceptedLabel:SubstituteLabel
    @FXML lateinit var normalAcceptedLabel:SubstituteLabel

    private var totalDataBitsEncoded:Long = 0
    private var totalDataBitsDecoded:Long = 0
    private var totalCodeBits:Long = 0
    private var bitErrorCount:Long = 0
    private val miscUpdater = object:UiComponent()
    {
        fun update()
        {
            publishUpdate {
                efficiencyLabel.substitute((totalDataBitsEncoded/totalCodeBits*100).toString())
                totalDataTxLabel.substitute(totalDataBitsEncoded.toString())
                totalDataRxLabel.substitute(totalDataBitsDecoded.toString())
                totalCodeLabel.substitute(totalCodeBits.toString())
                dataBlockSizeLabel.substitute(dataBlockSizeSlider.value.toString())
            }
        }
    }

    private var worker:Thread = Thread()
        set(value)
        {
            field.interrupt()
            field.join()
            field = value
            field.start()
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
        resetStats()
    }

    override fun close()
    {
        worker.interrupt()
    }

    @FXML fun resetStats()
    {
        worker = Worker(dataBlockSizeSlider.value.toInt(),codePanel.items.map {it.codingStrategy})
    }

    private inner class Worker(dataBlockSize:Int,codingStrategies:List<CodingStrategy>):Thread()
    {
        private val dataGenerator = DataBlockGenerator(dataBlockSize)
        private val encoderObserver = object:Transformer.Observer
        {
            val rawInput = mutableListOf<ByteArray?>()
            var rawOutput:ByteArray? = null
            override fun onTransform(input:List<ByteArray?>,output:ByteArray?)
            {
                rawInput += input
                rawOutput = output
            }
        }
        private val encoder = TransformerFlattener(codingStrategies.map {it.encoder})
            .apply {transformObservers += encoderObserver}
            .transform(dataGenerator)
        private val decoder = TransformerFlattener(codingStrategies.asReversed().map {it.decoder})
            .transform(encoder)

        override fun run()
        {
            // reset all statistics
            totalDataBitsEncoded = 0
            totalDataBitsDecoded = 0
            totalCodeBits = 0

            // simulate & collect statistics
            while (!isInterrupted)
            {
                val decoded = decoder.next()?.toList()
                val encoded = encoderObserver.rawOutput?.toList()
                val rawdata = encoderObserver.rawInput.map {it?.toList()}
                encoderObserver.rawInput.clear()
                totalDataBitsEncoded += rawdata.flatMap {it?:emptyList()}.size
                totalDataBitsDecoded += decoded?.size?:0
                totalCodeBits += encoded?.size?:0
                miscUpdater.update()
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
