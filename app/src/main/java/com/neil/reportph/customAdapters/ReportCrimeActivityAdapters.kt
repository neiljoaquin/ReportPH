package com.neil.reportph.customAdapters

import androidx.databinding.BindingAdapter
import android.text.method.KeyListener
import android.view.View
import android.widget.*

@BindingAdapter("keyListener")
fun setKeyListener(edit_text: EditText, listener: KeyListener?) {
    edit_text.keyListener = listener
}

@BindingAdapter("onClickListener")
fun setOnClickListener(edit_text: EditText, listener: View.OnClickListener) {
    edit_text.setOnClickListener(listener)
}

@BindingAdapter("focusChangeListener")
fun setFocusChangeListener(edit_text: EditText, listener: View.OnFocusChangeListener) {
    edit_text.setOnFocusChangeListener(listener)
}

@BindingAdapter("navigationOnClickListener")
fun setNavigationOnClickListener(toolbar: Toolbar, listener: View.OnClickListener) {
    //TODO find equivalent for lower version
    toolbar.setNavigationOnClickListener(listener)
}
