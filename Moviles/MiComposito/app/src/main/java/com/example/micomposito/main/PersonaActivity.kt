package com.example.micomposito.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.micomposito.domain.Persona
import com.example.micomposito.ui.theme.MiCompositoTheme

class PersonaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiCompositoTheme {
                PersonaPantalla()
            }
        }
    }
}

@Composable
fun PersonaPantalla(
    modifier: Modifier = Modifier,
    viewModel: PersonaViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PersonaPantallaContent(
        modifier = modifier,
        state = state,
        onNombreChange = viewModel::onNombreChange,
        onApellidoChange = viewModel::onApellidoChange,
        onAddPersona = viewModel::onAddPersona,
    )
}

@Composable
fun PersonaPantallaContent(
    modifier: Modifier = Modifier,
    state: PersonaState,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onAddPersona: () -> Unit,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Text(
                text = "Personas",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.apellido,
                    onValueChange = onApellidoChange,
                    label = { Text("Apellido") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddPersona,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.nombre.isNotBlank() && state.apellido.isNotBlank(),
            ) {
                Text("Añadir")
            }
            Spacer(modifier = Modifier.height(16.dp))
            PersonasList(personas = state.personas)
        }
    }
}

@Composable
private fun PersonasList(personas: List<Persona>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(personas) { persona ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${persona.nombre} ${persona.apellido}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonaPantallaPreview() {
    MiCompositoTheme {
        PersonaPantallaContent(
            state = PersonaState(
                nombre = "Nami",
                apellido = "Navegante",
                personas = listOf(
                    Persona("Monkey", "D. Luffy"),
                    Persona("Roronoa", "Zoro"),
                ),
            ),
            onNombreChange = {},
            onApellidoChange = {},
            onAddPersona = {},
        )
    }
}
