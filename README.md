# Texas Poker Trainer 德州扑克训练

A native Android app that teaches complete beginners to play Texas Hold'em through deliberate practice — presenting real game scenarios, asking for decisions, and providing immediate GTO-based optimal solutions with explanations.

**Target user:** Someone who has never played live poker and wants to be table-ready for a $60 tournament within 2 weeks.

**Core principle:** Learn by doing, not by reading. Every interaction is a decision point with instant feedback.

---

## Screenshots

Coming soon.

---

## Features

### 📖 Learn (学习)
- 6 comprehensive lessons covering all poker fundamentals
- Visual card examples rendered as real playing cards
- Swipe navigation + button controls
- Progressive difficulty from absolute zero to table-ready

| Lesson | Content |
|--------|---------|
| 牌型大小 | 10 hand types with visual card examples |
| 游戏流程 | Preflop → Flop → Turn → River walkthrough |
| 位置 | 6-max/9-max positional strategy |
| 起手牌与胜率 | 169 hands with exact win percentages |
| 加注大小 | 2.5x / 3x / 4x / All-in sizing |
| 底池赔率与期望值 | Pot odds, outs, EV calculation |

### 🎯 Practice (练习)
- Random scenario generation (hand + position + situation)
- Two-step decision: Action (Fold/Call/Raise) → Sizing (2.5x/3x/4x/All-in)
- Instant GTO-based feedback with explanation
- Support for 2-9 player tables
- Streak tracking and accuracy stats

### 📊 Stats (统计)
- Overall accuracy percentage
- Accuracy breakdown by position (UTG, MP, CO, BTN, SB, BB)
- Visual progress bars
- Table readiness indicator
- Reset button with confirmation

---

## Product Architecture

```
┌─────────────────────────────────────────────┐
│           Texas Poker Trainer                │
├──────────────┬──────────────┬───────────────┤
│   📖 Learn   │  🎯 Practice  │  📊 Stats     │
│   Lessons    │  Drill Mode  │  Progress     │
└──────────────┴──────────────┴───────────────┘
```

---

## How Optimal Solutions Are Determined

### Core formula: Expected Value (EV)
```
EV(action) = P(win) × amount_won - P(lose) × amount_lost
```

The action with the highest EV = optimal play.

### Preflop Solutions: Lookup Table

Preflop is **solved** — all 169 starting hands × positions × common scenarios have been calculated by GTO solvers using Counterfactual Regret Minimization (CFR).

The app stores them as a structured strategy engine:

```kotlin
// Simplified example
PreflopStrategy.getOptimalAction(
    hand = "AJs",
    position = Position.CO,
    villainAction = VillainAction.ONE_RAISE,
    tableSize = TableSize.NINE_MAX
) // → PreflopDecision(action=RAISE, raiseSize=THREE_X, explanation="...")
```

### Postflop Solutions (V2)

Decision tree approach:
1. Classify hand strength (top pair+, draw, air)
2. Calculate pot odds
3. Count outs if drawing
4. Compare equity vs pot odds
5. Factor in position
6. Return optimal action + explanation

---

## Learning Pedagogy

| Principle | Implementation |
|-----------|---------------|
| Immediate feedback | Show optimal answer right after each decision |
| Focused repetition | Isolate one decision type per level |
| Progressive difficulty | Unlock harder levels only after mastery |
| Spaced repetition | Wrong answers resurface automatically |
| Contextual explanation | "Why" is always explained, not just "what" |
| Low-stakes practice | No real money, no embarrassment |

### Progression Model

```
Week 1: Preflop mastery
         → Correct preflop decisions 80%+ of the time
         → Ready for $60 tournament

Week 2: Basic postflop (V2)
         → Understands when to continue vs give up
         → Can calculate simple pot odds

Week 3-4: Full hand play (V2)
         → Competent across all streets
         → Ready for cash games
```

---

## Differentiation

| Product | Gap | This App |
|---------|-----|----------|
| PokerStars Play | Play without feedback | Every hand has optimal answer |
| GTO Wizard | Expert-only, $50/mo | Beginner-first, free |
| Poker books/videos | Passive learning | Active drill-based learning |
| Poker Trainer Pro | Preflop only, English | Full game + Chinese + explains why |

---

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Build | Gradle 8.11.1, AGP 8.7.0 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| Dependencies | Compose BOM 2024.10, Navigation Compose, Coroutines |

---

## Project Structure

```
app/src/main/java/com/pokertrainer/app/
├── MainActivity.kt
├── PokerTrainerApp.kt              # Navigation (Learn/Practice/Stats tabs)
├── data/
│   ├── model/Models.kt             # Card, Position, TableSize, ActionType, etc.
│   ├── strategy/PreflopStrategy.kt # GTO preflop decision engine
│   └── LessonData.kt              # 6 lessons, ~30 pages of content (Chinese)
└── ui/
    ├── components/CardView.kt      # Poker card UI component (normal + small)
    ├── screens/
    │   ├── LearnScreen.kt          # Tutorial with HorizontalPager
    │   ├── TrainingScreen.kt       # Core practice drill
    │   ├── TrainingViewModel.kt    # State management
    │   └── StatsScreen.kt          # Progress tracking
    └── theme/
        ├── Color.kt                # Casino dark green palette
        └── Theme.kt                # Material3 dark theme
```

---

## Build & Run

```bash
# Clone
git clone https://github.com/StevenJhw/TexasPokerTrainer.git
cd TexasPokerTrainer

# Build
./gradlew assembleDebug

# Or open in Android Studio → Run
```

---

## Roadmap

### V1 (Current) ✅
- [x] Preflop training with GTO optimal solutions
- [x] 2-9 player table support
- [x] Two-step raise sizing (action + amount)
- [x] 6 tutorial lessons in Chinese with visual card examples
- [x] Accuracy tracking by position
- [x] Dark casino-themed UI

### V2 (Planned)
- [ ] Postflop training (flop/turn/river decisions)
- [ ] Board texture classification drills
- [ ] Tournament M-ratio mode (push/fold charts)
- [ ] Player type identification quiz
- [ ] Spaced repetition for wrong answers
- [ ] Local data persistence (Room/DataStore)

### V3 (Future)
- [ ] ICM calculator for tournament bubbles
- [ ] Combinatorics & blocker drills
- [ ] Session review (enter hands from live play)
- [ ] AI-powered opponent modeling
- [ ] Cloud sync & leaderboard

---

## License

MIT
