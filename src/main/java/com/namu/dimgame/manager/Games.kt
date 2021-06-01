package com.namu.dimgame.manager

import com.namu.dimgame.minigame.DimGame
import com.namu.dimgame.minigame.battle_of_push.BattleOfPush
import com.namu.dimgame.minigame.bomb_spinning.BombSpinning
import com.namu.dimgame.minigame.fast_combination.FastCombination
import com.namu.dimgame.minigame.jump_map.JumpMap
import com.namu.dimgame.minigame.ox_quiz.OXQuiz
import com.namu.dimgame.minigame.score_of_push.ScoreOfPush
import com.namu.dimgame.minigame.spleef.Spleef
import com.namu.dimgame.minigame.to_avoid_anvil.ToAvoidAnvil

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