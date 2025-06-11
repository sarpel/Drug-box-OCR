"""
Configuration settings for Windows Automation Client
Adjust these values based on your system and prescription software
"""

# Android Server Configuration
ANDROID_IP = "192.168.1.100"  # Replace with your Android device IP
ANDROID_PORT = 8080           # Default port (or 8081 if busy)

# Timing Configuration (in seconds)
# Adjust these based on your prescription software response times
TIMING = {
    "paste_delay": 1.0,         # Delay between drug entries
    "field_focus_delay": 0.5,   # Delay for field activation  
    "f4_delay": 2.0,           # Delay before pressing F4
    "browser_delay": 3.0,      # Delay before opening browser
    "countdown_delay": 5,      # Countdown before starting automation
}

# E-Signature Configuration
ESIGN_CONFIG = {
    "default_url": "",                           # Default e-signature URL (if any)
    "health_department_url": "",                 # Health department portal URL
    "browser_path": "",                          # Custom browser path (optional)
}

# Prescription Software Configuration
PRESCRIPTION_SOFTWARE = {
    "name": "Default",                           # Name of your prescription software
    "field_separator": "enter",                  # Key to press between fields ("enter", "tab")
    "submit_key": "f4",                         # Key to submit prescription ("f4", "ctrl+s", etc.)
    "window_title": "",                         # Window title to focus (optional)
}

# Safety Configuration
SAFETY = {
    "failsafe_enabled": True,                   # Enable pyautogui failsafe
    "pause_between_actions": 0.1,              # Small pause between GUI actions
    "max_drugs_per_session": 50,               # Safety limit for drug count
    "confirmation_required": False,             # Require confirmation before each step
}

# Network Configuration
NETWORK = {
    "connection_timeout": 5,                    # HTTP request timeout
    "retry_attempts": 3,                        # Number of retry attempts
    "retry_delay": 1,                          # Delay between retries
}

# Logging Configuration
LOGGING = {
    "log_level": "INFO",                       # DEBUG, INFO, WARNING, ERROR
    "log_to_file": False,                      # Save logs to file
    "log_file_path": "automation.log",         # Log file location
}

# Development/Testing Configuration
DEVELOPMENT = {
    "dry_run_mode": False,                     # Test mode without actual automation
    "simulate_delays": True,                   # Use actual delays in test mode
    "verbose_output": True,                    # Detailed console output
}

# Quick Configuration Presets
PRESETS = {
    "fast": {
        "paste_delay": 0.5,
        "field_focus_delay": 0.2,
        "f4_delay": 1.0,
        "browser_delay": 2.0,
    },
    "slow": {
        "paste_delay": 2.0,
        "field_focus_delay": 1.0,
        "f4_delay": 3.0,
        "browser_delay": 5.0,
    },
    "debug": {
        "paste_delay": 3.0,
        "field_focus_delay": 2.0,
        "f4_delay": 5.0,
        "browser_delay": 3.0,
    }
}

def get_preset_config(preset_name: str) -> dict:
    """Get timing configuration for a specific preset"""
    return PRESETS.get(preset_name, TIMING)

def get_android_url() -> str:
    """Get complete Android server URL"""
    return f"http://{ANDROID_IP}:{ANDROID_PORT}"

# Usage Examples:
# 
# from config import ANDROID_IP, TIMING, get_android_url
# 
# client = WindowsAutomationClient(ANDROID_IP)
# client.paste_delay = TIMING["paste_delay"]
# 
# server_url = get_android_url()
