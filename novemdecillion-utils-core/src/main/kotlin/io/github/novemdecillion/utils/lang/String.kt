package io.github.novemdecillion.utils.lang

fun String?.nullOrNotEmpty(): String? {
  if (this.isNullOrEmpty()) {
    return null
  }
  return this
}

fun String?.nullOrNotBlank(): String? {
  if (this.isNullOrBlank()) {
    return null
  }
  return this
}

