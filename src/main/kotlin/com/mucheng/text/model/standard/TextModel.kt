package com.mucheng.text.model.standard

import com.mucheng.text.model.base.AbstractTextModel

@Suppress("unused", "LeakingThis")
open class TextModel(capacity: Int, threadSafe: Boolean) : AbstractTextModel(capacity, threadSafe) {

    constructor(capacity: Int) : this(capacity, true)

    constructor(threadSafe: Boolean) : this(DEFAULT_CAPACITY, threadSafe)

    constructor() : this(DEFAULT_CAPACITY, true)

    constructor(charSequence: CharSequence) : this(charSequence.length) {
        append(charSequence)
    }

    constructor(charSequence: CharSequence, threadSafe: Boolean) : this(charSequence.length, threadSafe) {
        append(charSequence)
    }

}