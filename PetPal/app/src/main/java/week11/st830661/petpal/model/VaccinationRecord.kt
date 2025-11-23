package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate

data class VaccinationRecord(
    val vaccine : String = "",
    val dateAdministered : LocalDate = LocalDate.now(),
    val nextVaccineDate : LocalDate = dateAdministered.plusYears(2),
    val administeredBy : String = "",
    @DocumentId
    val vacID : String = ""
)
