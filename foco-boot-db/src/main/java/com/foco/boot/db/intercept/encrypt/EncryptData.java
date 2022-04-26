package com.foco.boot.db.intercept.encrypt;

import java.lang.annotation.*;

@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptData {
}
