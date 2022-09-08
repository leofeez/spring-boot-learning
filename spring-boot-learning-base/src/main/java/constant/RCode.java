package constant;

import core.BizAssert;
import core.IRCode;
import lombok.AllArgsConstructor;

/**
 * @author leofee
 */
@AllArgsConstructor
public enum RCode implements IRCode, BizAssert {

    USER_NOT_FOUND("U_0001", "用户账号{0}对应的用户信息不存在"),
    USER_IS_LOCKED("U_0002", "用户已经被锁定"),

    USER_STATUS_INCORRECT("U_0003", "用户状态为【{0}】，无法执行该操作"),

    ;

    private final String code;

    private final String message;


    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getCode() {
        return this.code;
    }
}
