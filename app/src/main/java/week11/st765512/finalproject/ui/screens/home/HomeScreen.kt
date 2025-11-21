package week11.st765512.finalproject.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import week11.st765512.finalproject.R
import week11.st765512.finalproject.data.model.Trip
import week11.st765512.finalproject.ui.components.InfoChip
import week11.st765512.finalproject.ui.components.StatCard
import week11.st765512.finalproject.ui.components.SuccessPill
import week11.st765512.finalproject.ui.components.TripListCard
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel
import week11.st765512.finalproject.ui.viewmodel.TripUiState
import week11.st765512.finalproject.ui.viewmodel.TripViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    tripViewModel: TripViewModel,
    onNavigateToLogTrip: () -> Unit,
    onNavigateToTripList: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var authUiState by remember { mutableStateOf(authViewModel.uiState.value) }
    var tripUiState by remember { mutableStateOf(tripViewModel.uiState.value) }

    LaunchedEffect(Unit) {
        authViewModel.uiState.collect { authUiState = it }
    }

    LaunchedEffect(Unit) {
        tripViewModel.uiState.collect { tripUiState = it }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onHome = { scope.launch { drawerState.close() } },
                onLogTrip = {
                    scope.launch { drawerState.close() }
                    onNavigateToLogTrip()
                },
                onSavedTrips = {
                    scope.launch { drawerState.close() }
                    onNavigateToTripList()
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    tripViewModel.clearSelectedTrip()
                    authViewModel.signOut()
                }
            )
        }
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToLogTrip,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Log trip")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HeaderSection(
                    userName = authUiState.user?.displayName
                        ?: authUiState.user?.email?.substringBefore("@")
                        ?: "Traveler",
                    onMenuClick = { scope.launch { drawerState.open() } }
                )

                SummarySection(tripUiState = tripUiState)

                RecentTripsSection(
                    trips = tripUiState.trips.take(3),
                    onViewAll = onNavigateToTripList,
                    onTripClick = onNavigateToTripDetail
                )

                tripUiState.successMessage?.let { message ->
                    SuccessPill(
                        message = message,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (tripUiState.errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = tripUiState.errorMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(tripUiState.successMessage) {
        if (tripUiState.successMessage != null) {
            delay(2500)
            tripViewModel.clearMessage()
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Here is your travel summary",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SummarySection(tripUiState: TripUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Trips Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Trips",
                        value = tripUiState.summary.totalTrips.toString(),
                        caption = "Logged journeys",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Distance",
                        value = "${"%.1f".format(tripUiState.summary.totalDistanceKm)} km",
                        caption = "Total traveled",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                InfoChip(
                    label = "Average Duration",
                    value = if (tripUiState.summary.averageDurationHours > 0) {
                        "${"%.1f".format(tripUiState.summary.averageDurationHours)} hrs"
                    } else {
                        "—"
                    }
                )
            }
        }
    }
}

@Composable
private fun RecentTripsSection(
    trips: List<Trip>,
    onViewAll: () -> Unit,
    onTripClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Trips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "View Trips",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { onViewAll() }
            )
        }

        if (trips.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.background),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop,
                        alpha = 0.4f
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No trips logged yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap the + button to log your first journey.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            trips.forEach { trip ->
                TripListCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) }
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    onHome: () -> Unit,
    onLogTrip: () -> Unit,
    onSavedTrips: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "TravelTrack",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Navigate through the app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            NavigationDrawerItem(
                label = { Text("Home") },
                selected = false,
                onClick = onHome,
                colors = NavigationDrawerItemDefaults.colors()
            )
            NavigationDrawerItem(
                label = { Text("Log Trip") },
                selected = false,
                onClick = onLogTrip,
                colors = NavigationDrawerItemDefaults.colors()
            )
            NavigationDrawerItem(
                label = { Text("View Trips") },
                selected = false,
                onClick = onSavedTrips,
                colors = NavigationDrawerItemDefaults.colors()
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "See you soon!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Logout"
                    )
                }
            }
        }
    }
}
