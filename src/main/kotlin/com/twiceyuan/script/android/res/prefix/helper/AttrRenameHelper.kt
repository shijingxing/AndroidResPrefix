package com.twiceyuan.script.android.res.prefix.helper

import com.twiceyuan.script.android.res.prefix.bean.RenameResult
import java.io.File

object AttrRenameHelper {

    fun renameAttrName(attrFile: File, oldPrefix: String, newPrefix: String, tagMatcher: Regex): List<RenameResult> {
        var content = attrFile.readText()
        var isChanged = false
        val results = mutableListOf<RenameResult>()
        val nameMatcher = Regex("name=\"(.*?)\"")
        //println("renameAttrName:"+tagMatcher)
        tagMatcher.findAll(content).forEach { tagResult ->
            val result = nameMatcher.find(tagResult.value)?.groups ?: return@forEach
            //println("tagResult=${tagResult.value}::${result.size}")
            val nameDefinition = result[0]?.value ?: return@forEach
            val oldName = result[1]?.value ?: return@forEach
            // 命名符合规则的跳过
            if (oldName.startsWith(newPrefix)) {
                return@forEach
            }
            println("$oldName---$oldPrefix---$newPrefix")
            val newName = if (oldName.startsWith(oldPrefix)) {
                //如果以老的前缀命名
                oldName.replace(oldPrefix, newPrefix)
            }else {
                newPrefix + oldName
            }

            //val newName = newPrefix + oldName
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
