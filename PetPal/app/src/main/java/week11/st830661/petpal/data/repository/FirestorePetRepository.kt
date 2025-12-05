/**
 * Author: Sarah McCrie (991405606)
 * Repository for reading and writing pet data in firestore
 */

package week11.st830661.petpal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import week11.st830661.petpal.data.models.Pet

class FirestorePetRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Gets the pet subcollection from firestore
    // - Collection path users/{ownerId}/pets
    private fun petsCollection(ownerId: String) =
        firestore.collection("users")
            .document(ownerId)
            .collection("pets")

    //Gets all pets for the current user - uses snapshot listening
    fun getPetsForUser(ownerId: String): Flow<List<Pet>> = callbackFlow {
        val registration = petsCollection(ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                //Map the firestore documents into pet model objects
                val list = snapshot?.documents?.map { doc ->
                    Pet(
                        id = doc.id,
                        ownerId = ownerId,
                        name = doc.getString("name") ?: "",
                        species = doc.getString("species") ?: "",
                        breed = doc.getString("breed") ?: "",
                        age = (doc.getLong("age") ?: 0L).toInt(),
                        photoUrl = doc.getString("photoUrl") ?: ""
                    )
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    //Add the new pet document under users/{ownerId}/pets
    suspend fun addPet(pet: Pet) {
        petsCollection(pet.ownerId).add(pet.toMap())
    }

    //Update existing pet document - must have a valid id
    suspend fun updatePet(pet: Pet) {
        if (pet.id.isNotEmpty()) {
            petsCollection(pet.ownerId)
                .document(pet.id)
                .set(pet.toMap())
        }
    }

    //Delete pet document - must have valid id
    suspend fun deletePet(pet: Pet) {
        if (pet.id.isNotEmpty()) {
            petsCollection(pet.ownerId)
                .document(pet.id)
                .delete()
        }
    }

    private fun Pet.toMap(): Map<String, Any?> = mapOf(
        "ownerId" to ownerId,
        "name" to name,
        "species" to species,
        "breed" to breed,
        "age" to age,
        "photoUrl" to photoUrl
    )
}
