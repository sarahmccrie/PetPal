package week11.st830661.petpal.viewmodel

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import week11.st830661.petpal.data.models.Pet
import week11.st830661.petpal.R
import week11.st830661.petpal.ui.theme.components.PetPalTextField

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
            style = MaterialTheme.typography.headlineMedium,
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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

    val scope = rememberCoroutineScope()
    var isAnimating by remember { mutableStateOf(false) }

    var pawStep by remember { mutableStateOf(-1) }
    var pawVisible by remember { mutableStateOf(false) }

    val pawAlpha by animateFloatAsState(
        targetValue = if (pawVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "pawAlpha"
    )

    // Paw positions on screen
    val pawPositions = listOf(190.dp, 220.dp, 250.dp, 280.dp)

    // Rotations to make it look like stepping footprints
    val pawRotations = listOf(-25f, 18f, -18f, 25f)

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = !isAnimating) {
                isAnimating = true
                scope.launch {
                    // Play 4 steps
                    repeat(4) { step ->
                        pawStep = step
                        pawVisible = true
                        delay(130)
                        pawVisible = false
                        delay(60)
                    }
                    onClick()
                    isAnimating = false
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = pet.photoUrl.ifBlank { null },
            contentDescription = pet.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
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
        if (pawStep >= 0 && pawStep < pawPositions.size) {
            val xOffset = pawPositions[pawStep]
            val rotation = pawRotations[pawStep]

            Image(
                painter = painterResource(id = R.drawable.pawprint),
                contentDescription = "Pawprint step",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = xOffset)
                    .size(28.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                        alpha = pawAlpha
                    }
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
                style = MaterialTheme.typography.headlineSmall,
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
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
                style = MaterialTheme.typography.headlineSmall,
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
                .background(MaterialTheme.colorScheme.secondaryContainer),
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
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
            Text("Delete Pet", color = MaterialTheme.colorScheme.error)
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
    //Pet name
    PetPalTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = "Pet Name",
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    //Pet breed
    PetPalTextField(
        value = breed,
        onValueChange = onBreedChange,
        placeholder = "Breed (optional)",
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    //Pet species
    PetPalTextField(
        value = species,
        onValueChange = onSpeciesChange,
        placeholder = "Species",
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    //Pet age
    PetPalTextField(
        value = age,
        onValueChange = onAgeChange,
        placeholder = "Age",
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    Spacer(modifier = Modifier.height(12.dp))

    //Pet image url
    PetPalTextField(
        value = photoUrl,
        onValueChange = onPhotoUrlChange,
        placeholder = "Photo URL (optional)",
        modifier = Modifier.fillMaxWidth()
    )
}
