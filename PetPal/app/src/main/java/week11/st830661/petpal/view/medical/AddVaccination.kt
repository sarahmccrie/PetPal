package week11.st830661.petpal.view.medical

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st830661.petpal.viewmodel.HealthScreen
import week11.st830661.petpal.viewmodel.MedicalRecordViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddVaccination(
    uid : String,
    medRecID : String,
    vm : MedicalRecordViewModel,
//    onSave : (vaccine : String, dateAdministerd : LocalDate, nextVaccDate : LocalDate, adminBy : String) -> Unit,
    onNavigateBack: () -> Unit) {
    var vaccineType by remember { mutableStateOf("") }
    var dateAdministered by remember { mutableStateOf(LocalDate.now()) }
    var dateAdministeredRaw by remember { mutableStateOf(dateAdministered.toString()) }
    var nextVaccineDate by remember { mutableStateOf(dateAdministered.plusYears(2)) }
    var nextVaccineDateRaw by remember { mutableStateOf(nextVaccineDate.toString()) }
    var administeredBy by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    modifier = Modifier.padding(5.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Add Vaccination",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(64.dp))
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
            }
        }) {
            if(!continueToHealth)
                Toast.makeText(LocalContext.current, "Date fields must be formatted properly", Toast.LENGTH_LONG)
            else
                HealthScreen(uid = uid)
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