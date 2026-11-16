package com.example.improject.data

import com.example.improject.data.seed.DefaultVocabulary
import com.example.improject.domain.GradeBand
import com.example.improject.domain.VocabEntry
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** vocabulary 集合：學生可以點選的句子。由管理員在後台增修。 */
class VocabularyRepository {
    private val db = FirebaseFirestore.getInstance()
    private val vocabulary = db.collection("vocabulary")

    /** 某個年段的全部句子（含停用的），依分類、順序排列。 */
    fun observeBand(band: GradeBand): Flow<List<VocabEntry>> =
        vocabulary.whereEqualTo("gradeBand", band.key)
            .asFlow { it.toVocabEntry() }
            .map { list -> list.sortedWith(compareBy({ it.category }, { it.order })) }

    /** 全部年段的句子。老師端用來找「老師收到這句時的推薦回應」。 */
    fun observeAll(): Flow<List<VocabEntry>> = vocabulary.asFlow { it.toVocabEntry() }

    suspend fun save(entry: VocabEntry) {
        val ref = if (entry.id.isBlank()) vocabulary.document() else vocabulary.document(entry.id)
        ref.set(entry.toMap()).awaitResult()
    }

    suspend fun setEnabled(id: String, enabled: Boolean) {
        vocabulary.document(id).update(mapOf("enabled" to enabled)).awaitResult()
    }

    /** 把程式內建的預設句庫寫進資料庫，回傳寫入筆數。同代碼的句子會被覆蓋。 */
    suspend fun importDefaults(): Int {
        val entries = DefaultVocabulary.all()
        val batch = db.batch()
        entries.forEach { batch.set(vocabulary.document(it.id), it.toMap()) }
        batch.commit().awaitResult()
        return entries.size
    }
}

private fun VocabEntry.toMap(): Map<String, Any> = mapOf(
    "text" to text,
    "category" to category,
    "gradeBand" to gradeBand.key,
    "suggestions" to suggestions,
    "teacherSuggestions" to teacherSuggestions,
    "caution" to caution,
    "enabled" to enabled,
    "order" to order
)

private fun DocumentSnapshot.toVocabEntry(): VocabEntry = VocabEntry(
    id = id,
    text = getString("text").orEmpty(),
    category = (getLong("category") ?: 1L).toInt(),
    gradeBand = GradeBand.fromKey(getString("gradeBand")),
    suggestions = (get("suggestions") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    teacherSuggestions = (get("teacherSuggestions") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    caution = getBoolean("caution") ?: false,
    enabled = getBoolean("enabled") ?: true,
    order = (getLong("order") ?: 0L).toInt()
)
