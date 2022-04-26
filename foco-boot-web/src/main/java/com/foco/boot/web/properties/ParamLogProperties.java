package com.foco.boot.web.properties;

import com.foco.properties.AbstractProperties;
import com.foco.model.constant.FocoConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;


/**
 * <p> 接口入参日志输出配置，过滤文件参数 </p>
 *
 * @Author lucoo
 * @Date 2021/6/26 11:19
 */
@Getter
@Setter
@ConfigurationProperties(prefix = ParamLogProperties.PREFIX)
public class ParamLogProperties extends AbstractProperties {
    private boolean enabled=true;
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"param-log";
    public static ParamLogProperties getConfig(){
        return getConfig(ParamLogProperties.class);
    }
    /***是否打印入参*/
    private Boolean IsPrintIn=true;
    /***是否打印出参*/
    private Boolean isPrintOut=false;

    private Item  outFilter=new Item();

    private Item  inFilter=new Item();
    /**
     * 参数日志是否上传到阿里云链路追踪控制台
     */
    private boolean uploadSkyWalking=false;
    /**
     * 参数日志长度
     */
    private int logLength=128;

    @Getter
    @Setter
    public static class Item{

        /***过滤包*/
        private List<String>  classNames=new ArrayList<>();

        /***过滤具体方法*/
        private List<String>  methods=new ArrayList<>();

    }
}
