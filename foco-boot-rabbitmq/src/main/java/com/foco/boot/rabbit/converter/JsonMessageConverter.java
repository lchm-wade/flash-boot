package com.foco.boot.rabbit.converter;



import com.alibaba.fastjson.JSON;
import com.foco.context.core.LoginContext;
import com.foco.context.core.LoginContextConstant;
import com.foco.context.core.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * mq消息处理器：
 * 使用json 格式消息
 * @Author lucoo
 * @Date 2021/6/27 10:13
 */
@Slf4j
public class JsonMessageConverter extends AbstractMessageConverter {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static  final String CONTENT_TYPE="json";


    private DefaultClassMapper classMapper;




    public JsonMessageConverter() {
        super();
        this.classMapper = new RabbitMqClassMapper();
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) {
        byte[] bytes;
        try {
            String jsonString = JSON.toJSONString(object);
            bytes = jsonString.getBytes(DEFAULT_CHARSET);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new MessageConversionException("Failed to convert Message content", e);
        }
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(DEFAULT_CHARSET);
        messageProperties.setContentLength(bytes.length);

        setMqMessageHeader(messageProperties);

        classMapper.fromClass(object.getClass(), messageProperties);
        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message)
            throws MessageConversionException {
        Object content = null;
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String contentType = properties.getContentType();
            if (contentType != null && contentType.contains(CONTENT_TYPE)) {
                String encoding = properties.getContentEncoding();
                if (encoding == null) {
                    encoding = DEFAULT_CHARSET;
                }
                try {
                    Class<?> targetClass = classMapper.toClass(message.getMessageProperties());
                    content = convertBytesToObject(message.getBody(), encoding, targetClass);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    throw new MessageConversionException("Failed to convert Message content", e);
                }
            } else {
                log.warn("Could not convert incoming message with content-type [" + contentType + "]");
            }
            setLocalContext(properties);
        }
        if (content == null) {
            content = message.getBody();
        }
        return content;
    }

    private Object convertBytesToObject(byte[] body, String encoding, Class<?> clazz) throws UnsupportedEncodingException {
        return JSON.parseObject(new String(body, encoding), clazz);
    }

    private void setMqMessageHeader(MessageProperties messageProperties){
        //设置LoginContextHolder
        LoginContext loginContext= LoginContextHolder.getLoginContext(LoginContext.class);
        if(loginContext!=null){
            //为了避免消息数据太多，消息队列中只传递主要的上下文数据，如用户iD
            LoginContext newLoginContext=new LoginContext();
            newLoginContext.setUserId(loginContext.getUserId());
            newLoginContext.setUserName(loginContext.getUserName());
            messageProperties.getHeaders().put(LoginContextConstant.LOGIN_CONTEXT,newLoginContext);
        }
    }
    private void setLocalContext(MessageProperties messageProperties){
        Map<String, Object> headers = messageProperties.getHeaders();
        //设置LoginContextHolder
        String loginUser=(String) headers.getOrDefault(LoginContextConstant.LOGIN_CONTEXT,null);
        LoginContextHolder.set(loginUser);
    }
}