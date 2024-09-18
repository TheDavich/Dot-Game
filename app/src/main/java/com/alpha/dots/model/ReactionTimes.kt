package com.alpha.dots.model
data class ReactionTimeCategory(val name: String, val minTime: Long, val maxTime: Long)

val reactionTimeCategories = listOf(
    ReactionTimeCategory("Cheetah", 200L, 300L),  // Very fast reaction
    ReactionTimeCategory("Usain Bolt", 301L, 350L),  // Extremely fast, but adjusted
    ReactionTimeCategory("F1 Racer", 351L, 400L),  // Formula 1 drivers, very fast reflexes
    ReactionTimeCategory("Pro Boxer", 401L, 450L),  // Professional boxers, still fast
    ReactionTimeCategory("Table Tennis Player", 451L, 500L),  // Quick reactions, game adjusted
    ReactionTimeCategory("Astronaut", 501L, 550L),  // NASA astronauts have fast reactions
    ReactionTimeCategory("Michael Jordan", 551L, 600L),  // Michael Jordan, faster than most athletes
    ReactionTimeCategory("Baseball Player", 601L, 650L),  // Baseball hitters
    ReactionTimeCategory("Tiger", 651L, 700L),  // Fast, predatory reflexes
    ReactionTimeCategory("Teenager (Age 15-20)", 701L, 750L),  // Teenagers tend to have faster reflexes
    ReactionTimeCategory("Adult (Age 25-30)", 751L, 800L),  // Peak performance adults
    ReactionTimeCategory("Football Quarterback", 801L, 850L),  // Quick decision-making and reflexes
    ReactionTimeCategory("Dog", 851L, 900L),  // Quick reactions compared to other pets
    ReactionTimeCategory("Adult (Age 30-40)", 901L, 950L),  // Adults, slightly slower with age
    ReactionTimeCategory("Lion", 951L, 1000L),  // Lions have fast predatory reflexes
    ReactionTimeCategory("Adult (Age 40-50)", 1001L, 1050L),  // Noticeable decline with age
    ReactionTimeCategory("Senior (Age 60-65)", 1051L, 1100L),  // Seniors are slower, but game adjusted
    ReactionTimeCategory("Cat", 1101L, 1150L),  // Cats have fast reflexes, though slower in-game
    ReactionTimeCategory("Turtle", 1151L, 1200L)  // Average human reaction time for a game
)

fun getReactionCategory(reactionTime: Long): String {
    return reactionTimeCategories.find { reactionTime in it.minTime..it.maxTime }?.name ?: "Unknown"
}

