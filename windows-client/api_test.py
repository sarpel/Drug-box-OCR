#!/usr/bin/env python3
"""
Simple API testing script for Android Box OCR HTTP server
Tests all available endpoints without automation
"""

import requests
import json
import sys
import time

def test_android_api(android_ip: str, port: int = 8080):
    """Test all Android HTTP server endpoints"""
    base_url = f"http://{android_ip}:{port}"
    
    print(f"🧪 Testing Android API at {base_url}")
    print("=" * 50)
    
    # Test 1: Server status
    print("\n1️⃣  Testing server status...")
    try:
        response = requests.get(f"{base_url}/status", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print("✅ Status endpoint working")
            print(f"📊 Response: {json.dumps(data, indent=2)}")
        else:
            print(f"❌ Status endpoint failed: {response.status_code}")
    except Exception as e:
        print(f"❌ Status endpoint error: {e}")
    
    # Test 2: Current prescription
    print("\n2️⃣  Testing current prescription...")
    try:
        response = requests.get(f"{base_url}/prescription/current", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print("✅ Current prescription endpoint working")
            print(f"📊 Response: {json.dumps(data, indent=2)}")
        else:
            print(f"❌ Current prescription failed: {response.status_code}")
            if response.status_code == 404:
                print("ℹ️  This is normal if no prescription session is active")
    except Exception as e:
        print(f"❌ Current prescription error: {e}")
    
    # Test 3: Pending drugs
    print("\n3️⃣  Testing pending drugs...")
    try:
        response = requests.get(f"{base_url}/prescription/drugs", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print("✅ Pending drugs endpoint working")
            print(f"📊 Response: {json.dumps(data, indent=2)}")
            
            # Show drugs in a nice format
            drugs = data.get('drugs', [])
            if drugs:
                print(f"\n💊 Found {len(drugs)} drugs:")
                for i, drug in enumerate(drugs, 1):
                    print(f"   {i}. {drug}")
            else:
                print("ℹ️  No drugs in current prescription")
        else:
            print(f"❌ Pending drugs failed: {response.status_code}")
    except Exception as e:
        print(f"❌ Pending drugs error: {e}")
    
    # Test 4: Start session (optional)
    print("\n4️⃣  Testing start session...")
    start_test = input("Do you want to test starting a new session? (y/N): ").strip().lower()
    if start_test == 'y':
        try:
            payload = {"patientInfo": "Test Patient 123"}
            response = requests.post(f"{base_url}/prescription/start", 
                                   json=payload, timeout=5)
            if response.status_code == 200:
                data = response.json()
                print("✅ Start session endpoint working")
                print(f"📊 Response: {json.dumps(data, indent=2)}")
            else:
                print(f"❌ Start session failed: {response.status_code}")
        except Exception as e:
            print(f"❌ Start session error: {e}")
    else:
        print("⏭️  Skipping start session test")
    
    # Test 5: Send prescription
    print("\n5️⃣  Testing send prescription...")
    try:
        response = requests.post(f"{base_url}/prescription/send", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print("✅ Send prescription endpoint working")
            print(f"📊 Response: {json.dumps(data, indent=2)}")
        else:
            print(f"❌ Send prescription failed: {response.status_code}")
            if response.status_code == 400:
                print("ℹ️  This is normal if no drugs are in the prescription")
    except Exception as e:
        print(f"❌ Send prescription error: {e}")
    
    print("\n" + "=" * 50)
    print("🧪 API testing completed")
    print("📱 Check the Android app for any session changes")

def main():
    if len(sys.argv) < 2:
        print("Usage: python api_test.py <android_ip> [port]")
        print("Example: python api_test.py 192.168.1.100")
        print("Example: python api_test.py 192.168.1.100 8081")
        sys.exit(1)
    
    android_ip = sys.argv[1]
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8080
    
    test_android_api(android_ip, port)

if __name__ == "__main__":
    main()
