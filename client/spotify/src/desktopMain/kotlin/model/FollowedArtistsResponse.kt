package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowedArtistsResponse(
    val artists: ArtistsWrapper,
) {
    @Serializable
    data class ArtistsWrapper(
        @SerialName("items")
        val artists: List<Artist>,
    )
}
