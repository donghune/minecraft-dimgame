package com.namu.dimgame.minigame.ox_quiz

import org.bukkit.entity.Player

class Quiz(
    val content: String,
    val answer: Boolean
)

val quizList = listOf<Quiz>(
    Quiz("[딤튜브] : 따식님의 나이는 한우님보다 낮다 ", true),
    Quiz("[딤튜브] : 딤튜브의 매니저에는 상덕님이 있다", true),
    Quiz("[딤튜브] : 딤퍼니소속 동훈님은 미니게임 제작자다", true),
    Quiz("[딤튜브] : 반반님은 탈모일 것이다", true),
    Quiz("[딤튜브] : 딤튜브의 귀요미송은 딤딤님의 대표곡이다", true),
    Quiz("[딤튜브] : 위개님과 뚜비님은 사실 사이가 좋은 절친이다", true),
    Quiz("[딤튜브] : 안알랴줌", true),
    Quiz("[딤튜브] : 안알랴줌", false),
    Quiz("[딤튜브] : 거북알 위에 모루가 떨어지면 거북알이 부셔진다", false),
    Quiz("[딤튜브] : 썩은 고기를 먹었을 때 허기 디버프를 받지 않을 수 있다", true),
    Quiz("[딤튜브] : 마법부여된 황금사과를 만들 수 있다", false),
    Quiz("[딤튜브] : 가스트의 화염구는 무한으로 날라간다", true),
    Quiz("[딤튜브] : 북극곰은 불속에서 빠르다", false),
    Quiz("[딤튜브] : 플레이어의 키는 약 190cm이다", false),
    Quiz("[딤튜브] : 개발자가 돼지를 만들 떄 오류가 생겨 만들어진 몬스터는 크리퍼다", true),
    Quiz("[상식] : 두꺼비는 이빨이 있다", false),
    Quiz("[상식] : 개구리는 이빨이 있다", true),
    Quiz("[상식] : 더울 땐 감기에 걸리지 않는다", false),
    Quiz("[상식] : 아기 때의 뼈 갯수가 어른일 때보다 더 많다", true),
    Quiz("[상식] : 기린은 뿔이 없다", false),
    Quiz("[상식] : 펭귄은 물 속에서 숨을 쉰다", false),
    Quiz("[상식] : 물갈퀴를 흉내내어 만든 발명품이 축구화다", false),
    Quiz("[상식] : 얼룩말의 줄무늬는 뭉쳐있으면 어지러워 보여서 사자가 사냥을 제대로 할 수 없게 만든다", true),
    Quiz("[상식] : 문어의 심장은 3개다", true),
    Quiz("[영화] : 007스카이폴은 007시리즈의 24번째 영화이다", false),
)