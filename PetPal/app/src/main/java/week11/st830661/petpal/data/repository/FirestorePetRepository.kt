package week11.st830661.petpal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import week11.st830661.petpal.data.models.Pet

class FirestorePetRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Collection path users/{ownerId}/pets
    private fun petsCollection(ownerId: String) =
        firestore.collection("users")
            .document(ownerId)
            .collection("pets")

    fun getPetsForUser(ownerId: String): Flow<List<Pet>> = callbackFlow {
        val registration = petsCollection(ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

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

    suspend fun addPet(pet: Pet) {
        petsCollection(pet.ownerId).add(pet.toMap())
    }

    suspend fun updatePet(pet: Pet) {
        if (pet.id.isNotEmpty()) {
            petsCollection(pet.ownerId)
                .document(pet.id)
                .set(pet.toMap())
        }
    }

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
