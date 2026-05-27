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
        val playersDesc = "${tableSize.players}-player table"
        return when {
            tier == 1 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand is a premium hand. Always raise for value at any table size ($playersDesc).",
                rule = "Premium hands = always raise, any position, any table size"
            )
            tier == 2 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand is a strong hand. Raise from any position at a $playersDesc.",
                rule = "Strong hands = raise from any position"
            )
            tier == 3 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand is playable from late position at a $playersDesc. Fewer players to act behind you.",
                rule = "Playable hands + late position = raise"
            )
            tier == 3 && posCat == PositionCategory.MIDDLE && tableSize.players <= 6 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand is marginal from middle position, but at a $playersDesc with fewer opponents, it's profitable to open.",
                rule = "Short-handed tables = open wider from middle positions"
            )
            tier == 3 && posCat == PositionCategory.MIDDLE -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand is too weak from middle position at a $playersDesc. Too many players behind who could have better.",
                rule = "Full ring middle position = tighter than 6-max"
            )
            tier == 3 && posCat == PositionCategory.EARLY -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand isn't strong enough from early position at a $playersDesc. ${tableSize.players - 1} players still to act.",
                rule = "Early position = only premium and strong hands"
            )
            tier == 3 && posCat == PositionCategory.BLIND -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand from the blinds without action is a fold — you'll be out of position postflop at a $playersDesc.",
                rule = "Don't open raise weak hands from the blinds"
            )
            tier == 4 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand is marginal but playable from late position at a $playersDesc. Position advantage compensates for weaker cards.",
                rule = "Button/CO = widest opening range"
            )
            tier == 4 && posCat == PositionCategory.MIDDLE && tableSize.players <= 4 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.TWO_FIVE_X,
                explanation = "$hand is playable at a short $playersDesc. With few opponents, marginal hands gain value.",
                rule = "Short-handed = play more hands from all positions"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand is too weak from this position at a $playersDesc. Wait for a better hand or better position.",
                rule = "More players = tighter range needed"
            )
        }
    }

    private fun getFacingLimpDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            tier <= 2 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.FOUR_X,
                explanation = "$hand is strong enough to raise over limpers. Raise bigger (4x) to punish their weak range and isolate.",
                rule = "Strong hand vs limper = raise bigger to isolate"
            )
            tier == 3 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.FOUR_X,
                explanation = "$hand with position over a limper is a good iso-raise spot. Limpers usually have weak hands.",
                rule = "Late position vs limper = raise to isolate"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand isn't strong enough to raise over a limper from this position. Don't limp behind — it creates bad multiway pots.",
                rule = "Don't limp. Raise or fold."
            )
        }
    }

    private fun getFacingRaiseDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            tier == 1 && hand in listOf("AA", "KK", "AKs") -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.THREE_X,
                explanation = "$hand is strong enough to 3-bet (re-raise). You want to build the pot with your best hands and get value from worse hands that call.",
                rule = "Premium hands = always 3-bet when facing a raise"
            )
            tier == 1 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.THREE_X,
                explanation = "$hand should 3-bet here. Even QQ is ahead of most raising ranges.",
                rule = "Top premium = 3-bet for value"
            )
            tier == 2 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand is strong enough to call a raise in position. You'll see a flop with a good hand and positional advantage.",
                rule = "Strong hand + position vs raiser = call (or 3-bet)"
            )
            tier == 2 -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand can call a raise but be cautious without position. You'll need to hit the flop well.",
                rule = "Strong hand out of position vs raiser = call, proceed carefully"
            )
            tier == 3 && posCat == PositionCategory.LATE -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand can call in position. Suited connectors and pairs have good implied odds — you can win big pots when you hit.",
                rule = "Suited connectors/pairs + position = call for implied odds"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand is not strong enough to continue facing a raise from this position. Save your chips for better spots.",
                rule = "Marginal hands facing a raise = fold"
            )
        }
    }

    private fun getFacingMultipleDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            tier == 1 -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.FOUR_X,
                explanation = "$hand is premium — raise big to thin the field. Multiway pots reduce your equity even with great hands.",
                rule = "Premium hands in multiway = raise big to isolate"
            )
            tier <= 3 && hand.endsWith("s") -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand has good implied odds multiway. Suited hands can make flushes/straights and win big pots from multiple opponents.",
                rule = "Suited hands multiway = call for implied odds"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand doesn't play well multiway. Offsuit hands and weak holdings lose value with more opponents in the pot.",
                rule = "Weak hands multiway = fold"
            )
        }
    }

    private fun getFacing3BetDecision(tier: Int, posCat: PositionCategory, hand: String): PreflopDecision {
        return when {
            hand in listOf("AA", "KK") -> PreflopDecision(
                action = ActionType.RAISE,
                raiseSize = RaiseSize.ALL_IN,
                explanation = "$hand is the nuts preflop. Against a 3-bet, go all-in to get maximum value. They'll call with worse hands like QQ/AK.",
                rule = "AA/KK vs 3-bet = 4-bet or all-in"
            )
            hand in listOf("QQ", "AKs") -> PreflopDecision(
                action = ActionType.CALL,
                raiseSize = null,
                explanation = "$hand is strong enough to call a 3-bet but not always strong enough to 4-bet. See a flop and re-evaluate.",
                rule = "QQ/AKs vs 3-bet = call, reassess on flop"
            )
            tier == 2 -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand is not strong enough to continue vs a 3-bet. The 3-bettor usually has a very strong range (QQ+, AK). Cut your losses.",
                rule = "Most hands vs 3-bet = fold unless you have premiums"
            )
            else -> PreflopDecision(
                action = ActionType.FOLD,
                raiseSize = null,
                explanation = "$hand should always fold to a 3-bet. You need the top of your range to continue here.",
                rule = "Fold to 3-bets unless you have a premium hand"
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
