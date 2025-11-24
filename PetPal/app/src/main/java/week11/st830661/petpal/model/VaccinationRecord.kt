package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate

data class VaccinationRecord(
    val vaccine : String = "",
    val dateAdministered : String = LocalDate.now().toString(),
    val nextVaccineDate : String = LocalDate
        .parse(dateAdministered)
        .plusYears(2)
        .toString(),
    val administeredBy : String = "",
    @DocumentId
    val vacID : String = ""
)
