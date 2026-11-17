package com.example.improject.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import com.example.improject.ui.components.*
import androidx.compose.ui.draw.clip
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.improject.ui.theme.appTopBarColors
import com.example.improject.data.SchoolRepository
import com.example.improject.data.seed.DemoDataSeeder
import com.example.improject.domain.AppUser
import com.example.improject.domain.CATEGORY_COUNT
import com.example.improject.domain.Club
import com.example.improject.domain.GradeBand
import com.example.improject.domain.Role
import com.example.improject.domain.SchoolClass
import com.example.improject.domain.VocabEntry

private const val NONE_LABEL = "（無）"

/** 管理後台：帳號、班級社團、句庫、系統四個分頁。只有管理員角色會進到這裡。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    user: AppUser,
    onLogout: () -> Unit,
    adminViewModel: AdminViewModel = viewModel(key = "admin-${user.uid}")
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("帳號", "班級社團", "句庫", "系統")
    val message by adminViewModel.message.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(message) {
        val text = message
        if (text != null) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            adminViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            AppHeader("管理後台", "校園小神通", onLogout = onLogout)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppSegments(tabs, selectedTab) { selectedTab = it }
            when (selectedTab) {
                0 -> AccountsTab(adminViewModel)
                1 -> SchoolTab(adminViewModel)
                2 -> VocabularyTab(adminViewModel)
                else -> SystemTab(adminViewModel)
            }
        }
    }
}

// ====================== 帳號 ======================

@Composable
private fun AccountsTab(vm: AdminViewModel) {
    val users by vm.users.collectAsState()
    val classes by vm.classes.collectAsState()
    val clubs by vm.clubs.collectAsState()
    val busy by vm.busy.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AppUser?>(null) }

    val classNames = remember(classes) { classes.associate { it.id to it.name } }
    val clubNames = remember(clubs) { clubs.associate { it.id to it.name } }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("使用者帳號", Modifier.weight(1f), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { showCreate = true }, enabled = !busy, shape = RoundedCornerShape(14.dp)) {
                Text("＋ 新增帳號")
            }
        }
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
            items(users, key = { it.uid }) { item: AppUser ->
                val parts = listOfNotNull(
                    item.role.label,
                    classNames[item.classId],
                    clubNames[item.clubId]
                )
                ListItem(
                    headlineContent = { Text(item.realName, fontWeight = FontWeight.Bold) },
                    leadingContent = { InitialAvatar(item.realName, item.role) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    supportingContent = { Text(item.account + "・" + parts.joinToString("・"), fontSize = 12.sp) },
                    trailingContent = {
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = if (item.active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer) {
                            Text(if (item.active) "啟用" else "已停用", fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (item.active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer)
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(enabled = item.role != Role.ADMIN) { editing = item }
                )
                HorizontalDivider()
            }
        }
    }

    if (showCreate) {
        CreateAccountDialog(
            classes = classes,
            clubs = clubs,
            busy = busy,
            onDismiss = { showCreate = false },
            onCreate = { role, account, name, password, schoolClass, club ->
                vm.createAccount(role, account, name, password, schoolClass, club) { showCreate = false }
            }
        )
    }

    val target = editing
    if (target != null) {
        EditUserDialog(
            user = target,
            classes = classes,
            clubs = clubs,
            busy = busy,
            onDismiss = { editing = null },
            onSave = { name, schoolClass, club, active ->
                vm.updateUser(target, name, schoolClass, club, active) { editing = null }
            }
        )
    }
}

@Composable
private fun CreateAccountDialog(
    classes: List<SchoolClass>,
    clubs: List<Club>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onCreate: (Role, String, String, String, SchoolClass?, Club?) -> Unit
) {
    var role by remember { mutableStateOf(Role.STUDENT) }
    var account by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var schoolClass by remember { mutableStateOf<SchoolClass?>(null) }
    var club by remember { mutableStateOf<Club?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增帳號") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DropdownField(
                    label = "身分",
                    options = listOf(Role.STUDENT, Role.TEACHER),
                    selectedLabel = role.label,
                    optionLabel = { it.label },
                    onSelect = { role = it }
                )
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("帳號（學號／工號）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("初始密碼（至少 6 碼）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = "班級",
                    options = listOf<SchoolClass?>(null) + classes,
                    selectedLabel = schoolClass?.name ?: NONE_LABEL,
                    optionLabel = { it?.name ?: NONE_LABEL },
                    onSelect = { schoolClass = it }
                )
                DropdownField(
                    label = "社團",
                    options = listOf<Club?>(null) + clubs,
                    selectedLabel = club?.name ?: NONE_LABEL,
                    optionLabel = { it?.name ?: NONE_LABEL },
                    onSelect = { club = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(role, account, name, password, schoolClass, club) },
                enabled = !busy
            ) {
                Text(if (busy) "建立中…" else "建立")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun EditUserDialog(
    user: AppUser,
    classes: List<SchoolClass>,
    clubs: List<Club>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, SchoolClass?, Club?, Boolean) -> Unit
) {
    var name by remember(user.uid) { mutableStateOf(user.realName) }
    var schoolClass by remember(user.uid) { mutableStateOf(classes.firstOrNull { it.id == user.classId }) }
    var club by remember(user.uid) { mutableStateOf(clubs.firstOrNull { it.id == user.clubId }) }
    var active by remember(user.uid) { mutableStateOf(user.active) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("編輯 ${user.account}（${user.role.label}）") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = "班級",
                    options = listOf<SchoolClass?>(null) + classes,
                    selectedLabel = schoolClass?.name ?: NONE_LABEL,
                    optionLabel = { it?.name ?: NONE_LABEL },
                    onSelect = { schoolClass = it }
                )
                DropdownField(
                    label = "社團",
                    options = listOf<Club?>(null) + clubs,
                    selectedLabel = club?.name ?: NONE_LABEL,
                    optionLabel = { it?.name ?: NONE_LABEL },
                    onSelect = { club = it }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("帳號啟用", modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
                Text(
                    "停用後該帳號無法登入，也讀不到任何資料。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, schoolClass, club, active) }, enabled = !busy) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ====================== 班級與社團 ======================

@Composable
private fun SchoolTab(vm: AdminViewModel) {
    val classes by vm.classes.collectAsState()
    val clubs by vm.clubs.collectAsState()
    val busy by vm.busy.collectAsState()

    var showAddClass by remember { mutableStateOf(false) }
    var showAddClub by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SectionHeader(title = "班級（${classes.size}）", actionLabel = "＋ 新增班級", enabled = !busy) {
                showAddClass = true
            }
        }
        items(classes, key = { "class-item-${it.id}" }) { item: SchoolClass ->
            ListItem(
                headlineContent = { Text(item.name) },
                supportingContent = { Text("${item.schoolYear} 學年度・代碼 ${item.id}") }
            )
            HorizontalDivider()
        }
        item {
            SectionHeader(title = "社團（${clubs.size}）", actionLabel = "＋ 新增社團", enabled = !busy) {
                showAddClub = true
            }
        }
        items(clubs, key = { "club-item-${it.id}" }) { item: Club ->
            ListItem(headlineContent = { Text(item.name) })
            HorizontalDivider()
        }
        item {
            Text(
                "新增班級或社團時會自動建立對應的聊天室。導師與成員請到「帳號」分頁指派。",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showAddClass) {
        var grade by remember { mutableIntStateOf(1) }
        var number by remember { mutableIntStateOf(1) }
        AlertDialog(
            onDismissRequest = { showAddClass = false },
            title = { Text("新增班級") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownField(
                        label = "年級",
                        options = (1..6).toList(),
                        selectedLabel = SchoolRepository.GRADE_NAMES[grade - 1],
                        optionLabel = { SchoolRepository.GRADE_NAMES[it - 1] },
                        onSelect = { grade = it }
                    )
                    DropdownField(
                        label = "班別",
                        options = (1..8).toList(),
                        selectedLabel = SchoolRepository.CLASS_NAMES[number - 1],
                        optionLabel = { SchoolRepository.CLASS_NAMES[it - 1] },
                        onSelect = { number = it }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { vm.addClass(grade, number) { showAddClass = false } }, enabled = !busy) {
                    Text("新增")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClass = false }) { Text("取消") }
            }
        )
    }

    if (showAddClub) {
        var clubName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddClub = false },
            title = { Text("新增社團") },
            text = {
                OutlinedTextField(
                    value = clubName,
                    onValueChange = { clubName = it },
                    label = { Text("社團名稱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { vm.addClub(clubName) { showAddClub = false } }, enabled = !busy) {
                    Text("新增")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClub = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, enabled: Boolean, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction, enabled = enabled) { Text(actionLabel) }
    }
}

// ====================== 句庫 ======================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VocabularyTab(vm: AdminViewModel) {
    val band by vm.band.collectAsState()
    val entries by vm.vocabulary.collectAsState()
    val busy by vm.busy.collectAsState()

    var category by remember { mutableIntStateOf(1) }
    var editing by remember { mutableStateOf<VocabEntry?>(null) }
    var creating by remember { mutableStateOf(false) }

    val shown = remember(entries, category) { entries.filter { it.category == category } }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradeBand.entries.forEach { option ->
                FilterChip(
                    selected = band == option,
                    onClick = { vm.selectBand(option) },
                    label = { Text(option.label) }
                )
            }
        }
        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..CATEGORY_COUNT).toList()) { option: Int ->
                FilterChip(
                    selected = category == option,
                    onClick = { category = option },
                    label = { Text(band.categoryName(option)) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { creating = true }, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text("＋ 新增句子")
            }
            OutlinedButton(onClick = { vm.importDefaults() }, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text("匯入預設句庫")
            }
        }

        if (entries.isEmpty()) {
            Text(
                "這個年段還沒有句子。按「匯入預設句庫」可以一次建立三個年段各 50 句。",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(shown, key = { it.id }) { item: VocabEntry ->
                val notes = buildList {
                    if (item.suggestions.isNotEmpty()) add("推薦回應：" + item.suggestions.joinToString("、"))
                    if (item.teacherSuggestions.isNotEmpty()) add("老師回應：" + item.teacherSuggestions.joinToString("、"))
                    if (item.caution) add("注意：容易被拿來反諷")
                }
                val supporting: (@Composable () -> Unit)? = if (notes.isNotEmpty()) {
                    { Text(notes.joinToString("\n")) }
                } else {
                    null
                }
                ListItem(
                    headlineContent = { Text(item.text) },
                    supportingContent = supporting,
                    trailingContent = {
                        Switch(
                            checked = item.enabled,
                            onCheckedChange = { vm.setVocabEnabled(item, it) },
                            enabled = !busy
                        )
                    },
                    modifier = Modifier.clickable { editing = item }
                )
                HorizontalDivider()
            }
        }
    }

    val target = editing
    if (creating || target != null) {
        VocabDialog(
            band = band,
            initial = target,
            defaultCategory = category,
            busy = busy,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { text, selectedCategory, suggestionsText ->
                vm.saveVocab(target, text, selectedCategory, suggestionsText) {
                    creating = false
                    editing = null
                }
            }
        )
    }
}

@Composable
private fun VocabDialog(
    band: GradeBand,
    initial: VocabEntry?,
    defaultCategory: Int,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit
) {
    var text by remember(initial?.id) { mutableStateOf(initial?.text.orEmpty()) }
    var category by remember(initial?.id) { mutableIntStateOf(initial?.category ?: defaultCategory) }
    var suggestionsText by remember(initial?.id) {
        mutableStateOf(initial?.suggestions?.joinToString("\n").orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新增句子（${band.label}）" else "編輯句子") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("句子") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = "分類",
                    options = (1..CATEGORY_COUNT).toList(),
                    selectedLabel = band.categoryName(category),
                    optionLabel = { band.categoryName(it) },
                    onSelect = { category = it }
                )
                OutlinedTextField(
                    value = suggestionsText,
                    onValueChange = { suggestionsText = it },
                    label = { Text("推薦回應（每行一句，可留空）") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(text, category, suggestionsText) }, enabled = !busy) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ====================== 系統 ======================

@Composable
private fun SystemTab(vm: AdminViewModel) {
    val busy by vm.busy.collectAsState()
    val log by vm.seedLog.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("示範資料", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "一次建立校內展示需要的資料：一年一班、三年一班、五年一班，5 個社團，" +
                "三個年段的預設句庫，以及 3 位老師和 12 位學生。可以重複執行。",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "老師帳號：T101、T301、T501\n" +
                "學生帳號：10101～10104、30101～30104、50101～50104\n" +
                "密碼一律是 ${DemoDataSeeder.DEMO_PASSWORD}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { vm.seedDemo() }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (busy) "處理中…" else "建立示範資料")
        }
        Spacer(modifier = Modifier.height(12.dp))
        log.forEach { line ->
            Text(line, fontSize = 13.sp)
        }
    }
}

// ====================== 共用元件 ======================

/** 下拉選單欄位。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    options: List<T>,
    selectedLabel: String,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
