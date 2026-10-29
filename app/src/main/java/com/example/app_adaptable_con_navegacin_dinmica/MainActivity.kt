package com.example.app_adaptable_con_navegacin_dinmica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app_adaptable_con_navegacin_dinmica.ui.theme.App_adaptable_con_navegación_dinámicaTheme

private enum class Mailbox(val title: String, val symbol: String) {
    Inbox("Recibidos", "✉"), Sent("Enviados", "↗"), Drafts("Borradores", "▤"), Spam("Spam", "⚑")
}

private data class Email(val id: Int, val sender: String, val subject: String, val preview: String, val body: String, val time: String, val color: Long)

private val emails = listOf(
    Email(1, "Ana Martínez", "Ideas para el proyecto", "Estuve revisando las propuestas y creo que…", "Hola, estuve revisando las propuestas para el proyecto. ¿Podemos reunirnos esta semana para elegir una dirección y repartir las tareas? Tengo algunas ideas que me gustaría compartir contigo.", "9:42 a. m.", 0xFF6750A4),
    Email(2, "Equipo de diseño", "Actualización semanal", "Esta semana completamos los primeros…", "Esta semana completamos los primeros diseños y recibimos comentarios del equipo. La próxima semana trabajaremos en los ajustes y prepararemos una revisión para todos.", "Ayer", 0xFF006C4C),
    Email(3, "Carlos Ruiz", "Fotos del viaje", "Te comparto las fotos que tomamos…", "¡Hola! Te comparto las fotos que tomamos durante el viaje. Fue un fin de semana increíble. Avísame cuáles son tus favoritas.", "Mar", 0xFF9C4146),
    Email(4, "Boletín Android", "Novedades de Compose", "Descubre las últimas herramientas…", "En esta edición encontrarás novedades de Jetpack Compose, recomendaciones para crear interfaces adaptables y recursos para tus próximos proyectos.", "Lun", 0xFF52616B),
    Email(5, "Lucía Pérez", "Almuerzo el viernes", "¿Te viene bien a las 12:30?…", "Hola, ¿te viene bien almorzar el viernes a las 12:30? Encontré un lugar nuevo cerca de la oficina que creo que te va a gustar.", "Dom", 0xFF805500)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App_adaptable_con_navegación_dinámicaTheme { ReplyApp() } }
    }
}

@Composable
private fun ReplyApp() {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        val medium = maxWidth >= 600.dp
        var mailbox by remember { mutableStateOf(Mailbox.Inbox) }
        var selected by remember { mutableStateOf<Email?>(null) }
        val mailboxEmails = when (mailbox) {
            Mailbox.Inbox -> emails
            Mailbox.Sent -> emails.reversed().take(3)
            Mailbox.Drafts -> emails.take(2)
            Mailbox.Spam -> emails.takeLast(2)
        }

        if (!expanded && selected != null) {
            BackHandler { selected = null }
            EmailDetail(selected!!, onBack = { selected = null }, modifier = Modifier.fillMaxSize())
            return@BoxWithConstraints
        }

        val mainContent: @Composable () -> Unit = {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.inverseOnSurface)) {
                Text(mailbox.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp))
                if (expanded) {
                    Row(Modifier.weight(1f).fillMaxWidth().padding(start = 12.dp, end = 24.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EmailList(mailboxEmails, selected, onSelect = { selected = it }, modifier = Modifier.weight(0.43f).fillMaxHeight())
                        if (selected != null) EmailDetail(selected!!, onBack = { selected = null }, modifier = Modifier.weight(0.57f).fillMaxHeight(), fullScreen = true)
                        else EmptyDetail(Modifier.weight(0.57f).fillMaxHeight())
                    }
                } else {
                    EmailList(mailboxEmails, selected, onSelect = { selected = it }, modifier = Modifier.weight(1f).fillMaxWidth())
                    if (!medium) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        Mailbox.entries.forEach { item ->
                            NavigationBarItem(selected = mailbox == item, onClick = { mailbox = item; selected = null }, icon = { Text(item.symbol) }, label = { Text(item.title, maxLines = 1) })
                        }
                    }
                }
            }
        }

        if (expanded) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(250.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Text("Reply", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 28.dp, top = 30.dp, bottom = 24.dp))
                        Mailbox.entries.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(item.title) },
                                selected = mailbox == item,
                                onClick = { mailbox = item; selected = null },
                                icon = { Text(item.symbol) },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            ) { mainContent() }
        } else if (medium) {
            Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.inverseOnSurface)) {
                NavigationRail(modifier = Modifier.fillMaxHeight(), containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    Spacer(Modifier.height(12.dp))
                    Mailbox.entries.forEach { item ->
                        NavigationRailItem(selected = mailbox == item, onClick = { mailbox = item; selected = null }, icon = { Text(item.symbol) }, label = { Text(item.title) })
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { mainContent() }
            }
        } else {
            mainContent()
        }
    }
}

@Composable
private fun EmailList(list: List<Email>, selected: Email?, onSelect: (Email) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
        items(list, key = { it.id }) { email ->
            val isSelected = selected?.id == email.id
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp).clip(RoundedCornerShape(24.dp)).background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface).clickable { onSelect(email) }.padding(horizontal = 16.dp, vertical = 18.dp), verticalAlignment = Alignment.Top) {
                Avatar(email)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(email.sender, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(email.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(email.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(email.preview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun Avatar(email: Email) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(email.color)), contentAlignment = Alignment.Center) {
        Text(email.sender.split(" ").take(2).mapNotNull { it.firstOrNull() }.joinToString(""), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmailDetail(email: Email, onBack: () -> Unit, modifier: Modifier = Modifier, fullScreen: Boolean = false) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.inverseOnSurface).padding(horizontal = 8.dp, vertical = 8.dp)) {
        if (!fullScreen) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                Text("‹", Modifier.clip(CircleShape).clickable(onClick = onBack).padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text(email.subject, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(email)
            Column(Modifier.padding(start = 14.dp)) {
                Text(email.sender, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("para mí · ${email.time}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
        if (fullScreen) Spacer(Modifier.height(28.dp)) else Text(email.subject, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))
        Text(email.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.25f)
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {}) { Text("Responder") }
            OutlinedButton(onClick = {}) { Text("Reenviar") }
        }
        }
    }
}

@Composable
private fun EmptyDetail(modifier: Modifier = Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
        Text("Selecciona un correo para ver los detalles", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true, widthDp = 1000)
@Composable
private fun ReplyPreview() { App_adaptable_con_navegación_dinámicaTheme { ReplyApp() } }
