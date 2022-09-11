package com.mucheng.text.model.mark

/**
 * 此注解修饰后缀为 "Unsafe" 的 API.
 * */
@RequiresOptIn(
    message = "This API is unsafe, you should use without the \"Unsafe\" API instead.",
    level = RequiresOptIn.Level.WARNING
)
annotation class UnsafeApi