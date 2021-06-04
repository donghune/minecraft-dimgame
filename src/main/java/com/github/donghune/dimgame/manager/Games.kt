package com.github.donghune.dimgame.manager

import com.github.donghune.dimgame.minigame.DimGame
import com.github.donghune.dimgame.minigame.battle_of_push.BattleOfPush
import com.github.donghune.dimgame.minigame.bomb_spinning.BombSpinning
import com.github.donghune.dimgame.minigame.fast_combination.FastCombination
import com.github.donghune.dimgame.minigame.jump_map.JumpMap
import com.github.donghune.dimgame.minigame.ox_quiz.OXQuiz
import com.github.donghune.dimgame.minigame.score_of_push.ScoreOfPush
import com.github.donghune.dimgame.minigame.spleef.Spleef
import com.github.donghune.dimgame.minigame.to_avoid_anvil.ToAvoidAnvil

fun getNewGameList(): List<DimGame<*, *>> {
    return listOf(
        BattleOfPush(),
        BombSpinning(),
        FastCombination(),
        JumpMap(),
        OXQuiz(),
        ScoreOfPush(),
        Spleef(),
        ToAvoidAnvil()
    )
}