package week11.st830661.petpal.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class VaccinationRecord(
    val vaccine : String = "",
    val dateAdministered : Date,
    val nextVaccineDate : Date,
    @DocumentId
    val vacId : String
)
