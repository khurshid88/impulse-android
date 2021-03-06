package uz.impulse.impulse.data.remote.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "illness")
data class Illness(
    @PrimaryKey
    val id: Int,
    val illnessName: String?,
    val info1: String?,
    val info2: String?,
    val info3: String?,
    val info4: String?,
    val info5: String?,
    val info6: String?,
    val info7: String?,
    val info8: String?,
    val photoUrl: String?
)