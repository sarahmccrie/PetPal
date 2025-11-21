package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDateTime
import java.util.Date

data class VaccinationRecord(
    val vaccine : String = "",
    val dateAdministered : LocalDateTime = LocalDateTime.now(),
    val nextVaccineDate : LocalDateTime = dateAdministered.plusYears(2),
    val administeredBy : String = "",
    @DocumentId
    val vacId : String
)
