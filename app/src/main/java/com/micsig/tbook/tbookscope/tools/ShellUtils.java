package com.micsig.tbook.tbookscope.tools;                    // 声明包名，属于tbookscope工具模块

import java.io.BufferedReader;                                 // 导入缓冲读取流，用于读取命令输出
import java.io.DataOutputStream;                               // 导入数据输出流，用于向进程写入命令
import java.io.IOException;                                    // 导入IO异常类
import java.io.InputStreamReader;                              // 导入输入流转换器，用于将字节流转换为字符流
import java.util.List;                                         // 导入List集合类

/*
 * +=============================================================================+
 * |                          ShellUtils - Shell命令工具类                        |
 * +=============================================================================+
 * | 模块定位：tbookscope工具模块，提供Shell命令执行能力                              |
 * | 核心职责：封装Android Shell命令的执行、结果获取与Root权限检测                      |
 * | 架构设计：纯静态工具类，无状态，所有方法均为static，核心逻辑委托给execCommand三参方法    |
 * | 数据流向：调用方 → execCommand重载方法 → execCommand(核心) → Runtime.exec → 进程 |
 * |           进程输出 → BufferedReader → StringBuilder → CommandResult → 调用方    |
 * | 依赖关系：依赖java.io（流操作）、内部类CommandResult                              |
 * | 使用场景：需要执行Shell命令时（如系统配置、文件操作、Root权限检测等）                    |
 * +=============================================================================+
 */
public class ShellUtils {
    public static final String COMMAND_SU = "su";               // Root权限命令标识，用于以超级用户身份执行
    public static final String COMMAND_SH = "sh";               // 普通Shell命令标识，用于以普通用户身份执行
    public static final String COMMAND_EXIT = "exit\n";         // 退出Shell命令，带换行符，用于结束Shell会话
    public static final String COMMAND_LINE_END = "\n";         // 命令行结束符，每条命令后需换行以提交执行


    /**
     * 查看是否有了root权限
     *
     * @return true表示已获取Root权限，false表示未获取
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0; // 执行"echo root"命令，以Root身份运行，不需要结果消息，返回码为0则表示有Root权限
    }


    /**
     * 执行shell命令，默认返回结果
     *
     * @param command
     *            command 要执行的Shell命令字符串
     * @param isRoot
     *            运行是否需要root权限
     * @return 命令执行结果对象
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[] { command }, isRoot, true); // 将单条命令包装为数组，默认需要返回结果消息
    }


    /**
     * 执行shell命令，默认返回结果
     *
     * @param commands
     *            command list 命令列表
     * @param isRoot
     *            运行是否需要root权限
     * @return 命令执行结果对象
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands,
                                            boolean isRoot) {
        return execCommand(
                commands == null ? null : commands.toArray(new String[] {}), // 将List转为数组，若为null则传null
                isRoot, true);                                               // 默认需要返回结果消息
    }


    /**
     * 执行shell命令，默认返回结果
     *
     * @param commands
     *            command array 命令数组
     * @param isRoot
     *            运行是否需要root权限
     * @return 命令执行结果对象
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);               // 默认需要返回结果消息
    }


    /**
     * execute shell command
     *
     * @param command
     *            command 要执行的Shell命令字符串
     * @param isRoot
     *            运行是否需要root权限
     * @param isNeedResultMsg
     *            whether need result msg 是否需要返回结果消息
     * @return 命令执行结果对象
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot,
                                            boolean isNeedResultMsg) {
        return execCommand(new String[] { command }, isRoot, isNeedResultMsg); // 将单条命令包装为数组，委托给数组参数版本
    }


    /**
     * execute shell commands
     *
     * @param commands
     *            command list 命令列表
     * @param isRoot
     *            运行是否需要root权限
     * @param isNeedResultMsg
     *            是否需要返回运行结果
     * @return 命令执行结果对象
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands,
                                            boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(
                commands == null ? null : commands.toArray(new String[] {}), // 将List转为数组，若为null则传null
                isRoot, isNeedResultMsg);                                    // 透传是否需要Root权限和是否需要结果消息
    }


    /**
     * execute shell commands - 核心执行方法，所有重载方法最终委托至此
     *
     * @param commands
     *            command array 要执行的命令数组
     * @param isRoot
     *            运行是否需要root权限
     * @param isNeedResultMsg
     *            是否需要返回运行结果
     * @return <ul>
     *         <li>if isNeedResultMsg is false, {@link CommandResult#successMsg}
     *         is null and {@link CommandResult#errorMsg} is null.</li>
     *         <li>if {@link CommandResult#result} is -1, there maybe some
     *         excepiton.</li>
     *         </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        int result = -1;                                          // 初始化执行结果为-1，表示异常/未执行
        if (commands == null || commands.length == 0) {           // 检查命令数组是否为空或长度为0
            return new CommandResult(result, null, null);         // 若无命令则直接返回默认失败结果
        }


        Process process = null;                                   // 声明进程对象，用于执行Shell命令
        BufferedReader successResult = null;                      // 声明成功输出读取器，用于读取标准输出流
        BufferedReader errorResult = null;                        // 声明错误输出读取器，用于读取错误输出流
        StringBuilder successMsg = null;                          // 声明成功消息构建器，用于拼接标准输出内容
        StringBuilder errorMsg = null;                            // 声明错误消息构建器，用于拼接错误输出内容


        DataOutputStream os = null;                               // 声明数据输出流，用于向进程写入命令
        try {
            process = Runtime.getRuntime().exec(                  // 根据是否需要Root权限选择执行"su"或"sh"命令
                    isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream()); // 获取进程的输出流并包装为DataOutputStream，用于写入命令
            for (String command : commands) {                     // 遍历所有待执行的命令
                if (command == null) {                            // 跳过空命令
                    continue;
                }


                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());                     // 将命令以字节方式写入（避免writeBytes的中文编码问题）
                os.writeBytes(COMMAND_LINE_END);                  // 写入换行符，表示该条命令结束
                os.flush();                                       // 刷新输出流，确保命令被提交执行
            }
            os.writeBytes(COMMAND_EXIT);                          // 写入exit命令，通知Shell会话结束
            os.flush();                                           // 刷新输出流，确保exit命令被提交


            result = process.waitFor();                           // 等待进程执行完毕，获取返回码（0表示成功）
            // get command result
            if (isNeedResultMsg) {                                // 如果需要获取命令执行的结果消息
                successMsg = new StringBuilder();                 // 创建成功消息构建器
                errorMsg = new StringBuilder();                   // 创建错误消息构建器
                successResult = new BufferedReader(new InputStreamReader( // 创建标准输出读取器
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(   // 创建错误输出读取器
                        process.getErrorStream()));
                String s;                                        // 声明临时字符串，用于逐行读取
                while ((s = successResult.readLine()) != null) {  // 逐行读取标准输出内容
                    successMsg.append(s);                         // 将每行内容追加到成功消息中
                }
                while ((s = errorResult.readLine()) != null) {    // 逐行读取错误输出内容
                    errorMsg.append(s);                           // 将每行内容追加到错误消息中
                }
            }
        } catch (IOException e) {                                 // 捕获IO异常
            e.printStackTrace();                                  // 打印IO异常堆栈信息
        } catch (Exception e) {                                   // 捕获其他异常
            e.printStackTrace();                                  // 打印异常堆栈信息
        } finally {
            try {
                if (os != null) {                                 // 如果输出流不为空
                    os.close();                                   // 关闭输出流，释放资源
                }
                if (successResult != null) {                      // 如果成功输出读取器不为空
                    successResult.close();                        // 关闭成功输出读取器
                }
                if (errorResult != null) {                        // 如果错误输出读取器不为空
                    errorResult.close();                          // 关闭错误输出读取器
                }
            } catch (IOException e) {                             // 捕获关闭流时的IO异常
                e.printStackTrace();                              // 打印关闭流时的异常堆栈信息
            }


            if (process != null) {                                // 如果进程不为空
                process.destroy();                                // 销毁进程，释放系统资源
            }
        }
        return new CommandResult(result, successMsg == null ? null // 构建并返回命令执行结果对象
                : successMsg.toString(), errorMsg == null ? null   // 若成功消息为空则传null，否则转为字符串
                : errorMsg.toString());                            // 若错误消息为空则传null，否则转为字符串
    }


    /**
     * 运行结果 - 命令执行结果的封装类
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal,
     * else means error, same to excute in linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command
     * result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a>
     *         2013-5-16
     */
    public static class CommandResult {


        /** 运行结果 - 命令执行返回码，0表示成功，非0表示失败 **/
        public int result;
        /** 运行成功结果 - 命令标准输出内容 **/
        public String successMsg;
        /** 运行失败结果 - 命令错误输出内容 **/
        public String errorMsg;


        /**
         * 仅包含返回码的构造方法
         *
         * @param result 命令执行返回码
         */
        public CommandResult(int result) {
            this.result = result;                                 // 设置命令执行返回码
        }


        /**
         * 包含完整信息的构造方法
         *
         * @param result    命令执行返回码
         * @param successMsg 成功输出消息
         * @param errorMsg   错误输出消息
         */
        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;                                 // 设置命令执行返回码
            this.successMsg = successMsg;                         // 设置成功输出消息
            this.errorMsg = errorMsg;                             // 设置错误输出消息
        }
    }
}
