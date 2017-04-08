package com.github.ericytsang.comp7481.finalproject.gui

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.Label
import java.net.URL
import java.util.ResourceBundle

class SubstituteLabel:Label(),Initializable
{
    init
    {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("label_substitute.fxml")!!)
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }

    private lateinit var preSubText:String

    private lateinit var postSubText:String

    override fun initialize(location:URL,resources:ResourceBundle?)
    {
        Platform.runLater {
            val subIndex = text.indexOf('$')
            check(subIndex != -1) {"no $ found in \"$text\""}
            preSubText = text.substring(0,subIndex)
            postSubText = text.substring(subIndex+1)
        }
    }

    fun substitute(substitute:String)
    {
        text = StringBuilder(preSubText.length+substitute.length+postSubText.length)
            .append(preSubText)
            .append(substitute)
            .append(postSubText)
            .toString()
    }
}
