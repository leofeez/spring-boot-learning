package core;

import ex.BizException;

import java.text.MessageFormat;

/**
 * @author leofee
 */
public interface BizAssert extends IRCode, Assert {

    @Override
    default BizException newException(Object... args) {
        String message = getMessage();
        if (args != null && args.length > 0) {
            if (message.contains("{") && message.contains("}")) {
                message = MessageFormat.format(message, args);
            } else {
                message = args[0].toString();
            }
        }
        return new BizException(getCode(), message);
    }
}
