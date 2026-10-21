package com.example.data.model

data class PollOption(
    val id: Int,
    val text: String,
    val voteCount: Int = 0
)

data class PollData(
    val id: String,
    val question: String,
    val options: List<PollOption>,
    val totalVotes: Int = 0,
    val userVotedOptionId: Int? = null,
    val votesByUser: Map<String, Int> = emptyMap(),
    val isAnonymous: Boolean = true
) {
    fun getVotedOptionFor(userId: String): Int? = votesByUser[userId] ?: userVotedOptionId
}
