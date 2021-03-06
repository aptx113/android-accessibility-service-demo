package com.hs.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import androidx.annotation.RequiresApi
import com.hs.accessibility.utils.AutoUtil.findNodeInfoById
import com.hs.accessibility.utils.AutoUtil.logDebugMsg
import com.hs.accessibility.utils.AutoUtil.performSetText
import com.hs.accessibility.utils.FileUtils.saveImage
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.Executor


private const val KEYWORD = "disneyland"
private const val SEARCH_RESULTS = "Search Results"
private const val WEB_RESULTS = "Web results"
private const val TWITTER_RESULTS = "Twitter results"

private const val APP_SEARCH_BOX_ID =
    "com.google.android.googlequicksearchbox:id/googleapp_search_box"
private const val WEBVIEW_CONTAINER_ID =
    "com.google.android.googlequicksearchbox:id/webx_web_container"
private const val RSO_ID = "rso"
private const val BOTSTUFF_ID = "botstuff"

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
    private var idBotStuff: AccessibilityNodeInfo? = null
    private val sb = StringBuilder()

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo().apply {
            eventTypes =
                AccessibilityEvent.TYPE_VIEW_SCROLLED or AccessibilityEvent.TYPE_VIEW_LONG_CLICKED or
                        AccessibilityEvent.TYPE_VIEW_CLICKED
//            packageNames = arrayOf("com.hs.accessibility")
        }
        serviceInfo = info
        scope.launch {
            while (true) {
                search()
                delay(1000)
            }
        }
    }

    fun search() {
        logDebugMsg("launch")
        val rootNode = rootInActiveWindow ?: return
        findNodeInfoById(
            rootNode,
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
            rootNode,
            WEBVIEW_CONTAINER_ID
        )?.let {
            findWebViewNode(it.getChild(0))
        }

        findNodeCenterColChild(accessibilityNodeInfoWebView)
        findNodeUnderCol(idCenterCol)
        Thread.sleep(2000L)
        getRecordNodes(idRso, idBotStuff, sb)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        when (event?.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> accessibilityNodeInfoWebView?.performAction(
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            )
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                performGlobalAction(
                    GLOBAL_ACTION_TAKE_SCREENSHOT
                )
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> idCenterCol?.getChild(4)?.getChild(0)
                ?.getChild(3)
                ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            else -> {
                Timber.d("${event?.contentDescription}")
            }
        }
    }

    override fun onInterrupt() {
    }

    override fun takeScreenshot(
        displayId: Int,
        executor: Executor,
        callback: TakeScreenshotCallback
    ) {
        super.takeScreenshot(displayId, executor, callback)
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            applicationContext.mainExecutor,
            object : TakeScreenshotCallback {
                @RequiresApi(api = Build.VERSION_CODES.R)
                override fun onSuccess(screenshot: ScreenshotResult) {
                    Timber.i("ScreenShot Result: onSuccess")
                    val bitmap =
                        Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)
                            ?: return
                    saveImage(bitmap, applicationContext, "Accessibility")
                }

                override fun onFailure(errorCode: Int) {
                    Timber.i("ScreenShotResult: onFailure code is $errorCode")
                }
            })
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

    private fun findNodeUnderCol(
        nodeInfo: AccessibilityNodeInfo?
    ) {
        if (nodeInfo?.getChild(2) == null) return
        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(2).getChild(i) ?: return
            val colChild = nodeInfo.getChild(4) ?: return
            if (colChild.viewIdResourceName == BOTSTUFF_ID && child.viewIdResourceName == RSO_ID) {
                idBotStuff = colChild
                Timber.d("find BotStuffColChild, id =${idBotStuff?.viewIdResourceName}")
                idRso = child
                Timber.d("find RsoColChild, id =${idRso?.viewIdResourceName}")
                return
            }
        }
        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(4) ?: return
            val colChild = nodeInfo.getChild(i) ?: return
            if (child.viewIdResourceName == "rso" && colChild.viewIdResourceName == BOTSTUFF_ID) {
                idRso = child
                Timber.d("find RsoCol, id =${idRso?.viewIdResourceName}")
                idBotStuff = colChild
                Timber.d("find BotStuffCol, id =${idBotStuff?.viewIdResourceName}")
                return
            }
        }
    }

    private fun getRecordNodes(
        idRso: AccessibilityNodeInfo?,
        idBotStuff: AccessibilityNodeInfo?,
        sb: StringBuilder
    ) {
        if (idRso == null) return
        for (i in 0 until idRso.childCount) {
            val child = idRso.getChild(i) ?: return
            Timber.d("Child$i" + "Count, = ${child.childCount}")
            if (idRso.parent == null) return
            if (idRso.parent.viewIdResourceName.isNullOrEmpty()) {
                val result = AccessibilityNodeInfo()
                val intermediaryNode = child.getChild(0)?.getChild(0)
                result.text = when {
                    child.childCount >= 1 && !intermediaryNode
                        ?.getChild(1)?.contentDescription.isNullOrEmpty()
                    ->
                        intermediaryNode
                            ?.getChild(1)?.getChild(1)?.text
                    child.childCount >= 1 &&
                            !intermediaryNode
                                ?.getChild(0)?.contentDescription.isNullOrEmpty() ->
                        intermediaryNode
                            ?.getChild(0)?.getChild(1)?.text
                    child.childCount >= 1 && !intermediaryNode
                        ?.getChild(0)
                        ?.getChild(0)?.contentDescription.isNullOrEmpty() -> intermediaryNode
                        ?.getChild(0)?.getChild(0)?.getChild(1)?.text
                    child.childCount >= 2 && !child.getChild(1)?.getChild(0)
                        ?.getChild(0)?.contentDescription.isNullOrEmpty() -> child.getChild(
                        1
                    )
                        ?.getChild(0)
                        ?.getChild(0)?.getChild(1)?.text
                    else -> "child$i is not our target"
                }
                Timber.d("Result title = ${result.text}")
                if (!sb.toString()
                        .contains(result.text)
                ) sb.append("(title: ${result.text})")
                    .append("\n")
            } else {
                val result = AccessibilityNodeInfo()
                result.text = when {
                    child.childCount >= 2 && !child.getChild(0)?.contentDescription.isNullOrEmpty() -> child.getChild(
                        0
                    ).getChild(1).text
                    child.childCount >= 2 && !child.getChild(0)
                        ?.getChild(0)?.contentDescription.isNullOrEmpty() -> child.getChild(
                        0
                    )
                        .getChild(0).getChild(1).text
                    else -> "child$i is not our target"
                }
                Timber.d("Result title = ${result.text}")
                if (!sb.toString().contains(result.text)) sb.append("\n")
                    .append("(title: ${result.text})")
            }
        }
        if (idBotStuff == null) return
        val newResults = idBotStuff.getChild(0).getChild(3)
        for (i in 0 until newResults.childCount) {
            val child = newResults.getChild(i) ?: return
            Timber.d("Child$i" + "Count, = ${child.childCount}")
            val result = AccessibilityNodeInfo()
            val intermediaryNode = child.getChild(0)
            result.text = when {
                !intermediaryNode
                    ?.getChild(0)?.contentDescription.isNullOrEmpty() -> intermediaryNode
                    .getChild(0).getChild(1).text
                !intermediaryNode?.getChild(0)
                    ?.getChild(0)?.contentDescription.isNullOrEmpty() ->
                    intermediaryNode.getChild(0).getChild(1).getChild(0).text
                !intermediaryNode?.getChild(0)
                    ?.getChild(0)?.text.isNullOrEmpty() -> intermediaryNode.getChild(0)
                    .getChild(1)
                    .getChild(0).text
                child.childCount == 2 && !intermediaryNode?.getChild(1)?.contentDescription.isNullOrEmpty() ->
                    intermediaryNode.getChild(1).getChild(1).text
                else -> "child${i + idRso.childCount} is not our target"
            }
            Timber.d("Result title = ${result.text}")
            if (!sb.toString().contains(result.text)) sb.append("(title: ${result.text})")
                .append("\n")
        }
        Timber.i("$sb")
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
