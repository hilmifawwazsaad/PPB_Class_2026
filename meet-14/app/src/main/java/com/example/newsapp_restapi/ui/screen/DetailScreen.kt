package com.example.newsapp_restapi.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.newsapp_restapi.data.model.Article
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    article: Article,
    onBack: () -> Unit
) {
    val context     = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                },
                title = {
                    Text(article.safeSource, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${article.safeTitle}\n\n${article.url ?: ""}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Article"))
                    }) {
                        Icon(Icons.Outlined.Share, "Share")
                    }
                    if (!article.url.isNullOrBlank()) {
                        IconButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.OpenInNew, "Open in browser")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (!article.urlToImage.isNullOrBlank()) {
                AsyncImage(
                    model              = article.urlToImage,
                    contentDescription = article.safeTitle,
                    modifier           = Modifier.fillMaxWidth().height(240.dp),
                    contentScale       = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                SuggestionChip(
                    onClick = {},
                    label   = { Text(article.safeSource, fontSize = 12.sp) }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = article.safeTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp
                    )
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text  = article.safeAuthor.first().uppercaseChar().toString(),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column {
                        Text(
                            text  = article.safeAuthor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text  = formatFullDate(article.publishedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(20.dp))
                if (article.description?.isNotBlank() == true) {
                    Text(
                        text  = article.safeDescription,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 26.sp
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Text(
                    text  = article.safeContent,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(24.dp))
                if (!article.url.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text  = "Read full article",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text      = "Continue reading on ${article.safeSource}",
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontStyle = FontStyle.Italic
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick  = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Open in Browser")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private fun formatFullDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "Unknown date"
    return try {
        val zdt = ZonedDateTime.parse(raw)
        zdt.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy • HH:mm"))
    } catch (e: Exception) {
        raw.take(10)
    }
}