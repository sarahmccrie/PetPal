package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId

data class Prescription(
    val medication : String = "",
    val dosage : String = "",
    val instructions : String = "",
    @DocumentId
    val prescID : String
)
