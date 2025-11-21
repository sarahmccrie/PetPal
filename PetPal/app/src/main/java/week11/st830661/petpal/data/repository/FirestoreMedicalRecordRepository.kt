package week11.st830661.petpal.data.repository

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import week11.st830661.petpal.model.MedicalRecord
import week11.st830661.petpal.model.VaccinationRecord
import week11.st830661.petpal.model.Visit


class FirestoreMedicalRecordRepository (
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /** Requirements:
     * TODO #1: Get Medical Record for specific pet                 -> DONE
     * TODO #2: Get Vaccination History for specific pet            -> DONE
     * TODO #3: Get Medical Treatment History for specific pet      -> DONE
     * TODO #4: Add Vaccination Treatment for specific pet          -> DONE
     * TODO #5: Add Medical Treatment/Visit record for specific pet -> DONE
     * TODO #6: Edit specific Vaccination record                    -> DONE
     * TODO #7: Edit specific Medical Treatment/Visit record        -> DONE
     * TODO #8: Delete specific Vaccination Record                  -> DONE
     * TODO #9: Delete specific Medical Treatment/Visit record      -> DONE
     * */
    private fun medicalRecords(petId : String) =
        db.collection("medical-records").whereEqualTo("petId", petId)

    // Get medical records for a given pet
    private fun getMedicalRecordsForPet(petId : String) : Flow<MedicalRecord> = callbackFlow{
        val query = medicalRecords(petId)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.toObjects(MedicalRecord::class.java)?.get(0)
                    ?: MedicalRecord(medRecID = ""))
            }
        awaitClose { query.remove() }
    }

    // Vaccination records:
    // Gets the vaccination records for a given pet
    fun getVaccinationRecords(medRecID : String) : Flow<List<VaccinationRecord>> = callbackFlow{
        val query = db.collection("medical-records")
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

    fun addVaccinationRecord(medRecID : String, vaccRec : VaccinationRecord) : Boolean{
        if(medRecID.isEmpty())
            return false
        db.collection("medical-records").document(medRecID)
            .collection("vaccinations")
            .add(vaccRec)
        return true
    }

    fun editVaccinationRecord(medRecID : String, vaccRec : VaccinationRecord) : Boolean{
        if(medRecID.isEmpty())
            return false
        db.collection("medical-records").document(medRecID)
            .collection("vaccinations")
            .document(vaccRec.vacID)
            .set(vaccRec)
        return true
    }

    fun deleteVaccinationRecord(medRecID : String, vaccRecID : String) : Boolean{
        if(medRecID.isEmpty() || vaccRecID.isEmpty())
            return false
        db.collection("medical-records").document(medRecID)
            .collection("vaccinations")
            .document(vaccRecID)
            .delete()
        return true
    }

    // Visit records:
    // Gets the visit history for a given pet
    fun getVisitRecords(medRecID : String) : Flow<List<Visit>> = callbackFlow {
        val query = db.collection("medical-records")
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

    fun addVisitRecord(medRecID : String, vaccRec : VaccinationRecord) : Boolean{
        if(medRecID.isEmpty())
            return false
        db.collection("medical-records").document(medRecID)
            .collection("vaccinations")
            .add(vaccRec)
        return true
    }

    fun editVisitRecord(medRecID : String, visitRec : Visit) : Boolean{
        if(medRecID.isEmpty())
            return false
        db.collection("medical-records").document(medRecID)
            .collection("vaccinations")
            .document(visitRec.visitID)
            .set(visitRec)
        return true
    }

    fun deleteVisitRecord(medRecID : String, visitRecID : String) : Boolean{
        if(medRecID.isEmpty() || visitRecID.isEmpty())
            return false
        db.collection("medical-records").document(medRecID)
            .collection("vaccinations")
            .document(visitRecID)
            .delete()
        return true
    }

//    private fun MedicalRecord.toMap() : Map<String, Any?> = mapOf(
//        "petId" to petId,
//        "visits" to visits,
//        "diagnoses" to diagnoses,
//        "prescriptions" to prescriptions,
//        "vaccinations" to vaccinations
//    )
}