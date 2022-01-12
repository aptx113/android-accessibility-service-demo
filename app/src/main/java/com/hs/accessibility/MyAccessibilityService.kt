package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import com.hs.accessibility.utils.AutoUtil.findNodeInfoById
import com.hs.accessibility.utils.AutoUtil.findNodeInfoByText
import com.hs.accessibility.utils.AutoUtil.logDebugMsg
import com.hs.accessibility.utils.AutoUtil.performClick
import com.hs.accessibility.utils.AutoUtil.performScroll
import com.hs.accessibility.utils.AutoUtil.performSetText
import kotlinx.coroutines.*
import timber.log.Timber

private const val KEYWORD = "disneyland"
private const val SEARCH_RESULTS = "Search Results"
private const val WEB_RESULTS = "Web results"
private const val TWITTER_RESULTS = "Twitter results"

private const val APP_SEARCH_BOX_ID =
    "com.google.android.googlequicksearchbox:id/googleapp_search_box"
private const val WEBVIEW_CONTAINER_ID =
    "com.google.android.googlequicksearchbox:id/webx_web_container"

private const val WEBVIEW_CLASSNAME = "android.webkit.WebView"
private const val VIEW_CLASSNAME = "android.view.View"

class MyAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private var accessibilityNodeInfoWebView: AccessibilityNodeInfo? = null
    private var idCnt: AccessibilityNodeInfo? = null
    private var idCenterCol: AccessibilityNodeInfo? = null
    private var idCenterColChild: AccessibilityNodeInfo? = null
    private var idRso: AccessibilityNodeInfo? = null
    private val sb = StringBuilder()

    override fun onServiceConnected() {
        super.onServiceConnected()

        scope.launch {
            while (true) {
                search()
                delay(1000)
            }
        }
    }

    fun search() {
        logDebugMsg("launch")
        findNodeInfoById(
            rootInActiveWindow,
            APP_SEARCH_BOX_ID
        )?.let { nodeInfo ->
            if (nodeInfo.text == null || !nodeInfo.text.toString()
                    .equals(KEYWORD, ignoreCase = true)
            ) performSetText(
                nodeInfo,
                KEYWORD, SET_TEXT_ACTION
            )
            else {
                // require API 30 up
                nodeInfo.performAction(ACTION_IME_ENTER.id)
            }
        }
        Thread.sleep(1000L)
        findNodeInfoById(
            rootInActiveWindow,
            WEBVIEW_CONTAINER_ID
        )?.let {
            findWebViewNode(it.getChild(0))
        }

        findNodeCenterColChild(accessibilityNodeInfoWebView)
        findNodeRso(idCenterCol)
        Thread.sleep(2000L)
        getRecordNodes(idRso, sb)

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        accessibilityNodeInfoWebView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

//       idCenterCol?.getChild(4)?.getChild(0)?.getChild(3)
//            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
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
            if (child.className == WEBVIEW_CLASSNAME && !child.text.isNullOrEmpty()) {
                accessibilityNodeInfoWebView = child
                logDebugMsg(
                    "find webView, text = ${accessibilityNodeInfoWebView?.text}"
                )
                return
            } else if (child.childCount > 0) findWebViewNode(rootNode)
        }
    }

    private fun findNodeCenterColChild(webViewNode: AccessibilityNodeInfo?) {
        if (webViewNode == null) return
        for (i in 0 until webViewNode.childCount) {
            val child = webViewNode.getChild(i) ?: return
            if (child.className == VIEW_CLASSNAME) {
                idCnt = webViewNode.getChild(2)?.getChild(0)?.getChild(0) ?: return
                idCenterCol = idCnt?.getChild(3) ?: return
                idCenterColChild = idCenterCol?.getChild(2) ?: return
                return
            }
            if (child.childCount > 0) findNodeCenterColChild(webViewNode)
        }
    }

    private fun findNodeRso(nodeInfo: AccessibilityNodeInfo?, index: Int = 0, id: String = "") {
        if (nodeInfo == null) return
        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(i) ?: return
            if (child.viewIdResourceName == "rso") {
                idRso = child
                Timber.d("find RsoCol, id =${idRso?.viewIdResourceName}")
                return
            }
        }

        if (nodeInfo.getChild(2) == null) return
        for (i in 0 until nodeInfo.getChild(index).childCount) {
            val child = nodeInfo.getChild(index).getChild(i) ?: return
            if (child.viewIdResourceName == "rso") {
                idRso = child
                Timber.d("find RsoColChild, id =${idRso?.viewIdResourceName}")
                return
            }
        }
    }

    private fun getRecordNodes(idRso: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (idRso == null) return
        for (i in 0 until idRso.childCount) {
            val child = idRso.getChild(i) ?: return
            Timber.d("Child$i" + "Count, = ${child.childCount}")
            if (idRso.parent == null) return
            if (idRso.parent.viewIdResourceName.isNullOrEmpty()) {
                var result = AccessibilityNodeInfo()
                val intermediaryNode = child.getChild(0)?.getChild(0)
                when {
                    !intermediaryNode
                        ?.getChild(1)?.contentDescription.isNullOrEmpty() -> result =
                        child.getChild(0).getChild(0)
                            .getChild(1).getChild(1)
                    !intermediaryNode
                        ?.getChild(0)?.contentDescription.isNullOrEmpty() -> result =
                        child.getChild(0).getChild(0)
                            .getChild(0).getChild(1)
                    !intermediaryNode
                        ?.getChild(0)?.contentDescription.isNullOrEmpty() -> child.getChild(0)
                        .getChild(0)
                        .getChild(0).getChild(1)
                    else -> result.text = "child$i is not our target"
                }
                Timber.d("Result title = ${result.text}")
                if (!sb.toString().contains(result.text)) sb.append("(title: ${result.text})")
                    .append("\n")
            } else if (child.childCount > 1 && child.getChild(1).childCount > 1 && child.getChild(
                    1
                )
                    ?.getChild(1) != null
            ) {
                val result = child.getChild(1).getChild(1)
                Timber.d("Result title = ${result?.text}")
                if (!sb.toString().contains(result.text)) sb.append("\n")
                    .append("(title: ${result?.text})")
            }
            Timber.i("$sb")
        }
    }

    private fun printNodeInfo(accessibilityService: AccessibilityService) {
        if (accessibilityService.rootInActiveWindow == null) {
            Timber.i("Can't get RootNode")
        }

        val sb = StringBuilder()
        dumpNodeInfo(accessibilityService.rootInActiveWindow, sb, ArrayList())
        Timber.i("UI info (call stack:{$sb}")
    }

    private fun dumpNodeInfo(
        nodeInfo: AccessibilityNodeInfo?,
        sb: StringBuilder,
        layerList: ArrayList<Int>
    ) {
        if (nodeInfo == null) return

        val degree = layerList.size
        for (i in 0 until degree) {
            sb.append("\t\t")
        }

        sb.append(TextUtils.join(".", layerList)).append(' ')

        if (nodeInfo.className != null) {
            sb.append(nodeInfo.className)
        } else {
            sb.append("nullClass")
        }

        if (nodeInfo.text != null) {
            sb.append("(text: ${nodeInfo.text})")
        }

        if (nodeInfo.contentDescription != null) {
            sb.append(" (contentdesc:").append(nodeInfo.contentDescription).append(')')
        }

        sb.append("(id:").append(nodeInfo.viewIdResourceName).append(")")

//        val rect = Rect()
//        nodeInfo.getBoundsInScreen(rect)
//        sb.append(" (").append(rect).append(")")

        sb.append("\n")

        val childCount = nodeInfo.childCount
        for (i in 0 until childCount) {
            layerList.add(i)
            dumpNodeInfo(nodeInfo.getChild(i), sb, layerList)
            layerList.remove(degree)
        }
    }
}
