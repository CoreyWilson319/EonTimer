package io.github.dylmeadows.eontimer.controller.timer

import io.github.dylmeadows.eontimer.model.ApplicationModel
import io.github.dylmeadows.eontimer.model.TimerState
import io.github.dylmeadows.eontimer.model.timer.TimerType
import io.github.dylmeadows.eontimer.service.TimerRunnerService
import io.github.dylmeadows.eontimer.service.factory.TimerFactoryService
import io.github.dylmeadows.eontimer.util.asFlux
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TimerControlPane @Autowired constructor(
    private val model: ApplicationModel,
    private val timerState: TimerState,
    private val timerRunner: TimerRunnerService,
    private val timerFactory: TimerFactoryService,
    private val gen3: Gen3TimerPane,
    private val gen4: Gen4TimerPane,
    private val gen5: Gen5TimerPane,
    private val custom: CustomTimerPane) {

    @FXML
    private lateinit var gen3Tab: Tab
    @FXML
    private lateinit var gen4Tab: Tab
    @FXML
    private lateinit var gen5Tab: Tab
    @FXML
    private lateinit var customTab: Tab
    @FXML
    private lateinit var timerTabPane: TabPane
    @FXML
    private lateinit var updateBtn: Button
    @FXML
    private lateinit var timerBtn: Button

    fun initialize() {
        timerTabPane.selectionModel.select(model.selectedTimerType.tab)
        timerTabPane.selectionModel.selectedItemProperty().asFlux()
            .map { it.timerType }
            .subscribe {
                model.selectedTimerType = it
            }

        gen3Tab.disableProperty().bind(
            timerTabPane.selectionModel.selectedItemProperty().isNotEqualTo(gen3Tab)
                .and(timerState.runningProperty))
        gen4Tab.disableProperty().bind(
            timerTabPane.selectionModel.selectedItemProperty().isNotEqualTo(gen4Tab)
                .and(timerState.runningProperty))
        gen5Tab.disableProperty().bind(
            timerTabPane.selectionModel.selectedItemProperty().isNotEqualTo(gen5Tab)
                .and(timerState.runningProperty))
        customTab.disableProperty().bind(
            timerTabPane.selectionModel.selectedItemProperty().isNotEqualTo(customTab)
                .and(timerState.runningProperty))

        timerState.runningProperty.asFlux()
            .map { if (!it) "Start" else "Stop" }
            .subscribe { timerBtn.text = it }
        timerBtn.setOnAction {
            if (!timerState.running) {
                timerRunner.start(timerFactory.stages)
            } else {
                timerRunner.stop()
            }
        }

        updateBtn.disableProperty().bind(
            timerState.runningProperty)
        updateBtn.setOnAction {
            timerFactory.calibrate()
        }
    }

    private val Tab.timerType: TimerType
        get() {
            return when (this) {
                gen3Tab -> TimerType.GEN3
                gen4Tab -> TimerType.GEN4
                gen5Tab -> TimerType.GEN5
                customTab -> TimerType.CUSTOM
                else -> throw IllegalStateException("unable to find TimerType for selected tab")
            }
        }

    private val TimerType.tab: Tab
        get() {
            return when (this) {
                TimerType.GEN3 -> gen3Tab
                TimerType.GEN4 -> gen4Tab
                TimerType.GEN5 -> gen5Tab
                TimerType.CUSTOM -> customTab
            }
        }
}