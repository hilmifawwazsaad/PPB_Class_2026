package com.example.kalkulatorsederhana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kalkulatorsederhana.ui.theme.KalkulatorSederhanaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KalkulatorSederhanaTheme {
                CalculatorScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        CalculatorCard(viewModel = viewModel)
    }
}

@Composable
fun CalculatorCard(viewModel: CalculatorViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        DisplayBox(text = viewModel.displayValue)
        Spacer(modifier = Modifier.height(12.dp))
        InfoBox(text = viewModel.infoText)
        
        Spacer(modifier = Modifier.weight(1f))
        
        ButtonGrid(viewModel = viewModel)
    }
}

@Composable
fun DisplayBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 34.sp
        )
    }
}

@Composable
fun InfoBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun ButtonGrid(viewModel: CalculatorViewModel) {
    val rows = listOf(
        listOf("+", "-", "*", "/"),
        listOf("1", "2", "3", "C"),
        listOf("4", "5", "6", ""),
        listOf("7", "8", "9", "0"),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { label ->
                    val containerColor = when {
                        label == "C" -> Color(0xFFE57373)
                        label in setOf("+", "-", "*", "/") -> Color(0xFFFFB74D)
                        label in setOf("") -> Color.Black
                        else -> Color.White
                    }
                    val contentColor = if (label == "C" || label in setOf("+", "-", "*", "/", "")) {
                        Color.White
                    } else {
                        Color.Black
                    }

                    CalculatorButton(
                        label = label,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        onClick = {
                            when {
                                label == "C" -> viewModel.onClearClick()
                                label == "" -> viewModel.onEqualsClick()
                                label in setOf("+", "-", "*", "/") -> viewModel.onOperatorClick(label)
                                else -> viewModel.onDigitClick(label)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, Color.Black),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    KalkulatorSederhanaTheme {
        CalculatorScreen(viewModel = CalculatorViewModel())
    }
}
