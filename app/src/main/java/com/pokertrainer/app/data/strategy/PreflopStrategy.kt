package com.pokertrainer.app.data.strategy

import com.pokertrainer.app.data.model.*

object PreflopStrategy {

    private val PREMIUM = listOf("AA", "KK", "QQ", "AKs", "AKo")
    private val STRONG = listOf("JJ", "TT", "AQs", "AQo", "AJs", "KQs")
    private val PLAYABLE = listOf("99", "88", "77", "ATs", "AJo", "KJs", "KQo", "QJs", "JTs")
    private val MARGINAL = listOf("66", "55", "44", "33", "22", "A9s", "A8s", "KTs", "QTs", "T9s", "98s", "87s", "76s")

    fun getOptimalAction(
        hand: String,
        position: Position,
        villainAction: VillainAction,
        tableSize: TableSize = TableSize.SIX_MAX
    ): PreflopDecision {
        val tier = getHandTier(hand)
        val positionCategory = getPositionCategory(position, tableSize)
        return when (villainAction) {
            VillainAction.NO_ACTION -> getRFIDecision(tier, positionCategory, hand, tableSize)
            VillainAction.LIMPED -> getFacingLimpDecision(tier, positionCategory, hand)
            VillainAction.ONE_RAISE -> getFacingRaiseDecision(tier, positionCategory, hand)
            VillainAction.MULTIPLE_CALLERS -> getFacingMultipleDecision(tier, positionCategory, hand)
            VillainAction.THREE_BET -> getFacing3BetDecision(tier, positionCategory, hand)
        }
    }

    private enum class PositionCategory { EARLY, MIDDLE, LATE, BLIND }

    private fun getPositionCategory(position: Position, tableSize: TableSize): PositionCategory {
        return when (position) {
            Position.SB, Position.BB -> PositionCategory.BLIND
            Position.BTN -> PositionCategory.LATE
            Position.CO -> if (tableSize.players <= 3) PositionCategory.LATE else PositionCategory.LATE
            Position.HJ -> PositionCategory.MIDDLE
            Position.MP, Position.MP1 -> PositionCategory.MIDDLE
            Position.UTG, Position.UTG1, Position.UTG2 -> PositionCategory.EARLY
        }
    }

    private fun getHandTier(hand: String): Int {
        return when {
            hand in PREMIUM -> 1
            hand in STRONG -> 2
            hand in PLAYABLE -> 3
            hand in MARGINAL -> 4
            else -> 5
        }
    }

    private fun getRFIDecision(tier: Int, posCat: PositionCategory, hand: String, tableSize: TableSize): PreflopDecision {
        val playersDesc = "${tableSize.players}人桌"
        return when {
            tier == 1 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand 是顶级强牌。在${playersDesc}的任何位置都应该加注，榨取价值。",
                rule = "顶级强牌 = 任何位置、任何桌型都加注"
            )
            tier == 2 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand 是强牌。在${playersDesc}的任何位置都可以开池加注。",
                rule = "强牌 = 任何位置都加注"
            )
            tier == 3 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand 从晚位（CO/BTN）在${playersDesc}可以加注。后面待行动的人少，被压制风险低。",
                rule = "可打牌 + 晚位 = 加注"
            )
            tier == 3 && posCat == PositionCategory.MIDDLE && tableSize.players <= 6 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand 在中间位本来偏弱，但${playersDesc}人少对手少，从中间位开池有利可图。",
                rule = "人少的桌 = 中间位可以打更宽"
            )
            tier == 3 && posCat == PositionCategory.MIDDLE -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 在${playersDesc}的中间位太弱了。后面还有太多人可能拿着更好的牌。",
                rule = "满桌中间位 = 比6人桌要紧很多"
            )
            tier == 3 && posCat == PositionCategory.EARLY -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 从早位不够强。${playersDesc}中后面还有${tableSize.players - 1}个人待行动，被压制概率太高。",
                rule = "早位 = 只打顶级和强牌"
            )
            tier == 3 && posCat == PositionCategory.BLIND -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 从盲位主动开池是亏损的。翻牌后你在${playersDesc}会第一个行动，没有位置优势。",
                rule = "盲位不要用弱牌主动加注开池"
            )
            tier == 4 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand 是边缘牌，但从晚位在${playersDesc}可以打。位置优势弥补了牌力不足。",
                rule = "BTN/CO = 最宽的开池范围"
            )
            tier == 4 && posCat == PositionCategory.MIDDLE && tableSize.players <= 4 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand 在${playersDesc}这种人少的桌可以打。对手少时边缘牌也能盈利。",
                rule = "人少的桌 = 所有位置都可以打更宽"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 从这个位置在${playersDesc}太弱了。等更好的牌或者更好的位置再出手。",
                rule = "对手越多 = 需要越强的牌才能开池"
            )
        }
    }

    private fun getFacingLimpDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            tier <= 2 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.FOUR_X,
                explanation = "$hand 足够强，可以加注隔离跛入者。加大到4倍大盲来惩罚他们的弱范围，争取单挑。",
                rule = "强牌面对跛入 = 加大隔离"
            )
            tier == 3 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.FOUR_X,
                explanation = "$hand 有位置优势面对跛入者，是很好的隔离加注机会。跛入者通常拿着弱牌。",
                rule = "晚位面对跛入 = 加注隔离"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 从这个位置不够强，无法加注隔离跛入者。不要跟着跛入——那会制造糟糕的多人底池。",
                rule = "不要跛入。要么加注，要么弃牌。"
            )
        }
    }

    private fun getFacingRaiseDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            tier == 1 && hand in listOf("AA", "KK", "AKs") -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.THREE_X,
                explanation = "$hand 足够强，应该3-bet（再加注）。用你最好的牌做大底池，从跟注的较差牌中榨取价值。",
                rule = "顶级强牌面对加注 = 永远3-bet"
            )
            tier == 1 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.THREE_X,
                explanation = "$hand 应该在这里3-bet。QQ领先于大多数加注范围。",
                rule = "顶级牌 = 3-bet获取价值"
            )
            tier == 2 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand 有位置时足够强可以跟注加注。你会带着好牌+位置优势看翻牌。",
                rule = "强牌 + 有位置 面对加注 = 跟注"
            )
            tier == 2 -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand 可以跟注加注，但没有位置要谨慎。翻牌需要配合得好才能继续。",
                rule = "强牌没位置面对加注 = 跟注，翻牌后谨慎"
            )
            tier == 3 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand 有位置可以跟注。同花连牌和对子有很好的隐含赔率——中了能赢大底池。",
                rule = "同花连牌/对子 + 有位置 = 跟注博隐含赔率"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 从这个位置面对加注不够强。省下筹码等更好的机会。",
                rule = "边缘牌面对加注 = 弃牌"
            )
        }
    }

    private fun getFacingMultipleDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            tier == 1 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.FOUR_X,
                explanation = "$hand 是顶级强牌——大加注清场。多人底池会大幅降低你的胜率，即使拿着好牌也需要减少对手。",
                rule = "顶级牌在多人底池 = 大加注隔离"
            )
            tier <= 3 && hand.endsWith("s") -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand 在多人底池有很好的隐含赔率。同花牌能做成同花/顺子，从多个对手那里赢大底池。",
                rule = "同花牌在多人底池 = 跟注博隐含赔率"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 在多人底池打不好。不同花牌和弱牌在多个对手面前会失去价值。",
                rule = "弱牌在多人底池 = 弃牌"
            )
        }
    }

    private fun getFacing3BetDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            hand in listOf("AA", "KK") -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.ALL_IN,
                explanation = "$hand 是翻牌前的坚果牌。面对3-bet直接全推榨取最大价值。对手会用QQ、AK等较差的牌跟注。",
                rule = "AA/KK 面对3-bet = 4-bet全推"
            )
            hand in listOf("QQ", "AKs") -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand 足够强可以跟注3-bet，但不一定够强到4-bet。先看翻牌再重新评估局势。",
                rule = "QQ/AKs 面对3-bet = 跟注，看翻牌再决定"
            )
            tier == 2 -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 面对3-bet不够强。3-bet者通常拿着很强的范围（QQ+、AK）。及时止损。",
                rule = "大部分牌面对3-bet = 弃牌，除非你有顶级牌"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand 面对3-bet应该永远弃牌。在这里你需要范围最顶端的牌才能继续。",
                rule = "面对3-bet没有顶级牌 = 弃牌"
            )
        }
    }

    fun handToString(card1: Card, card2: Card): String {
        val r1 = card1.rank
        val r2 = card2.rank
        val suited = card1.suit == card2.suit
        val high = if (r1.value >= r2.value) r1 else r2
        val low = if (r1.value >= r2.value) r2 else r1
        val highStr = when (high) {
            Rank.TEN -> "T"
            else -> high.display
        }
        val lowStr = when (low) {
            Rank.TEN -> "T"
            else -> low.display
        }
        return if (high == low) {
            "${highStr}${lowStr}"
        } else {
            "${highStr}${lowStr}${if (suited) "s" else "o"}"
        }
    }
}

data class PreflopDecision(
    val action: ActionType,
    val raiseSize: RaiseSize?,
    val explanation: String,
    val rule: String
)
