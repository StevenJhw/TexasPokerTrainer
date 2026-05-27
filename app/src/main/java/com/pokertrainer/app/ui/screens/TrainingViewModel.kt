package com.pokertrainer.app.ui.screens

import androidx.lifecycle.ViewModel
import com.pokertrainer.app.data.model.*
import com.pokertrainer.app.data.strategy.PreflopStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrainingState(
    val currentScenario: Scenario? = null,
    val userAnswer: UserAnswer? = null,
    val isCorrect: Boolean? = null,
    val optimalDecision: String = "",
    val explanation: String = "",
    val rule: String = "",
    val showResult: Boolean = false,
    val showRaiseSizing: Boolean = false,
    val progress: UserProgress = UserProgress(),
    val currentStreak: Int = 0,
    val tableSize: TableSize = TableSize.NINE_MAX
)

class TrainingViewModel : ViewModel() {

    private val _state = MutableStateFlow(TrainingState())
    val state: StateFlow<TrainingState> = _state.asStateFlow()

    private val deck = mutableListOf<Card>()

    init {
        dealNewHand()
    }

    fun setTableSize(size: TableSize) {
        _state.value = _state.value.copy(tableSize = size)
        dealNewHand()
    }

    fun dealNewHand() {
        buildDeck()
        deck.shuffle()

        val tableSize = _state.value.tableSize
        val card1 = deck.removeFirst()
        val card2 = deck.removeFirst()
        val availablePositions = tableSize.getPositions()
        val position = availablePositions.random()
        val villainAction = VillainAction.entries.random()
        val potSize = when (villainAction) {
            VillainAction.NO_ACTION -> 4
            VillainAction.LIMPED -> 7
            VillainAction.ONE_RAISE -> 14
            VillainAction.MULTIPLE_CALLERS -> 18
            VillainAction.THREE_BET -> 30
        }

        val handStr = PreflopStrategy.handToString(card1, card2)
        val decision = PreflopStrategy.getOptimalAction(handStr, position, villainAction, tableSize)

        val scenario = Scenario(
            heroHand = Pair(card1, card2),
            position = position,
            villainAction = villainAction,
            potSize = potSize,
            optimalAction = decision.action,
            optimalRaiseSize = decision.raiseSize,
            explanation = decision.explanation,
            rule = decision.rule
        )

        _state.value = _state.value.copy(
            currentScenario = scenario,
            userAnswer = null,
            isCorrect = null,
            showResult = false,
            showRaiseSizing = false,
            optimalDecision = if (decision.raiseSize != null) {
                "${decision.action.display} (${decision.raiseSize.display})"
            } else {
                decision.action.display
            },
            explanation = decision.explanation,
            rule = decision.rule
        )
    }

    fun submitAction(action: ActionType) {
        if (action == ActionType.RAISE) {
            _state.value = _state.value.copy(showRaiseSizing = true)
        } else {
            evaluateAnswer(UserAnswer(action, null))
        }
    }

    fun submitRaiseSize(size: RaiseSize) {
        evaluateAnswer(UserAnswer(ActionType.RAISE, size))
    }

    fun cancelRaise() {
        _state.value = _state.value.copy(showRaiseSizing = false)
    }

    private fun evaluateAnswer(answer: UserAnswer) {
        val scenario = _state.value.currentScenario ?: return

        val actionCorrect = answer.action == scenario.optimalAction
        val sizeCorrect = if (scenario.optimalRaiseSize != null && answer.action == ActionType.RAISE) {
            answer.raiseSize == scenario.optimalRaiseSize
        } else {
            true
        }
        val isCorrect = actionCorrect && sizeCorrect

        val progress = _state.value.progress
        val newStreak = if (isCorrect) _state.value.currentStreak + 1 else 0

        val positionStats = progress.accuracyByPosition.toMutableMap()
        val current = positionStats[scenario.position] ?: Pair(0, 0)
        positionStats[scenario.position] = Pair(
            current.first + 1,
            current.second + if (isCorrect) 1 else 0
        )

        _state.value = _state.value.copy(
            userAnswer = answer,
            isCorrect = isCorrect,
            showResult = true,
            showRaiseSizing = false,
            currentStreak = newStreak,
            progress = UserProgress(
                totalHands = progress.totalHands + 1,
                correctHands = progress.correctHands + if (isCorrect) 1 else 0,
                streak = maxOf(progress.streak, newStreak),
                accuracyByPosition = positionStats
            )
        )
    }

    fun resetProgress() {
        _state.value = _state.value.copy(
            progress = UserProgress(),
            currentStreak = 0
        )
    }

    private fun buildDeck() {
        deck.clear()
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                deck.add(Card(rank, suit))
            }
        }
    }
}
