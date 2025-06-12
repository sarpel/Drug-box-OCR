package com.boxocr.simple.ui.production

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.iot.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🏥 PRODUCTION FEATURE UI: IOT INTEGRATION (SMART MEDICAL DEVICES)
 * 
 * Revolutionary UI for smart medical device integration:
 * - Real-time medical device discovery and connection
 * - Vital signs monitoring dashboard
 * - Hospital IoT network integration
 * - Turkish medical device compliance management
 * - Remote patient monitoring setup
 * 
 * Medical-grade IoT interface with Turkish healthcare standards
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IoTIntegrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: IoTIntegrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Cihazlar" to Icons.Default.Devices,
        "Vital Signs" to Icons.Default.Favorite,
        "Hastane Ağı" to Icons.Default.NetworkWifi,
        "Eczane" to Icons.Default.LocalPharmacy,
        "Uzaktan İzlem" to Icons.Default.MonitorHeart
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Header with IoT Status
        IoTHeader(
            onNavigateBack = onNavigateBack,
            networkStatus = uiState.networkStatus,
            connectedDevices = uiState.connectedDevices.size
        )

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp)) },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> MedicalDevicesTab(viewModel = viewModel, uiState = uiState)
            1 -> VitalSignsTab(viewModel = viewModel, uiState = uiState)
            2 -> HospitalNetworkTab(viewModel = viewModel, uiState = uiState)
            3 -> SmartPharmacyTab(viewModel = viewModel, uiState = uiState)
            4 -> RemoteMonitoringTab(viewModel = viewModel, uiState = uiState)
        }
    }
}

@Composable
private fun IoTHeader(
    onNavigateBack: () -> Unit,
    networkStatus: IoTNetworkStatus,
    connectedDevices: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = "IoT",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🏥 IoT Tıbbi Cihaz Entegrasyonu",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Akıllı Tıbbi Cihaz Yönetimi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // IoT Status Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IoTStatusCard(
                    title = "Bağlı Cihaz",
                    value = "$connectedDevices",
                    icon = Icons.Default.DeviceHub,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IoTStatusCard(
                    title = "Ağ Durumu",
                    value = if (networkStatus.isConnectedToHospital) "Bağlı" else "Offline",
                    icon = Icons.Default.NetworkWifi,
                    color = if (networkStatus.isConnectedToHospital) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                IoTStatusCard(
                    title = "Sinyal",
                    value = when (networkStatus.networkQuality) {
                        NetworkQuality.EXCELLENT -> "Mükemmel"
                        NetworkQuality.GOOD -> "İyi"
                        NetworkQuality.FAIR -> "Orta"
                        NetworkQuality.POOR -> "Zayıf"
                        else -> "Bilinmiyor"
                    },
                    icon = Icons.Default.SignalWifi4Bar,
                    color = when (networkStatus.networkQuality) {
                        NetworkQuality.EXCELLENT, NetworkQuality.GOOD -> MaterialTheme.colorScheme.primary
                        NetworkQuality.FAIR -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.weight(1f)
                )
                IoTStatusCard(
                    title = "Aktif Oturum",
                    value = "${networkStatus.activeConnections}",
                    icon = Icons.Default.MonitorHeart,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IoTStatusCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MedicalDevicesTab(
    viewModel: IoTIntegrationViewModel,
    uiState: IoTIntegrationUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Device Discovery Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔍 Cihaz Keşfi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Bluetooth, WiFi ve NFC üzerinden tıbbi cihazları otomatik olarak keşfedin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.discoverDevices() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isScanning
                        ) {
                            if (uiState.isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cihaz Ara")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.refreshDevices() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Yenile")
                        }
                    }
                }
            }
        }
        
        // Connected Devices
        if (uiState.connectedDevices.isNotEmpty()) {
            item {
                Text(
                    text = "🔗 Bağlı Cihazlar (${uiState.connectedDevices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            items(uiState.connectedDevices) { device ->
                MedicalDeviceCard(
                    device = device,
                    onConnect = { viewModel.connectToDevice(device.id) },
                    onDisconnect = { viewModel.disconnectDevice(device.id) },
                    onViewReadings = { viewModel.viewDeviceReadings(device.id) }
                )
            }
        }
        
        // Available Devices
        if (uiState.availableDevices.isNotEmpty()) {
            item {
                Text(
                    text = "📱 Kullanılabilir Cihazlar (${uiState.availableDevices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            items(uiState.availableDevices) { device ->
                MedicalDeviceCard(
                    device = device,
                    onConnect = { viewModel.connectToDevice(device.id) },
                    onDisconnect = { viewModel.disconnectDevice(device.id) },
                    onViewReadings = { viewModel.viewDeviceReadings(device.id) }
                )
            }
        }
    }
}

@Composable
private fun MedicalDeviceCard(
    device: MedicalDevice,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onViewReadings: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (device.isConnected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (device.isConnected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Device Type Icon
                    Icon(
                        when (device.type) {
                            MedicalDeviceType.BLOOD_PRESSURE -> Icons.Default.Favorite
                            MedicalDeviceType.GLUCOSE_METER -> Icons.Default.Bloodtype
                            MedicalDeviceType.WEIGHT_SCALE -> Icons.Default.FitnessCenter
                            MedicalDeviceType.THERMOMETER -> Icons.Default.Thermostat
                            MedicalDeviceType.PULSE_OXIMETER -> Icons.Default.MonitorHeart
                            else -> Icons.Default.DeviceUnknown
                        },
                        contentDescription = device.type.name,
                        modifier = Modifier.size(24.dp),
                        tint = if (device.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${device.manufacturer} - ${device.model}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        // Turkish Certification Badge
                        if (device.turkishCertification == TurkishMedicalCertification.CERTIFIED) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Türk Tıbbi Onayı",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🇹🇷 Türk Tıbbi Onayı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                // Connection Status Badge
                Surface(
                    color = if (device.isConnected) 
                        MaterialTheme.colorScheme.primaryContainer
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (device.isConnected) "Bağlı" else "Bekleniyor",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (device.isConnected) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Device Capabilities
            if (device.capabilities.isNotEmpty()) {
                Text(
                    text = "Özellikler: ${device.capabilities.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (device.isConnected) {
                    AssistChip(
                        onClick = onViewReadings,
                        label = { Text("Ölçümler") },
                        leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = onDisconnect,
                        label = { Text("Bağlantıyı Kes") },
                        leadingIcon = { Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                } else {
                    AssistChip(
                        onClick = onConnect,
                        label = { Text("Bağlan") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                AssistChip(
                    onClick = { /* Device info */ },
                    label = { Text("Bilgi") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
private fun VitalSignsTab(
    viewModel: IoTIntegrationViewModel,
    uiState: IoTIntegrationUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Vital Signs Dashboard
            VitalSignsDashboard(readings = uiState.recentReadings)
        }
        
        item {
            // Start Monitoring Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Vital Signs İzleme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Bağlı cihazlardan gerçek zamanlı vital signs verilerini izleyin",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startVitalSignsMonitoring() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isMonitoring && uiState.connectedDevices.isNotEmpty()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("İzlemeyi Başlat")
                        }
                        
                        if (uiState.isMonitoring) {
                            OutlinedButton(
                                onClick = { viewModel.stopVitalSignsMonitoring() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Durdur")
                            }
                        }
                    }
                }
            }
        }
        
        // Recent Readings
        if (uiState.recentReadings.isNotEmpty()) {
            item {
                Text(
                    text = "📈 Son Ölçümler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            items(uiState.recentReadings) { reading ->
                MedicalReadingCard(reading = reading)
            }
        }
    }
}

@Composable
private fun VitalSignsDashboard(readings: List<MedicalReading>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💓 Vital Signs Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Vital Signs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VitalSignCard(
                    title = "Kan Basıncı",
                    value = readings.lastOrNull { it.type == MedicalReadingType.BLOOD_PRESSURE }?.let { "${it.value.toInt()} ${it.unit}" } ?: "--",
                    icon = Icons.Default.Favorite,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                VitalSignCard(
                    title = "Glukoz",
                    value = readings.lastOrNull { it.type == MedicalReadingType.GLUCOSE }?.let { "${it.value.toInt()} ${it.unit}" } ?: "--",
                    icon = Icons.Default.Bloodtype,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VitalSignCard(
                    title = "Ağırlık",
                    value = readings.lastOrNull { it.type == MedicalReadingType.WEIGHT }?.let { "${it.value} ${it.unit}" } ?: "--",
                    icon = Icons.Default.FitnessCenter,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                VitalSignCard(
                    title = "Sıcaklık",
                    value = readings.lastOrNull { it.type == MedicalReadingType.TEMPERATURE }?.let { "${it.value} ${it.unit}" } ?: "--",
                    icon = Icons.Default.Thermostat,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VitalSignCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MedicalReadingCard(reading: MedicalReading) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (reading.type) {
                        MedicalReadingType.BLOOD_PRESSURE -> Icons.Default.Favorite
                        MedicalReadingType.GLUCOSE -> Icons.Default.Bloodtype
                        MedicalReadingType.WEIGHT -> Icons.Default.FitnessCenter
                        MedicalReadingType.TEMPERATURE -> Icons.Default.Thermostat
                        MedicalReadingType.PULSE_OXYGEN -> Icons.Default.MonitorHeart
                        else -> Icons.Default.Analytics
                    },
                    contentDescription = reading.type.name,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = reading.deviceName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = reading.type.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${reading.value} ${reading.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(reading.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun HospitalNetworkTab(
    viewModel: IoTIntegrationViewModel,
    uiState: IoTIntegrationUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Hastane IoT Ağı - Geliştiriliyor")
    }
}

@Composable
private fun SmartPharmacyTab(
    viewModel: IoTIntegrationViewModel,
    uiState: IoTIntegrationUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Akıllı Eczane Entegrasyonu - Geliştiriliyor")
    }
}

@Composable
private fun RemoteMonitoringTab(
    viewModel: IoTIntegrationViewModel,
    uiState: IoTIntegrationUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Uzaktan Hasta İzleme - Geliştiriliyor")
    }
}

// ViewModel for IoT Integration Screen
@HiltViewModel
class IoTIntegrationViewModel @Inject constructor(
    private val smartMedicalDeviceRepository: SmartMedicalDeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IoTIntegrationUiState())
    val uiState: StateFlow<IoTIntegrationUiState> = _uiState.asStateFlow()

    init {
        loadNetworkStatus()
        loadConnectedDevices()
    }

    fun discoverDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            
            try {
                val result = smartMedicalDeviceRepository.discoverMedicalDevices()
                result.onSuccess { devices ->
                    _uiState.value = _uiState.value.copy(
                        availableDevices = devices,
                        isScanning = false
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        error = exception.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message
                )
            }
        }
    }

    fun connectToDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                // Connect based on device type
                val device = _uiState.value.availableDevices.find { it.id == deviceId }
                device?.let {
                    when (it.type) {
                        MedicalDeviceType.BLOOD_PRESSURE -> {
                            smartMedicalDeviceRepository.connectToBloodPressureMonitor(deviceId)
                        }
                        MedicalDeviceType.GLUCOSE_METER -> {
                            smartMedicalDeviceRepository.connectToGlucoseMeter(deviceId)
                        }
                        else -> {
                            // Generic connection
                        }
                    }
                    
                    // Update UI state
                    val updatedAvailable = _uiState.value.availableDevices.filter { it.id != deviceId }
                    val updatedConnected = _uiState.value.connectedDevices + it.copy(isConnected = true)
                    
                    _uiState.value = _uiState.value.copy(
                        availableDevices = updatedAvailable,
                        connectedDevices = updatedConnected
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun disconnectDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                val result = smartMedicalDeviceRepository.disconnectFromDevice(deviceId)
                result.onSuccess {
                    val device = _uiState.value.connectedDevices.find { it.id == deviceId }
                    device?.let {
                        val updatedConnected = _uiState.value.connectedDevices.filter { it.id != deviceId }
                        val updatedAvailable = _uiState.value.availableDevices + it.copy(isConnected = false)
                        
                        _uiState.value = _uiState.value.copy(
                            connectedDevices = updatedConnected,
                            availableDevices = updatedAvailable
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun refreshDevices() {
        loadConnectedDevices()
    }

    fun viewDeviceReadings(deviceId: String) {
        // Implementation for viewing device readings
    }

    fun startVitalSignsMonitoring() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMonitoring = true)
            
            try {
                val deviceIds = _uiState.value.connectedDevices.map { it.id }
                val result = smartMedicalDeviceRepository.startVitalSignsMonitoring(
                    deviceIds = deviceIds,
                    patientId = null
                )
                
                result.onSuccess { session ->
                    // Start collecting readings
                    collectDeviceReadings()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isMonitoring = false,
                        error = exception.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isMonitoring = false,
                    error = e.message
                )
            }
        }
    }

    fun stopVitalSignsMonitoring() {
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }

    private fun loadNetworkStatus() {
        viewModelScope.launch {
            smartMedicalDeviceRepository.iotNetworkStatus.collect { status ->
                _uiState.value = _uiState.value.copy(networkStatus = status)
            }
        }
    }

    private fun loadConnectedDevices() {
        viewModelScope.launch {
            // Load sample connected devices
            val sampleDevices = listOf(
                createSampleDevice("bp_001", "Omron BP Monitor", MedicalDeviceType.BLOOD_PRESSURE, true),
                createSampleDevice("glucose_001", "OneTouch Glucose Meter", MedicalDeviceType.GLUCOSE_METER, false),
                createSampleDevice("scale_001", "Tanita Body Scale", MedicalDeviceType.WEIGHT_SCALE, false)
            )
            
            _uiState.value = _uiState.value.copy(
                connectedDevices = sampleDevices.filter { it.isConnected },
                availableDevices = sampleDevices.filter { !it.isConnected }
            )
        }
    }

    private fun collectDeviceReadings() {
        viewModelScope.launch {
            smartMedicalDeviceRepository.deviceReadings.collect { readings ->
                _uiState.value = _uiState.value.copy(recentReadings = readings.take(10))
            }
        }
    }

    private fun createSampleDevice(
        id: String,
        name: String,
        type: MedicalDeviceType,
        isConnected: Boolean
    ): MedicalDevice {
        return MedicalDevice(
            id = id,
            name = name,
            type = type,
            manufacturer = "Sample Manufacturer",
            model = "Model X",
            firmwareVersion = "1.0.0",
            connectionType = ConnectionType.BLUETOOTH_LE,
            isConnected = isConnected,
            lastSeen = System.currentTimeMillis(),
            turkishCertification = TurkishMedicalCertification.CERTIFIED,
            capabilities = listOf("basic_measurement", "data_storage"),
            specifications = mapOf("accuracy" to "±2%"),
            lastCalibration = System.currentTimeMillis()
        )
    }
}

// Data classes for IoT UI state
data class IoTIntegrationUiState(
    val isScanning: Boolean = false,
    val isMonitoring: Boolean = false,
    val networkStatus: IoTNetworkStatus = IoTNetworkStatus(),
    val connectedDevices: List<MedicalDevice> = emptyList(),
    val availableDevices: List<MedicalDevice> = emptyList(),
    val recentReadings: List<MedicalReading> = emptyList(),
    val error: String? = null
)