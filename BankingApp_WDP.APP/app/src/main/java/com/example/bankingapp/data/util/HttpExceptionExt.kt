package com.example.bankingapp.data.util

import org.json.JSONObject

/**
 * Reads the error body returned by the API and extracts a human-readable message.
 *
 * Handles two formats produced by the .NET backend:
 *  - Custom errors:          { "message": "..." }
 *  - ASP.NET Core validation: { "title": "...", "errors": { "Field": ["msg", ...] } }
 *
 * Returns null if the body is empty, not JSON, or contains no recognisable message field.
 */
fun retrofit2.HttpException.parseApiMessage(): String? {
    return try {
        val body = response()?.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: return null
        val json = JSONObject(body)

        // 1. { "message": "..." }
        json.optString("message").takeIf { it.isNotBlank() }
            // 2. ASP.NET Core validation: join first error from each field
            ?: json.optJSONObject("errors")?.let { errors ->
                val messages = mutableListOf<String>()
                errors.keys().forEach { key ->
                    val arr = errors.optJSONArray(key)
                    if (arr != null && arr.length() > 0) {
                        messages.add(arr.getString(0))
                    }
                }
                messages.joinToString("\n").takeIf { it.isNotBlank() }
            }
            // 3. { "title": "..." } – ProblemDetails fallback
            ?: json.optString("title").takeIf { it.isNotBlank() && it != "One or more validation errors occurred." }
    } catch (_: Exception) {
        null
    }
}
