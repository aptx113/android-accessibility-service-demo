package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.hs.accessibility.utils.AutoUtil.autoDelay
import com.hs.accessibility.utils.AutoUtil.findNodeInfoById
import com.hs.accessibility.utils.AutoUtil.findNodeInfoListById
import com.hs.accessibility.utils.AutoUtil.performClick
import com.hs.accessibility.utils.AutoUtil.performClickSuggestion
import com.hs.accessibility.utils.AutoUtil.performSetText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

private const val keyword = "Messages"

class MyAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootInActiveWindow = rootInActiveWindow ?: return

        findNodeInfoById(
            rootInActiveWindow,
            "com.google.android.apps.nexuslauncher:id/g_icon"
        )?.let {
            performClick(it, "Idle SearchBox Icon", CLICK_ACTION, scope)
        }

        findNodeInfoById(
            rootInActiveWindow,
            "com.google.android.googlequicksearchbox:id/googleapp_search_box"
        )?.let { nodeInfo ->
            if (nodeInfo.text == null || !nodeInfo.text.toString()
                    .equals(keyword, ignoreCase = true)
            ) performSetText(
                nodeInfo,
                keyword, "set keyword in search box", SET_TEXT_ACTION, scope
            )
            else findNodeInfoListById(
                rootInActiveWindow,
                "com.google.android.googlequicksearchbox:id/googleapp_app_name"
            )?.let {
                autoDelay(scope, 3000L)
                performClickSuggestion(it, keyword, "search suggestion", CLICK_ACTION,scope)
            }
        }
//
//        if (rootInActiveWindow != null) {
//            val searchBarIdle =
//                rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.google.android.apps.nexuslauncher:id/g_icon")
//            if (searchBarIdle.size > 0) {
//                val searchBar = searchBarIdle[0]
//                searchBar.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//            }
//            val searchBars =
//                rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.google.android.googlequicksearchbox:id/googleapp_search_box")
//            if (searchBars.size > 0) {
//                val searchBar = searchBars[0]
//                if (searchBar.text == null || !searchBar.text.toString()
//                        .equals("Messages", ignoreCase = true)
//                ) {
//                    val args = Bundle()
//                    args.putString(
//                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
//                        "messages"
//                    )
//                    searchBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
//                } else {
//                    val searchSuggestions =
//                        rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.google.android.googlequicksearchbox:id/googleapp_app_name")
//                    searchSuggestions.forEach {
//                        if (it.text.toString() == "Messages") {
//                            val clickableParent = it.parent
//                            clickableParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                        }
//                    }
//                }
//                searchBar.recycle()
//            }
//        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
