package com.foco.boot.db.intercept.logicdelete;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.parser.SqlInfo;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.foco.boot.db.properties.LogicDeleteProperties;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 *
 * 逻辑删除 SQL 解析
 * @Author lucoo
 * @Date 2021/6/26 17:13
 */
@Slf4j
public class LogicDeleteSqlParser extends TenantSqlParser {
    private LogicDeleteProperties properties;

    public LogicDeleteSqlParser(LogicDeleteProperties properties) {
        this.properties = properties;
    }
    @Override
    public SqlInfo processParser(Statement statement) {
        if (statement instanceof Select) {
            this.processSelectBody(((Select)statement).getSelectBody());
        }if (statement instanceof Update) {
            this.processUpdate((Update)statement);
        }else if(statement instanceof Delete){
            String deleteSql = statement.toString();
            String tableName=((Delete) statement).getTable().getName();
            return SqlInfo.newInstance().setSql(handler(deleteSql,tableName));
        }
        return SqlInfo.newInstance().setSql(statement.toString());
    }
    private String handler(String deleteSql,String tableName){
        if(CollectionUtil.isNotEmpty(properties.getIgnoreTableName())&&properties.getIgnoreTableName().contains(tableName)){
            return deleteSql;
        }
        String finalSql=deleteSql.replaceAll("(?i)delete from","UPDATE")
                .replace(tableName,tableName+" SET "+properties.getColumnName()+"="+properties.getDeleteValue());
        return finalSql;
    }
}
