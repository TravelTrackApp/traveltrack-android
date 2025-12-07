/**
 * File: HomeScreen.kt
 * 
 * Main home/dashboard screen. Displays trip statistics, recent trips, and provides
 * navigation to log new trips, view trip list, and access trip details.
 */
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
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
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
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
        }
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToLogTrip,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add, 
                        contentDescription = "Log trip",
                        modifier = Modifier.size(26.dp)
                    )
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
                    userName = authUiState.user?.displayName?.takeIf { it.isNotBlank() }
                        ?: authUiState.user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
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
    val displayName = userName.ifBlank { "Traveler" }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu button on left - aligns with drawer opening from left
        Surface(
            onClick = onMenuClick,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            tonalElevation = 1.dp
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $displayName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Here is your travel summary",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummarySection(tripUiState: TripUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Your Journey Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
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
                        text = "AVG DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (tripUiState.summary.averageDurationHours > 0) {
                            "${"%.1f".format(tripUiState.summary.averageDurationHours)} hours"
                        } else {
                            "No data yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Trips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Surface(
                onClick = onViewAll,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        if (trips.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.background),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        alpha = 0.5f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No trips logged yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // App branding section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TravelTrack",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your journey companion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            NavigationDrawerItem(
                label = { 
                    Text(
                        "Home",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                selected = true,
                onClick = onHome,
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedContainerColor = Color.Transparent
                )
            )
            NavigationDrawerItem(
                label = { 
                    Text(
                        "Log Trip",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                selected = false,
                onClick = onLogTrip,
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedContainerColor = Color.Transparent
                )
            )
            NavigationDrawerItem(
                label = { 
                    Text(
                        "View Trips",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                selected = false,
                onClick = onSavedTrips,
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedContainerColor = Color.Transparent
                )
            )
        }

        // Logout section
        Surface(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "See you soon!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
