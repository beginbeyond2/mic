package com.micsig.tbook.scope.Data;

import java.nio.ByteBuffer;

/**
 * Created by zhuzh on 2018-3-27.
 */

public interface IDataInterface {
    boolean onRecv(ByteBuffer byteBuffer,int length);
}
