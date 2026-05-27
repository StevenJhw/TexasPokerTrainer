package com.pokertrainer.app.data.model

enum class Suit(val symbol: String, val color: CardColor) {
    SPADES("♠", CardColor.BLACK),
    HEARTS("♥", CardColor.RED),
    DIAMONDS("♦", CardColor.RED),
    CLUBS("♣", CardColor.BLACK)
}

enum class CardColor { RED, BLACK }

enum class Rank(val display: String, val value: Int) {
    TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5),
    SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9),
    TEN("10", 10), JACK("J", 11), QUEEN("Q", 12), KING("K", 13), ACE("A", 14)
}

data class Card(val rank: Rank, val suit: Suit) {
    val display: String get() = "${rank.display}${suit.symbol}"
}

enum class Position(val display: String, val shortName: String) {
    UTG("Under the Gun", "UTG"),
    UTG1("UTG+1", "UTG1"),
    UTG2("UTG+2", "UTG2"),
    MP("Middle Position", "MP"),
    MP1("Middle Position+1", "MP1"),
    HJ("Hijack", "HJ"),
    CO("Cut-Off", "CO"),
    BTN("Button", "BTN"),
    SB("Small Blind", "SB"),
    BB("Big Blind", "BB")
}

enum class TableSize(val players: Int, val display: String) {
    HEADS_UP(2, "2人 (单挑)"),
    THREE_MAX(3, "3人桌"),
    FOUR_MAX(4, "4人桌"),
    FIVE_MAX(5, "5人桌"),
    SIX_MAX(6, "6人桌 (6-Max)"),
    SEVEN(7, "7人桌"),
    EIGHT(8, "8人桌"),
    NINE_MAX(9, "9人桌 (满桌)");

    fun getPositions(): List<Position> {
        return when (players) {
            2 -> listOf(Position.BTN, Position.BB)
            3 -> listOf(Position.BTN, Position.SB, Position.BB)
            4 -> listOf(Position.CO, Position.BTN, Position.SB, Position.BB)
            5 -> listOf(Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)
            6 -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)
            7 -> listOf(Position.UTG, Position.MP, Position.HJ, Position.CO, Position.BTN, Position.SB, Position.BB)
            8 -> listOf(Position.UTG, Position.UTG1, Position.MP, Position.HJ, Position.CO, Position.BTN, Position.SB, Position.BB)
            9 -> listOf(Position.UTG, Position.UTG1, Position.UTG2, Position.MP, Position.HJ, Position.CO, Position.BTN, Position.SB, Position.BB)
            else -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)
        }
    }
}

enum class ActionType(val display: String) {
    FOLD("弃牌 Fold"),
    CALL("跟注 Call"),
    RAISE("加注 Raise"),
    CHECK("过牌 Check"),
    ALL_IN("全押 All-In")
}

enum class RaiseSize(val display: String, val description: String) {
    TWO_FIVE_X("2.5x 大盲", "标准开池加注"),
    THREE_X("3x 对方加注", "标准3-bet"),
    FOUR_X("4x 大盲", "隔离/惩罚跛入者"),
    ALL_IN("全押 All-in", "最大压力")
}

enum class VillainAction(val display: String) {
    NO_ACTION("无人行动"),
    LIMPED("有人跛入(只跟大盲)"),
    ONE_RAISE("有人加注"),
    MULTIPLE_CALLERS("多人入池"),
    THREE_BET("有人3-bet(再加注)")
}

data class Scenario(
    val heroHand: Pair<Card, Card>,
    val position: Position,
    val villainAction: VillainAction,
    val potSize: Int,
    val optimalAction: ActionType,
    val optimalRaiseSize: RaiseSize? = null,
    val explanation: String,
    val rule: String
)

data class UserAnswer(
    val action: ActionType,
    val raiseSize: RaiseSize? = null
) {
    val display: String
        get() = if (raiseSize != null) "${action.display} (${raiseSize.display})" else action.display
}

data class UserProgress(
    val totalHands: Int = 0,
    val correctHands: Int = 0,
    val streak: Int = 0,
    val accuracyByPosition: MutableMap<Position, Pair<Int, Int>> = mutableMapOf()
) {
    val overallAccuracy: Float
        get() = if (totalHands > 0) correctHands.toFloat() / totalHands else 0f
}
