package week11.st830661.petpal.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st830661.petpal.data.repository.FirestoreMedicalRecordRepository
import week11.st830661.petpal.model.MedicalRecord
import week11.st830661.petpal.model.VaccinationRecord
import week11.st830661.petpal.model.Visit

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
    private val _medicalRecords = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val medicalRecords : StateFlow<List<MedicalRecord>> = _medicalRecords

    private val _vaccinations = MutableStateFlow<List<VaccinationRecord>>(emptyList())
    val vaccinations : StateFlow<List<VaccinationRecord>> = _vaccinations

    private val _visits = MutableStateFlow<List<Visit>>(emptyList())
    val visits : StateFlow<List<Visit>> = _visits

    fun getMedicalRecordsForOwner(ownerID : String){
        viewModelScope.launch {
            medRepository.getMedicalRecordsForOwner(ownerID).collect { list ->
                _medicalRecords.value = list
            }
        }
    }

    fun getVaccinationRecords(ownerID : String, medRecID : String){
        viewModelScope.launch {
            medRepository.getVaccinationRecords(ownerID, medRecID).collect { list ->
                _vaccinations.value = list
            }
        }
    }

    fun getVaccinationRecordByID(vaccRecID: String) : VaccinationRecord?{
        if(vaccRecID.isEmpty())
            return null

        return vaccinations.value.find {it.vacID == vaccRecID }
    }

    fun addVaccinationRecord(ownerID : String, medRecID : String, vaccRec : VaccinationRecord){
        viewModelScope.launch {
            val success = medRepository.addVaccinationRecord(ownerID, medRecID, vaccRec)
            if(success)
                Log.d("Success", "Vaccination record added successfully")
        }
    }

    fun editVaccinationRecord(ownerID : String, medRecID : String, vaccRec : VaccinationRecord){
        viewModelScope.launch {
            val success = medRepository.editVaccinationRecord(ownerID, medRecID, vaccRec)
            if(success)
                Log.d("Success", "Vaccination record modified successfully")
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

    fun addVisitRecord(ownerID : String, medRecID : String, visitRec : Visit){
        viewModelScope.launch {
            val success = medRepository.addVisitRecord(ownerID, medRecID, visitRec)
            if(success)
                Log.d("Success", "Visit record added successfully")
        }
    }

    fun editVisitRecord(ownerID : String, medRecID : String, visitRec : Visit){
        viewModelScope.launch {
            val success = medRepository.editVisitRecord(ownerID, medRecID, visitRec)
            if(success)
                Log.d("Success", "Visit record modified successfully")
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