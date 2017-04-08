package com.github.ericytsang.comp7481.finalproject.gui

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class Main:Application()
{
    companion object
    {
        @JvmStatic fun main(args:Array<String>)
        {
            launch(Main::class.java)
        }
    }

    private lateinit var simPanel:SimulatorPanel

    override fun start(primaryStage:Stage)
    {
        simPanel = SimulatorPanel()
        primaryStage.title = "Coding Scheme Evaluator"
        primaryStage.scene = Scene(simPanel)
        primaryStage.show()
    }

    override fun stop()
    {
        simPanel.close()
        super.stop()
    }
}
