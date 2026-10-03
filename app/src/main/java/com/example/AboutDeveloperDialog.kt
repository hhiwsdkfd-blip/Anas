package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
private val WhatsAppGreenDark = Color(0xFF1EBE5D)

@Composable
fun AboutDeveloperDialog(
  str: (Int) -> String,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NightSurfaceElevated,
    modifier = Modifier.testTag("about_developer_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = SleepAmberDim,
          modifier = Modifier.size(38.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Bedtime,
              contentDescription = null,
              tint = SleepAmber,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        Column {
          Text(
            text = str(R.string.title_wifi_sleep_timer),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimaryNight,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = str(R.string.app_version_val),
            style = MaterialTheme.typography.bodySmall,
            color = SleepAmberBright,
          )
        }
      }
    },
    text = {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Hero Developer Card with Photo & Full Name
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurface),
          border =
            BorderStroke(
              1.5.dp,
              Brush.linearGradient(
                listOf(SleepAmber, SleepAmberBright, WifiActiveGreen),
              ),
            ),
          modifier = Modifier.fillMaxWidth().testTag("developer_profile_card"),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            // Circular Developer Photo with Gradient Ring
            Box(
              contentAlignment = Alignment.BottomEnd,
              modifier = Modifier.size(96.dp),
            ) {
              Box(
                modifier =
                  Modifier.size(96.dp)
                    .clip(CircleShape)
                    .background(
                      Brush.sweepGradient(
                        listOf(SleepAmber, SleepAmberBright, WifiActiveGreen, SleepAmber),
                      ),
                    )
                    .padding(3.dp),
              ) {
                Image(
                  painter = painterResource(id = R.drawable.developer_photo),
                  contentDescription = str(R.string.developer_name),
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxWidth().clip(CircleShape),
                )
              }

              // Verified Checkmark Badge
              Surface(
                shape = CircleShape,
                color = WifiActiveGreen,
                border = BorderStroke(2.dp, NightSurface),
                modifier = Modifier.size(26.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verified",
                    tint = NightObsidian,
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Full Developer Name
            Text(
              text = str(R.string.developer_name),
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimaryNight,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
            )

            // Developer Title & Verified Tag
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.padding(top = 2.dp),
            ) {
              Text(
                text = str(R.string.developer_title),
                style = MaterialTheme.typography.bodyMedium,
                color = SleepAmberBright,
                fontWeight = FontWeight.Medium,
              )
              Text(
                text = "•",
                color = TextMutedNight,
              )
              Text(
                text = str(R.string.developer_verified_badge),
                style = MaterialTheme.typography.labelSmall,
                color = WifiActiveGreen,
                fontWeight = FontWeight.Bold,
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bio Note
            Text(
              text = str(R.string.developer_bio),
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryNight,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Contact Buttons Row (WhatsApp, Call, Email)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              // WhatsApp Button (Official Green)
              Button(
                onClick = {
                  openWhatsApp(context, str(R.string.developer_phone_raw))
                },
                shape = RoundedCornerShape(12.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = WhatsAppGreen,
                    contentColor = Color.White,
                  ),
                modifier =
                  Modifier.weight(1.3f)
                    .height(44.dp)
                    .testTag("whatsapp_contact_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Chat,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = str(R.string.developer_whatsapp_button),
                  fontWeight = FontWeight.Bold,
                )
              }

              // Call Phone Button
              Button(
                onClick = {
                  dialPhoneNumber(context, str(R.string.developer_phone_raw))
                },
                shape = RoundedCornerShape(12.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = NightSurfaceElevated,
                    contentColor = TextPrimaryNight,
                  ),
                border = BorderStroke(1.dp, NightOutline),
                modifier =
                  Modifier.weight(1f)
                    .height(44.dp)
                    .testTag("call_phone_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Call,
                  contentDescription = null,
                  tint = SleepAmber,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = str(R.string.developer_call_button),
                  fontWeight = FontWeight.SemiBold,
                )
              }

              // Email Button
              Button(
                onClick = {
                  sendEmailToDeveloper(context, str(R.string.developer_email_val))
                },
                shape = RoundedCornerShape(12.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = NightSurfaceElevated,
                    contentColor = TextPrimaryNight,
                  ),
                border = BorderStroke(1.dp, NightOutline),
                modifier =
                  Modifier.weight(1f)
                    .height(44.dp)
                    .testTag("email_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Email,
                  contentDescription = null,
                  tint = SleepAmber,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = str(R.string.developer_contact_button),
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }

            // Display Phone & Email under buttons for easy reading
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "📱 ${str(R.string.developer_phone_val)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryNight,
              )
              Text(
                text = "✉️ ${str(R.string.developer_email_val)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryNight,
              )
            }
          }
        }

        // Core Features Summary
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurface),
          border = BorderStroke(1.dp, NightOutline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = str(R.string.core_features_title),
              style = MaterialTheme.typography.titleSmall,
              color = TextPrimaryNight,
              fontWeight = FontWeight.Bold,
            )

            FeatureBullet(icon = Icons.Filled.Bedtime, text = str(R.string.feature_1_desc), tint = SleepAmber)
            FeatureBullet(icon = Icons.Filled.DataUsage, text = str(R.string.feature_2_desc), tint = WifiActiveGreen)
            FeatureBullet(icon = Icons.Filled.BatteryChargingFull, text = str(R.string.feature_3_desc), tint = SleepAmberBright)
            FeatureBullet(icon = Icons.Filled.Vibration, text = str(R.string.feature_4_desc), tint = TextSecondaryNight)
          }
        }

        // Privacy Guarantee Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurface),
          border = BorderStroke(1.dp, NightOutline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = WifiActiveGreen,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = str(R.string.privacy_guarantee_title),
                style = MaterialTheme.typography.titleSmall,
                color = WifiActiveGreen,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = str(R.string.privacy_guarantee_desc),
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryNight,
            )
          }
        }

        // Share App Button
        OutlinedButton(
          onClick = {
            shareApp(
              context = context,
              subject = str(R.string.share_app_subject),
              text = str(R.string.share_app_text),
            )
          },
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, NightOutline),
          modifier = Modifier.fillMaxWidth().height(44.dp).testTag("share_app_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = null,
            tint = TextPrimaryNight,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = str(R.string.share_app_button),
            color = TextPrimaryNight,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(text = str(R.string.close_button), color = SleepAmberBright, fontWeight = FontWeight.Bold)
      }
    },
  )
}

@Composable
private fun FeatureBullet(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String,
  tint: androidx.compose.ui.graphics.Color,
) {
  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(16.dp).padding(top = 2.dp),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondaryNight,
      modifier = Modifier.weight(1f),
    )
  }
}

private fun openWhatsApp(context: Context, rawPhone: String) {
  try {
    val cleanNumber = rawPhone.replace("+", "").replace(" ", "").trim()
    val url = "https://wa.me/$cleanNumber?text=${Uri.encode("مرحباً بك أستاذ أنس، أتواصل معك بخصوص تطبيق مؤقت نوم الواي فاي")}"
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
