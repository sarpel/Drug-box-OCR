package com.boxocr.simple.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Turkish Medical Database API Integration
 * Integrates with Turkish drug databases: ilacabak.com and ilacrehberi.com
 * Provides comprehensive drug information with Turkish medical terminology
 */

// API Data Models for Turkish Medical Databases
@Serializable
data class TurkishDrugApiResponse(
    val success: Boolean,
    val data: List<TurkishDrugDetail>? = null,
    val message: String? = null,
    val totalCount: Int = 0
)

@Serializable
data class TurkishDrugDetail(
    val id: String,
    val name: String, // İlaç adı
    val activeIngredient: String? = null, // Etken madde
    val manufacturer: String? = null, // Üretici firma
    val atcCode: String? = null, // ATC kodu
    val atcName: String? = null, // ATC adı
    val barcode: String? = null, // Barkod
    val price: TurkishDrugPrice? = null, // Fiyat bilgisi
    val packageInfo: TurkishDrugPackage? = null, // Ambalaj bilgisi
    val prescriptionType: String? = null, // Reçete türü
    val sgkStatus: TurkishSGKStatus? = null, // SGK durumu
    val prospectus: TurkishProspectus? = null, // Prospektüs
    val equivalents: List<String>? = null, // Eşdeğer ilaçlar
    val interactions: List<TurkishDrugInteraction>? = null, // İlaç etkileşimleri
    val sideEffects: List<String>? = null, // Yan etkiler
    val contraindications: List<String>? = null, // Kontrendikasyonlar
    val dosageInfo: TurkishDosageInfo? = null // Doz bilgisi
)

@Serializable
data class TurkishDrugPrice(
    val depot: Double? = null, // Depo fiyatı
    val pharmacy: Double? = null, // Eczane fiyatı
    val patient: Double? = null, // Hasta payı
    val currency: String = "TRY", // Para birimi
    val lastUpdated: String? = null // Son güncelleme tarihi
)

@Serializable
data class TurkishDrugPackage(
    val size: String? = null, // Ambalaj büyüklüğü
    val unit: String? = null, // Birim
    val form: String? = null, // Farmasötik form (tablet, kapsül, vs.)
    val strength: String? = null // Güç/konsantrasyon
)

@Serializable
data class TurkishSGKStatus(
    val isActive: Boolean = false, // SGK kapsamında mı
    val reimbursement: Double? = null, // Geri ödeme oranı
    val patientContribution: Double? = null, // Hasta katkı payı
    val specialConditions: List<String>? = null // Özel şartlar
)

@Serializable
data class TurkishProspectus(
    val url: String? = null, // Prospektüs linki
    val summary: String? = null, // Özet
    val indications: List<String>? = null, // Endikasyonlar
    val warnings: List<String>? = null, // Uyarılar
    val storage: String? = null // Saklama koşulları
)

@Serializable
data class TurkishDrugInteraction(
    val drugName: String, // Etkileşim yapan ilaç
    val severity: String, // Ciddiyet (hafif, orta, şiddetli)
    val description: String? = null, // Açıklama
    val recommendation: String? = null // Öneri
)

@Serializable
data class TurkishDosageInfo(
    val adult: String? = null, // Yetişkin dozu
    val pediatric: String? = null, // Çocuk dozu
    val elderly: String? = null, // Yaşlı dozu
    val frequency: String? = null, // Kullanım sıklığı
    val duration: String? = null, // Kullanım süresi
    val specialInstructions: String? = null // Özel talimatlar
)

// Search request models
@Serializable
data class TurkishDrugSearchRequest(
    val query: String,
    val searchType: String = "name", // name, ingredient, atc, barcode
    val limit: Int = 50,
    val offset: Int = 0,
    val includeInactive: Boolean = false,
    val includePrices: Boolean = true,
    val includeProspectus: Boolean = true
)

// API Service interfaces
interface IlacabakApiService {
    @POST("api/v1/drugs/search")
    suspend fun searchDrugs(
        @Body request: TurkishDrugSearchRequest
    ): Response<TurkishDrugApiResponse>
    
    @GET("api/v1/drugs/{id}")
    suspend fun getDrugById(
        @Path("id") drugId: String,
        @Query("include_prospectus") includeProspectus: Boolean = true,
        @Query("include_interactions") includeInteractions: Boolean = true
    ): Response<TurkishDrugDetail>
    
    @GET("api/v1/drugs/barcode/{barcode}")
    suspend fun getDrugByBarcode(
        @Path("barcode") barcode: String
    ): Response<TurkishDrugDetail>
    
    @GET("api/v1/drugs/atc/{atcCode}")
    suspend fun getDrugsByAtc(
        @Path("atcCode") atcCode: String,
        @Query("limit") limit: Int = 100
    ): Response<TurkishDrugApiResponse>
    
    @GET("api/v1/drugs/manufacturer/{manufacturer}")
    suspend fun getDrugsByManufacturer(
        @Path("manufacturer") manufacturer: String,
        @Query("limit") limit: Int = 100
    ): Response<TurkishDrugApiResponse>
    
    @GET("api/v1/drugs/equivalents/{drugId}")
    suspend fun getEquivalentDrugs(
        @Path("drugId") drugId: String
    ): Response<TurkishDrugApiResponse>
    
    @GET("api/v1/drugs/interactions/{drugId}")
    suspend fun getDrugInteractions(
        @Path("drugId") drugId: String
    ): Response<List<TurkishDrugInteraction>>
    
    @GET("api/v1/drugs/prices/compare")
    suspend fun comparePrices(
        @Query("drug_ids") drugIds: String // Comma-separated IDs
    ): Response<List<TurkishDrugPrice>>
}

interface IlacrehberiApiService {
    @GET("api/search")
    suspend fun searchDrugs(
        @Query("q") query: String,
        @Query("type") searchType: String = "drug_name",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1
    ): Response<TurkishDrugApiResponse>
    
    @GET("api/drug/{id}")
    suspend fun getDrugDetails(
        @Path("id") drugId: String,
        @Query("include_all") includeAll: Boolean = true
    ): Response<TurkishDrugDetail>
    
    @GET("api/prospectus/{drugId}")
    suspend fun getProspectus(
        @Path("drugId") drugId: String,
        @Query("format") format: String = "json"
    ): Response<TurkishProspectus>
    
    @GET("api/sgk/status/{drugId}")
    suspend fun getSgkStatus(
        @Path("drugId") drugId: String
    ): Response<TurkishSGKStatus>
    
    @GET("api/categories/{category}")
    suspend fun getDrugsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int = 100
    ): Response<TurkishDrugApiResponse>
}

// Repository for Turkish medical database integration
@Singleton
class TurkishMedicalApiRepository @Inject constructor(
    private val ilacabakService: IlacabakApiService,
    private val ilacrehberiService: IlacrehberiApiService,
    private val jsonParser: Json
) {
    
    // Comprehensive drug search combining both APIs
    suspend fun searchDrugs(
        query: String,
        searchType: SearchType = SearchType.NAME,
        limit: Int = 50
    ): Flow<List<TurkishDrugDetail>> = flow {
        try {
            val results = mutableListOf<TurkishDrugDetail>()
            
            // Search in ilacabak.com
            try {
                val ilacabakRequest = TurkishDrugSearchRequest(
                    query = query,
                    searchType = searchType.apiValue,
                    limit = limit / 2
                )
                val ilacabakResponse = ilacabakService.searchDrugs(ilacabakRequest)
                if (ilacabakResponse.isSuccessful) {
                    ilacabakResponse.body()?.data?.let { drugs ->
                        results.addAll(drugs)
                    }
                }
            } catch (e: Exception) {
                // Log error but continue with other API
            }
            
            // Search in ilacrehberi.com
            try {
                val ilacrehberiResponse = ilacrehberiService.searchDrugs(
                    query = query,
                    searchType = searchType.apiValue,
                    limit = limit / 2
                )
                if (ilacrehberiResponse.isSuccessful) {
                    ilacrehberiResponse.body()?.data?.let { drugs ->
                        results.addAll(drugs)
                    }
                }
            } catch (e: Exception) {
                // Log error but continue
            }
            
            // Remove duplicates and sort by relevance
            val uniqueResults = results
                .distinctBy { it.id }
                .sortedByDescending { calculateRelevanceScore(it, query) }
                .take(limit)
            
            emit(uniqueResults)
            
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    // Get detailed drug information with enriched data
    suspend fun getDrugDetails(drugId: String): TurkishDrugDetail? {
        return withContext(Dispatchers.IO) {
            try {
                // Try ilacabak first
                val ilacabakResponse = ilacabakService.getDrugById(drugId, true, true)
                if (ilacabakResponse.isSuccessful) {
                    val drug = ilacabakResponse.body()
                    if (drug != null) {
                        return@withContext enrichDrugData(drug)
                    }
                }
                
                // Fallback to ilacrehberi
                val ilacrehberiResponse = ilacrehberiService.getDrugDetails(drugId, true)
                if (ilacrehberiResponse.isSuccessful) {
                    val drug = ilacrehberiResponse.body()
                    if (drug != null) {
                        return@withContext enrichDrugData(drug)
                    }
                }
                
                null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Search by barcode with both APIs
    suspend fun searchByBarcode(barcode: String): TurkishDrugDetail? {
        return withContext(Dispatchers.IO) {
            try {
                // Try ilacabak barcode search
                val response = ilacabakService.getDrugByBarcode(barcode)
                if (response.isSuccessful) {
                    return@withContext response.body()
                }
                
                // Fallback to general search in ilacrehberi
                val searchResponse = ilacrehberiService.searchDrugs(barcode, "barcode", 1)
                if (searchResponse.isSuccessful) {
                    return@withContext searchResponse.body()?.data?.firstOrNull()
                }
                
                null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Get drugs by medical condition/ATC code
    suspend fun getDrugsByCondition(condition: MedicalCondition): List<TurkishDrugDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val results = mutableListOf<TurkishDrugDetail>()
                
                // Get ATC codes for the condition
                val atcCodes = condition.atcCodes
                
                for (atcCode in atcCodes) {
                    try {
                        val response = ilacabakService.getDrugsByAtc(atcCode)
                        if (response.isSuccessful) {
                            response.body()?.data?.let { drugs ->
                                results.addAll(drugs)
                            }
                        }
                    } catch (e: Exception) {
                        // Continue with next ATC code
                    }
                }
                
                results.distinctBy { it.id }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // Get equivalent drugs
    suspend fun getEquivalentDrugs(drugId: String): List<TurkishDrugDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ilacabakService.getEquivalentDrugs(drugId)
                if (response.isSuccessful) {
                    response.body()?.data ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // Price comparison
    suspend fun comparePrices(drugIds: List<String>): List<TurkishDrugPrice> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ilacabakService.comparePrices(drugIds.joinToString(","))
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // Get SGK status
    suspend fun getSgkStatus(drugId: String): TurkishSGKStatus? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ilacrehberiService.getSgkStatus(drugId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Get full prospectus
    suspend fun getProspectus(drugId: String): TurkishProspectus? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ilacrehberiService.getProspectus(drugId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Private helper functions
    private fun calculateRelevanceScore(drug: TurkishDrugDetail, query: String): Double {
        val normalizedQuery = query.lowercase()
        var score = 0.0
        
        // Name match
        if (drug.name.lowercase().contains(normalizedQuery)) {
            score += if (drug.name.lowercase() == normalizedQuery) 1.0 else 0.8
        }
        
        // Active ingredient match
        drug.activeIngredient?.let { ingredient ->
            if (ingredient.lowercase().contains(normalizedQuery)) {
                score += 0.7
            }
        }
        
        // ATC name match
        drug.atcName?.let { atcName ->
            if (atcName.lowercase().contains(normalizedQuery)) {
                score += 0.5
            }
        }
        
        // Exact barcode match
        if (drug.barcode == query) {
            score += 1.0
        }
        
        return score
    }
    
    private suspend fun enrichDrugData(drug: TurkishDrugDetail): TurkishDrugDetail {
        // Enrich with additional data from secondary API if needed
        try {
            // Get SGK status if not present
            if (drug.sgkStatus == null) {
                val sgkStatus = getSgkStatus(drug.id)
                if (sgkStatus != null) {
                    return drug.copy(sgkStatus = sgkStatus)
                }
            }
            
            // Get prospectus if not present
            if (drug.prospectus == null) {
                val prospectus = getProspectus(drug.id)
                if (prospectus != null) {
                    return drug.copy(prospectus = prospectus)
                }
            }
        } catch (e: Exception) {
            // Return original drug if enrichment fails
        }
        
        return drug
    }
}

// Search type enumeration
enum class SearchType(val apiValue: String) {
    NAME("name"),
    INGREDIENT("ingredient"),
    ATC("atc"),
    BARCODE("barcode"),
    MANUFACTURER("manufacturer")
}

// Medical condition enumeration with ATC codes
enum class MedicalCondition(val turkishName: String, val atcCodes: List<String>) {
    DIABETES("Diyabet", listOf("A10A", "A10B")),
    HYPERTENSION("Hipertansiyon", listOf("C02", "C03", "C07", "C08", "C09")),
    CARDIOVASCULAR("Kardiyovasküler", listOf("C01", "C02", "C03", "C04")),
    RESPIRATORY("Solunum", listOf("R03", "R05", "R06")),
    PAIN("Ağrı", listOf("N02", "M01")),
    ANTIBIOTICS("Antibiyotik", listOf("J01")),
    GASTROINTESTINAL("Sindirim", listOf("A02", "A03", "A06", "A07")),
    NEUROLOGICAL("Nörolojik", listOf("N03", "N04", "N05", "N06")),
    INFECTIOUS("Enfeksiyon", listOf("J01", "J02", "J04", "J05")),
    HORMONAL("Hormonal", listOf("G03", "H01", "H02", "H03"))
}

// API response caching
@Singleton
class TurkishDrugApiCache @Inject constructor() {
    private val cache = mutableMapOf<String, Pair<TurkishDrugDetail, Long>>()
    private val cacheTimeout = 24 * 60 * 60 * 1000L // 24 hours
    
    fun getCachedDrug(drugId: String): TurkishDrugDetail? {
        val cached = cache[drugId]
        return if (cached != null && System.currentTimeMillis() - cached.second < cacheTimeout) {
            cached.first
        } else {
            cache.remove(drugId)
            null
        }
    }
    
    fun cacheDrug(drugId: String, drug: TurkishDrugDetail) {
        cache[drugId] = Pair(drug, System.currentTimeMillis())
    }
    
    fun clearCache() {
        cache.clear()
    }
    
    fun getCacheSize(): Int = cache.size
}
