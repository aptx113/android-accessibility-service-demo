package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import com.hs.accessibility.utils.AutoUtil.findNodeInfoById
import com.hs.accessibility.utils.AutoUtil.findNodeInfoByText
import com.hs.accessibility.utils.AutoUtil.logDebugMsg
import com.hs.accessibility.utils.AutoUtil.performSetText
import kotlinx.coroutines.*

private const val keyword = "disneyland"

class MyAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var accessibilityNodeInfoWebView: AccessibilityNodeInfo? = null
    private var idCnt: AccessibilityNodeInfo? = null
    private var idRso: AccessibilityNodeInfo? = null

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
                findWebViewNode(it.getChild(0))
            }

            findNodeCnt(accessibilityNodeInfoWebView)
            getRecordNodes(idCnt)
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
                logDebugMsg(
                    "findWebViewNode--",
                    "find webView, text = ${accessibilityNodeInfoWebView?.text}"
                )
                return
            }
            if (child.childCount > 0) findWebViewNode(child)
        }
    }

    private fun findNodeCnt(webViewNode: AccessibilityNodeInfo?) {
        if (webViewNode == null) return
        for (i in 0 until webViewNode.childCount) {
            val child = webViewNode.getChild(i)
            if (child.className == "android.view.View") {
                idCnt = webViewNode.getChild(2)?.getChild(0)?.getChild(0) ?: return
                logDebugMsg("find cnt", "find cnt, childCount = ${idCnt?.childCount}")
                return
            }
            if (child.childCount > 0) findNodeCnt(child)
        }
    }

    private fun findNodeRso(idCnt: AccessibilityNodeInfo?) {
        if (idCnt == null) return
        for (i in 0 until idCnt.childCount) {
            val child = idCnt.getChild(i)
            if (child.className == "android.view.View") {
                idRso = idCnt.getChild(3)?.getChild(2)?.getChild(1) ?: return
            }
        }
    }

    private fun getRecordNodes(idCnt: AccessibilityNodeInfo?) {
        val idRso = idCnt?.getChild(3)?.getChild(2)?.getChild(1) ?: return
        logDebugMsg("null", "${idRso.childCount}")
        for (i in 0 until idRso.childCount) {
            val child = idRso.getChild(i)
            if (child.getChild(0).getChild(0).getChild(1).getChild(1) != null) {
                val result = child.getChild(0).getChild(0).getChild(1).getChild(1)
                logDebugMsg("Result tile", "${result.text}")
            }
        }
    }
}
