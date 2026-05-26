package com.example.crudsiswa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.crudsiswa.data.entities.Student
import com.example.crudsiswa.viewmodels.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(
    viewModel: StudentViewModel,
    studentId: Int?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = studentId != null

    // Form state
    var name by remember { mutableStateOf("") }
    var studentIdField by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var gpa by remember { mutableStateOf("") }

    // Validation state
    var nameError by remember { mutableStateOf<String?>(null) }
    var studentIdError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var majorError by remember { mutableStateOf<String?>(null) }
    var gpaError by remember { mutableStateOf<String?>(null) }

    var existingStudent by remember { mutableStateOf<Student?>(null) }

    // Load student data if edit mode
    LaunchedEffect(studentId) {
        if (studentId != null) {
            val student = viewModel.getStudentById(studentId)
            student?.let {
                existingStudent = it
                name = it.name
                studentIdField = it.studentId
                email = it.email
                major = it.major
                gpa = it.gpa.toString()
            }
        }
    }

    // Navigate back on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    fun validate(): Boolean {
        var valid = true
        nameError = if (name.isBlank()) { valid = false; "Name is required" } else null
        studentIdError = if (studentIdField.isBlank()) { valid = false; "Student ID is required" } else null
        emailError = when {
            email.isBlank() -> { valid = false; "Email is required" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { valid = false; "Invalid email format" }
            else -> null
        }
        majorError = if (major.isBlank()) { valid = false; "Major is required" } else null
        gpaError = when {
            gpa.isBlank() -> { valid = false; "GPA is required" }
            gpa.toDoubleOrNull() == null -> { valid = false; "GPA must be a number" }
            gpa.toDouble() !in 0.0..4.0 -> { valid = false; "GPA must be between 0.0 - 4.0" }
            else -> null
        }
        return valid
    }

    fun onSubmit() {
        if (!validate()) return
        val student = Student(
            id = existingStudent?.id ?: 0,
            name = name.trim(),
            studentId = studentIdField.trim(),
            email = email.trim(),
            major = major.trim(),
            gpa = gpa.toDouble()
        )
        if (isEditMode) viewModel.updateStudent(student)
        else viewModel.addStudent(student)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditMode) "Edit Student" else "Add New Student",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Student Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                FormField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = "Full Name",
                    icon = Icons.Default.Person,
                    errorMessage = nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                FormField(
                    value = studentIdField,
                    onValueChange = { studentIdField = it; studentIdError = null },
                    label = "Student ID",
                    icon = Icons.Default.Badge,
                    errorMessage = studentIdError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    )
                )

                FormField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = "Email Address",
                    icon = Icons.Default.Email,
                    errorMessage = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                FormField(
                    value = major,
                    onValueChange = { major = it; majorError = null },
                    label = "Major / Department",
                    icon = Icons.Default.MenuBook,
                    errorMessage = majorError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                FormField(
                    value = gpa,
                    onValueChange = { gpa = it; gpaError = null },
                    label = "GPA (0.0 – 4.0)",
                    icon = Icons.Default.Star,
                    errorMessage = gpaError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Button
                Button(
                    onClick = { onSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (isEditMode) Icons.Default.Save else Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Save Changes" else "Register Student",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        isError = errorMessage != null,
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = keyboardOptions,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}