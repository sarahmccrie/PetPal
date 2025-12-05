/**
 * Author: Sarah McCrie (991405606)
 * ViewModel for managing user's pets
 */


package week11.st830661.petpal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st830661.petpal.data.models.Pet
import week11.st830661.petpal.data.repository.FirestorePetRepository

class PetsViewModel(
    private val ownerId: String,
    private val repository: FirestorePetRepository = FirestorePetRepository()
) : ViewModel() {

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPetsForUser(ownerId).collect { list ->
                _pets.value = list
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    //Add a new pet for the current user - validates against nulls
    fun addPet(
        name: String,
        species: String,
        breed: String,
        ageText: String,
        photoUrl: String,
        onDone: (Boolean) -> Unit
    ) {
        if (name.isBlank() || species.isBlank()) {
            _errorMessage.value = "Name and species are required."
            onDone(false)
            return
        }

        val ageInt = ageText.toIntOrNull()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pet = Pet(
                    ownerId = ownerId,
                    name = name,
                    species = species,
                    breed = breed,
                    age = ageInt,
                    photoUrl = photoUrl
                )
                repository.addPet(pet)
                _isLoading.value = false
                onDone(true)
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to add pet."
                onDone(false)
            }
        }
    }

    //Update/edit an existing pet - same validation as add pet
    fun updatePet(
        existing: Pet,
        name: String,
        species: String,
        breed: String,
        ageText: String,
        photoUrl: String,
        onDone: (Boolean) -> Unit
    ) {
        if (name.isBlank() || species.isBlank()) {
            _errorMessage.value = "Name and species are required."
            onDone(false)
            return
        }

        val ageInt = ageText.toIntOrNull()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = existing.copy(
                    name = name,
                    species = species,
                    breed = breed,
                    age = ageInt,
                    photoUrl = photoUrl
                )
                repository.updatePet(updated)
                _isLoading.value = false
                onDone(true)
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to update pet."
                onDone(false)
            }
        }
    }

    //Deletes pet from firestore
    fun deletePet(pet: Pet, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deletePet(pet)
                _isLoading.value = false
                onDone(true)
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to delete pet."
                onDone(false)
            }
        }
    }
}

class PetsViewModelFactory(
    private val ownerId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PetsViewModel(ownerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
