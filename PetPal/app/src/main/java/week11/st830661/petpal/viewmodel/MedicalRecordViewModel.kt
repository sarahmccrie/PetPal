package week11.st830661.petpal.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import week11.st830661.petpal.data.repository.FirestoreMedicalRecordRepository
import week11.st830661.petpal.model.MedicalRecord
import week11.st830661.petpal.model.VaccinationRecord
import week11.st830661.petpal.model.Visit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicalRecordViewModel (
    private val uid : String,
    private val medRepository : FirestoreMedicalRecordRepository = FirestoreMedicalRecordRepository()
) : ViewModel() {
    /** Requirements:
     * TODO #1: Get Medical Records for specific owner              -> DONE
     * TODO #2: Get Vaccination History for specific pet            -> DONE
     * TODO #3: Get Medical Treatment History for specific pet      -> DONE
     * TODO #4: Add Vaccination Treatment for specific pet          -> DONE
     * TODO #5: Add Medical Treatment/Visit record for specific pet -> DONE
     * TODO #6: Edit specific Vaccination record                    -> DONE
     * TODO #7: Edit specific Medical Treatment/Visit record        -> DONE
     * TODO #8: Delete specific Vaccination Record                  -> DONE
     * TODO #9: Delete specific Medical Treatment/Visit record      -> DONE
     * */

    // Formatter for checking and ensuring all dates are stored properly
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val _medicalRecords = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val medicalRecords : StateFlow<List<MedicalRecord>> = _medicalRecords

    private val _vaccinations = MutableStateFlow<List<VaccinationRecord>>(emptyList())
    val vaccinations : StateFlow<List<VaccinationRecord>> = _vaccinations

    private val _visits = MutableStateFlow<List<Visit>>(emptyList())
    val visits : StateFlow<List<Visit>> = _visits

    fun getMedicalRecordsForOwner(){
        viewModelScope.launch {
            medRepository.getMedicalRecordsForOwner(uid)
                .distinctUntilChanged()
                .collect { list ->
                _medicalRecords.value = list
            }
        }
    }

//    suspend fun fetchMedicalRecordsForOwner(ownerID: String): List<MedicalRecord> {
//        return medRepository.getMedicalRecordsForOwner(ownerID).first()
//    }

    fun addOrGetMedicalRecord(ownerID : String,
                         petID: String,
                         onReady: (MedicalRecord?) -> Unit) {
        viewModelScope.launch {
            // Gets an up to date version of the list of medical records
            val currentRecords = medRepository.getMedicalRecordsForOwner(ownerID)
                .distinctUntilChanged().first()
            _medicalRecords.value = currentRecords
//            getMedicalRecordsForOwner(ownerID)

//            delay(500)
            val medRec = medicalRecords.value.find { it.petID == petID}
            if(medRec != null) {
                onReady(medRec)
                return@launch
            }
            medRepository.addMedicalRecord(ownerID, petID)

            try {
                withTimeout(5000) {  // 5 second timeout
                    medicalRecords.value
                        .find { it.petID == petID }
                        .let { onReady(it) }
                }
            } catch (e: Exception) {
                Log.e("MedicalVM", "Timeout waiting for medical record", e)
                onReady(null)
            }
            getMedicalRecordsForOwner()
        }
        medicalRecords.value.forEach {
            Log.d("Test", "Medical record with ID: ${it.medRecID}")
        }
    }

    fun getVaccinationRecords(ownerID : String, medRecID : String){
        viewModelScope.launch {
            medRepository.getVaccinationRecords(ownerID, medRecID)
                .distinctUntilChanged()
                .collect { list ->
                _vaccinations.value = list
            }
        }
    }

    fun getVaccinationRecordByID(vaccRecID: String) : VaccinationRecord?{
        if(vaccRecID.isEmpty())
            return null

        return vaccinations.value.find {it.vacID == vaccRecID }
    }

    fun addVaccinationRecord(ownerID : String,
                             medRecID : String,
                             vaccine : String,
                             dateAdminRaw : String,
                             nextVaccDateRaw :String,
                             adminBy : String,
                             onDone : (Boolean) -> Unit){
        viewModelScope.launch {
            var success = false
            if(formatter.parse(dateAdminRaw) != null
                || formatter.parse(nextVaccDateRaw) != null) {
                val dateAdmin = LocalDate.parse(dateAdminRaw, formatter)
                val nextVaccDate = LocalDate.parse(nextVaccDateRaw, formatter)
                success = medRepository.addVaccinationRecord(
                    ownerID,
                    medRecID,
                    VaccinationRecord(
                        vaccine,
                        dateAdmin.toString(),
                        nextVaccDate.toString(),
                        adminBy
                    )
                )
            }else
                onDone(false)
            if(success) {
                onDone(true)
                Log.d("Success", "Vaccination record added successfully")
            }
        }
    }

    fun editVaccinationRecord(ownerID : String,
                              medRecID : String,
                              vaccine : String,
                              dateAdmin : String,
                              nextVaccDate :String,
                              adminBy : String,
                              vaccRecID : String,
                              onDone : (Boolean) -> Unit){
        viewModelScope.launch {
            var success = false
            if(LocalDate.parse(dateAdmin, formatter) != null
                || LocalDate.parse(nextVaccDate, formatter) != null) {
                val dateAdmin = LocalDate.parse(dateAdmin, formatter).toString()
                val nextVaccDate = LocalDate.parse(nextVaccDate, formatter).toString()
                success = medRepository.editVaccinationRecord(
                    ownerID, medRecID,
                    VaccinationRecord(
                        vaccine,
                        dateAdmin,
                        nextVaccDate,
                        adminBy,
                        vaccRecID
                    )
                )
            }else
                onDone(false)
            if(success) {
                onDone(true)
                Log.d("Success", "Vaccination record updated successfully")
            }
        }
    }

    fun deleteVaccinationRecord(ownerID : String, medRecID : String, vaccRecID : String){
        viewModelScope.launch {
            val success = medRepository.deleteVaccinationRecord(ownerID, medRecID, vaccRecID)
            if(success)
                Log.d("Success", "Vaccination record removed successfully")
        }
    }

    fun getVisitRecords(ownerID : String, medRecID : String){
        viewModelScope.launch {
            medRepository.getVisitRecords(ownerID, medRecID).collect { list ->
                _visits.value = list
            }
        }
    }

    fun getVisitByID(visitRecID : String) : Visit?{
        if(visitRecID.isEmpty())
            return null
        return visits.value.find { it.visitID == visitRecID }
    }

    fun addVisitRecord(ownerID : String,
                       medRecID : String,
                       visitDateRaw : String,
                       vetName : String,
                       visitReason : String,
                       visitOutcome : String,
                       treatment : String,
                       prescription : String,
                       onDone : (Boolean) -> Unit){
        viewModelScope.launch {
            var success = false
            if(formatter.parse(visitDateRaw) != null) {
                val visitDate = LocalDate.parse(visitDateRaw, formatter).toString()
                success = medRepository.addVisitRecord(
                    ownerID, medRecID,
                    Visit(
                        visitDate,
                        vetName,
                        visitReason,
                        visitOutcome,
                        treatment,
                        prescription
                    )
                )
            }else
                onDone(false)
            if (success) {
                onDone(true)
                Log.d("Success", "Visit record added successfully")
            }
        }
    }

    fun editVisitRecord(ownerID : String,
                        medRecID : String,
                        visitDateRaw : String,
                        vetName : String,
                        visitReason : String,
                        visitOutcome : String,
                        treatment : String,
                        prescription : String,
                        visitID : String,
                        onDone : (Boolean) -> Unit){
        viewModelScope.launch {
            var success = false
            if(formatter.parse(visitDateRaw) != null) {
                val visitDate = LocalDateTime.parse(visitDateRaw, formatter).toString()
                success = medRepository.editVisitRecord(
                    ownerID, medRecID,
                    Visit(
                        visitDate,
                        vetName,
                        visitReason,
                        visitOutcome,
                        treatment,
                        prescription,
                        visitID
                    )
                )
            }else
                onDone(false)
            if (success) {
                onDone(true)
                Log.d("Success", "Visit record modified successfully")
            }
        }
    }

    fun deleteVisitRecord(ownerID : String, medRecID : String, visitRecID : String){
        viewModelScope.launch {
            val success = medRepository.deleteVisitRecord(ownerID, medRecID, visitRecID)
            if(success)
                Log.d("Success", "Visit record removed successfully")
        }
    }
}

class MedicalRecordViewModelFactory(
    private val ownerId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicalRecordViewModel(ownerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//class MedicalRecordViewModelFactory(
//    private val ownerId: String
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MedicalRecordViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return MedicalRecordViewModel(ownerId) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//class MedicalRecordViewModelFactory(
//    private val ownerId: String
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MedicalRecordViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return MedicalRecordViewModel(ownerId) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}