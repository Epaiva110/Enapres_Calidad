package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation3.runtime.NavBackStack
import com.minedu.gob.pe.encuestasatisfaccinenapres.R
import com.minedu.gob.pe.encuestasatisfaccinenapres.models.LoginState
import com.minedu.gob.pe.encuestasatisfaccinenapres.models.LoginViewModel
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.Routes
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.utils.SetupMapSystemUI

// --------------------- SCREEN PRINCIPAL ---------------------
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {

    SetupMapSystemUI()

    val viewModel: LoginViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var codsup by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isLoginEnabled by remember {
        derivedStateOf {
            codsup.isNotBlank() && password.length >= 1
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            Box {
                LoginContent(
                    codsup = codsup,
                    password = password,
                    onCodsupChange = { codsup = it },
                    onPasswordChange = { password = it },
                    isLoginEnabled = isLoginEnabled,
                    onLoginClick = {
                        viewModel.login(codsup, password)   // 🔥 PASO 6 AQUÍ
                    }
                )

                // 🔥 PASO 7: OBSERVAR ESTADO
                when (state) {

                    is LoginState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is LoginState.Success -> {
                        val user = (state as LoginState.Success).user

                        LaunchedEffect(Unit) {
                            onLoginSuccess(user) // 🔥 navega solo si es correcto
                        }
                    }

                    is LoginState.Error -> {
                        val message = (state as LoginState.Error).message

                        LaunchedEffect(message) {
                            snackbarHostState.showSnackbar(message)
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}
@Composable
fun LoginContent(
    codsup: String,
    password: String,
    onCodsupChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isLoginEnabled: Boolean,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginHeader(title = stringResource(id = R.string.sup_nac))

            Spacer(modifier = Modifier.height(32.dp))

            LoginInputField(
                value = codsup,
                onValueChange = onCodsupChange,
                label = stringResource(R.string.codigo_supervisor)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInputField(
                value = password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.contrasena),
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            LoginActions(
                onLoginClick = onLoginClick,
                isLoginEnabled = isLoginEnabled
            )
        }
    }
}


@Composable
fun LoginHeader(title: String = "Texto de ejemplo") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.inei_logo),
            contentDescription = "Logo INEI",
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .wrapContentHeight()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation =
            if (isPassword && !passwordVisible) PasswordVisualTransformation()
            else VisualTransformation.None,

        trailingIcon = {
            if (isPassword) {
                val icon = if (passwordVisible)
                    Icons.Default.Visibility
                else
                    Icons.Default.VisibilityOff

                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        }
    )
}

@Composable
fun LoginActions(
    onLoginClick: () -> Unit,
    isLoginEnabled: Boolean
) {
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        enabled = isLoginEnabled
    ) {
        Text(text = stringResource(R.string.ingresar))
    }
}
