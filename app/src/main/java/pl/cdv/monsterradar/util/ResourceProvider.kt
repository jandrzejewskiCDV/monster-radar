package pl.cdv.monsterradar.util

import android.content.Context
import androidx.annotation.StringRes
import pl.cdv.monsterradar.R

class ResourceProvider(private val context: Context) {

    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    fun getFloat(resId: Int): Float {
        val typedValue = android.util.TypedValue()
        context.resources.getValue(resId, typedValue, true)
        return typedValue.float
    }

    fun getInteger(resId: Int): Int {
        return context.resources.getInteger(resId)
    }
}
