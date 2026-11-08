package com.athimue.pleinmalin.core.domain

/** Erreurs que la couche data peut remonter, indépendantes de Ktor. */
enum class DataError {
    NO_INTERNET,
    TIMEOUT,
    SERVER,
    NOT_FOUND,
    SERIALIZATION,
    UNKNOWN,
}
