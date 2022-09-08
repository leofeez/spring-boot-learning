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

    default void eq(String src, String target, Object... messageArgs) {
        if (!StrUtil.equals(src, target)) {
            throw newException(messageArgs);
        }
    }
}
