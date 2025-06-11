package com.boxocr.simple.automation

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP Server running on Android to communicate with Windows application
 * Provides REST API for prescription automation workflow
 */
@Singleton
class WindowsAutomationServer @Inject constructor() {
    
    companion object {
        private const val TAG = "WindowsAutomationServer"
        private const val DEFAULT_PORT = 8080
        private const val BACKUP_PORT = 8081
    }
    
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var serverJob: Job? = null
    private var currentPort = DEFAULT_PORT
    
    // Server status for UI
    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Stopped)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()
    
    // Current prescription session
    private val _currentSession = MutableStateFlow<PrescriptionSession?>(null)
    val currentSession: StateFlow<PrescriptionSession?> = _currentSession.asStateFlow()
    
    // Pending drugs to be sent to Windows
    private val _pendingDrugs = MutableStateFlow<List<String>>(emptyList())
    val pendingDrugs: StateFlow<List<String>> = _pendingDrugs.asStateFlow()
    
    sealed class ServerStatus {
        object Stopped : ServerStatus()
        data class Starting(val port: Int) : ServerStatus()
        data class Running(val port: Int, val ipAddress: String) : ServerStatus()
        data class Error(val message: String) : ServerStatus()
    }
    
    /**
     * Start the HTTP server on available port
     */
    suspend fun startServer(): Boolean = withContext(Dispatchers.IO) {
        if (isRunning) {
            Log.w(TAG, "Server already running")
            return@withContext true
        }
        
        try {
            _serverStatus.value = ServerStatus.Starting(currentPort)
            
            // Try default port first, then backup
            serverSocket = try {
                ServerSocket(DEFAULT_PORT).also { currentPort = DEFAULT_PORT }
            } catch (e: IOException) {
                Log.w(TAG, "Port $DEFAULT_PORT busy, trying $BACKUP_PORT")
                try {
                    ServerSocket(BACKUP_PORT).also { currentPort = BACKUP_PORT }
                } catch (e2: IOException) {
                    throw IOException("Both ports $DEFAULT_PORT and $BACKUP_PORT are busy")
                }
            }
            
            isRunning = true
            val localIp = getLocalIPAddress()
            _serverStatus.value = ServerStatus.Running(currentPort, localIp)
            
            Log.i(TAG, "Server started on $localIp:$currentPort")
            
            // Start handling requests
            serverJob = CoroutineScope(Dispatchers.IO).launch {
                handleRequests()
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server", e)
            _serverStatus.value = ServerStatus.Error("Failed to start: ${e.message}")
            false
        }
    }
    
    /**
     * Stop the HTTP server
     */
    fun stopServer() {
        try {
            isRunning = false
            serverJob?.cancel()
            serverSocket?.close()
            serverSocket = null
            _serverStatus.value = ServerStatus.Stopped
            Log.i(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }
    
    /**
     * Add drug to pending prescription
     */
    fun addDrugToPrescription(drugName: String) {
        val current = _pendingDrugs.value.toMutableList()
        current.add(drugName)
        _pendingDrugs.value = current
        Log.d(TAG, "Added drug to prescription: $drugName (total: ${current.size})")
    }
    
    /**
     * Start new prescription session
     */
    fun startPrescriptionSession(patientInfo: String = ""): String {
        val sessionId = System.currentTimeMillis().toString()
        val session = PrescriptionSession(
            sessionId = sessionId,
            patientInfo = patientInfo,
            startTime = System.currentTimeMillis(),
            drugs = mutableListOf()
        )
        _currentSession.value = session
        _pendingDrugs.value = emptyList()
        Log.i(TAG, "Started prescription session: $sessionId")
        return sessionId
    }
    
    /**
     * Complete prescription session and return summary
     */
    fun completePrescriptionSession(): PrescriptionSession? {
        val session = _currentSession.value
        session?.let {
            it.endTime = System.currentTimeMillis()
            it.drugs.addAll(_pendingDrugs.value)
            _currentSession.value = null
            _pendingDrugs.value = emptyList()
            Log.i(TAG, "Completed prescription session: ${it.sessionId} with ${it.drugs.size} drugs")
        }
        return session
    }
    
    /**
     * Handle incoming HTTP requests
     */
    private suspend fun handleRequests() {
        while (isRunning && serverSocket != null) {
            try {
                val clientSocket = serverSocket?.accept() ?: continue
                
                // Handle request in separate coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    handleClientRequest(clientSocket)
                }
                
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(TAG, "Error accepting connection", e)
                }
            }
        }
    }
    
    /**
     * Handle individual client request
     */
    private suspend fun handleClientRequest(clientSocket: Socket) {
        try {
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val output = PrintWriter(clientSocket.getOutputStream(), true)
            
            // Read request line
            val requestLine = input.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return
            
            val method = parts[0]
            val path = parts[1]
            
            Log.d(TAG, "Request: $method $path")
            
            // Route request
            val response = when {
                method == "GET" && path == "/status" -> handleGetStatus()
                method == "GET" && path == "/prescription/current" -> handleGetCurrentPrescription()
                method == "GET" && path == "/prescription/drugs" -> handleGetPendingDrugs()
                method == "POST" && path == "/prescription/start" -> handleStartPrescription(input)
                method == "POST" && path == "/prescription/complete" -> handleCompletePrescription()
                method == "POST" && path == "/prescription/send" -> handleSendPrescription()
                method == "DELETE" && path == "/prescription/clear" -> handleClearPrescription()
                else -> createErrorResponse(404, "Not Found")
            }
            
            // Send response
            output.println(response)
            output.flush()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling request", e)
        } finally {
            try {
                clientSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing client socket", e)
            }
        }
    }
    
    /**
     * Handle GET /status - Server and session status
     */
    private fun handleGetStatus(): String {
        val json = JSONObject().apply {
            put("server", "running")
            put("port", currentPort)
            put("timestamp", System.currentTimeMillis())
            
            val session = _currentSession.value
            if (session != null) {
                put("session", JSONObject().apply {
                    put("sessionId", session.sessionId)
                    put("patientInfo", session.patientInfo)
                    put("startTime", session.startTime)
                    put("drugCount", _pendingDrugs.value.size)
                })
            } else {
                put("session", null)
            }
        }
        
        return createSuccessResponse(json.toString())
    }
    
    /**
     * Handle GET /prescription/current - Current prescription session
     */
    private fun handleGetCurrentPrescription(): String {
        val session = _currentSession.value
        return if (session != null) {
            val json = JSONObject().apply {
                put("sessionId", session.sessionId)
                put("patientInfo", session.patientInfo)
                put("startTime", session.startTime)
                put("drugs", JSONArray(_pendingDrugs.value))
                put("drugCount", _pendingDrugs.value.size)
            }
            createSuccessResponse(json.toString())
        } else {
            createErrorResponse(404, "No active prescription session")
        }
    }
    
    /**
     * Handle GET /prescription/drugs - Get pending drugs list
     */
    private fun handleGetPendingDrugs(): String {
        val json = JSONObject().apply {
            put("drugs", JSONArray(_pendingDrugs.value))
            put("count", _pendingDrugs.value.size)
        }
        return createSuccessResponse(json.toString())
    }
    
    /**
     * Handle POST /prescription/start - Start new prescription
     */
    private fun handleStartPrescription(input: BufferedReader): String {
        // Read request body for patient info (optional)
        val patientInfo = try {
            val bodyLine = input.readLine() ?: ""
            if (bodyLine.startsWith("{")) {
                val json = JSONObject(bodyLine)
                json.optString("patientInfo", "")
            } else ""
        } catch (e: Exception) {
            ""
        }
        
        val sessionId = startPrescriptionSession(patientInfo)
        val json = JSONObject().apply {
            put("sessionId", sessionId)
            put("message", "Prescription session started")
        }
        return createSuccessResponse(json.toString())
    }
    
    /**
     * Handle POST /prescription/complete - Complete prescription session
     */
    private fun handleCompletePrescription(): String {
        val session = completePrescriptionSession()
        return if (session != null) {
            val json = JSONObject().apply {
                put("sessionId", session.sessionId)
                put("drugCount", session.drugs.size)
                put("duration", session.endTime!! - session.startTime)
                put("message", "Prescription completed successfully")
            }
            createSuccessResponse(json.toString())
        } else {
            createErrorResponse(400, "No active prescription session")
        }
    }
    
    /**
     * Handle POST /prescription/send - Send drugs to Windows for automation
     */
    private fun handleSendPrescription(): String {
        val drugs = _pendingDrugs.value
        return if (drugs.isNotEmpty()) {
            val json = JSONObject().apply {
                put("drugs", JSONArray(drugs))
                put("count", drugs.size)
                put("message", "Drugs ready for Windows automation")
                put("action", "PASTE_AND_ENTER") // Instruction for Windows app
            }
            createSuccessResponse(json.toString())
        } else {
            createErrorResponse(400, "No drugs to send")
        }
    }
    
    /**
     * Handle DELETE /prescription/clear - Clear current prescription
     */
    private fun handleClearPrescription(): String {
        _currentSession.value = null
        _pendingDrugs.value = emptyList()
        val json = JSONObject().apply {
            put("message", "Prescription cleared")
        }
        return createSuccessResponse(json.toString())
    }
    
    /**
     * Create HTTP success response
     */
    private fun createSuccessResponse(body: String): String {
        return """
            HTTP/1.1 200 OK
            Content-Type: application/json
            Content-Length: ${body.length}
            Access-Control-Allow-Origin: *
            
            $body
        """.trimIndent()
    }
    
    /**
     * Create HTTP error response
     */
    private fun createErrorResponse(code: Int, message: String): String {
        val body = JSONObject().apply {
            put("error", message)
            put("code", code)
        }.toString()
        
        return """
            HTTP/1.1 $code $message
            Content-Type: application/json
            Content-Length: ${body.length}
            Access-Control-Allow-Origin: *
            
            $body
        """.trimIndent()
    }
    
    /**
     * Get local IP address for the server
     */
    private fun getLocalIPAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress ?: "localhost"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
        }
        return "localhost"
    }
}
