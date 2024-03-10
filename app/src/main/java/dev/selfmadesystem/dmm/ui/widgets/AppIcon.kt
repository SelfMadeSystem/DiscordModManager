package dev.selfmadesystem.dmm.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.utils.DiscordVersion

@Composable
fun AppIcon(
    customIcon: Boolean,
    releaseChannel: DiscordVersion.Type,
    modifier: Modifier = Modifier
) {
    val iconColor = remember(customIcon, releaseChannel) {
        when {
            customIcon -> Color(0xFF3AB8BA)
            releaseChannel == DiscordVersion.Type.ALPHA -> Color(0xFFFBB33C)
            else -> Color(0xFF5865F2)
        }
    }

    Image(
        painter = painterResource(id = R.drawable.ic_discord_icon),
        contentDescription = null,
        modifier = modifier
            .clip(CircleShape)
            .background(iconColor)
    )
}