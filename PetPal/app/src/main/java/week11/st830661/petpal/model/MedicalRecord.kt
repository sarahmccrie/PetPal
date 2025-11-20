package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId

data class MedicalRecord(
    val numberOfVisits : Int = 0,
    val diagnoses : List<String> = emptyList(),
    val prescriptions : List<Prescription> = emptyList(),
    @DocumentId
    val medRecID : String
)