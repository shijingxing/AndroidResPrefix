package com.twiceyuan.script.android.res.prefix.helper

import com.twiceyuan.script.android.res.prefix.bean.RenameResult
import java.io.File


object FileRenameHelper {

    fun rename(
        oldFile: File,
        oldPrefix: String,
        newPrefix: String,
        moduleFile: File
    ): RenameResult {
        // 如果文件已经以前缀命名，则跳过该文件
        if (oldFile.name.startsWith(newPrefix)) {
            return RenameResult.Passed
        }
        val newName = if (oldFile.name.startsWith(oldPrefix, true)) {
            //如果以老的前缀命名
            oldFile.name.replace(oldPrefix, newPrefix, true)
        }else {
            newPrefix + oldFile.name
        }
        println("File rename ${oldFile.name}->$newName")

        //val newName = newPrefix + oldFile.name
        val newFile = File(oldFile.parent, newName)

        return if (renameFile(oldFile, newFile, moduleFile)) {
            val oldResName = oldFile.nameWithoutExtension
            val newResName = newFile.nameWithoutExtension
            RenameResult.Success(oldResName, newResName)
        } else {
            RenameResult.Failed
        }
    }

    /**
     * 使用 git mv 重命名文件，没有使用 File API 是因为需要保留 git 记录
     */
    private fun renameFile(oldFile: File, newFile: File, dir: File): Boolean {
        val cmd = "git mv ${oldFile.absolutePath} ${newFile.absolutePath}"
        return runCommand(cmd, dir)
    }
}

