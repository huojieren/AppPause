package com.huojieren.apppause.data.diagnostics.storage

/**
 * 诊断文件的空间上限。
 *
 * 运行日志用于追溯连续行为，事故文件用于保留少量高价值的 crash / 退出证据，二者分别限额。
 */
object LogRetentionPolicy {
    /** 单个运行日志达到此大小后轮转，避免一次写入形成过大的文件。 */
    const val RUNTIME_MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L

    /** 除当前 app.log 外，最多保留的历史运行日志份数。 */
    const val RUNTIME_MAX_BACKUP_FILES = 5

    /** crash 和进程退出 incident 最多保留的文件数量。 */
    const val INCIDENT_MAX_FILES = 30

    /** 已处理的系统进程退出记录最多保存的去重标识数量。 */
    const val HANDLED_EXIT_MAX_IDS = 100

    /** Timber 异步写入器最多暂存的普通运行日志数量，满时丢弃最旧记录以保护主线程。 */
    const val RUNTIME_QUEUE_CAPACITY = 512
}
