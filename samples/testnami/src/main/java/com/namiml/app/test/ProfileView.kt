package com.namiml.app.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.namiml.app.test.ui.theme.DarkGrey
import com.namiml.customer.NamiCustomerManager

val TEST_EXTERNAL_ID = "bff491f5-9b9f-4532-89b2-7199592bf02e"

@Composable
fun RowHeader(name: String) {
    Text(
        text = name,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(14.dp),
        color = DarkGrey
    )
}

@Composable
fun ProfileStatusText(status: String) {
    Text(
        text = status,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        color = Color.Black
    )
}

@Composable
fun ProfileIdentifierText(identifier: String) {
    Text(
        text = identifier,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        color = Color.Black
    )
}

@Composable
fun StatusCircle(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun ProfileCard(status: String, identifier: String) {
    BoxWithConstraints {
        if (maxWidth < 400.dp) {
            Column {
                ProfileStatusText(status = status)
                ProfileIdentifierText(identifier = identifier)
            }
        } else {
            Row {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileStatusText(status = status)
                    ProfileIdentifierText(identifier = identifier)
                }
            }
        }
    }
}

@Composable
fun JourneyStateRow(name: String, on: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusCircle(
                if (on) {
                    Color.Green
                } else {
                    Color.LightGray
                }
            )
            Text(
                text = name,
                fontSize = 18.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                color = Color.Black
            )
        }
    }
}

@Composable
fun ProfileView(leanback: Boolean = false, profileViewModel: ProfileViewModel = viewModel()) {
    val journeyState by profileViewModel.journeyStateFlow.collectAsState()
    val isLoggedIn by profileViewModel.isLoggedInFlow.collectAsState()
    val externalId by profileViewModel.externalIdFlow.collectAsState()

    val deviceId by remember {
        profileViewModel.deviceId
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Logout".takeIf { isLoggedIn } ?: "Login") },
                onClick = {
                    NamiCustomerManager.logout().takeIf { isLoggedIn } ?: NamiCustomerManager.login(
                        TEST_EXTERNAL_ID
                    )
                },
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.background
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.padding(start = 40.dp, end = 40.dp).takeIf { leanback } ?: Modifier.padding(
                    0.dp
                )
            ) {
                item {
                    ProfileCard(
                        status = "Registered User".takeIf { isLoggedIn }
                            ?: "Anonymous User",
                        identifier = "External Id: $externalId".takeIf { isLoggedIn }
                            ?: "Device Id: $deviceId"
                    )
                }
                item {
                    RowHeader("Journey State")
                }
                item {
                    JourneyStateRow("In Trial Period", journeyState.inTrialPeriod)
                }
                item {
                    JourneyStateRow("In Intro Offer Period", journeyState.inIntroOfferPeriod)
                }
                item {
                    JourneyStateRow("Has Cancelled", journeyState.isCancelled)
                }
                item {
                    JourneyStateRow("Former Subscriber", journeyState.formerSubscriber)
                }
                item {
                    JourneyStateRow("In Grace Period", journeyState.inGracePeriod)
                }
                item {
                    JourneyStateRow("In Pause", journeyState.inPause)
                }
                item {
                    JourneyStateRow("In Account Hold", journeyState.inAccountHold)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileViewPreview() {
    ProfileView(leanback = false)
}
