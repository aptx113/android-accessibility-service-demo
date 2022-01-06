package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import com.hs.accessibility.utils.AutoUtil.findNodeInfoById
import com.hs.accessibility.utils.AutoUtil.performScroll
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

//        findNodeInfoById(
//            rootInActiveWindow,
//            "com.google.android.apps.nexuslauncher:id/g_icon"
//        )?.let {
//            performClick(it, "Idle SearchBox Icon", CLICK_ACTION, scope)
//        }

//        findNodeInfoById(
//            rootInActiveWindow,
//            "com.google.android.googlequicksearchbox:id/googleapp_search_box"
//        )?.let { if (it.)  }

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
            else {
//                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2) {nodeInfo.performAction(AccessibilityNodeInfo.ACTION_IME_ENTER}
            }
        }

        findNodeInfoById(
            rootInActiveWindow,
            "com.google.android.googlequicksearchbox:id/webx_web_container"
        )?.let {
            val webViewNodeInfo = it.getChild(0)
            if (webViewNodeInfo.getChild(0).className == "android.webkit.WebView") performScroll(
                webViewNodeInfo.getChild(0),
                "Scroll to Bottom",
                SCROLL_ACTION
            )
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
