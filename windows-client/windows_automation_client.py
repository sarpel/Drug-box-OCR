#!/usr/bin/env python3
"""
Windows Automation Client for Box OCR Android App
Handles the complete prescription workflow automation
"""

import requests
import pyautogui
import time
import json
import webbrowser
import sys
from typing import List, Dict, Optional

class WindowsAutomationClient:
    def __init__(self, android_ip: str, android_port: int = 8080):
        """
        Initialize Windows automation client
        
        Args:
            android_ip: IP address of Android device
            android_port: Port number of Android HTTP server (default: 8080)
        """
        self.android_ip = android_ip
        self.android_port = android_port
        self.base_url = f"http://{android_ip}:{android_port}"
        
        # Timing configuration (adjust for your system)
        self.paste_delay = 1.0          # Delay between drug entries
        self.field_focus_delay = 0.5    # Delay for field activation
        self.f4_delay = 2.0            # Delay before pressing F4
        self.browser_delay = 3.0       # Delay before opening browser
        
        # Safety settings
        pyautogui.FAILSAFE = True      # Move mouse to corner to stop
        pyautogui.PAUSE = 0.1         # Small pause between actions
        
        print(f"🤖 Windows Automation Client initialized")
        print(f"📱 Android server: {self.base_url}")
        print(f"⚠️  FAILSAFE: Move mouse to top-left corner to stop automation")
    
    def test_connection(self) -> bool:
        """Test connection to Android server"""
        try:
            print("🔍 Testing connection to Android server...")
            response = requests.get(f"{self.base_url}/status", timeout=5)
            
            if response.status_code == 200:
                data = response.json()
                print(f"✅ Connected to Android server")
                print(f"📊 Server status: {data.get('server', 'unknown')}")
                
                if 'session' in data and data['session']:
                    session = data['session']
                    print(f"💊 Active session: {session.get('sessionId', 'unknown')}")
                    print(f"🏥 Patient: {session.get('patientInfo', 'N/A')}")
                    print(f"💉 Drugs: {session.get('drugCount', 0)}")
                else:
                    print("⚠️  No active prescription session")
                
                return True
            else:
                print(f"❌ Server responded with status: {response.status_code}")
                return False
                
        except requests.exceptions.ConnectionError:
            print(f"❌ Cannot connect to Android server at {self.base_url}")
            print("📱 Make sure Android app is running with batch scanning mode active")
            return False
        except Exception as e:
            print(f"❌ Connection error: {e}")
            return False
    
    def get_prescription_drugs(self) -> Optional[List[str]]:
        """Get the current prescription drugs from Android"""
        try:
            print("📋 Fetching prescription drugs from Android...")
            response = requests.get(f"{self.base_url}/prescription/drugs", timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                drugs = data.get('drugs', [])
                count = data.get('count', 0)
                
                print(f"💊 Retrieved {count} drugs:")
                for i, drug in enumerate(drugs, 1):
                    print(f"   {i}. {drug}")
                
                return drugs
            else:
                print(f"❌ Failed to get drugs: {response.status_code}")
                return None
                
        except Exception as e:
            print(f"❌ Error fetching drugs: {e}")
            return None
    
    def paste_drugs_to_application(self, drugs: List[str]) -> bool:
        """
        Paste drugs into prescription application
        
        Args:
            drugs: List of drug names to paste
            
        Returns:
            True if successful, False otherwise
        """
        if not drugs:
            print("⚠️  No drugs to paste")
            return False
        
        print(f"\n🖥️  Starting drug entry automation...")
        print(f"📝 Will paste {len(drugs)} drugs into prescription application")
        print(f"⏰ You have 5 seconds to focus the prescription application...")
        
        # Countdown for user to focus application
        for i in range(5, 0, -1):
            print(f"   {i}...")
            time.sleep(1)
        
        print("🚀 Starting automation now!")
        
        try:
            for i, drug in enumerate(drugs, 1):
                print(f"📝 Entering drug {i}/{len(drugs)}: {drug}")
                
                # Wait for field to be ready
                time.sleep(self.field_focus_delay)
                
                # Type the drug name
                pyautogui.typewrite(drug)
                
                # Press Enter to move to next field
                pyautogui.press('enter')
                
                # Delay before next drug
                time.sleep(self.paste_delay)
            
            print(f"✅ Successfully entered all {len(drugs)} drugs")
            return True
            
        except pyautogui.FailSafeException:
            print("🛑 Automation stopped by failsafe (mouse moved to corner)")
            return False
        except Exception as e:
            print(f"❌ Error during drug entry: {e}")
            return False
    
    def send_prescription_to_health_department(self) -> bool:
        """Press F4 to send prescription to health department"""
        try:
            print(f"\n📨 Sending prescription to health department...")
            print(f"⏰ Waiting {self.f4_delay} seconds before pressing F4...")
            
            time.sleep(self.f4_delay)
            
            print("⌨️  Pressing F4...")
            pyautogui.press('f4')
            
            print("✅ F4 pressed - prescription sent to health department")
            return True
            
        except pyautogui.FailSafeException:
            print("🛑 Automation stopped by failsafe")
            return False
        except Exception as e:
            print(f"❌ Error pressing F4: {e}")
            return False
    
    def open_browser_for_esignature(self, esign_url: str = None) -> bool:
        """Open web browser for e-signature"""
        try:
            print(f"\n🌐 Opening browser for e-signature...")
            print(f"⏰ Waiting {self.browser_delay} seconds...")
            
            time.sleep(self.browser_delay)
            
            if esign_url:
                print(f"🔗 Opening specific URL: {esign_url}")
                webbrowser.open(esign_url)
            else:
                print("🔗 Opening default browser (user will navigate to e-signature)")
                webbrowser.open('about:blank')
            
            print("✅ Browser opened for e-signature")
            print("👆 Please complete the e-signature process manually")
            return True
            
        except Exception as e:
            print(f"❌ Error opening browser: {e}")
            return False
    
    def complete_prescription_session(self) -> bool:
        """Mark prescription session as complete on Android"""
        try:
            print("📱 Completing prescription session on Android...")
            response = requests.post(f"{self.base_url}/prescription/complete", timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                print(f"✅ Prescription session completed")
                print(f"📊 {data.get('message', 'Session completed')}")
                return True
            else:
                print(f"⚠️  Warning: Could not complete session on Android: {response.status_code}")
                return False
                
        except Exception as e:
            print(f"⚠️  Warning: Error completing session on Android: {e}")
            return False
    
    def run_complete_workflow(self, esign_url: str = None) -> bool:
        """
        Run the complete prescription automation workflow
        
        Args:
            esign_url: Optional URL for e-signature portal
            
        Returns:
            True if workflow completed successfully
        """
        print("🏁 Starting complete prescription automation workflow")
        print("=" * 60)
        
        # Step 1: Test connection
        if not self.test_connection():
            print("❌ Workflow failed: Cannot connect to Android")
            return False
        
        # Step 2: Get prescription drugs
        drugs = self.get_prescription_drugs()
        if not drugs:
            print("❌ Workflow failed: No drugs to process")
            return False
        
        # Step 3: Paste drugs into application
        if not self.paste_drugs_to_application(drugs):
            print("❌ Workflow failed: Could not enter drugs")
            return False
        
        # Step 4: Send to health department (F4)
        if not self.send_prescription_to_health_department():
            print("❌ Workflow failed: Could not send prescription")
            return False
        
        # Step 5: Open browser for e-signature
        if not self.open_browser_for_esignature(esign_url):
            print("⚠️  Warning: Could not open browser, but continuing...")
        
        # Step 6: Complete session on Android
        self.complete_prescription_session()
        
        print("=" * 60)
        print("🎉 Prescription automation workflow completed successfully!")
        print("👆 Please complete the e-signature process in your browser")
        print("🖨️  After e-signing, prescription barcode will be ready for thermal printing")
        
        return True

def main():
    """Main function - command line interface"""
    print("🏥 Box OCR Windows Automation Client")
    print("=" * 50)
    
    if len(sys.argv) < 2:
        print("Usage: python windows_automation_client.py <android_ip> [esign_url]")
        print("Example: python windows_automation_client.py 192.168.1.100")
        print("Example: python windows_automation_client.py 192.168.1.100 https://esign.health.gov")
        sys.exit(1)
    
    android_ip = sys.argv[1]
    esign_url = sys.argv[2] if len(sys.argv) > 2 else None
    
    # Create automation client
    client = WindowsAutomationClient(android_ip)
    
    # Interactive menu
    while True:
        print("\n📋 Available Actions:")
        print("1. Test connection to Android")
        print("2. Get prescription drugs")
        print("3. Run complete workflow")
        print("4. Manual drug entry only")
        print("5. Send to health department (F4)")
        print("6. Open browser for e-signature")
        print("0. Exit")
        
        choice = input("\nSelect action (0-6): ").strip()
        
        if choice == '0':
            print("👋 Goodbye!")
            break
        elif choice == '1':
            client.test_connection()
        elif choice == '2':
            client.get_prescription_drugs()
        elif choice == '3':
            client.run_complete_workflow(esign_url)
        elif choice == '4':
            drugs = client.get_prescription_drugs()
            if drugs:
                client.paste_drugs_to_application(drugs)
        elif choice == '5':
            client.send_prescription_to_health_department()
        elif choice == '6':
            client.open_browser_for_esignature(esign_url)
        else:
            print("❌ Invalid choice")

if __name__ == "__main__":
    main()
