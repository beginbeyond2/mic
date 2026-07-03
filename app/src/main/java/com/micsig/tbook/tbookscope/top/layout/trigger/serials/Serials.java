package com.micsig.tbook.tbookscope.top.layout.trigger.serials; // 串行触发模块的根包声明

import android.os.Parcel; // Android Parcelable序列化工具
import android.os.Parcelable; // Android Parcelable接口

import com.micsig.tbook.ui.bean.RxMsgSelect; // Rx消息选择基类

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                          Serials（串行协议项）                              ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/Serials.java                                            ║
 * ║ 核心职责: 表示串行触发界面中每个可选的串行协议选项（如UART、LIN、CAN等）          ║
 * ║ 架构设计: 继承RxMsgSelect实现Parcelable支持跨进程传递                        ║
 * ║ 数据流向: TopLayoutTriggerSerials → SerialsAdapter显示 → 用户选择           ║
 * ║ 依赖关系: 继承RxMsgSelect，被TopLayoutTriggerSerials和SerialsAdapter使用     ║
 * ║ 使用场景: 串行触发类型列表中的每个条目                                       ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/4/27.
 */

public class Serials extends RxMsgSelect implements Parcelable{ // 串行协议选项实体类，继承RxMsgSelect并实现Parcelable接口
    private int id; // 协议选项的唯一标识ID
    private String name; // 协议选项的显示名称
    private boolean enabled = true; // 该选项是否可用，默认为true
    private boolean selected = false; // 该选项是否被选中，默认为false
    private int detailFlag; // 详情页面的标志位，用于标识对应的详情Fragment
    private String cacheListKey; // 缓存列表选中状态的键名

    /**
     * 从Parcel对象反序列化构造Serials对象
     * @param in 包含序列化数据的Parcel对象
     */
    protected Serials(Parcel in) { // Parcelable反序列化构造方法
        id = in.readInt(); // 从Parcel读取id字段
        name = in.readString(); // 从Parcel读取name字段
        enabled = in.readByte() != 0; // 从Parcel读取enabled字段（byte转boolean）
        selected = in.readByte() != 0; // 从Parcel读取selected字段（byte转boolean）
        detailFlag = in.readInt(); // 从Parcel读取detailFlag字段
        cacheListKey = in.readString(); // 从Parcel读取cacheListKey字段
    }

    public static final Creator<Serials> CREATOR = new Creator<Serials>() { // Parcelable创建器，用于从Parcel创建Serials实例
        @Override
        public Serials createFromParcel(Parcel in) { // 从单个Parcel创建Serials对象
            return new Serials(in); // 调用反序列化构造方法
        }

        @Override
        public Serials[] newArray(int size) { // 创建指定大小的Serials数组
            return new Serials[size]; // 返回新数组
        }
    };

    /**
     * 获取详情页面标志位
     * @return 详情标志位整数
     */
    public int getDetailFlag() { // 获取详情标志位的getter方法
        return detailFlag; // 返回detailFlag值
    }

    /**
     * 设置详情页面标志位
     * @param detailFlag 要设置的详情标志位
     */
    public void setDetailFlag(int detailFlag) { // 设置详情标志位的setter方法
        this.detailFlag = detailFlag; // 将参数赋值给成员变量
    }

    /**
     * 获取缓存列表键名
     * @return 缓存键名字符串
     */
    public String getCacheListKey() { // 获取缓存列表键名的getter方法
        return cacheListKey; // 返回cacheListKey值
    }

    /**
     * 设置缓存列表键名
     * @param cacheListKey 要设置的缓存键名
     */
    public void setCacheListKey(String cacheListKey) { // 设置缓存列表键名的setter方法
        this.cacheListKey = cacheListKey; // 将参数赋值给成员变量
    }

    /**
     * 获取协议选项ID
     * @return 选项ID整数
     */
    public int getId() { // 获取ID的getter方法
        return id; // 返回id值
    }

    /**
     * 设置协议选项ID
     * @param id 要设置的ID值
     */
    public void setId(int id) { // 设置ID的setter方法
        this.id = id; // 将参数赋值给成员变量
    }

    /**
     * 获取协议选项名称
     * @return 名称字符串
     */
    public String getName() { // 获取名称的getter方法
        return name; // 返回name值
    }

    /**
     * 设置协议选项名称
     * @param name 要设置的名称字符串
     */
    public void setName(String name) { // 设置名称的setter方法
        this.name = name; // 将参数赋值给成员变量
    }

    /**
     * 判断该选项是否可用
     * @return true表示可用，false表示不可用
     */
    public boolean isEnabled() { // 判断是否启用的getter方法
        return enabled; // 返回enabled状态
    }

    /**
     * 设置该选项是否可用
     * @param enabled true表示启用，false表示禁用
     */
    public void setEnabled(boolean enabled) { // 设置启用状态的setter方法
        this.enabled = enabled; // 将参数赋值给成员变量
    }

    /**
     * 判断该选项是否被选中
     * @return true表示已选中，false表示未选中
     */
    public boolean isSelected() { // 判断是否选中的getter方法
        return selected; // 返回selected状态
    }

    /**
     * 设置该选项是否被选中
     * @param selected true表示选中，false表示取消选中
     */
    public void setSelected(boolean selected) { // 设置选中状态的setter方法
        this.selected = selected; // 将参数赋值给成员变量
    }

    /**
     * 全参构造方法，创建一个完整的Serials对象
     * @param name 协议选项名称
     * @param id 协议选项唯一标识
     * @param cacheListKey 缓存列表键名
     * @param detailFlag 详情页面标志位
     */
    public Serials(String name, int id, String cacheListKey, int detailFlag) { // 全参构造方法
        this.name = name; // 将name参数赋值给成员变量
        this.id = id; // 将id参数赋值给成员变量
        this.cacheListKey = cacheListKey; // 将cacheListKey参数赋值给成员变量
        this.detailFlag = detailFlag; // 将detailFlag参数赋值给成员变量
    }

    @Override
    public String toString() { // 重写toString方法，返回对象的字符串表示
        return "Serials{" + // 开始构建字符串
                "name='" + name + '\'' + // 拼接name字段
                ", enabled=" + enabled + // 拼接enabled字段
                ", selected=" + selected + // 拼接selected字段
                ", rxMsgSelect=" + rxMsgSelect + // 拼接父类rxMsgSelect字段
                '}'; // 结束字符串构建
    }

    @Override
    public int describeContents() { // 实现Parcelable接口的特殊内容描述方法
        return 0; // 返回0表示无特殊描述
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { // 将当前对象序列化写入Parcel
        dest.writeInt(id); // 写入id字段到Parcel
        dest.writeString(name); // 写入name字段到Parcel
        dest.writeByte((byte) (enabled ? 1 : 0)); // 写入enabled字段（boolean转byte）
        dest.writeByte((byte) (selected ? 1 : 0)); // 写入selected字段（boolean转byte）
        dest.writeInt(detailFlag); // 写入detailFlag字段到Parcel
        dest.writeString(cacheListKey); // 写入cacheListKey字段到Parcel
    }
}
