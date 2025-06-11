# Windows Automation Client for Box OCR Android App

This directory contains the Windows automation client that communicates with the Android Box OCR app to complete the prescription workflow.

## 🔄 COMPLETE PRESCRIPTION WORKFLOW

### **Android → Windows → E-Signature Process**

1. **Android App**: Scan drug boxes in batch mode
2. **Android App**: Send drug list to Windows automation client
3. **Windows Client**: Receive drugs from Android HTTP server
4. **Windows Client**: Paste drugs one by one into prescription application
5. **Windows Client**: Press F4 to send prescription to health department
6. **Windows Client**: Open web browser for e-signature
7. **Manual**: User completes e-signature process
8. **Future**: Thermal printer integration for prescription barcodes

## 📡 ANDROID HTTP SERVER API

The Android app runs an HTTP server on port **8080** (or 8081 if busy) that provides the following endpoints:

### **Server Status**
```http
GET http://[ANDROID_IP]:8080/status
```
Response:
```json
{
  "server": "running",
  "port": 8080,
  "timestamp": 1640995200000,
  "session": {
    "sessionId": "1640995200000",
    "patientInfo": "Patient123", 
    "startTime": 1640995100000,
    "drugCount": 3
  }
}
```

### **Get Current Prescription**
```http
GET http://[ANDROID_IP]:8080/prescription/current
```
Response:
```json
{
  "sessionId": "1640995200000",
  "patientInfo": "Patient123",
  "startTime": 1640995100000,
  "drugs": ["Aspirin 100mg", "Metformin 500mg", "Lisinopril 10mg"],
  "drugCount": 3
}
```

### **Get Pending Drugs for Automation**
```http
GET http://[ANDROID_IP]:8080/prescription/drugs
```
Response:
```json
{
  "drugs": ["Aspirin 100mg", "Metformin 500mg", "Lisinopril 10mg"],
  "count": 3
}
```

### **Start New Prescription Session**
```http
POST http://[ANDROID_IP]:8080/prescription/start
Content-Type: application/json

{
  "patientInfo": "Patient123"
}
```

### **Complete Prescription Session**
```http
POST http://[ANDROID_IP]:8080/prescription/complete
```

### **Send Drugs for Windows Automation**
```http
POST http://[ANDROID_IP]:8080/prescription/send
```
Response:
```json
{
  "drugs": ["Aspirin 100mg", "Metformin 500mg", "Lisinopril 10mg"],
  "count": 3,
  "message": "Drugs ready for Windows automation",
  "action": "PASTE_AND_ENTER"
}
```

### **Clear Current Prescription**
```http
DELETE http://[ANDROID_IP]:8080/prescription/clear
```

## 🖥️ WINDOWS CLIENT REQUIREMENTS

### **Dependencies**
- Python 3.8+
- `requests` library for HTTP communication
- `pyautogui` library for keyboard/mouse automation
- `time` library for delays between actions

### **Installation**
```bash
pip install requests pyautogui
```

## 🚀 USAGE INSTRUCTIONS

### **Step 1: Connect Android and Windows**
1. Connect Android device to same WiFi network as Windows PC
2. Start batch scanning mode in Android app
3. Note the server IP:port displayed in Android app (e.g., 192.168.1.100:8080)
4. Configure Windows client with Android IP address

### **Step 2: Windows Automation Setup**
1. Open your prescription software on Windows
2. Focus the drug input field
3. Run the Windows automation client
4. Client will automatically fetch drugs from Android and paste them

### **Step 3: Complete Prescription**
1. Client will press F4 to send prescription to health department
2. Client will open web browser for e-signature
3. Complete e-signature manually
4. Print prescription barcode (future feature)

## 🔧 CONFIGURATION

### **Android IP Discovery**
The Android app displays the server IP and port in the batch scanning screen status bar:
- **Status**: "Server: 192.168.1.100:8080"
- Use this IP address in the Windows client configuration

### **Timing Configuration**
Adjust delays in Windows client based on your prescription software:
- **Paste delay**: Time between drug entries
- **Field focus delay**: Time to wait for field activation
- **F4 delay**: Time to wait before pressing F4
- **Browser delay**: Time to wait before opening browser

## 📋 EXAMPLE WORKFLOW

```
1. Android: Start prescription session
   → Server starts on 192.168.1.100:8080

2. Android: Scan Aspirin box
   → Added to prescription queue

3. Android: Scan Metformin box  
   → Added to prescription queue

4. Android: Scan Lisinopril box
   → Added to prescription queue

5. Windows: Run automation client
   → GET /prescription/drugs
   → Returns: ["Aspirin 100mg", "Metformin 500mg", "Lisinopril 10mg"]

6. Windows: Paste each drug + Enter
   → Aspirin 100mg [Enter]
   → Metformin 500mg [Enter] 
   → Lisinopril 10mg [Enter]

7. Windows: Press F4
   → Send prescription to health department

8. Windows: Open browser for e-signature
   → User completes e-signature manually

9. Android: Complete session
   → POST /prescription/complete
```

## 🛠️ TROUBLESHOOTING

### **Connection Issues**
- Ensure Android and Windows are on same WiFi network
- Check Windows firewall settings
- Verify Android app shows "Server Running" status
- Try alternative port 8081 if 8080 is busy

### **Automation Issues**
- Ensure prescription software is focused and ready
- Adjust timing delays for slower systems
- Check that F4 key mapping is correct for your software
- Verify browser opens correctly for e-signature

### **Performance Tips**
- Use USB tethering for more reliable connection
- Close unnecessary applications during automation
- Test with small prescription batches first
- Keep Android device plugged in during long sessions

## 📁 FILES IN THIS DIRECTORY

- `windows_automation_client.py` - Main Windows automation script
- `api_test.py` - Simple API testing script
- `config.py` - Configuration settings
- `README.md` - This documentation
