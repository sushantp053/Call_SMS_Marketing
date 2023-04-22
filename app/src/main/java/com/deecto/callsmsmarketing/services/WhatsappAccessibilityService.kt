package com.deecto.callsmsmarketing.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class WhatsappAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (rootInActiveWindow == null) {
            return
        }
        val rootInActiveWindow = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)

        val messageNodeList =
            rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry")
        if (messageNodeList == null || messageNodeList.isEmpty()) {
            return
        }

        val messageField = messageNodeList[0]
        if (messageField.text == null || messageField.text.isEmpty()
        ) {
            return
        }

        val sendMessageNodeInfoList =
            rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
        if (sendMessageNodeInfoList == null || sendMessageNodeInfoList.isEmpty()) {
            return
        }
        val sendMessageButton = sendMessageNodeInfoList[0]
        if (!sendMessageButton.isVisibleToUser) {
            return
        }
        sendMessageButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Log.e("Accessibility 39", "Send Action Performed")

        try {
//            Thread.sleep(500)

            Log.e("Accessibility 44", "Back Action Performed")
            performGlobalAction(GLOBAL_ACTION_BACK)
//            Thread.sleep(500)
        } catch (ignored: InterruptedException) {
            Log.e("WAS 48 Error", ignored.toString())
        }
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onInterrupt() {
    }
}
