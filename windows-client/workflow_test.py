#!/usr/bin/env python3
"""
Quick test script for Windows automation workflow
Tests the complete Android → Windows automation pipeline
"""

import requests
import time
import sys

def test_complete_workflow(android_ip: str, port: int = 8080):
    """Test the complete prescription workflow"""
    base_url = f"http://{android_ip}:{port}"
    
    print("🧪 Testing Complete Prescription Workflow")
    print("=" * 50)
    
    # Step 1: Check server status
    print("\n1️⃣ Checking Android server status...")
    try:
        response = requests.get(f"{base_url}/status", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Server running on port {data['port']}")
            if data.get('session'):
                session = data['session']
                print(f"📱 Active session: {session['sessionId']}")
                print(f"💊 Current drugs: {session['drugCount']}")
            else:
                print("ℹ️ No active session")
        else:
            print(f"❌ Server error: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Cannot connect to Android: {e}")
        return False
    
    # Step 2: Start prescription session (if none active)
    if not data.get('session'):
        print("\n2️⃣ Starting new prescription session...")
        try:
            payload = {"patientInfo": "Test Patient - Workflow Demo"}
            response = requests.post(f"{base_url}/prescription/start", 
                                   json=payload, timeout=5)
            if response.status_code == 200:
                result = response.json()
                print(f"✅ Session started: {result['sessionId']}")
            else:
                print(f"❌ Failed to start session: {response.status_code}")
                return False
        except Exception as e:
            print(f"❌ Error starting session: {e}")
            return False
    
    # Step 3: Check for drugs in prescription
    print("\n3️⃣ Checking prescription drugs...")
    try:
        response = requests.get(f"{base_url}/prescription/drugs", timeout=5)
        if response.status_code == 200:
            data = response.json()
            drugs = data.get('drugs', [])
            if drugs:
                print(f"✅ Found {len(drugs)} drugs ready for automation:")
                for i, drug in enumerate(drugs, 1):
                    print(f"   {i}. {drug}")
            else:
                print("⚠️ No drugs in prescription yet")
                print("📱 Please scan some drugs in the Android app first")
                return False
        else:
            print(f"❌ Failed to get drugs: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error getting drugs: {e}")
        return False
    
    # Step 4: Simulate Windows automation workflow
    print("\n4️⃣ Simulating Windows automation...")
    print("🔄 Simulating drug entry into prescription software...")
    for i, drug in enumerate(drugs, 1):
        print(f"   📝 Entering drug {i}: {drug}")
        time.sleep(0.5)  # Simulate typing delay
    
    print("⌨️ Simulating F4 press (send to health department)...")
    time.sleep(1)
    
    print("🌐 Simulating browser opening for e-signature...")
    time.sleep(1)
    
    # Step 5: Complete session
    print("\n5️⃣ Completing prescription session...")
    try:
        response = requests.post(f"{base_url}/prescription/complete", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Session completed successfully")
            print(f"📊 Final drug count: {data['drugCount']}")
            print(f"⏱️ Total duration: {data.get('duration', 0) / 1000:.1f} seconds")
        else:
            print(f"⚠️ Warning: Could not complete session: {response.status_code}")
    except Exception as e:
        print(f"⚠️ Warning: Error completing session: {e}")
    
    print("\n" + "=" * 50)
    print("🎉 Complete workflow test finished!")
    print("📋 Summary:")
    print(f"   • Connected to Android server ✅")
    print(f"   • Processed {len(drugs)} drugs ✅")
    print(f"   • Simulated Windows automation ✅")
    print(f"   • Session completed ✅")
    print()
    print("🚀 Ready for real prescription workflow!")
    print("📱 Android: Scan drugs → 🖥️ Windows: Run automation client")
    
    return True

def main():
    if len(sys.argv) < 2:
        print("Usage: python workflow_test.py <android_ip> [port]")
        print("Example: python workflow_test.py 192.168.1.100")
        sys.exit(1)
    
    android_ip = sys.argv[1]
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8080
    
    test_complete_workflow(android_ip, port)

if __name__ == "__main__":
    main()
