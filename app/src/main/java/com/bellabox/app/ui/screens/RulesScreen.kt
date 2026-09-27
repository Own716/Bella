package com.bellabox.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CallSplit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellabox.app.ui.theme.BellaShapes
import com.bellabox.app.ui.theme.StatusConnected
import com.bellabox.app.ui.theme.StatusFailed
import com.bellabox.core.model.RouteRule
import com.bellabox.core.model.RuleActionType
import com.bellabox.core.model.RuleType

@Composable
fun RulesScreen(modifier: Modifier = Modifier) {
    val sampleRules = remember {
        mutableListOf(
            RouteRule(
                id = 1,
                name = "Domestic Direct (GeoIP CN)",
                ruleType = RuleType.GEOIP,
                values = listOf("cn", "private"),
                action = RuleActionType.DIRECT
            ),
            RouteRule(
                id = 2,
                name = "China Services (Geosite CN)",
                ruleType = RuleType.GEOSITE,
                values = listOf("cn"),
                action = RuleActionType.DIRECT
            ),
            RouteRule(
                id = 3,
                name = "AI Services (ChatGPT, Claude, Gemini)",
                ruleType = RuleType.GEOSITE,
                values = listOf("openai", "anthropic", "google"),
                action = RuleActionType.PROXY
            ),
            RouteRule(
                id = 4,
                name = "Malicious Ads & Trackers",
                ruleType = RuleType.GEOSITE,
                values = listOf("category-ads-all"),
                action = RuleActionType.BLOCK
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Route,
                contentDescription = "Rules",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.padding(start = 10.dp))
            Column {
                Text(
                    text = "Routing Rules",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Traffic split by Geosite, GeoIP, Domain & Package",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 96.dp)
        ) {
            items(sampleRules) { rule ->
                var isEnabled by remember { mutableStateOf(rule.isEnabled) }

                Card(
                    shape = BellaShapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action Badge icon
                        val (icon, color) = when (rule.action) {
                            RuleActionType.DIRECT -> Pair(Icons.Rounded.CheckCircle, StatusConnected)
                            RuleActionType.PROXY -> Pair(Icons.Rounded.CallSplit, MaterialTheme.colorScheme.primary)
                            RuleActionType.BLOCK -> Pair(Icons.Rounded.Block, StatusFailed)
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = rule.action.name,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.padding(start = 12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${rule.ruleType.label}: ${rule.values.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it }
                        )
                    }
                }
            }
        }
    }
}
