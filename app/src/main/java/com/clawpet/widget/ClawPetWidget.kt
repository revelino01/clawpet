package com.clawpet.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.clawpet.domain.*

class ClawPetWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = getRepo(context)
        val state = repo?.getPet() ?: PetState()

        provideContent {
            WidgetContent(state)
        }
    }
}

@GlanceComposable
@Composable
private fun WidgetContent(state: PetState) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "${state.mood.emoji} ${state.name}",
            style = TextStyle(fontSize = 28.sp)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Lv.${state.level} • ${state.xp}/${state.xpToNext} XP",
            style = TextStyle(fontSize = 12.sp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(text = "🍖 Hunger: ${state.hunger}%", style = TextStyle(fontSize = 14.sp))
        Text(text = "😊 Happy: ${state.happiness}%", style = TextStyle(fontSize = 14.sp))
        Text(text = "⚡ Energy: ${state.energy}%", style = TextStyle(fontSize = 14.sp))
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Button(text = "Feed", onClick = actionRunCallback<FeedAction>())
            Spacer(modifier = GlanceModifier.width(8.dp))
            Button(text = "Pet", onClick = actionRunCallback<PetActionCallback>())
            Spacer(modifier = GlanceModifier.width(8.dp))
            Button(text = "Play", onClick = actionRunCallback<PlayActionCallback>())
        }
    }
}

class FeedAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, params: ActionParameters) {
        getRepo(context)?.performAction(PetAction.FEED)
        ClawPetWidget().update(context, glanceId)
    }
}

class PetActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, params: ActionParameters) {
        getRepo(context)?.performAction(PetAction.PET)
        ClawPetWidget().update(context, glanceId)
    }
}

class PlayActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, params: ActionParameters) {
        getRepo(context)?.performAction(PetAction.PLAY)
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