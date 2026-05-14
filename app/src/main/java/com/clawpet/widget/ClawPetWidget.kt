package com.clawpet.widget

import android.content.Context
import android.content.Intent
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.clawpet.domain.*
import com.clawpet.ui.MainActivity

class ClawPetWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = (context.applicationContext as? com.clawpet.ClawPetApp)
            ?.let { dagger.hilt.android.EntryPointAccessors.fromApplication(it, WidgetRepoEntryPoint::class.java) }
            ?.petRepository() ?: return

        val state = repo.getPet()

        provideContent {
            WidgetContent(state)
        }
    }

    @Composable
    private fun WidgetContent(state: PetState) {
        val moodEmoji = state.mood.emoji
        val hungerLabel = "🍖 ${state.hunger}%"
        val happyLabel = "😊 ${state.happiness}%"
        val energyLabel = "⚡ ${state.energy}%"

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity(Intent(context = null, MainActivity::class.java)))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                // Pet emoji + name
                Text(
                    text = "$moodEmoji ${state.name}",
                    style = TextStyle(fontSize = androidx.glance.unit.TextUnit.Sp(28))
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Lv.${state.level} • ${state.xp}/${state.xpToNext} XP",
                    style = TextStyle(fontSize = androidx.glance.unit.TextUnit.Sp(12))
                )
                Spacer(modifier = GlanceModifier.height(8.dp))

                // Stats
                Text(text = hungerLabel, style = TextStyle(fontSize = androidx.glance.unit.TextUnit.Sp(14)))
                Text(text = happyLabel, style = TextStyle(fontSize = androidx.glance.unit.TextUnit.Sp(14)))
                Text(text = energyLabel, style = TextStyle(fontSize = androidx.glance.unit.TextUnit.Sp(14)))
                Spacer(modifier = GlanceModifier.height(8.dp))

                // Quick action buttons
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Button(
                        text = "Feed",
                        onClick = actionRunCallback<FeedAction>()
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Button(
                        text = "Pet",
                        onClick = actionRunCallback<PetActionCallback>()
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Button(
                        text = "Play",
                        onClick = actionRunCallback<PlayActionCallback>()
                    )
                }
            }
        }
    }
}

// Action callbacks that interact with the repository
class FeedAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, params: ActionParameters) {
        val repo = getRepo(context) ?: return
        repo.performAction(PetAction.FEED)
        ClawPetWidget().update(context, glanceId)
    }
}

class PetActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, params: ActionParameters) {
        val repo = getRepo(context) ?: return
        repo.performAction(com.clawpet.domain.PetAction.PET)
        ClawPetWidget().update(context, glanceId)
    }
}

class PlayActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, params: ActionParameters) {
        val repo = getRepo(context) ?: return
        repo.performAction(com.clawpet.domain.PetAction.PLAY)
        ClawPetWidget().update(context, glanceId)
    }
}

private fun getRepo(context: Context): PetRepository? {
    val app = context.applicationContext as? com.clawpet.ClawPetApp ?: return null
    val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(app, WidgetRepoEntryPoint::class.java)
    return entryPoint.petRepository()
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface WidgetRepoEntryPoint {
    fun petRepository(): PetRepository
}

class ClawPetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ClawPetWidget()
}