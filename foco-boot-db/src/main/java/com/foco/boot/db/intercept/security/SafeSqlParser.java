package com.foco.boot.db.intercept.security;


import com.baomidou.mybatisplus.core.parser.AbstractJsqlParser;
import com.baomidou.mybatisplus.core.parser.SqlInfo;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.foco.boot.db.properties.SafeSqlProperties;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.update.Update;

/**
 * 攻击 SQL 阻断解析器,防止全表删除，全表更新
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Slf4j
public class SafeSqlParser extends AbstractJsqlParser {

    private SafeSqlProperties safeSqlProperties;

    public SafeSqlParser(SafeSqlProperties safeSqlProperties) {
        this.safeSqlProperties = safeSqlProperties;
    }

    public SafeSqlProperties getSafeSqlProperties() {
        return safeSqlProperties;
    }

    /**
     * 只处理更新和删除操作
     */
    @Override
    public SqlInfo processParser(Statement statement) {
        if (statement instanceof Update) {
            this.processUpdate((Update)statement);
        } else if (statement instanceof Delete) {
            this.processDelete((Delete)statement);
        }
        return SqlInfo.newInstance().setSql(statement.toString());
    }

    @Override
    public void processInsert(Insert insert) {

    }

    /**
     * update 语句处理
     */
    @Override
    public void processUpdate(Update update) {
        if(safeSqlProperties.isEnableSafeUpdate()){
            String tableName=update.getTable().getName();
            if(!safeSqlProperties.getSafeUpdateIgnoreTable().contains(tableName)
                    &&update.getWhere()==null){
                // 不安全的delete语句
                Assert.notNull(update.getWhere(),
                        "数据更新安全检查, 当前更新的SQL没有指定查询条件, 不允许执行该操作!",
                        "");
            }
        }
    }

    @Override
    public void processSelectBody(SelectBody selectBody) {
    }

    /**
     * delete 语句处理
     */
    @Override
    public void processDelete(Delete delete)  {
        if(safeSqlProperties.isEnableSafeDelete()){
            String tableName=delete.getTable().getName();
            if(!safeSqlProperties.getSafeDeleteIgnoreTable().contains(tableName)){
                // 不安全的delete语句
                Assert.notNull(delete.getWhere(),
                        "数据删除安全检查, 当前删除的SQL没有指定查询条件, 不允许执行该操作!",
                       "");
            }
        }
    }
}

