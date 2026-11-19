package com.wherever.mobile.feature.friends.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.profile.ui.ProfileAvatar

/** Fila de persona: foto o inicial, nombre, @usuario y acciones a la derecha. */
@Composable
internal fun PersonRow(
    person: Person,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    detail: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    SurfaceCard(modifier = modifier.testTag("person_${person.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                .padding(12.dp)
        ) {
            ProfileAvatar(person.name, person.photoUrl, size = 44.dp)
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    person.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val secondary = detail ?: person.username?.let { stringResource(R.string.friends_username_at, it) }
                secondary?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            actions()
        }
    }
}
