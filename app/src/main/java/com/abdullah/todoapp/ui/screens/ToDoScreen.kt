package com.abdullah.todoapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import com.abdullah.todoapp.model.Attachment
import com.abdullah.todoapp.model.AttachmentType
import com.abdullah.todoapp.model.Category
import com.abdullah.todoapp.model.Priority
import com.abdullah.todoapp.model.ToDoItem
 
import com.abdullah.todoapp.viewmodel.ToDoViewModel
import com.abdullah.todoapp.viewmodel.ThemeViewModel
import com.abdullah.todoapp.viewmodel.SortOrder
import com.abdullah.todoapp.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.*
import com.abdullah.todoapp.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: ToDoViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showRemindersDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf<ToDoItem?>(null) }
    var selectedPriority by remember { mutableStateOf(Priority.ORTA) }
    var selectedCategory by remember { mutableStateOf(Category.DİĞER) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskNotes by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<Long?>(null) }
    var attemptedAdd by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val todoItems by viewModel.allItems.collectAsState(initial = emptyList())
    val showCompleted by viewModel.showCompleted.collectAsState(initial = false)
    val sortOrder by viewModel.sortOrder.collectAsState(initial = SortOrder.CREATED_AT_DESC)
    // removed unused dateFormat
    val itemsWithReminders by viewModel.itemsWithReminders.collectAsState(initial = emptyList())

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedItemId?.let { id ->
                viewModel.addAttachment(id, it)
            }
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yapılacaklar") },
                actions = {
                    // Sort
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sırala")
                    }
                    // Theme
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Filled.Palette, contentDescription = "Tema")
                    }
                    // Reminders
                    IconButton(onClick = { showRemindersDialog = true }) {
                        Icon(Icons.Filled.Alarm, contentDescription = "Hatırlatıcılar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ekle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showCompleted,
                    onClick = { viewModel.toggleShowCompleted() },
                    label = { Text("Tamamlananlar") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (todoItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Henüz görev eklenmemiş",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    items(todoItems) { item ->
                        ToDoItemCard(
                            item = item,
                            onItemClick = { onNavigateToDetail(item.id) },
                            onDeleteClick = { showDeleteConfirmation = item },
                            onToggleComplete = { 
                                viewModel.updateItem(
                                    item.copy(
                                        status = if (item.status == "TAMAMLANDI") "DEVAM_EDİYOR" else "TAMAMLANDI",
                                        completedAt = if (item.status != "TAMAMLANDI") Date() else null,
                                        updatedAt = Date()
                                    )
                                )
                            },
                            onAttachmentClick = { 
                                selectedItemId = item.id
                                showAttachmentDialog = true 
                            }
                        )
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Yeni Görev") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Başlık") },
                            isError = attemptedAdd && newTaskTitle.isBlank(),
                            supportingText = {
                                if (attemptedAdd && newTaskTitle.isBlank()) Text("Başlık boş olamaz")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newTaskNotes,
                            onValueChange = { newTaskNotes = it },
                            label = { Text("Notlar") },
                            isError = attemptedAdd && newTaskNotes.isBlank(),
                            supportingText = {
                                if (attemptedAdd && newTaskNotes.isBlank()) Text("Not boş olamaz")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        // Priority Selection
                        Text("Öncelik", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Priority.values().forEach { priority ->
                                FilterChip(
                                    selected = selectedPriority == priority,
                                    onClick = { selectedPriority = priority },
                                    label = { Text(priority.name) }
                                )
                            }
                        }
                        // Category Selection
                        Text("Kategori", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Category.values().forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.name) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newTaskTitle.isNotBlank() && newTaskNotes.isNotBlank()) {
                                viewModel.addItem(
                                    title = newTaskTitle,
                                    description = newTaskDescription,
                                    priority = selectedPriority.name,
                                    category = selectedCategory.name,
                                    notes = newTaskNotes
                                )
                                newTaskTitle = ""
                                newTaskDescription = ""
                                newTaskNotes = ""
                                selectedPriority = Priority.ORTA
                                selectedCategory = Category.DİĞER
                                showAddDialog = false
                                attemptedAdd = false
                            } else {
                                attemptedAdd = true
                            }
                        }
                    ) {
                        Text("Ekle")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }

        // Theme Selection Dialog
        if (showThemeDialog) {
            val currentTheme by themeViewModel.themeMode.collectAsState()

            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Tema Seçimi") },
                text = {
                    Column {
                        ListItem(
                            headlineContent = { Text("Sistem Teması") },
                            leadingContent = {
                                RadioButton(
                                    selected = currentTheme == ThemeMode.SYSTEM_DEFAULT,
                                    onClick = {
                                        themeViewModel.setThemeMode(ThemeMode.SYSTEM_DEFAULT)
                                        showThemeDialog = false
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                themeViewModel.setThemeMode(ThemeMode.SYSTEM_DEFAULT)
                                showThemeDialog = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Açık Tema") },
                            leadingContent = {
                                RadioButton(
                                    selected = currentTheme == ThemeMode.LIGHT,
                                    onClick = {
                                        themeViewModel.setThemeMode(ThemeMode.LIGHT)
                                        showThemeDialog = false
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                themeViewModel.setThemeMode(ThemeMode.LIGHT)
                                showThemeDialog = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Koyu Tema") },
                            leadingContent = {
                                RadioButton(
                                    selected = currentTheme == ThemeMode.DARK,
                                    onClick = {
                                        themeViewModel.setThemeMode(ThemeMode.DARK)
                                        showThemeDialog = false
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                themeViewModel.setThemeMode(ThemeMode.DARK)
                                showThemeDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Tamam")
                    }
                }
            )
        }

        // Reminders Dialog
        if (showRemindersDialog) {
            val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
            AlertDialog(
                onDismissRequest = { showRemindersDialog = false },
                title = { Text("Mevcut Hatırlatıcılar") },
                text = {
                    if (itemsWithReminders.isEmpty()) {
                        Text("Kayıtlı hatırlatıcı yok")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsWithReminders.forEach { item ->
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                                        if (item.reminderDate != null) {
                                            Text(
                                                "Zaman: ${dateFormat.format(item.reminderDate)}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        // Not içeriği gösterilmez
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                            TextButton(onClick = {
                                                viewModel.cancelReminder(item)
                                            }) { Text("Sil") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRemindersDialog = false }) { Text("Kapat") }
                }
            )
        }

        // Sort Dialog
        if (showSortDialog) {
            AlertDialog(
                onDismissRequest = { showSortDialog = false },
                title = { Text("Sıralama") },
                text = {
                    Column {
                        SortOrder.values().forEach { order ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSortOrder(order)
                                        showSortDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sortOrder == order,
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        showSortDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(order.displayName)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSortDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        showDeleteConfirmation?.let { item ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text("Görevi Sil") },
                text = { Text("Bu görevi silmek istediğinizden emin misiniz?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.removeItem(item.id)
                            showDeleteConfirmation = null
                        }
                    ) {
                        Text("Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = null }) {
                        Text("İptal")
                    }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoItemCard(
    item: ToDoItem,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onAttachmentClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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

    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onToggleComplete) {
                        Icon(
                            imageVector = if (item.status == "TAMAMLANDI") Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (item.status == "TAMAMLANDI") "Tamamlandı" else "Tamamlanmadı",
                            tint = if (item.status == "TAMAMLANDI") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onAttachmentClick) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = "Dosyalar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (item.notes.isNotBlank()) {
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (item.attachments.isNotEmpty()) {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.attachments.split(",").filter { it.isNotEmpty() }.mapNotNull { parseAttachment(it) }.forEach { attachment ->
                        IconButton(
                            onClick = { openFile(attachment) }
                        ) {
                            when (attachment.type) {
                                AttachmentType.IMAGE -> {
                                    Icon(
                                        Icons.Filled.Image,
                                        contentDescription = attachment.name,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                AttachmentType.DOCUMENT -> {
                                    Icon(
                                        Icons.Filled.Description,
                                        contentDescription = attachment.name,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                AttachmentType.AUDIO -> {
                                    Icon(
                                        Icons.Filled.AudioFile,
                                        contentDescription = attachment.name,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                AttachmentType.VIDEO -> {
                                    Icon(
                                        Icons.Filled.VideoFile,
                                        contentDescription = attachment.name,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                else -> {
                                    Icon(
                                        Icons.Filled.AttachFile,
                                        contentDescription = attachment.name,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.priority,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.dueDate != null) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(item.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 