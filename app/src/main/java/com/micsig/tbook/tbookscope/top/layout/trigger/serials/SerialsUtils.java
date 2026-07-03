package com.micsig.tbook.tbookscope.top.layout.trigger.serials; // 串行触发模块的根包声明

import com.micsig.tbook.scope.Bus.IBus; // 总线接口常量
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 进制常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 键盘数字工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                      SerialsUtils（串行触发工具类）                          ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/SerialsUtils.java                                       ║
 * ║ 核心职责: 提供串行触发模块中各种进制转换、数值计算和格式化工具方法                  ║
 * ║ 架构设计: 纯静态工具类，无状态，所有方法均为static                              ║
 * ║ 数据流向: 被TopLayoutTriggerSerials和各详情Fragment调用                      ║
 * ║ 依赖关系: 依赖KeyBoardNumberUtil、CacheUtil、IDigits、IBus                   ║
 * ║ 使用场景: 进制转换、SPI掩码/数据解析、CAN DLC映射、触发条件映射                   ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class SerialsUtils { // 串行触发工具类

    /**
     * 将带空格的二进制与十六进制互相转换，转换后依然带空格
     * @param text 待转换的数字文本
     * @param preDigits 原始进制
     * @param digits 目标进制
     * @return 转换后的数字文本
     */
    public static String HexBin(String text, int preDigits, int digits) { // 二进制与十六进制互转方法
        if (preDigits == IDigits.DIGITS_2 && digits == IDigits.DIGITS_16) { // 如果从2进制转16进制
            return KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.BToH(text.replace(" ", "")), IDigits.DIGITS_16).trim(); // 去空格→二进制转十六进制→重算空格→去首尾空格
        } else if (preDigits == IDigits.DIGITS_16 && digits == IDigits.DIGITS_2) { // 如果从16进制转2进制
            return KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.HToB(text.replace(" ", "")), IDigits.DIGITS_2).trim(); // 去空格→十六进制转二进制→重算空格→去首尾空格
        } else { // 其他进制转换不处理
            return text; // 原样返回
        }
    }

    /**
     * 将目标数字去空格、补位数、重新计算空格
     * @param s 待处理的数字字符串
     * @param bits 目标位数
     * @param digits 进制
     * @return 处理后的数字字符串
     */
    public static String reCalcSpace(String s, int bits, int digits) { // 重新计算空格格式
        return KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits(s.replace(" ", ""), bits), digits).trim(); // 去空格→补位数→重算空格→去首尾空格
    }

    /**
     * 根据ARINC429格式和显示进制获取数据位数限制
     * 二进制时根据格式依次为23、21、19位限制；十六进制时根据格式依次为6、6、5位
     * @param serialsNumber 串行通道编号
     * @return 数据位数限制
     */
    public static int getBitFor429Data(int serialsNumber) { // 获取ARINC429数据的位数限制
        int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber); // 从缓存获取ARINC429格式
        int digit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber); // 从缓存获取ARINC429显示进制
        if (digit == 0) { // 如果是二进制显示
            if (format == 0) { // 格式0
                return 23; // 返回23位
            } else if (format == 1) { // 格式1
                return 21; // 返回21位
            } else { // 格式2及以上
                return 19; // 返回19位
            }
        } else { // 如果是十六进制显示
            if (format == 0 || format == 1) { // 格式0或1
                return 6; // 返回6位
            } else { // 格式2及以上
                return 5; // 返回5位
            }
        }
    }

    /**
     * 获取ARINC429数据在二进制模式下的位数限制（不考虑显示进制）
     * @param serialsNumber 串行通道编号
     * @return 二进制位数限制
     */
    public static int getBitInBinFor429Data(int serialsNumber) { // 获取ARINC429数据在二进制下的位数
        int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber); // 从缓存获取ARINC429格式
        if (format == 0) { // 格式0
            return 23; // 返回23位
        } else if (format == 1) { // 格式1
            return 21; // 返回21位
        } else { // 格式2及以上
            return 19; // 返回19位
        }
    }

    /**
     * 将任意进制带空格数字转换为10进制int
     * @param text 数字文本
     * @param digits 进制
     * @return 十进制整数值
     */
    public static int toD(String text, int digits) { // 任意进制转十进制int
        text = text.replace(" ", ""); // 去除空格
        return Integer.parseInt(KeyBoardNumberUtil.toD(text, digits)); // 调用工具类转换并解析为int
    }

    /**
     * 将任意进制带空格数字转换为10进制long
     * @param text 数字文本
     * @param digits 进制
     * @return 十进制长整数值
     */
    public static long toDLong(String text, int digits) { // 任意进制转十进制long
        text = text.replace(" ", ""); // 去除空格
        return Long.parseLong(KeyBoardNumberUtil.toDLong(text, digits)); // 调用工具类转换并解析为long
    }

    /**
     * 将CAN DLC显示值转换为示波器底层DLC编码值
     * @param text DLC显示值字符串
     * @return 底层DLC编码值
     */
    public static int getCanDlcFromShow(String text) { // CAN DLC从显示值转底层编码
        if ("12".equals(text)) { // 显示12字节
            return 9; // 编码值9
        } else if ("16".equals(text)) { // 显示16字节
            return 10; // 编码值10
        } else if ("20".equals(text)) { // 显示20字节
            return 11; // 编码值11
        } else if ("24".equals(text)) { // 显示24字节
            return 12; // 编码值12
        } else if ("32".equals(text)) { // 显示32字节
            return 13; // 编码值13
        } else if ("48".equals(text)) { // 显示48字节
            return 14; // 编码值14
        } else if ("64".equals(text)) { // 显示64字节
            return 15; // 编码值15
        } else { // 其他标准DLC值
            return Integer.parseInt(text); // 直接解析为整数
        }
    }

    /**
     * 将示波器底层CAN DLC编码值转换为显示值
     * @param text 底层DLC编码值
     * @return DLC显示值
     */
    public static int getCanDlcFromScope(int text) { // CAN DLC从底层编码转显示值
        if (9 == text) { // 编码值9
            return 12; // 显示12字节
        } else if (10 == text) { // 编码值10
            return 16; // 显示16字节
        } else if (11 == text) { // 编码值11
            return 20; // 显示20字节
        } else if (12 == text) { // 编码值12
            return 24; // 显示24字节
        } else if (13 == text) { // 编码值13
            return 32; // 显示32字节
        } else if (14 == text) { // 编码值14
            return 48; // 显示48字节
        } else if (15 == text) { // 编码值15
            return 64; // 显示64字节
        } else { // 其他标准DLC值
            return text; // 直接返回原值
        }
    }

    /**
     * 将UI条件索引转换为EventBus触发关系常量
     * @param indexCondition UI条件索引（0=小于，1=大于，2=等于，3=不等于）
     * @return EventBus触发关系常量
     */
    public static int getConditionValueToEventBus(int indexCondition) { // UI条件索引转EventBus常量
        switch (indexCondition) { // 根据索引匹配
            case 0: // 索引0
                return IBus.TRIGGER_RELATION_LESS_THAN; // 返回"小于"常量
            case 1: // 索引1
                return IBus.TRIGGER_RELATION_MORE_THAN; // 返回"大于"常量
            case 2: // 索引2
                return IBus.TRIGGER_RELATION_EQUAL; // 返回"等于"常量
            case 3: // 索引3
                return IBus.TRIGGER_RELATION_NOT_EQUAL; // 返回"不等于"常量
            default: // 默认
                return IBus.TRIGGER_RELATION_LESS_THAN; // 返回"小于"常量
        }
    }

    /**
     * 将EventBus触发关系常量转换为UI条件索引
     * @param indexCondition EventBus触发关系常量
     * @return UI条件索引
     */
    public static int getConditionValueFromEventBus(int indexCondition) { // EventBus常量转UI条件索引
        switch (indexCondition) { // 根据常量匹配
            case IBus.TRIGGER_RELATION_LESS_THAN: // "小于"常量
                return 0; // 返回索引0
            case IBus.TRIGGER_RELATION_MORE_THAN: // "大于"常量
                return 1; // 返回索引1
            case IBus.TRIGGER_RELATION_EQUAL: // "等于"常量
                return 2; // 返回索引2
            case IBus.TRIGGER_RELATION_NOT_EQUAL: // "不等于"常量
                return 3; // 返回索引3
            default: // 默认
                return 0; // 返回索引0
        }
    }

    /**
     * 根据SPI数据文本生成掩码字符串（X替换为0，其他替换为1）
     * @param data SPI数据文本（含X占位符）
     * @return 掩码字符串
     */
    public static String getSpiMask(String data) { // 生成SPI掩码
        data = data.replace(" ", ""); // 去除空格
        StringBuilder result = new StringBuilder(); // 构建结果字符串
        for (int i = 0; i < data.length(); i++) { // 遍历每个字符
            if ('X' == data.charAt(i)) { // 如果是X占位符
                result.append('0'); // 掩码位为0（忽略位）
            } else { // 其他字符
                result.append('1'); // 掩码位为1（匹配位）
            }
        }
        return result.toString(); // 返回掩码字符串
    }

    /**
     * 根据SPI数据文本生成数据字符串（1保留为1，其他替换为0）
     * @param data SPI数据文本（含X占位符）
     * @return 数据字符串
     */
    public static String getSpiData(String data) { // 生成SPI数据
        data = data.replace(" ", ""); // 去除空格
        StringBuilder result = new StringBuilder(); // 构建结果字符串
        for (int i = 0; i < data.length(); i++) { // 遍历每个字符
            if ('1' == data.charAt(i)) { // 如果是1
                result.append('1'); // 保留1
            } else { // 其他字符（0或X）
                result.append('0'); // 替换为0
            }
        }
        return result.toString(); // 返回数据字符串
    }

    /**
     * 根据SPI掩码和数据生成显示文本（掩码0位显示X，掩码1位根据数据显示0/1）
     * 示例：Text 11 00 XX → mask 11 11 00 → data 11 00 00
     * @param mask 掩码整数值
     * @param data 数据整数值
     * @return 显示文本字符串
     */
    public static String getSpiText(int mask, int data) { // 根据掩码和数据生成SPI显示文本
        String sMask = Integer.toBinaryString(mask); // 将掩码转为二进制字符串
        String sData = Integer.toBinaryString(data); // 将数据转为二进制字符串
        String result = ""; // 结果字符串
        while (sMask.length() < sData.length()) { // 掩码位数少于数据时
            sMask = "0" + sMask; // 在掩码前补0
        }
        while (sData.length() < sMask.length()) { // 数据位数少于掩码时
            sData = "0" + sData; // 在数据前补0
        }
        for (int i = 0; i < sMask.length(); i++) { // 逐位遍历
            char cMask = sMask.charAt(i); // 获取掩码当前位
            char cdata = sData.charAt(i); // 获取数据当前位
            if (cMask == '0') { // 掩码位为0（忽略位）
                result += "X"; // 显示X
            } else if (cdata == '1') { // 数据位为1
                result += "1"; // 显示1
            } else { // 数据位为0
                result += "0"; // 显示0
            }
        }
        return result; // 返回显示文本
    }

    /**
     * 将long类型的数据转换成十六进制、二进制类型的带空格的数据
     * @param data long类型的原始数据
     * @param bits 最后数字的显示位数
     * @param digits 最后数字的显示进制数
     * @return 格式化后的字符串
     */
    public static String getHexBinFromLong(long data, int bits, int digits) { // long值转指定进制带空格字符串
        String sData; // 转换后的字符串
        if (digits == IDigits.DIGITS_16) { // 目标为16进制
            sData = Long.toHexString(data); // 转为十六进制字符串
        } else if (digits == IDigits.DIGITS_2) { // 目标为2进制
            sData = Long.toBinaryString(data); // 转为二进制字符串
        } else if (digits == IDigits.DIGITS_8) { // 目标为8进制
            sData = Long.toOctalString(data); // 转为八进制字符串
        } else { // 其他进制（10进制）
            sData = String.valueOf(data); // 转为十进制字符串
        }
        sData = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits( // 补位数并重算空格
                sData.replace(" ", ""), bits), digits).trim().toUpperCase(); // 去空格→补位数→重算空格→去首尾空格→转大写
        return sData; // 返回格式化后的字符串
    }

    /**
     * 将int类型的数据转换成十六进制、二进制类型的带空格的数据
     * @param data int类型的原始数据
     * @param bits 最后数字的显示位数
     * @param digits 最后数字的显示进制数
     * @return 格式化后的字符串
     */
    public static String getHexBinFromInt(int data, int bits, int digits) { // int值转指定进制带空格字符串
        String sData; // 转换后的字符串
        if (digits == IDigits.DIGITS_16) { // 目标为16进制
            sData = Integer.toHexString(data); // 转为十六进制字符串
        } else if (digits == IDigits.DIGITS_2) { // 目标为2进制
            sData = Integer.toBinaryString(data); // 转为二进制字符串
        } else if (digits == IDigits.DIGITS_8) { // 目标为8进制
            sData = Integer.toOctalString(data); // 转为八进制字符串
        } else { // 其他进制（10进制）
            sData = String.valueOf(data); // 转为十进制字符串
        }
        sData = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits( // 补位数并重算空格
                sData.replace(" ", ""), bits), digits).trim(); // 去空格→补位数→重算空格→去首尾空格
        return sData.toUpperCase(); // 返回大写格式化字符串
    }
}
