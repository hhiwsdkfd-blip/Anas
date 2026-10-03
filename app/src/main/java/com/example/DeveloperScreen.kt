package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NightObsidian
import com.example.ui.theme.NightOutline
import com.example.ui.theme.NightSurface
import com.example.ui.theme.NightSurfaceElevated
import com.example.ui.theme.SleepAmber
import com.example.ui.theme.SleepAmberBright
import com.example.ui.theme.SleepAmberDim
import com.example.ui.theme.TextMutedNight
import com.example.ui.theme.TextPrimaryNight
import com.example.ui.theme.TextSecondaryNight
import com.example.ui.theme.WifiActiveGreen

private val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun DeveloperScreen(
  str: (Int) -> String,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  // Handle hardware back button to return to home timer
  BackHandler(onBack = onNavigateBack)

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 500.dp)
          .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header Section
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = str(R.string.developer_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimaryNight,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = str(R.string.developer_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryNight,
          )
        }
      }

      // Hero Profile Card with Developer Photo
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurfaceElevated),
          border =
            BorderStroke(
              2.dp,
              Brush.linearGradient(
                listOf(SleepAmber, SleepAmberBright, WifiActiveGreen, SleepAmber),
              ),
            ),
          modifier = Modifier.fillMaxWidth().testTag("developer_hero_card"),
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            // Circular Developer Photo with Gradient Frame
            Box(
              contentAlignment = Alignment.BottomEnd,
              modifier = Modifier.size(110.dp),
            ) {
              Box(
                modifier =
                  Modifier.size(110.dp)
                    .clip(CircleShape)
                    .background(
                      Brush.sweepGradient(
                        listOf(SleepAmber, SleepAmberBright, WifiActiveGreen, SleepAmber),
                      ),
                    )
                    .padding(3.5.dp),
              ) {
                Image(
                  painter = painterResource(id = R.drawable.developer_photo),
                  contentDescription = str(R.string.developer_name),
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxWidth().clip(CircleShape),
                )
              }

              // Verified Badge
              Surface(
                shape = CircleShape,
                color = WifiActiveGreen,
                border = BorderStroke(2.dp, NightSurfaceElevated),
                modifier = Modifier.size(30.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verified Creator",
                    tint = NightObsidian,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Developer Name
            Text(
              text = str(R.string.developer_name),
              style = MaterialTheme.typography.headlineSmall,
              color = TextPrimaryNight,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
            )

            // Title & Role
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(top = 4.dp),
            ) {
              Text(
                text = str(R.string.developer_title),
                style = MaterialTheme.typography.titleMedium,
                color = SleepAmberBright,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = "•",
                color = TextMutedNight,
              )
              Text(
                text = str(R.string.developer_verified_badge),
                style = MaterialTheme.typography.labelMedium,
                color = WifiActiveGreen,
                fontWeight = FontWeight.Bold,
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bio
            Text(
              text = str(R.string.developer_bio),
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondaryNight,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Contact Channels
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              // WhatsApp Official Button
              Button(
                onClick = { openWhatsApp(context, str(R.string.developer_phone_raw)) },
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = WhatsAppGreen,
                    contentColor = Color.White,
                  ),
                modifier =
                  Modifier.weight(1.3f)
                    .height(48.dp)
                    .testTag("dev_page_whatsapp_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Chat,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = str(R.string.developer_whatsapp_button),
                  fontWeight = FontWeight.Bold,
                )
              }

              // Direct Phone Call
              Button(
                onClick = { dialPhoneNumber(context, str(R.string.developer_phone_raw)) },
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = NightSurface,
                    contentColor = TextPrimaryNight,
                  ),
                border = BorderStroke(1.dp, NightOutline),
                modifier =
                  Modifier.weight(1f)
                    .height(48.dp)
                    .testTag("dev_page_call_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Phone,
                  contentDescription = null,
                  tint = SleepAmber,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = str(R.string.developer_call_button),
                  fontWeight = FontWeight.SemiBold,
                )
              }

              // Email Button
              Button(
                onClick = { sendEmailToDeveloper(context, str(R.string.developer_email_val)) },
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = NightSurface,
                    contentColor = TextPrimaryNight,
                  ),
                border = BorderStroke(1.dp, NightOutline),
                modifier =
                  Modifier.weight(1f)
                    .height(48.dp)
                    .testTag("dev_page_email_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Email,
                  contentDescription = null,
                  tint = SleepAmber,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = str(R.string.developer_contact_button),
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direct Phone & Email info display
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = NightSurface,
              border = BorderStroke(1.dp, NightOutline),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "📱 ${str(R.string.developer_phone_val)}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextPrimaryNight,
                  fontWeight = FontWeight.Medium,
                )
                Text(
                  text = "✉️ ${str(R.string.developer_email_val)}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextSecondaryNight,
                )
              }
            }
          }
        }
      }

      // App Highlights & Capabilities
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurface),
          border = BorderStroke(1.dp, NightOutline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = str(R.string.core_features_title),
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimaryNight,
              fontWeight = FontWeight.Bold,
            )

            FeatureItem(
              icon = Icons.Filled.Bedtime,
              title = str(R.string.title_wifi_sleep_timer),
              desc = str(R.string.feature_1_desc),
              color = SleepAmber,
            )
            FeatureItem(
              icon = Icons.Filled.DataUsage,
              title = str(R.string.data_usage_title),
              desc = str(R.string.feature_2_desc),
              color = WifiActiveGreen,
            )
            FeatureItem(
              icon = Icons.Filled.BatteryChargingFull,
              title = str(R.string.feature_shake_label),
              desc = str(R.string.feature_3_desc),
              color = SleepAmberBright,
            )
            FeatureItem(
              icon = Icons.Filled.Vibration,
              title = str(R.string.feature_vibrate_label),
              desc = str(R.string.feature_4_desc),
              color = TextSecondaryNight,
            )
          }
        }
      }

      // Privacy Guarantee Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurface),
          border = BorderStroke(1.dp, NightOutline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = WifiActiveGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = WifiActiveGreen,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
              Text(
                text = str(R.string.privacy_guarantee_title),
                style = MaterialTheme.typography.titleMedium,
                color = WifiActiveGreen,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = str(R.string.privacy_guarantee_desc),
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondaryNight,
              lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )
          }
        }
      }

      // Technical Specifications Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurface),
          border = BorderStroke(1.dp, NightOutline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(text = "إصدار التطبيق (Version)", style = MaterialTheme.typography.bodySmall, color = TextSecondaryNight)
              Text(text = str(R.string.app_version_val), style = MaterialTheme.typography.bodySmall, color = SleepAmberBright, fontWeight = FontWeight.Bold)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(text = "التوافق (System)", style = MaterialTheme.typography.bodySmall, color = TextSecondaryNight)
              Text(text = "Android 15 (Target SDK 35)", style = MaterialTheme.typography.bodySmall, color = TextPrimaryNight)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(text = "واجهة المستخدم (UI Engine)", style = MaterialTheme.typography.bodySmall, color = TextSecondaryNight)
              Text(text = "Jetpack Compose M3", style = MaterialTheme.typography.bodySmall, color = TextPrimaryNight)
            }
          }
        }
      }

      // Share App Button
      item {
        OutlinedButton(
          onClick = {
            shareApp(
              context = context,
              subject = str(R.string.share_app_subject),
              text = str(R.string.share_app_text),
            )
          },
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, SleepAmber.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dev_page_share_app_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = null,
            tint = SleepAmberBright,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = str(R.string.share_app_button),
            color = TextPrimaryNight,
            fontWeight = FontWeight.Bold,
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun FeatureItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  desc: String,
  color: Color,
) {
  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Surface(
      shape = CircleShape,
      color = color.copy(alpha = 0.15f),
      modifier = Modifier.size(32.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(18.dp),
        )
      }
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = TextPrimaryNight,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryNight,
      )
    }
  }
}

private fun openWhatsApp(context: Context, rawPhone: String) {
  try {
    val cleanNumber = rawPhone.replace("+", "").replace(" ", "").trim()
    val url = "https://wa.me/$cleanNumber?text=${Uri.encode("مرحباً بك أستاذ أنس، أتواصل معك بخصوص تطبيق مؤقت نوم الواي فاي ومراقب البيانات")}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}

private fun dialPhoneNumber(context: Context, rawPhone: String) {
  try {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$rawPhone")).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}

private fun sendEmailToDeveloper(context: Context, email: String) {
  try {
    val intent =
      Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$email")
        putExtra(Intent.EXTRA_SUBJECT, "تطبيق مؤقت نوم الواي فاي ومراقب البيانات")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    context.startActivity(intent)
  } catch (_: Exception) {
    try {
      val fallbackIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = "message/rfc822"
          putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
          putExtra(Intent.EXTRA_SUBJECT, "تطبيق مؤقت نوم الواي فاي ومراقب البيانات")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(Intent.createChooser(fallbackIntent, "Choose email client"))
    } catch (_: Exception) {}
  }
}

private fun shareApp(context: Context, subject: String, text: String) {
  try {
    val intent =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    context.startActivity(Intent.createChooser(intent, subject))
  } catch (_: Exception) {}
}
