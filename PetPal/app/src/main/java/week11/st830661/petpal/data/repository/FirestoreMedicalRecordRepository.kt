package week11.st830661.petpal.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import week11.st830661.petpal.model.MedicalRecord
import week11.st830661.petpal.model.VaccinationRecord
import week11.st830661.petpal.model.Visit
import week11.st830661.petpal.viewmodel.medicalRecord


class FirestoreMedicalRecordRepository (
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /** Requirements:
     * TODO #1: Get Medical Record for specific pet                 -> DONE
     * TODO #2: Get Medical Records for specific owner              -> DONE
     * TODO #3: Get Vaccination History for specific pet            -> DONE
     * TODO #4: Get Medical Treatment History for specific pet      -> DONE
     * TODO #5: Add Vaccination Treatment for specific pet          -> DONE
     * TODO #6: Add Medical Treatment/Visit record for specific pet -> DONE
     * TODO #7: Edit specific Vaccination record                    -> DONE
     * TODO #8: Edit specific Medical Treatment/Visit record        -> DONE
     * TODO #9: Delete specific Vaccination Record                  -> DONE
     * TODO #10: Delete specific Medical Treatment/Visit record     -> DONE
     * */
    private suspend fun medicalRecords(ownerID : String) =
        db.collection("users")
            .document(ownerID)
            .collection("medical-records")

    suspend fun getMedicalRecordsForOwner(ownerID : String) : Flow<List<MedicalRecord>> = callbackFlow{
        val query = medicalRecords(ownerID)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(MedicalRecord::class.java)
                    ?: emptyList())
            }
        awaitClose { query.remove() }
    }

    suspend fun addMedicalRecord(ownerID : String,
                         petID : String){
        Log.d("Test", "Adding pet with id: $petID")
        if(petID.isEmpty())
            return
        Log.d("Test", "After if case")
        medicalRecords(ownerID)
            .add(MedicalRecord(petID = petID))
    }

    // Get medical records for a given pet
//    fun getMedicalRecordsForPet(petId : String) : Flow<MedicalRecord> = callbackFlow{
//        val query = medicalRecords(petId)
//            .addSnapshotListener { snapshot, error ->
//                if(error != null){
//                    close(error)
//                    return@addSnapshotListener
//                }
//
//                trySend(snapshot?.toObjects(MedicalRecord::class.java)?.get(0)
//                    ?: MedicalRecord(medRecID = ""))
//            }
//        awaitClose { query.remove() }
//    }

    // Vaccination records:
    // Gets the vaccination records for a given pet
    fun getVaccinationRecords(ownerID : String, medRecID : String) : Flow<List<VaccinationRecord>> = callbackFlow{
        val query = medicalRecords(ownerID)
            .document(medRecID)
            .collection("vaccinations")
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.toObjects(VaccinationRecord::class.java)
                    ?: emptyList())
            }
        awaitClose { query.remove() }
    }

    suspend fun addVaccinationRecord(ownerID : String, medRecID : String, vaccRec : VaccinationRecord) : Boolean{
        if(medRecID.isEmpty())
            return false
        medicalRecords(ownerID).document(medRecID)
            .collection("vaccinations")
            .add(vaccRec)
        return true
    }

    suspend fun editVaccinationRecord(ownerID : String, medRecID : String, vaccRec : VaccinationRecord) : Boolean{
        if(medRecID.isEmpty())
            return false
        medicalRecords(ownerID).document(medRecID)
            .collection("vaccinations")
            .document(vaccRec.vacID)
            .set(vaccRec)
        return true
    }

    suspend fun deleteVaccinationRecord(ownerID : String, medRecID : String, vaccRecID : String) : Boolean{
        if(medRecID.isEmpty() || vaccRecID.isEmpty())
            return false
        medicalRecords(ownerID).document(medRecID)
            .collection("vaccinations")
            .document(vaccRecID)
            .delete()
        return true
    }

    // Visit records:
    // Gets the visit history for a given pet
    fun getVisitRecords(ownerID : String, medRecID : String) : Flow<List<Visit>> = callbackFlow {
        val query = medicalRecords(ownerID)
            .document(medRecID)
            .collection("visits")
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.toObjects(Visit::class.java)
                    ?: emptyList())
            }
        awaitClose { query.remove() }
    }

    suspend fun addVisitRecord(ownerID : String, medRecID : String, visitRec : Visit) : Boolean{
        if(medRecID.isEmpty())
            return false
        medicalRecords(ownerID).document(medRecID)
            .collection("vaccinations")
            .add(visitRec)
        return true
    }

    suspend fun editVisitRecord(ownerID : String, medRecID : String, visitRec : Visit) : Boolean{
        if(medRecID.isEmpty())
            return false
        medicalRecords(ownerID).document(medRecID)
            .collection("vaccinations")
            .document(visitRec.visitID)
            .set(visitRec)
        return true
    }

    suspend fun deleteVisitRecord(ownerID : String, medRecID : String, visitRecID : String) : Boolean{
        if(medRecID.isEmpty() || visitRecID.isEmpty())
            return false
        medicalRecords(ownerID).document(medRecID)
            .collection("vaccinations")
            .document(visitRecID)
            .delete()
        return true
    }

//    private fun MedicalRecord.toMap() : Map<String, Any?> = mapOf(
//        "petID" to petID,
//        "visits" to visits,
//        "diagnoses" to diagnoses,
//        "prescriptions" to prescriptions,
//        "vaccinations" to vaccinations
//    )
}