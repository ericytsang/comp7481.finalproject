package com.github.ericytsang.comp7481.finalproject.gui

import com.github.ericytsang.comp7481.finalproject.model.CodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.SimpleBitErrorDiffusionStrategy
import com.github.ericytsang.comp7481.finalproject.model.SimpleChecksumCodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.SimpleHammingCodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.SimpleRepeatedMessageCodingStrategy
import javafx.application.Platform
import javafx.beans.Observable
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.Window
import java.net.URL
import java.util.ResourceBundle

class EditCodePanel private constructor(val stage:Stage,val initialResult:CodingStrategy? = null):VBox(),Initializable
{
    @FXML lateinit var codingStrategyComboBox:ComboBox<String>
    @FXML lateinit var configArea:VBox
    @FXML lateinit var errorDiffusionConfig:VBox
    @FXML lateinit var dataBlocksPerCodeBlockLabel:SubstituteLabel
    @FXML lateinit var dataBlocksPerCodeBlockSlider:Slider
    @FXML lateinit var repeatedMessageConfig:VBox
    @FXML lateinit var repeatedMessageCountLabel:SubstituteLabel
    @FXML lateinit var repeatedMessageCountSlider:Slider

    private var result:CodingStrategy? = initialResult
        private set

    private enum class CodingStraregyOption(val friendly:String)
    {HAMMING("Hamming"),ERR_DIFFUSE("Error diffusion"),CHECKSUM("Checksum"),REPEAT_MSG("Repeated message"),NONE("None") }

    override fun initialize(location:URL,resources:ResourceBundle?)
    {
        // initialize input control values as per result
        val result = result
        when (result)
        {
            null -> Unit
            is SimpleHammingCodingStrategy ->
            {
                codingStrategyComboBox.selectionModel.select(CodingStraregyOption.HAMMING.ordinal)
            }
            is SimpleBitErrorDiffusionStrategy ->
            {
                codingStrategyComboBox.selectionModel.select(CodingStraregyOption.ERR_DIFFUSE.ordinal)
                dataBlocksPerCodeBlockSlider.value = result.dataBlocksPerCodeBlock.toDouble()
            }
            is SimpleChecksumCodingStrategy ->
            {
                codingStrategyComboBox.selectionModel.select(CodingStraregyOption.CHECKSUM.ordinal)
            }
            is SimpleRepeatedMessageCodingStrategy ->
            {
                codingStrategyComboBox.selectionModel.select(CodingStraregyOption.REPEAT_MSG.ordinal)
                repeatedMessageCountSlider.value = result.times.toDouble()
            }
            else -> throw RuntimeException("unhandled coding strategy")
        }

        // add listeners to input controls
        codingStrategyComboBox.valueProperty().addListener {_:Observable -> onCodingStrategyChanged()}
        dataBlocksPerCodeBlockSlider.valueProperty().addListener {_:Observable -> onConfigChanged()}
        repeatedMessageCountSlider.valueProperty().addListener {_:Observable -> onConfigChanged()}

        Platform.runLater {onCodingStrategyChanged()}
    }

    fun onCodingStrategyChanged()
    {
        val codingStrategy = try
        {
            CodingStraregyOption.values()[codingStrategyComboBox.selectionModel.selectedIndex]
        }

        // none selected
        catch (ex:ArrayIndexOutOfBoundsException)
        {
            CodingStraregyOption.NONE
        }

        // add / remove config panels
        configArea.children.clear()
        when (codingStrategy)
        {
            CodingStraregyOption.HAMMING -> Unit
            CodingStraregyOption.ERR_DIFFUSE -> configArea.children.add(errorDiffusionConfig)
            CodingStraregyOption.CHECKSUM -> Unit
            CodingStraregyOption.REPEAT_MSG -> configArea.children.add(repeatedMessageConfig)
            CodingStraregyOption.NONE -> Unit
        }
        stage.sizeToScene()

        onConfigChanged()
    }

    fun onConfigChanged()
    {
        dataBlocksPerCodeBlockLabel.substitute(dataBlocksPerCodeBlockSlider.value.toInt().toString())
        repeatedMessageCountLabel.substitute(repeatedMessageCountSlider.value.toInt().toString())
        val codingStrategy = try
        {
            CodingStraregyOption.values()[codingStrategyComboBox.selectionModel.selectedIndex]
        }

        // none selected
        catch (ex:ArrayIndexOutOfBoundsException)
        {
            CodingStraregyOption.NONE
        }
        when (codingStrategy)
        {
            CodingStraregyOption.HAMMING ->
            {
                result = SimpleHammingCodingStrategy()
            }
            CodingStraregyOption.ERR_DIFFUSE ->
            {
                result = SimpleBitErrorDiffusionStrategy(dataBlocksPerCodeBlockSlider.value.toInt())
            }
            CodingStraregyOption.CHECKSUM ->
            {
                result = SimpleChecksumCodingStrategy()
            }
            CodingStraregyOption.REPEAT_MSG ->
            {
                result = SimpleRepeatedMessageCodingStrategy(repeatedMessageCountSlider.value.toInt())
            }
            CodingStraregyOption.NONE ->
            {
                result = null
            }
        }
    }

    @FXML fun onConfirm()
    {
        stage.close()
    }

    @FXML fun onCancel()
    {
        result = initialResult
        stage.close()
    }

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("window_edit_code.fxml")!!)
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }

    companion object
    {
        fun start(owner:Window,existingCodingStrategy:CodingStrategy?):CodingStrategy?
        {
            val stage = Stage()
            stage.initOwner(owner)
            val editCodePanel = EditCodePanel(stage,existingCodingStrategy)
            stage.scene = Scene(editCodePanel)
            stage.title = "Configure coding strategy"
            stage.showAndWait()
            return editCodePanel.result
        }
    }
}
