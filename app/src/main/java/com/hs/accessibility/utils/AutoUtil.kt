package com.hs.accessibility.utils

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

object AutoUtil {


    fun findNodeInfoByText(nodeInfo: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val list = nodeInfo.findAccessibilityNodeInfosByText(text)
        return if (list.size > 0) list[0] else null
    }


    fun findNodeInfoById(nodeInfo: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        val list = nodeInfo.findAccessibilityNodeInfosByViewId(id)
        return if (list.size > 0) list[0] else null
    }

    fun findNodeInfoListById(
        nodeInfo: AccessibilityNodeInfo,
        id: String
    ): List<AccessibilityNodeInfo>? =
        nodeInfo.findAccessibilityNodeInfosByViewId(id)

    fun performSetText(
        nodeInfo: AccessibilityNodeInfo?,
        text: String,
        msg: String
    ) {
        if (nodeInfo == null) return
        if (nodeInfo.isEditable) nodeInfo.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            createBundledText(text)
        )
        else performSetText(nodeInfo.parent, text, msg)
        nodeInfo.recycle()
        logDebugMsg(msg)
    }

    fun performClick(
        nodeInfo: AccessibilityNodeInfo?,
        tag: String,
        msg: String
    ) {
        if (nodeInfo == null) return
        if (nodeInfo.isClickable)
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        else performClick(nodeInfo.parent, tag, msg)
        nodeInfo.recycle()
        logDebugMsg(msg)
    }

    fun performClickSuggestion(
        nodeInfoList: List<AccessibilityNodeInfo>?,
        name: String,
        tag: String,
        msg: String
    ) {
        if (nodeInfoList == null) return
        nodeInfoList.forEach {
            if (it.text.toString() == name) performClick(
                it,
                tag,
                msg
            )
        }
    }

    fun performScroll(nodeInfo: AccessibilityNodeInfo?, msg: String) {
        if (nodeInfo == null) return
        if (nodeInfo.isScrollable) nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) else performScroll(
            nodeInfo.parent,
            msg
        )
        logDebugMsg(msg)
    }

    private fun createBundledText(text: String) = Bundle().let {
        it.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        it
    }

    fun logDebugMsg(msg: String) = Timber.d(msg)
}
