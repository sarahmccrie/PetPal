package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId

data class MedicalRecord(
    val petID : String = "",
    val visits : List<Visit> = emptyList(),
    val diagnoses : List<String> = emptyList(),
    val prescriptions : List<Prescription> = emptyList(),
    val vaccinations : List<VaccinationRecord> = emptyList(),
    @DocumentId
    val medRecID : String
)