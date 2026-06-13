package com.micsig.tbook.scope.probe;


import android.os.SystemClock;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.micsig.tbook.scope.Calibrate.MHO68v1.MHO68v1_ProbeDACalibrate;
import com.micsig.tbook.scope.DB.DBHelper;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.probe.bean.BaseBean;
import com.micsig.tbook.scope.probe.bean.BeanFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProbeFactory {
    private static final String TAG = "ProbeFactory";
    private static volatile ProbeFactory instance = null;

    public static final long PROBE_TIMEOUT_MS = 3000L;
    public static final int PROBE_SEND_MAX = 3;
    List<ProbeCommand> probeCommands = new ArrayList<>();

    List<ProbeCommand> probeSendCommands = new ArrayList<>();
    public synchronized void sendProbeCommand(int chIdx, byte[] bytes, ProbeCommand.ProbeCommandlistener probeCommandlistener){

        if(bytes == null){
            long s = SystemClock.elapsedRealtime();
            long m = 0,n;
            ProbeCommand cmdx = null;
            for(ProbeCommand cmd:probeSendCommands){
                if(cmd.getChIdx() == chIdx){
                    n = s - cmd.getTimestamp();
                    if(n > m){
                        m = n;
                        cmdx = cmd;
                    }
                }
            }
            if(cmdx != null) {
                probeSendCommands.remove(cmdx);
                bytes = cmdx.getCmd();
            }else {
                return;
            }
        }

        if(bytes[1] == ProbeCommand.TYPE_PROBE_ACK){
            if(probeCommandlistener != null){
                probeCommandlistener.onProbeCommand(new ProbeCommand(chIdx, bytes));
            }
            return;
        }

        if(isProbeSendCommand(chIdx)){
            ProbeCommand probeCommand = new ProbeCommand(chIdx,bytes);
            probeCommands.add(probeCommand);
            if(probeCommandlistener != null){
                probeCommandlistener.onProbeCommand(probeCommand);
            }
        }else{

            boolean bExist = false;
            for(ProbeCommand cmd:probeSendCommands){
                if(cmd.getChIdx() == chIdx){
                    if(cmd.getCmdType() == bytes[1]){
                        cmd.setCmd(bytes);
                        bExist = true;
                        break;
                    }
                }
            }
            if(!bExist){
                probeSendCommands.add(new ProbeCommand(chIdx, bytes));
            }
        }
    }
    private  boolean isProbeSendCommand(int chIdx){
        for(ProbeCommand probeCommand:probeCommands){
            if(probeCommand.getChIdx() == chIdx){
                return false;
            }
        }
        return true;
    }
    public synchronized List<ProbeCommand> checkProbeCommandTimeout(ProbeCommand.ProbeCommandlistener probeCommandlistener){

        List<ProbeCommand> list = new ArrayList<>();
        long s = SystemClock.elapsedRealtime();
        if(probeCommandlistener != null) {
            for (ProbeCommand probeCommand : probeCommands) {
                if ((s - probeCommand.getTimestamp()) > PROBE_TIMEOUT_MS * probeCommand.getCounter()) {
                    if(probeCommand.getCounter() >= PROBE_SEND_MAX){
                        list.add(probeCommand);
                    }else {
                        probeCommandlistener.onProbeCommand(probeCommand);
                        probeCommand.addCounter();
                    }
                }
            }
        }
        return list;
    }

    public synchronized boolean isSendAck(int chIdx,byte cmd){
        for(ProbeCommand probeCommand:probeCommands){
            if(probeCommand.getChIdx() == chIdx
                    && probeCommand.getCmdType() == cmd){
                return false;
            }
        }
        return true;
    }
    public synchronized void removeProbeCommand(int chIdx){
        List<ProbeCommand> list = new ArrayList<>();
        for(ProbeCommand probeCommand:probeCommands){
            if(probeCommand.getChIdx() == chIdx){
                list.add(probeCommand);
            }
        }
        probeCommands.removeAll(list);
    }

    public synchronized void clearProbeCommand(int chIdx){
        List<ProbeCommand> list = new ArrayList<>();
        for(ProbeCommand probeCommand:probeCommands){
            if(probeCommand.getChIdx() == chIdx){
                list.add(probeCommand);
            }
        }
        probeCommands.removeAll(list);
        list.clear();
        for(ProbeCommand probeCommand:probeSendCommands){
            if(probeCommand.getChIdx() == chIdx){
                list.add(probeCommand);
            }
        }
        probeSendCommands.removeAll(list);
    }

    public synchronized boolean isProbeCommand(int chIdx,byte cmd){
        for(ProbeCommand probeCommand:probeCommands){
            if(probeCommand.getChIdx() == chIdx && probeCommand.getCmdType() == cmd){
                return true;
            }
        }
        for(ProbeCommand probeCommand:probeSendCommands){
            if(probeCommand.getChIdx() == chIdx && probeCommand.getCmdType() == cmd){
                return true;
            }
        }
        return false;
    }

    public void probeAbnormal(int chIdx){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if(channel != null){
            boolean bProbe = channel.isAutoProbe();
            alive(channel,false);
            if(bProbe){
                EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_ALARM,
                                new ProbeNotifyInfo(chIdx,ProbeNotifyInfo.ALARM_COMM_ABNORMAL)),
                        true);
            }
        }
    }
    private void sendCommand(int chIdx,byte [] bytes){
        sendCommand(chIdx,bytes,0);
    }
    private void sendCommand(int chIdx,byte [] bytes,long ms){
        ScopeMessage scopeMessage = ScopeMessage.getInstance();
        scopeMessage.sendProbe(chIdx,bytes,ms);
    }

    public synchronized static ProbeFactory  getInstance() {
        if (instance == null) {
            if (instance == null ) {
                instance = new ProbeFactory();
            }
        }
        return instance;
    }

    private ProbeFactory(){
        probeCommands.clear();
    }

    private static final int CMD_MAX_SIZE = 1024;
    private byte [] [] cmdBuffer = new byte[ChannelFactory.CH_CNT][CMD_MAX_SIZE];
    private int [] cmdlength = new int[ChannelFactory.CH_CNT];
    private ProbeUpgrade [] probeUpgrades = new ProbeUpgrade[ChannelFactory.CH_CNT];

    public void Input(Channel channel,ByteBuffer byteBuffer,int offset,int len){
        int idx = channel.getChId();
        int x = cmdlength[idx];
        byte [] cmd = cmdBuffer[idx];
        StringBuffer sb = new StringBuffer();
        sb.append("ch:" + channel.getChId() + ",");
        for (int i=0;i<len;i += 4){
            for(int j=0;j<4;j++){
                cmd[x + i + j] = byteBuffer.get(offset + i + 3 - j);
                sb.append(Integer.toHexString(0xFF & byteBuffer.get(offset + i + 3 - j)));
                sb.append(",");
            }
        }
        Log.d(TAG,"sb:" + sb);
        cmdlength[idx] += len;
    }

    public void clear(int chIdx){
        synchronized (this) {
            if (ChannelFactory.isDynamicCh(chIdx)) {
                clearProbeCommand(chIdx);
                cmdlength[chIdx] = 0;
                if (probeUpgrades[chIdx] != null) {
                    probeUpgrades[chIdx].onEnd();
                }
                probeUpgrades[chIdx] = null;
            }
        }
    }

    public void alive(Channel channel,boolean bAlive){
        if(channel != null){
            //Log.d(TAG, "alive() called with: channel = [" + channel.getChId() + "], bAlive = [" + bAlive + "]");

            if(bAlive){
                synchronized (this) {
                    BaseProbe probe = channel.getProbe();
                    int chIdx = channel.getChId();
                    if (probe == null && probeUpgrades[chIdx] == null) {
                        if (!isProbeCommand(chIdx, ProbeCommand.TYPE_PROBE_INFO)) {
                            clearProbeCommand(chIdx);
                            sendCommand(chIdx, ProbeCommand.probeCommand(ProbeCommand.TYPE_PROBE_INFO));
                        }
                    }
                }
            }else {
                BaseProbe probe = channel.getProbe();
                int chIdx = channel.getChId();
                if(probe != null){
                    probe.remove();
                    channel.setProbe(null);
                }
                clear(chIdx);
            }
        }
    }

    public void process(Channel channel){
        int idx = channel.getChId();
        int len = cmdlength[idx];
        byte [] cmd = cmdBuffer[idx];
        int k = 0;

        while (k < len && len > 4){

            if(cmd[k] == (byte)0xAA && cmd[k+1] != (byte)0xAA){

                int cmdlen = (cmd[k + 2] & 0xFF) + 4;

                if(len >= cmdlen + k){
                    byte s = ProbeCommand.checkSum_Command(cmd,k + 1,cmdlen - 1);

                    if(s == 0 || (cmd[k + cmdlen - 1] == (byte) 0xAA)){
                        parserCommand(channel, cmd, k);
                    }
                    k += cmdlen;
                }else{
                    break;
                }
            }else{
                k++;
            }
        }
        len = len - k;
        if(len < 0) len = 0;
        if(k > 0){
            for(int i=0;i<len;i++)
            {
                cmd[i] = cmd[k + i];
            }
            cmdlength[idx] = len;
        }
    }

    private DBHelper dbHelper;
    public void setDbHelper(DBHelper dbHelper){
        this.dbHelper = dbHelper;
    }

    public void setProbeDa(int chIdx,String sn,int val){
        dbHelper.setChProbeDa(chIdx,sn,val);
    }

    public void parserCommand(Channel channel, byte [] cmd,int offset){
        if(cmd[offset] == (byte) 0xAA){
            int chIdx = channel.getChId();
            ProbeUpgrade probeUpgrade =  probeUpgrades[chIdx];
            BaseProbe probe = channel.getProbe();
            int len = cmd[2 + offset] & 0xFF;
            byte cmdType = cmd[1 + offset];
            boolean bAck = true;
            switch (cmdType){
                case ProbeCommand.TYPE_PROBE_INFO:
                    byte [] bytes = new byte[len + 1];
                    System.arraycopy(cmd,offset + 3,bytes,0,len);
                    String str = new String(bytes).trim().toUpperCase();
                    Map<String, String> map = parserProbeInfo(str);
                    if(map.size() > 0) {
                        String name = map.get(BaseProbe.PROBE_TYPE_KEY);
                        if (name != null){
                            if (probe == null
                                    || !probe.getProbeName().equals(name)) {
                                probe = createProbe(name);
                            }
                        }
                        if(probe != null){
                            probe.setInfoMap(map);
                            channel.setProbe(probe);
                            probe.setChIdx(channel.getChId());
                            probe.defaultParam();
                            if(probe.isDa()){
                                int val = dbHelper.getChProbeDa(chIdx,probe.getSN());
                                if(val < 0) val = MHO68v1_ProbeDACalibrate.DEFAULT_DA_VAL;
                                probe.setDaValue(val);
                            }
                            PushProbeInfo.postDevInfo(probe);
                        }else{
                            String ver = map.get(BaseProbe.PROBE_VER_KEY);
                            if(ver != null && ver.length() > 0) {
                                checkVersion(chIdx, ver);
                            }
                        }
                    }
                    break;
                case ProbeCommand.TYPE_PROBE_VERSION:
                {
                    commandVersion(channel,cmd,offset + 3,len);
                    break;
                }
                case ProbeCommand.TYPE_JUMPBOOT:
                    if(probeUpgrade != null) probeUpgrade.AckJumpBootCommand(cmd,offset + 3,len);
                    break;
                case ProbeCommand.TYPE_PROBE_UPGRADE_BEGIN:
                    if(probeUpgrade != null) probeUpgrade.AckUpgradeBegin(cmd,offset + 3,len);
                    bAck = false;
                    break;
                case ProbeCommand.TYPE_PROBE_UPGRADE_DATA:
                    if(probeUpgrade != null) probeUpgrade.AckUpgrade(cmd,offset + 3,len);
                    bAck = false;
                    break;
                case ProbeCommand.TYPE_PROBE_UPGRADE_END:
                    if(probeUpgrade != null) probeUpgrade.AckUpgradeEnd(cmd,offset + 3,len);
                    bAck = false;
                    break;
                case ProbeCommand.TYPE_PROBE_UPGRADE_SS:
                    if(probeUpgrade != null) probeUpgrade.AckUpgradeSS(cmd,offset + 3,len);
                    bAck = false;
                    break;
                case ProbeCommand.TYPE_PROBE_ACK:
                    bAck = false;
                    break;
                default:
                    if(probe != null){
                        probe.parserCommand(cmd,offset);
                    }
                    break;
            }
            if(bAck){
                sendCommand(chIdx,ProbeCommand.probeCommand(ProbeCommand.TYPE_PROBE_ACK));
            }
            removeProbeCommand(chIdx);
            sendCommand(chIdx,null);
        }
    }

    private void checkVersion(int chIdx,String ver){
        if(ver != null && !ver.isEmpty()){
            Log.d("zhuzh","version:" + ver);
            if(ver.startsWith("ver:")){
                ver = ver.substring(4);
            }
            String [] V = ver.split("_");
            if(V != null && V.length != 3){
                V = null;
            }
            if(V != null){
                int v_nums = Integer.parseInt(V[2]);
                int verCode = Integer.parseInt(V[1]);
                int code = ProbeUtils.findBinVerCode(V[0], verCode);

                if (code > verCode) {
                    synchronized (this) {
                        if (probeUpgrades[chIdx] == null) {
                            int nums = v_nums;
                            Channel channel = ChannelFactory.getDynamicChannel(chIdx);
                            BaseProbe baseProbe = channel.getProbe();
                            if (baseProbe != null) {
                                nums = baseProbe.getMcuNums();
                            }

                            boolean isBoot = false;
                            if(verCode == 0){
                                isBoot = true;
                                if(v_nums > 1){
                                    nums = v_nums;
                                }
                            }

                            probeUpgrades[chIdx] = new ProbeUpgrade(chIdx, V[0], code, nums, isBoot);
                        }
                    }
                }
            }
        }
    }

    private void commandVersion(Channel channel,byte[] bytes,int offset,int len){
        byte [] datas = new byte[len + 1];
        System.arraycopy(bytes,offset,datas,0,len);
        String ver = new String(datas).trim().toLowerCase();
        checkVersion(channel.getChId(),ver);
    }

    private static final String PROBE_INFO_SEPARATOR = ":";
    private static final String PROBE_INFO_END="\n";


    private  Map<String, String> parserProbeInfo(String info){
        Log.d(TAG, "parserProbeInfo() called with: info = [" + info + "]");
        Map<String, String> map = new HashMap<>();
        if(!info.isEmpty()){
            int idx = 0;
            for (String key:BaseProbe.PROBE_INFO_KEYS
            ) {
                idx = info.indexOf(key);
                if(idx >= 0){
                    idx = info.indexOf(PROBE_INFO_SEPARATOR,idx + key.length());
                    if(idx > 0){
                        int eidx = info.indexOf(PROBE_INFO_END,idx + PROBE_INFO_SEPARATOR.length());
                        if(eidx > 0) {
                            String str = info.substring(idx + PROBE_INFO_SEPARATOR.length(), eidx).trim();
                            map.put(key,str);
                        }else{
                            String str = info.substring(idx + PROBE_INFO_SEPARATOR.length()).trim();
                            map.put(key,str);
                        }
                    }
                }
            }
        }
        return map;
    }

    BaseProbe createProbe(String name){
        BaseProbe probe = null;
        BaseBean bean = null;
        if(baseBeanMap.containsKey(name)){
            bean = baseBeanMap.get(name);
        }else{
            for (Map.Entry<String,BaseBean> entry : baseBeanMap.entrySet()) {
                BaseBean baseBean = entry.getValue();
                if(baseBean != null){
                    if(name.startsWith(baseBean.getClassName())){
                        bean = baseBean;
                        break;
                    }else {
                        String strPreFix = baseBean.getPrefix();
                        if(!strPreFix.isEmpty() && name.startsWith(strPreFix)){
                            bean = baseBean;
                            break;
                        }
                    }
                }
            }
        }
        if(bean == null){
            bean = BeanFactory.getInstance().getMatchBean(name);
        }
        if(bean != null){
            probe = bean.createProbe();
        }
        return probe;
    }
    public Map<String,BaseBean> baseBeanMap = new LinkedHashMap<>();
    public void parseJsontoProbe(String json){

        if(json != null && !json.isEmpty()) {
            try {
                baseBeanMap.clear();
                BeanFactory beanFactory = BeanFactory.getInstance();
                JsonElement jsonElement = JsonParser.parseString(json);
                if (jsonElement != null) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    if (jsonArray != null) {
                        for (JsonElement je : jsonArray) {
                            JsonObject jsonObject = je.getAsJsonObject();
                            if (jsonObject != null) {
                                String strClass = jsonObject.get("class").getAsString();
                                if (strClass != null) {
                                    JsonArray subArray = jsonObject.get("data").getAsJsonArray();
                                    for (JsonElement el : subArray) {
                                        BaseBean bean = beanFactory.getBean(strClass);
                                        if (bean != null) {
                                            try {
                                                bean.parseJson(el);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            bean.setClassName(strClass);
                                            baseBeanMap.put(bean.getProbeType(), bean);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        }
    }
}
