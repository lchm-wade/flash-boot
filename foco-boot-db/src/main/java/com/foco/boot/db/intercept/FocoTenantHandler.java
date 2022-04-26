package com.foco.boot.db.intercept;


import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.foco.boot.db.properties.TenantProperties;
import com.foco.context.core.LoginContextHolder;
import com.foco.db.scanner.TableIgnoreHandler;
import com.foco.db.util.IgnoreHelper;
import com.foco.db.util.TenantContext;
import com.foco.db.util.TenantHelper;
import com.foco.db.util.TenantInfo;
import com.foco.model.constant.FocoErrorCode;
import com.foco.model.exception.SystemException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;

/**
 * 多租户配置
 * @author  bond
 * @Date 2021-05-25
 */

public class FocoTenantHandler implements TenantLineHandler {
    private TenantProperties tenantProperties;

    public FocoTenantHandler(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }
    /**
     * 获取租户ID
     * @return
     */
    @Override
    public Expression getTenantId() {
        TenantInfo tenantInfo = TenantContext.getTenantInfo();
        if(tenantInfo!=null){
            String columnValue = tenantInfo.getColumnValue();
            if(tenantInfo.getColumnType()== TenantInfo.ColumnType.LONG){
                return new LongValue(columnValue);
            }
            if(tenantInfo.getColumnType()== TenantInfo.ColumnType.DOUBLE){
                return new DoubleValue(columnValue);
            }
            if(tenantInfo.getColumnType()== TenantInfo.ColumnType.STRING){
                return new StringValue(columnValue);
            }
        }
        Long tenantId=LoginContextHolder.currentTenantId();
        if(tenantId==null){
           throw new SystemException(FocoErrorCode.TENANT_ID_NOT_EXIT);
        }
        return new LongValue(tenantId);
    }

    /**
     * 获取多租户的字段名
     * @return String
     */
    @Override
    public String getTenantIdColumn() {
        TenantInfo tenantInfo = TenantContext.getTenantInfo();
        if(tenantInfo!=null){
            return tenantInfo.getColumnName();
        }
        return tenantProperties.getTenantIdColumnName();
    }

    /**
     * 过滤不需要租户隔离的表
     * @param tableName
     * @return
     */
    @Override
    public boolean ignoreTable(String tableName) {
        return IgnoreHelper.getTenant()
                ||TableIgnoreHandler.getIgnoreTables().stream().anyMatch((t1) -> t1.equalsIgnoreCase(tableName.replaceAll("`","")))
                ||tenantProperties.getIgnoreTables().stream().anyMatch((t2) -> t2.equalsIgnoreCase(tableName.replaceAll("`",""))
        );
    }
}
