package com.foco.boot.db.intercept.logicdelete;

import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.foco.boot.db.properties.LogicDeleteProperties;
import com.foco.db.scanner.TableIgnoreHandler;
import com.foco.db.util.IgnoreHelper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/26 17:13
 */
@Slf4j
public class LogicDeleteSelectHandler implements TenantHandler {
    private LogicDeleteProperties logicDeleteProperties;


    public LogicDeleteSelectHandler(LogicDeleteProperties logicDeleteProperties){
        this. logicDeleteProperties=logicDeleteProperties;
    }
    @Override
    public Expression getTenantId(boolean where) {
        //查询条件自动加上 and deleted=0 ,查询未标记删除数据
        return new LongValue(logicDeleteProperties.getNoDeleteValue());
    }

    @Override
    public String getTenantIdColumn() {
        return logicDeleteProperties.getColumnName();
    }

    @Override
    public boolean doTableFilter(String tableName) {
        return IgnoreHelper.getDeleted()
                || TableIgnoreHandler.getIgnoreTables().stream().anyMatch((t1) -> t1.equalsIgnoreCase(tableName.replaceAll("`","")))
                || logicDeleteProperties.getIgnoreTableName().stream().anyMatch(
                (t2) -> t2.equalsIgnoreCase(tableName.replaceAll("`",""))
        );
    }
}
