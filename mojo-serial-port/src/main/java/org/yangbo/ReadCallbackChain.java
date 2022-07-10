package org.yangbo;

import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 读数回调函数链接器
 * <p>用来组合多个“回调函数对象”</p>
 * <p>用法：
 * <pre>
 *      ReadCallbackChain chain;
 *      chain.getCallbackList().addAll(callback1, callback2);
 *  </pre>
 * </p>
 *
 * @param <T>
 */
public class ReadCallbackChain<T> implements Callback<T[], Void> {

    private List<Callback<T[], Void>> callbackList = new ArrayList<>();

    public List<Callback<T[], Void>> getCallbackList() {
        return callbackList;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Void call(T[] param) {
        for (Callback<T[], Void> callback : callbackList) {
            try {
                callback.call(param);
            } catch (Exception exp) {
                logger.error("调用 callback {} 异常！", callback, exp);
            }
        }
        return null;
    }
}
