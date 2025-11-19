package com.abdullah.todoapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdullah.todoapp.model.*
import com.abdullah.todoapp.notification.AlarmScheduler
import com.abdullah.todoapp.util.FileUtils
import com.abdullah.todoapp.viewmodel.ToDoViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberTimePickerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ToDoDetailScreen(
    todoId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ToDoViewModel = viewModel()
) {
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedAttachmentIds = remember { mutableStateListOf<Int>() }
    var showConfirmDeleteSelected by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var editedNotes by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var selectedReminderTime by remember { mutableStateOf<Date?>(null) }
    var selectedDateText by remember { mutableStateOf("") }
    var selectedHourText by remember { mutableStateOf("") }
    var selectedMinuteText by remember { mutableStateOf("") }
    val alarmScheduler = remember { AlarmScheduler(context) }

    var isEditingTitle by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addAttachment(todoId, it)
        }
    }

    val todoItem by viewModel.getTodoById(todoId).collectAsState(initial = null)

    fun openFile(attachment: Attachment) {
        try {
            val uri = FileUtils.getUriForFile(context, attachment.uri)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, when (attachment.type) {
                    AttachmentType.IMAGE -> "image/*"
                    AttachmentType.DOCUMENT -> "application/*"
                    AttachmentType.AUDIO -> "audio/*"
                    AttachmentType.VIDEO -> "video/*"
                    else -> "*/*"
                })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dosya açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(todoItem) {
        todoItem?.let { item ->
            editedNotes = item.notes
            editedTitle = item.title
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Görev Detayı") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil")
                    }
                    IconButton(onClick = { showAttachmentDialog = true }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Dosya Ekle")
                    }
                    IconButton(onClick = { showReminderDialog = true }) {
                        Icon(Icons.Default.Alarm, contentDescription = "Hatırlatıcı Ekle")
                    }
                }
            )
        }
    ) { paddingValues ->
        todoItem?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Başlık - serbest düzenleme (not alanı ile aynı desen)
                if (isEditingTitle) {
                    BasicTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                if (editedTitle.isNotBlank()) {
                                    viewModel.updateItem(item.copy(title = editedTitle, updatedAt = Date()))
                                    isEditingTitle = false
                                }
                            }
                        ) { Text("Kaydet") }
                    }
                } else {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEditingTitle = true }
                    )
                }

                // Durum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (item.status) {
                            "TAMAMLANDI" -> "Tamamlandı"
                            "İPTAL_EDİLDİ" -> "İptal Edildi"
                            "ERTELENDİ" -> "Ertelendi"
                            "BEKLEMEDE" -> "Beklemede"
                            else -> "Devam Ediyor"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Öncelik ve Kategori
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Öncelik: ${item.priority}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Kategori: ${item.category}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Bitiş Tarihi
                if (item.dueDate != null) {
                    Text(
                        text = "Bitiş Tarihi: ${dateFormat.format(item.dueDate)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Notlar
                Text(
                    text = "Notlar:",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (isEditing) {
                    BasicTextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.updateItem(item.copy(notes = editedNotes, updatedAt = Date()))
                                isEditing = false
                            }
                        ) {
                            Text("Kaydet")
                        }
                    }
                } else {
                    Text(
                        text = item.notes.ifEmpty { "Not eklenmemiş" },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEditing = true }
                            .padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Oluşturulma ve Güncelleme Tarihleri
                Text(
                    text = "Oluşturulma: ${dateFormat.format(item.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Son Güncelleme: ${dateFormat.format(item.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (item.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dosyalar",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (selectionMode) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    val ids = item.attachments.split(",")
                                        .filter { it.isNotEmpty() }
                                        .mapNotNull { s -> s.substringBefore(":").toIntOrNull() }
                                    selectedAttachmentIds.clear()
                                    selectedAttachmentIds.addAll(ids)
                                }) { Text("Tümünü Seç") }
                                TextButton(onClick = {
                                    if (selectedAttachmentIds.isNotEmpty()) {
                                        // open confirmation dialog
                                        showConfirmDeleteSelected = true
                                    }
                                }) { Text("Sil") }
                                IconButton(onClick = {
                                    selectedAttachmentIds.clear()
                                    selectionMode = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Seçimi Kapat")
                                }
                            }
                        }
                    }

                    fun parseAttachment(str: String): Attachment? {
                        val s = str
                        if (s.isBlank()) return null
                        val first = s.indexOf(':')
                        val last = s.lastIndexOf(':')
                        if (first == -1 || last == -1 || last == first) return null
                        val secondLast = s.lastIndexOf(':', last - 1)
                        val thirdLast = s.lastIndexOf(':', secondLast - 1)
                        if (secondLast == -1 || thirdLast == -1) return null
                        val idStr = s.substring(0, first)
                        val uri = s.substring(first + 1, thirdLast)
                        val typeStr = s.substring(thirdLast + 1, secondLast)
                        val name = s.substring(secondLast + 1, last)
                        val sizeStr = s.substring(last + 1)
                        val id = idStr.toIntOrNull() ?: return null
                        val size = sizeStr.toLongOrNull() ?: 0L
                        val type = try { AttachmentType.valueOf(typeStr) } catch (_: Exception) { AttachmentType.OTHER }
                        return Attachment(id = id, uri = uri, type = type, name = name, size = size)
                    }

                    val attachments = remember(item.attachments) {
                        item.attachments.split(",").mapNotNull { parseAttachment(it) }
                    }

                    val rows = attachments.chunked(4)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { attachment ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .combinedClickable(
                                                onClick = {
                                                    if (selectionMode) {
                                                        if (selectedAttachmentIds.contains(attachment.id)) selectedAttachmentIds.remove(attachment.id) else selectedAttachmentIds.add(attachment.id)
                                                    } else {
                                                        openFile(attachment)
                                                    }
                                                },
                                                onLongClick = {
                                                    selectionMode = true
                                                    if (!selectedAttachmentIds.contains(attachment.id)) {
                                                        selectedAttachmentIds.add(attachment.id)
                                                    }
                                                }
                                            )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (selectionMode) {
                                                Checkbox(
                                                    checked = selectedAttachmentIds.contains(attachment.id),
                                                    onCheckedChange = { checked ->
                                                        if (checked) selectedAttachmentIds.add(attachment.id) else selectedAttachmentIds.remove(attachment.id)
                                                    }
                                                )
                                            }
                                            Icon(
                                                imageVector = when (attachment.type) {
                                                    AttachmentType.IMAGE -> Icons.Default.Image
                                                    AttachmentType.DOCUMENT -> Icons.Default.Description
                                                    AttachmentType.AUDIO -> Icons.Default.AudioFile
                                                    AttachmentType.VIDEO -> Icons.Default.VideoFile
                                                    else -> Icons.Default.AttachFile
                                                },
                                                contentDescription = null
                                            )
                                            Text(
                                                text = attachment.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                repeat(4 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hatırlatıcı Ekleme Dialog
        if (showReminderDialog) {
            AlertDialog(
                onDismissRequest = { showReminderDialog = false },
                title = { Text("Hatırlatıcı Ekle") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        var showDatePicker by remember { mutableStateOf(false) }

                        // Tarih Seçici
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = selectedDateText,
                                onValueChange = { },
                                label = { Text("Tarih") },
                                readOnly = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Tarih Seç")
                            }
                        }

                        // Saat Seçici
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedHourText,
                                onValueChange = { 
                                    if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                        val hour = it.toIntOrNull() ?: 0
                                        if (hour in 0..23) {
                                            selectedHourText = it
                                        }
                                    }
                                },
                                label = { Text("Saat") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("00-23") }
                            )
                            OutlinedTextField(
                                value = selectedMinuteText,
                                onValueChange = { 
                                    if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                        val minute = it.toIntOrNull() ?: 0
                                        if (minute in 0..59) {
                                            selectedMinuteText = it
                                        }
                                    }
                                },
                                label = { Text("Dakika") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("00-59") }
                            )
                        }

                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = System.currentTimeMillis(),
                                selectableDates = object : SelectableDates {
                                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                        val todayStart = Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        val dayStart = Calendar.getInstance().apply {
                                            timeInMillis = utcTimeMillis
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        return dayStart >= todayStart
                                    }
                                }
                            )
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val dayStart = Calendar.getInstance().apply {
                                                    timeInMillis = millis
                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }.timeInMillis
                                                val todayStart = Calendar.getInstance().apply {
                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }.timeInMillis
                                                if (dayStart >= todayStart) {
                                                    selectedDateText = dateFormat.format(Date(millis))
                                                } else {
                                                    Toast.makeText(context, "Bugünden önceki tarihler seçilemez", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            showDatePicker = false
                                        }
                                    ) {
                                        Text("Tamam")
                                    }
                                }
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    showModeToggle = false
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                val calendar = Calendar.getInstance()
                                
                                // Tarih işleme
                                if (selectedDateText.isNotEmpty()) {
                                    val date = dateFormat.parse(selectedDateText)
                                    date?.let {
                                        calendar.time = it
                                    }
                                }

                                // Saat işleme
                                val hour = selectedHourText.toIntOrNull() ?: 0
                                val minute = selectedMinuteText.toIntOrNull() ?: 0
                                if (hour in 0..23 && minute in 0..59) {
                                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                                    calendar.set(Calendar.MINUTE, minute)
                                } else {
                                    throw Exception("Geçersiz saat veya dakika")
                                }

                                selectedReminderTime = calendar.time
                                todoItem?.let { item ->
                                    if (calendar.timeInMillis > System.currentTimeMillis()) {
                                        alarmScheduler.scheduleReminder(item, calendar.timeInMillis)
                                        viewModel.updateItem(item.copy(reminderDate = calendar.time, updatedAt = Date()))
                                        Toast.makeText(context, "Hatırlatıcı eklendi: ${dateFormat.format(calendar.time)}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Geçmiş bir zaman seçilemez", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lütfen geçerli bir tarih ve saat girin", Toast.LENGTH_SHORT).show()
                            }
                            showReminderDialog = false
                        }
                    ) {
                        Text("Ekle")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReminderDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Görevi Sil") },
            text = { Text("Bu görevi silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        todoItem?.let { viewModel.removeItem(it.id) }
                        showDeleteConfirmation = false
                        onNavigateBack()
                    }
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showConfirmDeleteSelected) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteSelected = false },
            title = { Text("Dosyaları Sil") },
            text = { Text("Seçili dosyaları silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeAttachments(todoId, selectedAttachmentIds.toList())
                        selectedAttachmentIds.clear()
                        selectionMode = false
                        showConfirmDeleteSelected = false
                    }
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteSelected = false }) { Text("İptal") }
            }
        )
    }

    // Attachment Dialog
    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = { Text("Dosya Ekle") },
            text = {
                Column {
                    Button(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                            showAttachmentDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dosya Ekle")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
} 