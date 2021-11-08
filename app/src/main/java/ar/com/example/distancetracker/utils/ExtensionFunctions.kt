package ar.com.example.distancetracker.utils

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

object ExtensionFunctions {

    fun View.show(){
        this.isVisible = true
    }
    fun View.hide(){
        this.isVisible = false
    }

    fun Button.enable(){
        this.isEnabled = true
    }
    fun Button.disable(){
        this.isEnabled = false
    }

    fun Fragment.toast(context: Context, message:String, isLengthShort: Boolean){
        if (isLengthShort){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }else Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


}