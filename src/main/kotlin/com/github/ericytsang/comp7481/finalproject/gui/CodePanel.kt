package com.github.ericytsang.comp7481.finalproject.gui

import com.github.ericytsang.comp7481.finalproject.model.CodingStrategy
import com.github.ericytsang.lib.javafxutils.EditableTableView
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.util.Callback
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.FXMLLoader
import javafx.scene.control.TableView

class CodePanel:EditableTableView<CodePanel.RowItem>(),Initializable
{
    init
    {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("panel_codes.fxml")!!)
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }

    override fun initialize(location:URL,resources:ResourceBundle?)
    {
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        columns += TableColumn<RowItem,String>().apply()
        {
            text = "Coding Strategy"
            cellValueFactory = Callback {SimpleStringProperty(it.value.codingStrategy.toString())}
        }
    }

    data class RowItem(val codingStrategy:CodingStrategy)

    override fun createOrUpdateItem(previousInput:CodePanel.RowItem?):CodePanel.RowItem?
    {
        throw UnsupportedOperationException("not implemented") // todo
    }
}
