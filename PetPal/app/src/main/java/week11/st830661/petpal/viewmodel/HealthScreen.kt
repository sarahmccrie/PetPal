package week11.st830661.petpal.viewmodel

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import week11.st830661.petpal.R
import week11.st830661.petpal.data.models.Pet
import week11.st830661.petpal.model.MedicalRecord

private enum class MedicalRecordSubScreens{
    MED_RECS,
    PET_MED_REC,
    ADD_VACC,
    EDIT_VACC,
    ADD_VISIT,
    EDIT_VISIT,
    ADD_APPOINT,
    EDIT_APPOINT
}
@Composable
fun HealthScreen(uid : String,
                 modifier: Modifier = Modifier) {
    var currentScreen by remember {mutableStateOf(MedicalRecordSubScreens.MED_RECS) }
    val medicalVM = MedicalRecordViewModel(uid)
    val petVM = PetsViewModel(uid)

    var currentPet by remember { mutableStateOf<Pet?>(null) }
    var currentMedicalRecord by remember { mutableStateOf<MedicalRecord?>(null) }

    LaunchedEffect(Unit) {
        petVM.pets.value.forEach { pet ->
            medicalVM.getMedicalRecordsForOwner(pet.ownerId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            /*horizontalArrangement = Arrangement.SpaceAround*/) {
//            if(currentScreen != MedicalRecordSubScreens.MED_RECS)
//                IconButton(onClick = {currentScreen = MedicalRecordSubScreens.MED_RECS}) {
//                    Icon(
////                        modifier = Modifier.fillMaxWidth(.75f),
//                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                        contentDescription = "Back"
//                    )
//                }
//            else{
//                Spacer(modifier = Modifier.fillMaxWidth(.25f))
//            }
//            Text(
//                text = "Health",
//                fontSize = 22.sp,
//                fontWeight = FontWeight.SemiBold,
//                modifier = Modifier.weight(1f),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.fillMaxWidth(.25f))
            Box(modifier = Modifier.width(48.dp)) {  // Fixed width for button area
                if (currentScreen != MedicalRecordSubScreens.MED_RECS) {
                    IconButton(onClick = { currentScreen = MedicalRecordSubScreens.MED_RECS }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }

            // Center - title (takes remaining space)
            Text(
                text = "Health",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Right side - spacer to balance (same width as left)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        when(currentScreen){
            MedicalRecordSubScreens.MED_RECS -> {
                Log.d("Test", "Med Recs screen")
                petMedicalRecordsList(
                    petVM.pets.collectAsState().value,
                    onPetClick = { medicalRecord, pet ->
                        currentMedicalRecord = medicalRecord
                        currentPet = pet
                        currentScreen = MedicalRecordSubScreens.PET_MED_REC},
                    medicalVM,
                    modifier
                )
            }
            MedicalRecordSubScreens.PET_MED_REC -> {
                val pet = currentPet
                val medRec = currentMedicalRecord
                if(pet == null || medRec == null) {
                    Log.d("Test", "Both are null")
                    currentScreen = MedicalRecordSubScreens.MED_RECS
                }else {
                    Log.d("Test", "Both are not null")
                    medicalRecord(medRec, pet,
                        onAddVaccClick = {
                            currentScreen = MedicalRecordSubScreens.ADD_VACC
                        },
                        onAddVisitClick = {
                            currentScreen = MedicalRecordSubScreens.ADD_VISIT
                        },
                        onAddAppointClick = {
                            currentScreen = MedicalRecordSubScreens.ADD_APPOINT
                        }, modifier)
                }
            }
            MedicalRecordSubScreens.ADD_VACC -> {

            }
            MedicalRecordSubScreens.EDIT_VACC -> TODO()
            MedicalRecordSubScreens.ADD_VISIT -> TODO()
            MedicalRecordSubScreens.EDIT_VISIT -> TODO()
            MedicalRecordSubScreens.ADD_APPOINT -> TODO()
            MedicalRecordSubScreens.EDIT_APPOINT -> TODO()
        }
    }
}

@Composable
fun petMedicalRecordsList(
    pets : List<Pet>,
    onPetClick : (MedicalRecord, Pet) -> Unit,
    vm : MedicalRecordViewModel,
    modifier: Modifier
){
//    pets.forEach {
//        Log.d("Test", it.name)
//    }
//    val medicalRecords by vm.medicalRecords.collectAsState()
//    var pendingPet by remember { mutableStateOf<Pet?>(null) }
//
//    LaunchedEffect(medicalRecords, pendingPet) {
//        pendingPet?.let { pet ->
//            medicalRecords.find { it.petID == pet.id }?.let { medRec ->
//                onPetClick(medRec, pet)
//                pendingPet = null  // Clear pending
//            }
//        }
//    }
    LazyColumn(modifier = Modifier
        .fillMaxWidth()) {
        Log.d("Test", "Test inside the list")
        items(pets){ pet ->
            PetMedicalListItem(pet,
                onClick = {
                    vm.addOrGetMedicalRecord(pet.ownerId, pet.id){ medRec ->
                        if(medRec != null){
                            onPetClick(medRec, pet)
                        }else
                            Log.e("PetList", "Failed to create medical record")
                    }
//                    vm.getMedicalRecordsForOwner(pet.ownerId)
//                    val medRec = vm.medicalRecords.value.find { it.petID == pet.id }
//                    if(medRec != null) {
//                        onPetClick(medRec, pet)
//                    }else
//                        vm.addOrGetMedicalRecord(pet.ownerId, pet.id){ medRec ->
//
//                        }
                })
        }
    }
}

@Composable
fun PetMedicalListItem(
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
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun medicalRecord(
    medRec : MedicalRecord,
    pet : Pet,
    onAddVaccClick : () -> Unit,
    onAddVisitClick : () -> Unit,
    onAddAppointClick : () -> Unit,
    modifier : Modifier
) {
    Column(modifier = Modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
//                .clip(CircleShape)
//                .background(Color.White)
//                .padding( horizontal = 6.dp)
                /*.size(140.dp)*/,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = pet.photoUrl.ifBlank { null },
                contentDescription = pet.name,
                modifier = Modifier
//                    .size(48.dp)
                    .clip(CircleShape)
//                    .background(Color(0xFFE9F6EC))
                    .size(120.dp)
                    /*.padding(end = 12.dp)*/,
                contentScale = ContentScale.Crop
            )

            Column {
                Text(
                    text = pet.name.ifBlank { "Unnamed pet" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
//                    textAlign = TextAlign.Center
                )
                Text(
                    text = pet.species.ifBlank { "Unknown species" },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(120.dp))

        }
        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier) {
            Text(
                text = "Vaccination History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vaccinations
//            LazyColumn() {
//
//
//            }

            Row(modifier = Modifier.fillMaxWidth()/*.weight(1f)*/,
                horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = { onAddVaccClick },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF20C997),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        modifier = Modifier.padding(5.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Appointment"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column/*(modifier = Modifier.fillMaxWidth())*/ {
            Text(
                text = "Medical Treatments",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()/*.weight(1f)*/,
                horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = { onAddVisitClick },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF20C997),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        modifier = Modifier.padding(5.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Visit Details"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier) {
            Text(
                text = "Vet Appointments",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()/*.weight(1f)*/,
                horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = { onAddAppointClick },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF20C997),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        modifier = Modifier.padding(5.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Appointment"
                    )
                }
            }
        }
    }
}

@Composable
fun vaccinationListItem(medRecID : String,
                        onClick: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start){
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.syringe),
                contentDescription = "Vaccination",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun VisitListItem(medRecID : String,
                        onClick: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start){
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.pill),
                contentDescription = "Medication",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AppointmentListItem(medRecID : String,
                        onClick: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start){
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Calendar",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}