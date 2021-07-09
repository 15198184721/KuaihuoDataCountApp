package com.kuaihuo.data.count.dbmanager

import java.sql.*

/**
 * 数据库连接对象
 */
object DBConnectUtils {
    //数据库用户名称
    private val dbUserName = "kuaihuo"

    //数据库连接的密码
    private val dbUserPass = "Xiaoli920529"

    //数据库连接驱动
    private var dbDriver = "com.mysql.jdbc.Driver" //获取mysql数据库的驱动类(8.0以下)
    //private var dbDriver ="com.mysql.jc.jdbc.Driver";   //mysql8.0以上版本

    //连接数据库（jcdz是数据库名）,xxxxxx是数据库实例的外网地址，打开阿里云数据库在基本信息中可以查看
    private var dbUrl =
        "jdbc:mysql://rm-bp1u15ekqqd90x79lso.mysql.rds.aliyuncs.com/kuaihuo_data_count?zeroDateTimeBehavior=convertToNull&amp;user=$dbUserName&amp;password=$dbUserPass&amp;useUnicode=true&amp;characterEncoding=UTF8"

    // 数据库连接状态，T:已连接，F:未连接
    private var dbConnectStatus = false

    //数据库的操作对象
    private var dbStmt: Statement? = null

    /**
     * 连接数据库实例
     */
    fun connectDB() {
        try {
            val jdbcClass = Class.forName(dbDriver)
            val dbContent: Connection = DriverManager.getConnection(dbUrl)
            dbStmt = dbContent.createStatement()
            dbConnectStatus = true
        } catch (e: Exception) {
            e.printStackTrace()
            dbConnectStatus = false
        }
    }

    /**
     * 断开数据库连接
     */
    fun disconnectDB() {
        try {
            dbConnectStatus = false
            dbStmt?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 执行指定的Sql语句
     * @param sql 需要执行的sql
     * @return Boolean
     */
    fun executeSql(sql: String): Boolean {
        return try {
            dbStmt?.execute(sql) ?: false
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 执行更新的Sql语句,只负责执行更新数据的语句
     * @param sql 需要执行的sql
     * @return Boolean
     */
    fun executeUpdateSql(sql: String): Boolean {
        return try {
            dbStmt?.executeUpdate(sql) != 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

}