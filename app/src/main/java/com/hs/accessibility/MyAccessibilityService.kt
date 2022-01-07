package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import com.hs.accessibility.utils.AutoUtil.findNodeInfoById
import com.hs.accessibility.utils.AutoUtil.findNodeInfoByText
import com.hs.accessibility.utils.AutoUtil.logDebugMsg
import com.hs.accessibility.utils.AutoUtil.performScroll
import com.hs.accessibility.utils.AutoUtil.performSetText
import kotlinx.coroutines.*

private const val keyword = "disneyland"

class MyAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var accessibilityNodeInfoWebView: AccessibilityNodeInfo? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootInActiveWindow = rootInActiveWindow ?: return

        findNodeInfoByText(rootInActiveWindow, "Fri, Jan 7")?.let {
            logDebugMsg("ORZ", "${it.text}")
        }

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
                    // require API 30 up
                    nodeInfo.performAction(ACTION_IME_ENTER.id)
                }
            }



            findNodeInfoById(
                rootInActiveWindow,
                "com.google.android.googlequicksearchbox:id/webx_web_container"
            )?.let {
                val searchResult = it.getChild(0).getChild(0)
                if (searchResult != null)
                    if (searchResult.childCount > 0) findNodeInfoByText(
                        rootInActiveWindow,
                        "All"
                    )?.let {
                        logDebugMsg("OOOOOO", "${it.text}")
                    }
            }


//            findNodeInfoById(
//                rootInActiveWindow,
//                "com.google.android.googlequicksearchbox:id/webx_web_container"
//            )?.let {
//                val googleSearchKeyword = it.getChild(0).getChild(0)
//                val gsr = googleSearchKeyword.getChild(2)
//                val main = gsr.getChild(0)
//                val cnt = main.getChild(0)
//                val centerCol = cnt.getChild(3)
////                val temp = centerCol.getChild(2)
//                if (centerCol != null) {
//                    val rso = centerCol.getChild(2).getChild(1)
//                    var i = 0
//                    while (i < rso.childCount) {
//                        val result = rso.getChild(i).getChild(1).getChild(0).getChild(0)
//                        if (result.isFocusable && !result.contentDescription.isNullOrEmpty()) {
//                            println(result.getChild(1).text)
//                        }
//                        ++i + 1
//                    }
//                }
//            }

        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    // Traverse recursively to get WebView node from root
    private fun findWebViewNode(rootNode: AccessibilityNodeInfo) {
        for (i in 0 until rootNode.childCount) {
            val child = rootNode.getChild(i)
            if ("android.webkit.WebView" == child.className) {
                accessibilityNodeInfoWebView = child
                logDebugMsg("findWebViewNode--", "find webView")
                return
            }
            if (child.childCount > 0) findWebViewNode(child)
        }
    }

    private fun getRecordNode(webViewNode: AccessibilityNodeInfo) {
        val count = webViewNode.childCount
        Log.e("getRecordNode--", "childCount: $count")

    }
}
