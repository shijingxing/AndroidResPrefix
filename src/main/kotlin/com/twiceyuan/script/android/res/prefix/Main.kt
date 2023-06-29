@file:Suppress("SameParameterValue")

package com.twiceyuan.script.android.res.prefix

import com.twiceyuan.script.android.res.prefix.bean.RenameResult
import com.twiceyuan.script.android.res.prefix.bean.RenamedMapping
import com.twiceyuan.script.android.res.prefix.bean.ResType
import com.twiceyuan.script.android.res.prefix.bean.getNameStylePrefix
import com.twiceyuan.script.android.res.prefix.files.getCodeByFlavor
import com.twiceyuan.script.android.res.prefix.files.getXmlFiles
import com.twiceyuan.script.android.res.prefix.handler.*
import com.twiceyuan.script.android.res.prefix.helper.AttrRenameHelper
import com.twiceyuan.script.android.res.prefix.helper.FileRenameHelper
import com.twiceyuan.script.android.res.prefix.helper.IDRenameHelper
import java.io.File
import java.util.*

val modulePaths = File("AndroidResPrefix/module_paths.txt")
    .readText()
    .split("\n")
    .filter { it.isNotBlank() }

val properties = Properties().apply {
    load(File("AndroidResPrefix/config.properties").inputStream())
}

val prefix: String = properties["prefix"] as String

val File.subFiles
    get() = listFiles()?.toList() ?: emptyList()

private val renameMapping = hashSetOf<RenamedMapping>()

fun main() {
    renameResInModule(prefix, modulePaths)
    //test2()
}

fun test(){
    val results = IDRenameHelper.renameIdAttr(
        File("/Users/exstar/AndroidStudioProjects/VideoChat/testresprefix/src/main/res/layout/activity_main.xml"),
        "aaaa",
        idMatcher("id")
    )
}

fun test2(){
    println("(toolbar)".replace("(toolbar)", "(aoo_toolbar)"))
    var content = "getAAA(toolbar?bb)"
    val oldValue = "toolbar?"
    val newValue = "aaa_toolbar?"
    val old = oldValue
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("?",  "\\?")
    val oldValueMatcher = Regex("(?<!android.)$old")
    val matchResults = oldValueMatcher.findAll(content).toList()
    println(matchResults)
    if (matchResults.isNotEmpty()) {
        matchResults.forEach {
            val oldFullValue = it.value
            val newFullValue = it.value.replace(oldValue, newValue)
            println("oldValue=$oldValue newValue=$newValue oldFullValue=$oldFullValue newFullValue=$newFullValue")
            content = content.replace(oldFullValue, newFullValue)
        }
        println(content)
    }
}

private fun renameResInModule(prefix: String, modulePaths: List<String>) {

    val modules = modulePaths.map { File(it) }
    /*println("-----------------")
    println(modulePaths)
    modules.forEach {
        println(it.absolutePath)
    }*/
    ResType.values().forEach {
        renameResourceByType(prefix, it, modules)
    }
}

fun getFlavorDirs(modulePath: String): List<File> {
    val srcPath = File(modulePath, "src").absolutePath
    return File(srcPath)
        .subFiles
        .filter { "test" !in it.name.toLowerCase() }
}

/**
 * 重命名一类资源
 */
private fun renameResourceByType(prefix: String, resType: ResType, modules: List<File>) {
    val handler = getResTypeHandler(resType)

    // 重命名文件类型资源
    /*if (handler is FileResourceHandler) {
        for (module in modules) {
            // 获取所有的 drawable 文件
            for (file in handler.getResFiles(module.absolutePath)) {
                val resPrefix = handler.nameStyle().getNameStylePrefix(prefix)
                val result = FileRenameHelper.rename(file, resPrefix, module)
                if (result is RenameResult.Success) {
                    val mapping = RenamedMapping(resType, result.oldResName, result.newResName)
                    renameMapping.add(mapping)
                }
            }
        }
    }*/

    // 处理 attr 类型的资源
    /*if (handler is AttrResourceHandler) {
        for (module in modules) {
            for (file in handler.getAttrFiles(module.absolutePath)) {
                val resPrefix = handler.nameStyle().getNameStylePrefix(prefix)
                val results = AttrRenameHelper.renameAttrName(
                    file,
                    resPrefix,
                    handler.tagMatcher()
                )
                renameMapping.addAll(results
                    .filterIsInstance<RenameResult.Success>()
                    .map {
                        RenamedMapping(handler.resType, it.oldResName, it.newResName)
                    }
                )
            }
        }
    }*/

    // 处理 id 类型的资源
    if (handler is IDResourceHandler) {
        for (module in modules) {
            for (file in handler.getIdFiles(module.absolutePath)) {
                val resPrefix = handler.nameStyle().getNameStylePrefix(prefix)
                val results = IDRenameHelper.renameIdAttr(
                    file,
                    resPrefix,
                    handler.idMatcher()
                )
                renameMapping.addAll(results
                    .filterIsInstance<RenameResult.Success>()
                    .map {
                        RenamedMapping(handler.resType, it.oldResName, it.newResName)
                    }
                )
            }
        }
    }

    // 处理资源引用
    modules.forEach { renameReferences(handler, it) }
}

private fun renameReferences(handler: ResTypeHandler, moduleFile: File) {
    // 改变代码中的引用
    for (flavorDir in getFlavorDirs(moduleFile.absolutePath)) {
        for (codeFile in getCodeByFlavor(flavorDir.absolutePath)) {
            var content = codeFile.readText()
            var isChanged = false

            for (mapping in renameMapping) {
                val oldValues = handler.codeComposer(mapping.oldName)
                if(mapping.resType == ResType.ID && mapping.oldName == "view"){
                    continue
                }
                val newValues = handler.codeComposer(mapping.newName)
                for (valueIndex in oldValues.indices){
                    val oldValue = oldValues[valueIndex]
                    val newValue = newValues[valueIndex]
                    val oldValueMat = if(mapping.resType == ResType.ID){
                        oldValue
                            .replace("(", "\\(")
                            .replace(")", "\\)")
                            .replace("?",  "\\?")
                    }else {
                        oldValue
                    }
                    //println("oldValue="+oldValue+",newValue="+newValue)
                    // 避免 android.R.xxx 也被匹配并且替换，排除 R.xxx 前面有 . 的情况
                    val oldValueMatcher = Regex("(?<!android.)$oldValueMat")
                    val matchResults = oldValueMatcher.findAll(content).toList()
                    //println("matchResults="+matchResults+", oldValueMatcher="+oldValueMatcher)
                    if (matchResults.isNotEmpty()) {
                        matchResults.forEach {
                            val oldFullValue = it.value
                            val newFullValue = it.value.replace(oldValue, newValue)
                            //println("oldFullValue=$oldFullValue newFullValue=$newFullValue")
                            content = content.replace(oldFullValue, newFullValue)
                        }
                        isChanged = true
                    }

                    ExternalHandlers.extCodeHandler.forEach {
                        val newContent = it.handle(
                            flavorDir.name,
                            codeFile,
                            content,
                            handler.resType,
                            mapping.oldName,
                            mapping.newName
                        )
                        if (newContent != content) {
                            content = newContent
                            isChanged = true
                        }
                    }
                }
            }
            if (isChanged) {
                codeFile.writeText(content)
                println("[changed] ${codeFile.path}")
            }
        }
    }

    for (file in getXmlFiles(moduleFile.absolutePath)) {
        var content = file.readText()
        var isChanged = false
        for (mapping in renameMapping) {
            val oldValue = handler.xmlComposer(mapping.oldName)
            val newValue = handler.xmlComposer(mapping.newName)
            if (oldValue in content) {
                content = content.replace(oldValue, newValue)
                isChanged = true
            }
            for (extHandler in ExternalHandlers.extXmlHandler) {
                val newContent = extHandler.handle(
                    file,
                    content,
                    handler.resType,
                    mapping.oldName,
                    mapping.newName
                )
                if (content != newContent) {
                    content = newContent
                    isChanged = true
                }
            }
        }
        if (isChanged) {
            file.writeText(content)
            println("[changed] ${file.path}")
        }
    }
}
