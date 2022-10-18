@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*

val isMac = System.getProperty("os.name").contains("mac", ignoreCase = true)

val KeyEvent.isCtrlOrMetaPressed: Boolean
   get() = if (isMac) {
      this.isMetaPressed
   } else {
      this.isCtrlPressed
   }

object KeyShortcuts {
   val newTab = if(isMac) {
      KeyShortcut(Key.N, meta = true)
   } else {
      KeyShortcut(Key.N, ctrl = true)
   }
   val openFile = if(isMac) {
      KeyShortcut(Key.O, meta = true)
   } else {
      KeyShortcut(Key.O, ctrl = true)
   }
   val previousTab = if(isMac) {
      KeyShortcut(Key.LeftBracket, meta = true, shift = true)
   } else {
      KeyShortcut(Key.LeftBracket, ctrl = true, shift = true)
   }
   val nextTab = if(isMac) {
      KeyShortcut(Key.RightBracket, meta = true, shift = true)
   } else {
      KeyShortcut(Key.RightBracket, ctrl = true, shift = true)
   }
   val closeTab = if(isMac) {
      KeyShortcut(Key.W, meta = true)
   } else {
      KeyShortcut(Key.W, ctrl = true)
   }
   val preferences = if(isMac) {
      KeyShortcut(Key.Comma, meta = true)
   } else {
      KeyShortcut(Key.Comma, ctrl = true)
   }
}