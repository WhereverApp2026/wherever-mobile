package com.wherever.mobile.feature.vaca.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class PersonBalanceDto(
    @SerialName("usuario_id") val userId: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("puso") val put: Long = 0,
    @SerialName("le_toca") val share: Long = 0,
    @SerialName("saldo") val balance: Long = 0
)

/** `deudor` y `acreedor` pueden llegar como id o como `{id, nombre}`: se leen de las dos formas. */
@Serializable
data class DebtDto(
    @SerialName("id") val id: String,
    @SerialName("deudor") val debtor: JsonElement,
    @SerialName("acreedor") val creditor: JsonElement,
    @SerialName("monto_centavos") val amount: Long = 0,
    @SerialName("estado") val status: String = "pendiente"
)

@Serializable
data class VacaDto(
    @SerialName("total_centavos") val total: Long = 0,
    @SerialName("por_persona") val people: List<PersonBalanceDto> = emptyList(),
    @SerialName("deudas") val debts: List<DebtDto> = emptyList()
)

@Serializable
data class ExpenseDto(
    @SerialName("id") val id: String,
    @SerialName("monto_centavos") val amount: Long = 0,
    @SerialName("concepto") val concept: String = "otro",
    @SerialName("descripcion") val description: String? = null,
    @SerialName("pagado_por") val paidBy: JsonElement? = null
)

@Serializable
data class ExpensePageDto(@SerialName("items") val items: List<ExpenseDto> = emptyList())

@Serializable
data class CustomAmountDto(@SerialName("usuario_id") val userId: String, @SerialName("monto_centavos") val amount: Long)

@Serializable
data class NewExpenseDto(
    @SerialName("monto_centavos") val amount: Long,
    @SerialName("concepto") val concept: String,
    @SerialName("descripcion") val description: String? = null,
    @SerialName("pagado_por") val paidBy: String,
    @SerialName("dividir_entre") val splitBetween: List<String>,
    @SerialName("division") val mode: String,
    @SerialName("montos_personalizados") val custom: List<CustomAmountDto>? = null,
    @SerialName("plan_id") val planId: String? = null,
    @SerialName("id_cliente") val clientId: String
)

@Serializable
data class PayDebtDto(@SerialName("estado") val status: String)

/** wherever-backend `docs/endpoints.md`: vaca virtual y gastos del grupo (specs/nv-26). */
interface VacaApi {
    @GET("grupos/{id}/vaca")
    suspend fun vaca(@Path("id") groupId: String): VacaDto

    @GET("grupos/{id}/gastos")
    suspend fun expenses(@Path("id") groupId: String, @Query("limite") limit: Int): ExpensePageDto

    @POST("grupos/{id}/gastos")
    suspend fun addExpense(@Path("id") groupId: String, @Body body: NewExpenseDto)

    @PATCH("vaca/deudas/{id}")
    suspend fun payDebt(@Path("id") debtId: String, @Body body: PayDebtDto)
}
