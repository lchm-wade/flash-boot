package com.foco.boot.db.intercept.encrypt;

import java.lang.annotation.*;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/08/04 18:55
 */

@Inherited
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {
}
