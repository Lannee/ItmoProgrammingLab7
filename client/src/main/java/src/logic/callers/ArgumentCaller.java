package src.logic.callers;

import module.logic.exceptions.CannotCreateObjectException;
import src.utils.ObjectUtils;

public class ArgumentCaller extends BaseCaller {
    @Override
    public Object getObjectArgument(Class<?> tClass) throws CannotCreateObjectException {
        return ObjectUtils.createObjectInteractively(tClass);

    }

    public CallStatus getStringArrayFromObject() {
//        getting string array from Dragon object
        return CallStatus.SUCCESSFULLY;
    }
}
