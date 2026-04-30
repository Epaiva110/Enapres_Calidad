package com.minedu.gob.pe.enaprescalidad.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginState
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel
import com.minedu.gob.pe.enaprescalidad.R
import com.minedu.gob.pe.enaprescalidad.utils.SetupMapSystemUI


@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    SetupMapSystemUI()

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when (val s = state) {
            is LoginState.Success -> {
                onLoginSuccess(s.user,s.name,s.role)
            }
            is LoginState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LoginContent(
                codsup = viewModel.codsup,
                password = viewModel.password,
                onCodsupChange = viewModel::onCodsupChange,
                onPasswordChange = viewModel::onPasswordChange,
                isLoginEnabled = viewModel.isLoginEnabled,
                onLoginClick = {
                    viewModel.login(hasInternet(context))
                }
            )

            LoadingOverlay(visible = state is LoginState.Loading)
        }
    }
}

@Composable
fun LoadingOverlay(visible: Boolean) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun AppVersionLabel(version: String = "v1.0.0") {
    Text(
        text = version,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    )
}

// LoginContent ahora recibe el estado agrupado
@Composable
fun LoginContent(
    codsup: String,
    password: String,
    onCodsupChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isLoginEnabled: Boolean,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginHeader(title = stringResource(R.string.sup_nac))

        Spacer(Modifier.height(40.dp))

        LoginInputField(
            value = codsup,
            onValueChange = onCodsupChange,
            label = stringResource(R.string.codigo_supervisor),
            icon = Icons.Default.Person
        )

        Spacer(Modifier.height(16.dp))

        LoginInputField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.contrasena),
            keyboardType = KeyboardType.Password,
            isPassword = true,
            icon = Icons.Default.Lock
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isLoginEnabled,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.ingresar).uppercase())
        }

        Spacer(Modifier.height(16.dp))

        AppVersionLabel()
    }
}

@Composable
fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
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
        leadingIcon = { Icon(icon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle visibility"
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun LoginHeader(title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.inei_logo),
            contentDescription = "Logo",
            modifier = Modifier.height(80.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}