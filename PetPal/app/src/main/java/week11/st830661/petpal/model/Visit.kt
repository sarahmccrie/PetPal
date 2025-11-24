package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDateTime

data class Visit(
    val visitDate : LocalDateTime = LocalDateTime.now(),
    val vetName : String = "",
    val visitReason : String = "",
    val visitOutcome : String = "",
    val treatment : String = "",
    val prescription : String = "",
    @DocumentId
    val visitID : String = ""
)
