package com.athimue.pleinmalin.core.domain

/**
 * Résultat typé d'une opération : succès avec une donnée, ou échec avec une erreur métier.
 * Préféré à kotlin.Result pour avoir des erreurs exhaustives (sealed/enum) côté UI.
 */
sealed interface Outcome<out D, out E> {
    data class Success<out D>(val data: D) : Outcome<D, Nothing>
    data class Failure<out E>(val error: E) : Outcome<Nothing, E>
}

inline fun <D, E, R> Outcome<D, E>.map(transform: (D) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Failure -> this
}

inline fun <D, E> Outcome<D, E>.onSuccess(action: (D) -> Unit): Outcome<D, E> {
    if (this is Outcome.Success) action(data)
    return this
}

inline fun <D, E> Outcome<D, E>.onFailure(action: (E) -> Unit): Outcome<D, E> {
    if (this is Outcome.Failure) action(error)
    return this
}
