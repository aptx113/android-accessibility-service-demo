package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("ORZ", "create")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("GG service on", "service on")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootInActiveWindow = rootInActiveWindow

        if (rootInActiveWindow != null) {
            val searchBarIdle =
                rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.google.android.apps.nexuslauncher:id/g_icon")
            if (searchBarIdle.size > 0) {
                val searchBar = searchBarIdle[0]
                searchBar.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            val searchBars =
                rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.google.android.googlequicksearchbox:id/googleapp_search_box")
            if (searchBars.size > 0) {
                val searchBar = searchBars[0]
                val args = Bundle()
                args.putString(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    "messages"
                )
                searchBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

                val searchSuggestions =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.google.android.googlequicksearchbox:id/googleapp_app_name")
                searchSuggestions.forEach {
                    if (it.text.toString() == "Messages") {
                        val clickableParent = it.parent
                        clickableParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                searchBar.recycle()
            }
        }
    }

    override fun onInterrupt() {
    }
}
