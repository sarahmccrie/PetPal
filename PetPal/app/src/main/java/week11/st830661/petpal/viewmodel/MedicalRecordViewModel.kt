package week11.st830661.petpal.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import week11.st830661.petpal.data.repository.FirestoreMedicalRecordRepository
import week11.st830661.petpal.model.MedicalRecord

class MedicalRecordViewModel (
    private val petId : String,
    private val medRepository : FirestoreMedicalRecordRepository = FirestoreMedicalRecordRepository()
) : ViewModel() {
    private val _medicalRecords = MutableStateFlow<List<MedicalRecord>>(emptyList())
}