# 🏗️ Turkish Medical AI Platform - Project Lexicon

## 📋 NAMING SCHEMA & CONVENTIONS

**CRITICAL**: All new code MUST follow these exact naming conventions

## 🔧 CORE ARCHITECTURE

### **Main Files**:
- `MainActivity.kt` - 17-screen navigation hub
- `BoxOCRDatabase.kt` - Room database with visual entities
- `TurkishDrugDatabaseRepository.kt` - 16,632 Turkish drugs access
- `WindowsAutomationServer.kt` - Android HTTP server
- `windows_automation_client.py` - Python automation client

### **Database Entities**:
- `DrugEntity` - Turkish drug database records
- `SessionEntity` - Prescription session tracking
- `DrugBoxImageEntity` - Visual drug box images
- `DrugBoxFeatureEntity` - Visual feature vectors
- `VisualSimilarityMatchEntity` - Similarity match results
- `VisualCorrectionEntity` - User correction learning

### **Repository Pattern** (Suffix: `Repository`):
- `OCRRepository` - Gemini AI OCR processing
- `MultiDrugScannerRepository` - Main multi-drug orchestrator
- `MultiRegionOCRRepository` - Parallel OCR processing
- `VisualDrugDatabaseRepository` - Visual similarity matching
- `DamagedTextRecoveryRepository` - AI text reconstruction
- `BatchScanningRepository` - Batch processing workflow

## 🎯 MULTI-DRUG ENHANCEMENT CLASSES

### **Object Detection**:
- `MultiDrugObjectDetector` - ML Kit multiple drug box detection
- `DrugBoxRegion` - Detected drug box boundaries
- `MultiDrugDetectionState` - Real-time detection state

### **Visual Processing**:
- `AdvancedVisualFeatureExtractor` - 6-algorithm feature extraction
- `DrugBoxImageDatabaseManager` - Smart image processing
- `VisualFeatureType` enum - SIFT, COLOR_HISTOGRAM, TEXT_LAYOUT, EDGE_FEATURES, SHAPE_FEATURES, TEXTURE_FEATURES

### **Screen Components** (Suffix: `Screen`):
- `EnhancedCameraScreen` - Multi-mode camera interface
- `MultiDrugResultsScreen` - Comprehensive results display
- `DrugBoxImageDatabaseScreen` - Visual database management
- `VerificationScreen` - Drug verification workflow

### **ViewModels** (Suffix: `ViewModel`):
- `EnhancedCameraViewModel` - Advanced camera state
- `MultiDrugResultsViewModel` - Result management
- `DrugBoxImageDatabaseViewModel` - Database state management

## 🧠 AI & INTELLIGENCE CLASSES

### **AI Repositories**:
- `AdvancedAIModelsRepository` - Multi-provider AI integration
- `CustomAIIntegrationRepository` - Local & cloud AI models
- `IoTIntegrationRepository` - Medical device connectivity

### **AI Data Models**:
- `MedicalAnalysisResult` - AI medical analysis output
- `CustomAIConfiguration` - AI provider configuration
- `LocalAIModel` - Local AI model definition
- `PatientContext` - Medical consultation context

### **AI Enums**:
- `AIProviderType` - OPENAI, CLAUDE, GEMINI, GROK, DEEPSEEK, OPENROUTER, QWEN
- `UrgencyLevel` - LOW, MODERATE, HIGH, CRITICAL
- `EvidenceLevel` - WEAK, MODERATE, STRONG, VERY_STRONG

## 🔗 INTEGRATION COMPONENTS

### **Windows Automation**:
- `WindowsAutomationServer` - HTTP API server
- `AutomationSessionManager` - Session lifecycle management
- `PrescriptionAutomationHelper` - Smart automation utilities

### **Dependency Injection** (Suffix: `Module`):
- `MultiDrugModule` - Multi-drug scanner DI
- `ProductionFeaturesModule` - Production features DI
- `DatabaseModule` - Room database DI
- `WindowsAutomationModule` - Windows integration DI

## 📱 UI THEME & COMPONENTS

### **Theme Files**:
- `Color.kt` - Material 3 color scheme
- `Theme.kt` - Main theme configuration
- `Type.kt` - Typography definitions

### **UI Component Conventions**:
- `MultiDrugCameraComponents` - Reusable camera UI components
- `TabletLayoutSupport` - Adaptive layout components
- Turkish localization: `strings.xml` (652 strings)

## 🗄️ DATABASE SCHEMA

### **Room Database**:
- `BoxOCRDatabase` - Main database class (Version 2)
- `DatabaseMigrations` - Schema migration definitions

### **Visual Database Enums**:
- `DrugBoxCondition` - PERFECT, GOOD, WORN, DAMAGED, SEVERELY_DAMAGED
- `DrugBoxAngle` - FRONT, TOP, BOTTOM, LEFT, RIGHT, ANGLED
- `DrugBoxLighting` - NORMAL, OVEREXPOSED, UNDEREXPOSED, LOW_CONTRAST
- `ImageSource` - CAMERA_PHOTO, GALLERY_IMAGE, LIVE_VIDEO, BATCH_UPLOAD

### **Correction & Learning**:
- `CorrectionType` - TEXT_CORRECTION, VISUAL_MATCH_CORRECTION, FEATURE_WEIGHT_ADJUSTMENT
- `VisualFeatureType` - Feature extraction algorithm types

## 🎯 TURKISH MEDICAL DOMAIN

### **Medical Data Classes**:
- `TurkishDrug` - Turkish drug information
- `DrugInteractionAnalysis` - Drug interaction checking
- `TurkishMedicalKnowledge` - Medical knowledge base
- `MedicalConsensus` - AI consensus results

### **Turkish Integration**:
- `SGKDrugInfo` - Social Security drug information
- `MEDULAIntegration` - E-prescription system integration
- `TurkishMedicalAction` - Medical action classifications

## 🔧 TECHNICAL CONVENTIONS

### **File Naming**:
- **Activities**: `*Activity.kt`
- **Screens**: `*Screen.kt` 
- **ViewModels**: `*ViewModel.kt`
- **Repositories**: `*Repository.kt`
- **Entities**: `*Entity.kt`
- **Modules**: `*Module.kt`

### **Package Structure**:
- `ui/screens/` - All Compose screens
- `data/repository/` - All repository implementations
- `data/database/` - Room database components
- `data/models/` - Data classes and models
- `di/` - Dependency injection modules

### **Variable Naming**:
- `drugBoxRegions` - Detected drug box regions
- `multiDrugScanResult` - Multi-drug scan results
- `visualSimilarityScore` - Visual matching confidence
- `damagedTextRecovery` - Text recovery results
- `turkishDrugDatabase` - Turkish drug data access

## ⚠️ CRITICAL NAMING RULES

1. **NO DUPLICATES**: Always check lexicon before creating new classes
2. **CONSISTENT SUFFIXES**: Repository, Screen, ViewModel, Entity, Module
3. **TURKISH CONTEXT**: Prefix medical classes with "Turkish" when applicable
4. **MULTI-DRUG PREFIX**: Use "MultiDrug" for multi-drug scanner components
5. **VISUAL PREFIX**: Use "Visual" for visual processing components

---

**Status**: Complete naming schema for 27,500+ line Turkish Medical AI Platform  
**Usage**: MUST be consulted before creating any new code elements** 🇹🇷📋✅