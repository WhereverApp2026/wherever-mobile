package com.wherever.mobile.feature.account.domain

import com.wherever.mobile.core.common.Result

/** Seguridad y datos de la cuenta (specs/nv-51): contraseña, correo, exportación e historial. */
interface AccountRepository {
    /** `PUT /auth/password`; la API cierra las otras sesiones. */
    suspend fun changePassword(current: String, new: String): Result<Unit>

    /** `PUT /usuarios/yo/correo`; el correo nuevo queda sin verificar. Refresca el perfil en Room. */
    suspend fun changeEmail(newEmail: String, password: String): Result<Unit>

    /** `POST /usuarios/yo/exportar`; el enlace llega por correo. Una por día (429). */
    suspend fun requestDataExport(): Result<Unit>

    /** `DELETE /usuarios/yo/historial`; conserva la cuenta. */
    suspend fun clearHistory(): Result<Unit>
}
