package com.example.newsapp_restapi.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.newsapp_restapi.data.model.Article
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NewsCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            if (!article.urlToImage.isNullOrBlank()) {
                AsyncImage(
                    model              = article.urlToImage,
                    contentDescription = article.safeTitle,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale       = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick  = {},
                        label    = {
                            Text(
                                text     = article.safeSource,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                    Text(
                        text  = formatDate(article.publishedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text     = article.safeTitle,
                    style    = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                if (article.description?.isNotBlank() == true) {
                    Text(
                        text     = article.safeDescription,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (article.author?.isNotBlank() == true) {
                    Text(
                        text  = "By ${article.safeAuthor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun NewsCardFeatured(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(200.dp)
            .clickable { onClick() },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box {
            if (!article.urlToImage.isNullOrBlank()) {
                AsyncImage(
                    model              = article.urlToImage,
                    contentDescription = article.safeTitle,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
            }
            Surface(
                modifier = Modifier.fillMaxSize(),
                color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ) {}
            Column(
                modifier            = Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                SuggestionChip(
                    onClick = {},
                    label   = { Text(article.safeSource, fontSize = 10.sp, maxLines = 1) }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = article.safeTitle,
                    style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NewsCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .shimmerEffect()
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).shimmerEffect())
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).shimmerEffect())
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp).shimmerEffect())
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).shimmerEffect())
            }
        }
    }
}

private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val zdt = ZonedDateTime.parse(raw)
        zdt.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    } catch (e: Exception) {
        raw.take(10)
    }
}

fun Modifier.shimmerEffect(): Modifier = this.clip(RoundedCornerShape(8.dp))
