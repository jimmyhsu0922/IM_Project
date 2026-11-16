package com.example.improject.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.improject.data.Accounts
import com.example.improject.data.AuthRepository
import com.example.improject.data.SchoolRepository
import com.example.improject.data.UserRepository
import com.example.improject.data.VocabularyRepository
import com.example.improject.data.seed.DemoDataSeeder
import com.example.improject.data.toFriendlyMessage
import com.example.improject.domain.AppUser
import com.example.improject.domain.Club
import com.example.improject.domain.GradeBand
import com.example.improject.domain.Role
import com.example.improject.domain.SchoolClass
import com.example.improject.domain.VocabEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 管理後台的狀態：帳號、班級社團、句庫、示範資料。 */
class AdminViewModel : ViewModel() {
    private val auth = AuthRepository()
    private val userRepo = UserRepository()
    private val schoolRepo = SchoolRepository()
    private val vocabularyRepo = VocabularyRepository()

    private val _users = MutableStateFlow<List<AppUser>>(emptyList())
    val users: StateFlow<List<AppUser>> = _users

    private val _classes = MutableStateFlow<List<SchoolClass>>(emptyList())
    val classes: StateFlow<List<SchoolClass>> = _classes

    private val _clubs = MutableStateFlow<List<Club>>(emptyList())
    val clubs: StateFlow<List<Club>> = _clubs

    private val _band = MutableStateFlow(GradeBand.LOWER)
    val band: StateFlow<GradeBand> = _band

    private val _vocabulary = MutableStateFlow<List<VocabEntry>>(emptyList())
    val vocabulary: StateFlow<List<VocabEntry>> = _vocabulary

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _seedLog = MutableStateFlow<List<String>>(emptyList())
    val seedLog: StateFlow<List<String>> = _seedLog

    private var vocabularyJob: Job? = null

    init {
        observe(userRepo.observeAll()) { list ->
            _users.value = list.sortedWith(compareBy<AppUser> { it.role.ordinal }.thenBy { it.account })
        }
        observe(schoolRepo.observeClasses()) { list -> _classes.value = list.sortedBy { it.id } }
        observe(schoolRepo.observeClubs()) { list -> _clubs.value = list.sortedBy { it.name } }
        selectBand(GradeBand.LOWER)
    }

    private fun <T> observe(flow: Flow<T>, onEach: (T) -> Unit): Job = viewModelScope.launch {
        try {
            flow.collect { onEach(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 已經登出：監聽被伺服器中斷是正常的，不需要提示。
            if (auth.currentUid != null) {
                _message.value = e.toFriendlyMessage()
            }
        }
    }

    private fun runTask(successMessage: String?, onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            try {
                block()
                if (successMessage != null) _message.value = successMessage
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = e.toFriendlyMessage()
            } finally {
                _busy.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    // ---------- 帳號 ----------

    fun createAccount(
        role: Role,
        account: String,
        realName: String,
        password: String,
        schoolClass: SchoolClass?,
        club: Club?,
        onSuccess: () -> Unit
    ) {
        val cleanAccount = account.trim()
        val error = when {
            !Accounts.isValid(cleanAccount) -> "帳號需為 2～30 個英文或數字"
            Accounts.isAdmin(cleanAccount) -> "這個帳號保留給管理員"
            realName.isBlank() -> "請輸入姓名"
            password.length < 6 -> "密碼至少需要 6 個字元"
            role == Role.STUDENT && schoolClass == null -> "學生必須選擇班級"
            role == Role.TEACHER && schoolClass == null && club == null -> "老師至少要負責一個班級或社團"
            else -> null
        }
        if (error != null) {
            _message.value = error
            return
        }
        runTask("已建立帳號 $cleanAccount", onSuccess) {
            val uid = auth.createAccount(cleanAccount, password)
            userRepo.save(
                AppUser(
                    uid = uid,
                    account = cleanAccount,
                    realName = realName.trim(),
                    role = role,
                    grade = if (role == Role.STUDENT) schoolClass?.grade ?: 0 else 0,
                    classId = schoolClass?.id.orEmpty(),
                    clubId = club?.id.orEmpty(),
                    active = true
                )
            )
        }
    }

    fun updateUser(
        user: AppUser,
        realName: String,
        schoolClass: SchoolClass?,
        club: Club?,
        active: Boolean,
        onSuccess: () -> Unit
    ) {
        if (realName.isBlank()) {
            _message.value = "請輸入姓名"
            return
        }
        if (user.role == Role.STUDENT && schoolClass == null) {
            _message.value = "學生必須選擇班級"
            return
        }
        runTask("已更新 ${user.account}", onSuccess) {
            userRepo.save(
                user.copy(
                    realName = realName.trim(),
                    grade = if (user.role == Role.STUDENT) schoolClass?.grade ?: 0 else 0,
                    classId = schoolClass?.id.orEmpty(),
                    clubId = club?.id.orEmpty(),
                    active = active
                )
            )
        }
    }

    // ---------- 班級與社團 ----------

    fun addClass(grade: Int, number: Int, onSuccess: () -> Unit) {
        val id = SchoolRepository.classId(grade, number)
        if (_classes.value.any { it.id == id }) {
            _message.value = "這個班級已經存在"
            return
        }
        runTask("已新增 ${SchoolRepository.className(grade, number)}", onSuccess) {
            schoolRepo.addClass(grade, number, DemoDataSeeder.SCHOOL_YEAR)
        }
    }

    fun addClub(name: String, onSuccess: () -> Unit) {
        val clean = name.trim()
        if (clean.isBlank()) {
            _message.value = "請輸入社團名稱"
            return
        }
        if (_clubs.value.any { it.name == clean }) {
            _message.value = "這個社團已經存在"
            return
        }
        runTask("已新增 $clean", onSuccess) {
            schoolRepo.addClub("", clean)
        }
    }

    // ---------- 句庫 ----------

    fun selectBand(band: GradeBand) {
        _band.value = band
        vocabularyJob?.cancel()
        _vocabulary.value = emptyList()
        vocabularyJob = observe(vocabularyRepo.observeBand(band)) { list -> _vocabulary.value = list }
    }

    /** existing 為 null 表示新增。suggestionsText 每行一句推薦回應。 */
    fun saveVocab(
        existing: VocabEntry?,
        text: String,
        category: Int,
        suggestionsText: String,
        onSuccess: () -> Unit
    ) {
        val clean = text.trim()
        if (clean.isBlank()) {
            _message.value = "請輸入句子"
            return
        }
        val suggestions = suggestionsText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val entry = if (existing != null) {
            existing.copy(text = clean, category = category, suggestions = suggestions)
        } else {
            VocabEntry(
                id = "",
                text = clean,
                category = category,
                gradeBand = _band.value,
                suggestions = suggestions,
                enabled = true,
                order = (_vocabulary.value.maxOfOrNull { it.order } ?: 0) + 1
            )
        }
        runTask("已儲存", onSuccess) { vocabularyRepo.save(entry) }
    }

    fun setVocabEnabled(entry: VocabEntry, enabled: Boolean) {
        runTask(null) { vocabularyRepo.setEnabled(entry.id, enabled) }
    }

    fun importDefaults() {
        runTask(null) {
            val count = vocabularyRepo.importDefaults()
            _message.value = "已匯入 $count 句預設句子"
        }
    }

    // ---------- 示範資料 ----------

    fun seedDemo() {
        _seedLog.value = emptyList()
        runTask("示範資料建立完成") {
            DemoDataSeeder(auth, userRepo, schoolRepo, vocabularyRepo).seed { line ->
                _seedLog.value = _seedLog.value + line
            }
        }
    }
}
