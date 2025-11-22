package week11.st830661.petpal.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import week11.st830661.petpal.data.models.Pet

private enum class PetsSubScreen {
    LIST,
    ADD,
    EDIT
}

@Composable
fun PetsScreen(
    modifier: Modifier = Modifier,
    uid: String
) {
    val petsViewModel: PetsViewModel = viewModel(
        key = "PetsViewModel_$uid",
        factory = PetsViewModelFactory(uid)
    )

    val pets by petsViewModel.pets.collectAsState()
    val isLoading by petsViewModel.isLoading.collectAsState()
    val errorMessage by petsViewModel.errorMessage.collectAsState()

    var currentScreen by remember { mutableStateOf(PetsSubScreen.LIST) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }

    when (currentScreen) {
        PetsSubScreen.LIST -> PetsListScreen(
            pets = pets,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onClearError = { petsViewModel.clearError() },
            onAddPetClick = { currentScreen = PetsSubScreen.ADD },
            onPetClick = { pet ->
                selectedPet = pet
                currentScreen = PetsSubScreen.EDIT
            },
            modifier = modifier
        )

        PetsSubScreen.ADD -> AddPetScreen(
            modifier = modifier,
            isLoading = isLoading,
            onSave = { name, species, breed, age, photoUrl ->
                petsViewModel.addPet(
                    name = name,
                    species = species,
                    breed = breed,
                    ageText = age,
                    photoUrl = photoUrl
                ) { success ->
                    if (success) {
                        currentScreen = PetsSubScreen.LIST
                    }
                }
            },
            onCancel = { currentScreen = PetsSubScreen.LIST },
            errorMessage = errorMessage,
            onClearError = { petsViewModel.clearError() }
        )

        PetsSubScreen.EDIT -> {
            val pet = selectedPet
            if (pet == null) {
                currentScreen = PetsSubScreen.LIST
            } else {
                EditPetScreen(
                    modifier = modifier,
                    pet = pet,
                    isLoading = isLoading,
                    onSave = { name, species, breed, age, photoUrl ->
                        petsViewModel.updatePet(
                            existing = pet,
                            name = name,
                            species = species,
                            breed = breed,
                            ageText = age,
                            photoUrl = photoUrl
                        ) { success ->
                            if (success) {
                                currentScreen = PetsSubScreen.LIST
                            }
                        }
                    },
                    onDelete = {
                        petsViewModel.deletePet(pet) { success ->
                            if (success) {
                                currentScreen = PetsSubScreen.LIST
                            }
                        }
                    },
                    onBack = { currentScreen = PetsSubScreen.LIST },
                    errorMessage = errorMessage,
                    onClearError = { petsViewModel.clearError() }
                )
            }
        }
    }
}

//Pets List Screen
@Composable
fun PetsListScreen(
    pets: List<Pet>,
    isLoading: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onAddPetClick: () -> Unit,
    onPetClick: (Pet) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6FFF5))
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "My Pets",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (pets.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No pets yet. Tap 'Add New Pet' to get started.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(pets) { pet ->
                    PetListItem(
                        pet = pet,
                        onClick = { onPetClick(pet) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add New Pet button
        Button(
            onClick = onAddPetClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF20C997),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Add New Pet", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PetListItem(
    pet: Pet,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = pet.photoUrl.ifBlank { null },
            contentDescription = pet.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE9F6EC)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = pet.name.ifBlank { "Unnamed pet" },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = pet.species.ifBlank { "Unknown species" },
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

//Add Pet Screen
@Composable
fun AddPetScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onSave: (name: String, species: String, breed: String, age: String, photoUrl: String) -> Unit,
    onCancel: () -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { onClearError() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6FFF5))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Back")
            }
            Text(
                text = "Add Pet",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        PetFormFields(
            name = name,
            onNameChange = { name = it },
            species = species,
            onSpeciesChange = { species = it },
            breed = breed,
            onBreedChange = { breed = it },
            age = age,
            onAgeChange = { age = it },
            photoUrl = photoUrl,
            onPhotoUrlChange = { photoUrl = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onSave(name, species, breed, age, photoUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF20C997),
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text("Save Pet", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

//Edit Pet Screen
@Composable
fun EditPetScreen(
    modifier: Modifier = Modifier,
    pet: Pet,
    isLoading: Boolean,
    onSave: (name: String, species: String, breed: String, age: String, photoUrl: String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit
) {
    var name by remember { mutableStateOf(pet.name) }
    var species by remember { mutableStateOf(pet.species) }
    var breed by remember { mutableStateOf(pet.breed) }
    var age by remember { mutableStateOf(pet.age?.toString() ?: "") }
    var photoUrl by remember { mutableStateOf(pet.photoUrl) }

    LaunchedEffect(pet.id) { onClearError() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6FFF5))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
            Text(
                text = "Edit Pet",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pet image preview
        AsyncImage(
            model = photoUrl.ifBlank { null },
            contentDescription = pet.name,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE9F6EC)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        PetFormFields(
            name = name,
            onNameChange = { name = it },
            species = species,
            onSpeciesChange = { species = it },
            breed = breed,
            onBreedChange = { breed = it },
            age = age,
            onAgeChange = { age = it },
            photoUrl = photoUrl,
            onPhotoUrlChange = { photoUrl = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onSave(name, species, breed, age, photoUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF20C997),
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text("Save Changes", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Pet", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

//Pet form fields for sharing over screens
@Composable
private fun PetFormFields(
    name: String,
    onNameChange: (String) -> Unit,
    species: String,
    onSpeciesChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    photoUrl: String,
    onPhotoUrlChange: (String) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = { Text("Pet Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE9F6EC),
            unfocusedContainerColor = Color(0xFFE9F6EC),
            disabledContainerColor = Color(0xFFE9F6EC),
            errorContainerColor = Color(0xFFE9F6EC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = breed,
        onValueChange = onBreedChange,
        placeholder = { Text("Breed (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE9F6EC),
            unfocusedContainerColor = Color(0xFFE9F6EC),
            disabledContainerColor = Color(0xFFE9F6EC),
            errorContainerColor = Color(0xFFE9F6EC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = species,
        onValueChange = onSpeciesChange,
        placeholder = { Text("Species") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE9F6EC),
            unfocusedContainerColor = Color(0xFFE9F6EC),
            disabledContainerColor = Color(0xFFE9F6EC),
            errorContainerColor = Color(0xFFE9F6EC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = age,
        onValueChange = onAgeChange,
        placeholder = { Text("Age") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE9F6EC),
            unfocusedContainerColor = Color(0xFFE9F6EC),
            disabledContainerColor = Color(0xFFE9F6EC),
            errorContainerColor = Color(0xFFE9F6EC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = photoUrl,
        onValueChange = onPhotoUrlChange,
        placeholder = { Text("Photo URL (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE9F6EC),
            unfocusedContainerColor = Color(0xFFE9F6EC),
            disabledContainerColor = Color(0xFFE9F6EC),
            errorContainerColor = Color(0xFFE9F6EC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        )
    )
}
