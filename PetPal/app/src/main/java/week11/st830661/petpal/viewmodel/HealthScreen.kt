package week11.st830661.petpal.viewmodel

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import week11.st830661.petpal.R
import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Pet
import week11.st830661.petpal.model.MedicalRecord
import week11.st830661.petpal.model.VaccinationRecord
import week11.st830661.petpal.model.Visit
import week11.st830661.petpal.view.reminder.AddAppointmentDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import week11.st830661.petpal.model.Prescription

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
                 remVM : ReminderViewModel,
                 modifier: Modifier = Modifier) {
    var currentScreen by remember {mutableStateOf(MedicalRecordSubScreens.MED_RECS) }
    val medicalVM : MedicalRecordViewModel = viewModel(
        factory = MedicalRecordViewModelFactory(uid)
    )
    val petVM : PetsViewModel = viewModel(
        factory = PetsViewModelFactory(uid)
    )

    val pets by petVM.pets.collectAsState()
    var currentPet by remember { mutableStateOf<Pet?>(null) }
    var currentMedicalRecord by remember { mutableStateOf<MedicalRecord?>(null) }
    var currentVaccRecord by remember { mutableStateOf<VaccinationRecord?>(null) }
    var currentVisitRecord by remember { mutableStateOf<Visit?>(null) }
    var currentApptRecord by remember { mutableStateOf<Appointment?>(null) }
    var showAddApptDialog by remember { mutableStateOf(true) }
    var onSubScreen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        petVM.pets.value.forEach { pet ->
            medicalVM.getMedicalRecordsForOwner()
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
                    IconButton(onClick = { currentScreen = if(!onSubScreen)
                        MedicalRecordSubScreens.MED_RECS else
                            MedicalRecordSubScreens.PET_MED_REC }) {
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
                        onSubScreen = true
                        currentScreen = MedicalRecordSubScreens.PET_MED_REC },
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
                        onAddVaccClick = { medRec ->
                            currentMedicalRecord = medRec
                            onSubScreen = true
                            currentScreen = MedicalRecordSubScreens.ADD_VACC
                        },
                        onAddVisitClick = {medRec ->
                            currentMedicalRecord = medRec
                            onSubScreen = true
                            currentScreen = MedicalRecordSubScreens.ADD_VISIT
                        },
                        onAddAppointClick = {
                            onSubScreen = true
                            currentScreen = MedicalRecordSubScreens.ADD_APPOINT
                        },
                        onEditVaccClick = { medRec, vaccRec ->
                            currentMedicalRecord = medRec
                            currentVaccRecord = vaccRec
                            onSubScreen = true
                            currentScreen = MedicalRecordSubScreens.EDIT_VACC
                        },
                        onEditVisitClick = { medRec, visit ->
                            currentMedicalRecord = medRec
                            currentVisitRecord = visit
                            onSubScreen = true
                            currentScreen = MedicalRecordSubScreens.EDIT_VISIT
                        },
                        onEditAppointClick = { medRec, appt ->
                            currentMedicalRecord = medRec
                            currentApptRecord = appt
                            onSubScreen = true
                            MedicalRecordSubScreens.EDIT_APPOINT
                        },
                        medicalVM,
                        remVM,
                        modifier)
                }
            }
            MedicalRecordSubScreens.ADD_VACC -> {
                addVaccination(uid,
                    currentMedicalRecord!!.medRecID,
                    medicalVM,
                    onNavigateBack = {
                        onSubScreen = false
                        medicalVM.getVaccinationRecords(uid,
                            currentMedicalRecord!!.medRecID)
                        currentScreen = MedicalRecordSubScreens.PET_MED_REC
                    })
            }
            MedicalRecordSubScreens.EDIT_VACC -> {
                editVaccination(uid, currentMedicalRecord!!.medRecID,
                    currentVaccRecord!!,
                    medicalVM,
                    onNavigateBack = {
                        onSubScreen = false
                        currentScreen = MedicalRecordSubScreens.PET_MED_REC
                    })
            }
            MedicalRecordSubScreens.ADD_VISIT -> {
                addVisit(uid,
                    currentMedicalRecord!!.medRecID,
                    medicalVM,
                    onNavigateBack = {
                        onSubScreen = false
                        medicalVM.getVisitRecords(uid,
                            currentMedicalRecord!!.medRecID)
                        currentScreen = MedicalRecordSubScreens.PET_MED_REC
                    })
            }
            MedicalRecordSubScreens.EDIT_VISIT -> {
                editVisit(uid, currentMedicalRecord!!.medRecID,
                    currentVisitRecord!!,
                    medicalVM,
                    onNavigateBack = {
                        onSubScreen = false
                        medicalVM.getVisitRecords(uid,
                            currentMedicalRecord!!.medRecID)
                        currentScreen = MedicalRecordSubScreens.PET_MED_REC
                    })
            }
            MedicalRecordSubScreens.ADD_APPOINT -> {
                if(showAddApptDialog)
                AddAppointmentDialog(pets,
                    onDismiss = {
                        onSubScreen = false
                        showAddApptDialog = false
                    },
                    onSave = { appointment ->
                        onSubScreen = false
                        coroutineScope.launch {
                            remVM.addAppointment(appointment)
                        }
                        showAddApptDialog = false
                    })
                else {
                    showAddApptDialog = true
                    currentScreen = MedicalRecordSubScreens.PET_MED_REC
                }
            }
            MedicalRecordSubScreens.EDIT_APPOINT -> {
//                EditA
            }
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
        items(pets){ pet ->
            PetMedicalListItem(pet,
                onClick = {
                    vm.addOrGetMedicalRecord(pet.ownerId, pet.id){ medRec ->
                        if(medRec != null){
                            onPetClick(medRec, pet)
                        }else
                            Log.e("PetList", "Failed to create medical record")
                    }
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
            .background(MaterialTheme.colorScheme.surface)
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
    onAddVaccClick : (MedicalRecord) -> Unit,
    onAddVisitClick : (MedicalRecord) -> Unit,
    onAddAppointClick : () -> Unit,
    onEditVaccClick : (MedicalRecord, VaccinationRecord) -> Unit,
    onEditVisitClick : (MedicalRecord, Visit) -> Unit,
    onEditAppointClick : (MedicalRecord, Appointment) -> Unit,
    medicalVM : MedicalRecordViewModel,
    reminderVM : ReminderViewModel,
    modifier : Modifier
) {
    val coroutine = rememberCoroutineScope()
    val vaccs by medicalVM.vaccinations.collectAsState()
    val visits by medicalVM.visits.collectAsState()
    val appointments by reminderVM.appointments.collectAsState(initial = emptyList())
    var petsAppointments by remember {mutableStateOf<List<Appointment>?>(appointments.filter { it.petId == pet.id})}

    // Load data once when screen appears
    LaunchedEffect(medRec.medRecID) {
        medicalVM.getMedicalRecordsForOwner()
        medicalVM.getVaccinationRecords(pet.ownerId, medRec.medRecID)
        medicalVM.getVisitRecords(pet.ownerId, medRec.medRecID)
    }

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
            if(!vaccs.isEmpty())
            LazyColumn(modifier = Modifier
                .fillMaxWidth()) {
                items(vaccs) { vacc ->
                    vaccinationListItem(vacc,
                        onClick = {
                            onEditVaccClick(medRec, vacc)
                        })
                }
            }
            else
                Text("No vaccinations yet.\nAdd some and they will appear here")

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()/*.weight(1f)*/,
                horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = { onAddVaccClick(medRec) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        modifier = Modifier.padding(5.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Vaccine"
                    )
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

                // Visits
                if(!visits.isEmpty())
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()) {
                    items(visits) { visit ->
                        visitListItem(visit, onClick = {
                            onEditVisitClick(medRec, visit)
                        })
                    }
                }
                else
                    Text("No medical history yet.\nAdd some and it will appear here")

                Row(modifier = Modifier.fillMaxWidth()/*.weight(1f)*/,
                    horizontalArrangement = Arrangement.End) {
                    FilledTonalButton(
                        onClick = { onAddVisitClick(medRec) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(24.dp)
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

                // Appointments
                if(petsAppointments != null && !petsAppointments!!.isEmpty())
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()) {
                    items(appointments) { appt ->
                        appointmentListItem(appt) { }
                    }
                }
                else
                    Text("No appointments yet.\nAdd some and they will appear here")

                Row(modifier = Modifier.fillMaxWidth()/*.weight(1f)*/,
                    horizontalArrangement = Arrangement.End) {
                    FilledTonalButton(
                        onClick = { onAddAppointClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(24.dp)
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
}

@Composable
fun vaccinationListItem(vaccRec: VaccinationRecord,
                        onClick: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .background(MaterialTheme.colorScheme.surface),
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier,
            verticalArrangement = Arrangement.SpaceBetween){
            Text(text = vaccRec.dateAdministered,
                fontSize = 15.sp,
                color = Color.Black)
            Text(text = vaccRec.vaccine,
                fontSize = 11.sp,
                color = Color.Gray)
        }
    }
}

@Composable
fun visitListItem(visit : Visit,
                  onClick: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface),
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier,
            verticalArrangement = Arrangement.SpaceBetween){
            Text(text = visit.visitDate
                .format(DateTimeFormatter.ofPattern("yyyy-mm-dd")),
                fontSize = 15.sp,
                color = Color.Black)
            Text(text = visit.visitReason,
                fontSize = 11.sp,
                color = Color.Gray)
        }
    }
}

@Composable
fun appointmentListItem(apptRec : Appointment,
                        onClick: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface),
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier,
            verticalArrangement = Arrangement.SpaceBetween){
            Text(text = apptRec.dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString(),
                fontSize = 15.sp,
                color = Color.Black)
            Text(text = apptRec.title,
                fontSize = 11.sp,
                color = Color.Gray)
        }
    }
}

@Composable
fun addVaccination(
    uid : String,
    medRecID : String,
    vm : MedicalRecordViewModel,
//    onSave : (vaccine : String, dateAdministerd : LocalDate, nextVaccDate : LocalDate, adminBy : String) -> Unit,
    onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var vaccineType by remember { mutableStateOf("") }
    var dateAdministered by remember { mutableStateOf(LocalDate.now()) }
    var dateAdministeredRaw by remember { mutableStateOf(dateAdministered.toString()) }
    var nextVaccineDate by remember { mutableStateOf(dateAdministered.plusYears(2)) }
    var nextVaccineDateRaw by remember { mutableStateOf(nextVaccineDate.toString()) }
    var administeredBy by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Add Vaccination",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
//            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        VaccinationFormFields(vaccineType,
            {vaccineType = it},
            dateAdministeredRaw,
            {dateAdministeredRaw = it},
            nextVaccineDateRaw,
            {nextVaccineDateRaw = it},
            administeredBy,
            {administeredBy = it})

        // Pushes the next component to the bottom
        Spacer(modifier = Modifier.weight(1f))

        var continueToHealth = false
        Button(onClick = {
            vm.addVaccinationRecord(
                uid,
                medRecID,
                vaccineType,
                dateAdministeredRaw,
                nextVaccineDateRaw,
                administeredBy
            ){ success ->
                continueToHealth = success
                if(!continueToHealth)
                    Toast.makeText(context, "Date fields must be formatted properly", Toast.LENGTH_LONG)
                else
                    onNavigateBack()
            }
        },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Save Vaccine Details")
        }
    }
}

@Composable
fun editVaccination(uid : String,
                    medRecID : String,
                    vaccRec : VaccinationRecord,
                    vm : MedicalRecordViewModel,
                    onNavigateBack : () -> Unit){
    val context = LocalContext.current
    var vaccineType by remember { mutableStateOf(vaccRec.vaccine) }
    var dateAdministered by remember { mutableStateOf(vaccRec.dateAdministered) }
    var nextVaccineDate by remember { mutableStateOf(vaccRec.nextVaccineDate) }
    var administeredBy by remember { mutableStateOf(vaccRec.administeredBy) }

    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Edit Vaccination",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
//            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        VaccinationFormFields(vaccineType,
            {vaccineType = it},
            dateAdministered,
            {dateAdministered = it},
            nextVaccineDate,
            {nextVaccineDate = it},
            administeredBy,
            {administeredBy = it})

        // Pushes the next component to the bottom
        Spacer(modifier = Modifier.weight(1f))

        var continueToHealth = false
        Button(onClick = {
            vm.editVaccinationRecord(
                uid,
                medRecID,
                vaccineType,
                dateAdministered,
                nextVaccineDate,
                administeredBy,
                vaccRec.vacID
            ){ success ->
                continueToHealth = success
                if(!continueToHealth)
                    Toast.makeText(context, "Date fields must be formatted properly", Toast.LENGTH_LONG)
                else
                    onNavigateBack()
            }
        },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Save Vaccine Details")
        }
    }
}

@Composable
fun addVisit(
    uid : String,
    medRecID : String,
    vm : MedicalRecordViewModel,
    onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var visitDateRaw by remember { mutableStateOf(LocalDate.now()
        .format(DateTimeFormatter
            .ofPattern("yyyy-MM-dd"))
        .toString()) }
    var vetName by remember { mutableStateOf("") }
    var visitReason by remember { mutableStateOf("") }
    var visitOutcome by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Add Medical Visit",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
//            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        VisitFormFields(visitDateRaw,
            {visitDateRaw = it},
            vetName,
            {vetName = it},
            visitReason,
            {visitReason = it},
            visitOutcome,
            {visitOutcome = it},
            treatment,
            {treatment = it},
            prescription,
            {prescription = it})

        // Pushes the next component to the bottom
        Spacer(modifier = Modifier.weight(1f))

        var continueToHealth = false
        Button(onClick = {
            vm.addVisitRecord(
                uid,
                medRecID,
                visitDateRaw,
                vetName,
                visitReason,
                visitOutcome,
                treatment,
                prescription
            ){ success ->
                continueToHealth = success
                if(!continueToHealth)
                    Toast.makeText(context, "Date fields must be formatted properly", Toast.LENGTH_LONG)
                else
                    onNavigateBack()
            }
        },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Save Visit Details")
        }
    }
}

@Composable
fun editVisit(uid : String,
                    medRecID : String,
                    visitRec : Visit,
                    vm : MedicalRecordViewModel,
                    onNavigateBack : () -> Unit){
    val context = LocalContext.current
    var visitDate by remember { mutableStateOf(visitRec.visitDate) }
    var vetName by remember { mutableStateOf(visitRec.vetName) }
    var visitReason by remember { mutableStateOf(visitRec.visitReason) }
    var visitOutcome by remember { mutableStateOf(visitRec.visitOutcome) }
    var treatment by remember { mutableStateOf(visitRec.treatment) }
    var prescription by remember { mutableStateOf(visitRec.prescription) }

    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Edit Vaccination",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
//            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        VisitFormFields(visitDate,
            {visitDate = it},
            vetName,
            {vetName = it},
            visitReason,
            {visitReason = it},
            visitOutcome,
            {visitOutcome = it},
            treatment,
            {treatment = it},
            prescription,
            {prescription = it})

        // Pushes the next component to the bottom
        Spacer(modifier = Modifier.weight(1f))

        var continueToHealth = false
        Button(onClick = {
            vm.editVisitRecord(
                uid,
                medRecID,
                visitDate,
                vetName,
                visitReason,
                visitOutcome,
                treatment,
                prescription,
                visitRec.visitID
            ){ success ->
                continueToHealth = success
                if(!continueToHealth)
                    Toast.makeText(context, "Date fields must be formatted properly", Toast.LENGTH_LONG)
                else
                    onNavigateBack()
            }
        },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(24.dp)) {
            Text("Save Visit Details")
        }
    }
}

@Composable
fun VaccinationFormFields(
    type : String = "",
    onTypeChange : (String) -> Unit,
    dateAdministered : String = LocalDate.now().toString(),
    onDateAdminChange : (String) -> Unit,
    nextVaccineDate : String =
        LocalDate.parse(dateAdministered)
            .plusYears(2).toString(),
    onNextDateChange : (String) -> Unit,
    administeredBy : String = "",
    onVetChange : (String) -> Unit
){
    OutlinedTextField(
        value = type,
        label = {Text("Vaccine Type")},
        onValueChange = onTypeChange,
        placeholder = { Text("e.g. Rabies") },
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
        value = dateAdministered,
        label = {Text("Date Administered")},
        onValueChange = onDateAdminChange,
        placeholder = { Text("MM/DD/YYYY") },
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
        value = nextVaccineDate,
        label = {Text("Next Vaccination")},
        onValueChange = onNextDateChange,
        placeholder = { Text("MM/DD/YYYY") },
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
        value = administeredBy,
        label = {Text("Vet Name")},
        onValueChange = onVetChange,
        placeholder = { Text("") },
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

@Composable
fun VisitFormFields(
    visitDateRaw : String,
    onDateChange : (String) -> Unit,
    vetName : String,
    onVetChange : (String) -> Unit,
    visitReason : String,
    onReasonChange : (String) -> Unit,
    visitOutcome : String,
    onOutcomeChange : (String) -> Unit,
    treatment : String,
    onTreatmentChange : (String) -> Unit,
    prescription : String,
    onPrescriptionChange : (String) -> Unit
){
    OutlinedTextField(
        value = visitDateRaw,
        label = {Text("Visit Date")},
        onValueChange = onDateChange,
        placeholder = { Text("e.g. 2025-10-07") },
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
        value = vetName,
        label = { Text("Vet Name")},
        onValueChange = onVetChange,
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
        value = visitReason,
        label = {Text("Reason For Visit")},
        onValueChange = onReasonChange,
        placeholder = { Text("MM/DD/YYYY") },
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
        value = visitOutcome,
        label = {Text("Outcome of Visit")},
        onValueChange = onOutcomeChange,
        placeholder = { Text("") },
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
        value = treatment,
        label = {Text("Treatment Plan")},
        onValueChange = onTreatmentChange,
        placeholder = { Text("") },
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
        value = prescription,
        label = {Text("Prescription Details")},
        onValueChange = onPrescriptionChange,
        placeholder = { Text("") },
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