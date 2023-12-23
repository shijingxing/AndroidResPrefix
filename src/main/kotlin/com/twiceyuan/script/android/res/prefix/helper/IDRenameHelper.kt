package com.twiceyuan.script.android.res.prefix.helper

import com.twiceyuan.script.android.res.prefix.bean.RenameResult
import java.io.File

object IDRenameHelper {

    /**
     * android:id="@+id/toolbar"
     */
    fun renameIdAttr(attrFile: File, oldPrefix: String, newPrefix: String, tagMatcher: Regex): List<RenameResult> {
        var content = attrFile.readText()
        var isChanged = false
        val results = mutableListOf<RenameResult>()
        val nameMatcher = Regex("\"@\\+id/(.*?)\"")
        println("---------")
        tagMatcher.findAll(content).forEach { tagResult ->
            //println("tagResult="+tagResult.value)
            val result = nameMatcher.find(tagResult.value)?.groups ?: return@forEach
            //println("result="+result)
            val nameDefinition = tagResult.value
            //println("nameDefinition="+nameDefinition)
            val oldName = result[1]?.value ?: return@forEach
            //println("oldName="+oldName)
            // 命名符合规则的跳过
            if (oldName.startsWith(newPrefix)) {
                return@forEach
            }
            val newName = if (oldName.startsWith(oldPrefix)) {
                //如果以老的前缀命名
                oldName.replace(oldPrefix, newPrefix)
            }else {
                newPrefix + oldName
            }
            //val newName = newPrefix + oldName
            //println("renameAttrName oldName="+oldName+" ,newName=$newName")
            val newNameDefinition = nameDefinition.replace(oldName, newName)
            content = content.replace(nameDefinition, newNameDefinition)
            results.add(RenameResult.Success(oldName, newName))
            isChanged = true
        }

        if (isChanged) {
            attrFile.writeText(content)
        }

        return results
    }
}
