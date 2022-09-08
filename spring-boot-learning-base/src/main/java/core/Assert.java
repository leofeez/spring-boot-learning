package core;

import cn.hutool.core.util.StrUtil;
import ex.BizException;

/**
 * @author leofee
 */
public interface Assert {

    BizException newException(Object... args);

    default void notNull(Object obj, Object... messageArgs) {
        if (obj == null) {
            throw newException(messageArgs);
        }
    }

    default void notBlank(String obj, Object... messageArgs) {
        if (StrUtil.isBlank(obj)) {
            throw newException(messageArgs);
        }
    }

    default void notEmpty(String obj, Object... messageArgs) {
        if (StrUtil.isBlank(obj)) {
            throw newException(messageArgs);
        }
    }

    default void eq(String src, String target, Object... messageArgs) {
        if (!StrUtil.equals(src, target)) {
            throw newException(messageArgs);
        }
    }

    default void state(boolean state, Object... messageArgs) {
        if (!state) {
            throw newException(messageArgs);
        }
    }
}
