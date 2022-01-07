package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import com.hs.accessibility.utils.AutoUtil.findNodeInfoById
import com.hs.accessibility.utils.AutoUtil.performScroll
import com.hs.accessibility.utils.AutoUtil.performSetText
import kotlinx.coroutines.*

private const val keyword = "Messages"

class MyAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootInActiveWindow = rootInActiveWindow ?: return

        scope.launch {
            findNodeInfoById(
                rootInActiveWindow,
                "com.google.android.googlequicksearchbox:id/googleapp_search_box"
            )?.let { nodeInfo ->
                if (nodeInfo.text == null || !nodeInfo.text.toString()
                        .equals(keyword, ignoreCase = true)
                ) performSetText(
                    nodeInfo,
                    keyword, "set keyword in search box", SET_TEXT_ACTION
                )
                else {
                    Log.d("ORZ", "IME")
                    // require API 30 up
                    nodeInfo.performAction(ACTION_IME_ENTER.id)
                }
            }
            delay(5000)
            findNodeInfoById(
                rootInActiveWindow,
                "com.google.android.googlequicksearchbox:id/webx_web_container"
            )?.let {
                val webViewNodeInfo = it.getChild(0)
                performScroll(
                    webViewNodeInfo.getChild(0),
                    "Scroll to Bottom",
                    SCROLL_ACTION
                )
            }

        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
