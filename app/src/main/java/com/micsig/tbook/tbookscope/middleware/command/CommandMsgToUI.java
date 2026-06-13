package com.micsig.tbook.tbookscope.middleware.command;

/**
 * Created by yangj on 2018/1/23.
 */

public class CommandMsgToUI {
    public static final String PARAM_SPLIT = ";";

    //region flag
    public static final String FLAG_AUTO_CHANNEL = "FLAG_AUTO_CHANNEL";
    public static final String FLAG_AUTO_LEVEL = "FLAG_AUTO_LEVEL";
    public static final String FLAG_AUTO_SOURCE = "FLAG_AUTO_SOURCE";
    public static final String FLAG_AUTO_RANGE = "FLAG_AUTO_RANGE";
    public static final String FLAG_AUTO_RANGEVERTICAL = "FLAG_AUTO_RANGEVERTICAL";
    public static final String FLAG_AUTO_RANGEHORIZONTAL = "FLAG_AUTO_RANGEHORIZONTAL";
    public static final String FLAG_AUTO_RANGELEVEL = "FLAG_AUTO_RANGELEVEL";

    public static final String FLAG_CHANNEL_DISPLAY = "FLAG_CHANNEL_DISPLAY";
    public static final String FLAG_CHANNEL_INVERSE = "FLAG_CHANNEL_INVERSE";
    public static final String FLAG_CHANNEL_BANDWIDTH = "FLAG_CHANNEL_BANDWIDTH";
    public static final String FLAG_CHANNEL_PROBETYPE = "FLAG_CHANNEL_PROBETYPE";
    public static final String FLAG_CHANNEL_PROBEMULTIPLE = "FLAG_CHANNEL_PROBEMULTIPLE";
    public static final String FLAG_CHANNEL_COUPLE = "FLAG_CHANNEL_COUPLE";
    public static final String FLAG_CHANNEL_EXTENT = "FLAG_CHANNEL_EXTENT";
    public static final String FLAG_CHANNEL_INPUTRES = "FLAG_CHANNEL_INPUTRES";
    public static final String FLAG_CHANNEL_CURRENT = "FLAG_CHANNEL_CURRENT";
    public static final String FLAG_CHANNEL_VREF = "FLAG_CHANNEL_VREF";
    public static final String FLAG_CHANNEL_LABEL = "FLAG_CHANNEL_LABEL";
    public static final String FLAG_CHANNEL_LABEL_CLEAR = "FLAG_CHANNEL_LABEL_CLEAR";
    public static final String FLAG_CHANNEL_DELAY="FLAG_CHANNEL_DELAY";
    public static final String FLAG_CHANNEL_OFFSET="FLAG_CHANNEL_OFFSET";
    public static final String FLAG_CHANNEL_VERNIER="FLAG_CHANNEL_VERNIER";

    public static final String FLAG_CURSOR_HORIZONTAL = "FLAG_CURSOR_HORIZONTAL";
    public static final String FLAG_CURSOR_VERTICAL = "FLAG_CURSOR_VERTICAL";
    public static final String FLAG_CURSOR_SOURCE = "FLAG_CURSOR_SOURCE";
    public static final String FLAG_CURSOR_SETTING_TRACE="FLAG_CURSOR_SETTING_TRACE";
    public static final String FLAG_DISPLAY_WAVEFORM = "FLAG_DISPLAY_WAVEFORM";
    public static final String FLAG_DISPLAY_BACKGROUND = "FLAG_DISPLAY_BACKGROUND";
    public static final String FLAG_DISPLAY_BRIGHTNESS = "FLAG_DISPLAY_BRIGHTNESS";
    public static final String FLAG_DISPLAY_GRATICULE = "FLAG_DISPLAY_GRATICULE";
    public static final String FLAG_DISPLAY_INTENSITY = "FLAG_DISPLAY_INTENSITY";
    public static final String FLAG_DISPLAY_PERSISTMODE = "FLAG_DISPLAY_PERSISTMODE";
    public static final String FLAG_DISPLAY_FFT_PERSISTMODE = "FLAG_DISPLAY_FFT_PERSISTMODE";
    public static final String FLAG_DISPLAY_PERSISTADJUST = "FLAG_DISPLAY_PERSISTADJUST";
    public static final String FLAG_DISPLAY_FFT_PERSISTADJUST = "FLAG_DISPLAY_FFT_PERSISTADJUST";
    public static final String FLAG_DISPLAY_HIGHREFRESH = "FLAG_DISPLAY_HIGHREFRESH";
    public static final String FLAG_DISPLAY_HORREF = "FLAG_DISPLAY_HORREF";
    public static final String FLAG_DISPLAY_ZOOM = "FLAG_DISPLAY_ZOOM";
    public static final String FLAG_DISPLAY_PERSIST_CLEAR = "FLAG_DISPLAY_PERSIST_CLEAR";
    public static final String FLAG_DISPLAY_FFT_PERSIST_CLEAR = "FLAG_DISPLAY_FFT_PERSIST_CLEAR";

    public static final String FLAG_MENU_AUTO = "FLAG_MENU_AUTO";
    public static final String FLAG_MENU_RUN = "FLAG_MENU_RUN";
    public static final String FLAG_MENU_STOP = "FLAG_MENU_STOP";
    public static final String FLAG_MENU_SINGLE = "FLAG_MENU_SINGLE";
    public static final String FLAG_MENU_MULTIPLE = "FLAG_MENU_MULTIPLE";
    public static final String FLAG_MENU_BEEP = "FLAG_MENU_BEEP";
    public static final String FLAG_MATH_DISPLAY = "FLAG_MATH_DISPLAY";
    public static final String FLAG_MATH_MODE = "FLAG_MATH_MODE";
    public static final String FLAG_MATH_ADDS1 = "FLAG_MATH_ADDS1";
    public static final String FLAG_MATH_ADDS2 = "FLAG_MATH_ADDS2";
    public static final String FLAG_MATH_SUBS1 = "FLAG_MATH_SUBS1";
    public static final String FLAG_MATH_SUBS2 = "FLAG_MATH_SUBS2";
    public static final String FLAG_MATH_MULS1 = "FLAG_MATH_MULS1";
    public static final String FLAG_MATH_MULS2 = "FLAG_MATH_MULS2";
    public static final String FLAG_MATH_DIVS1 = "FLAG_MATH_DIVS1";
    public static final String FLAG_MATH_DIVS2 = "FLAG_MATH_DIVS2";
    public static final String FLAG_MATH_FFTSOURCE = "FLAG_MATH_FFTSOURCE";
    public static final String FLAG_MATH_FFTWINDOW = "FLAG_MATH_FFTWINDOW";
    public static final String FLAG_MATH_FFTTYPE = "FLAG_MATH_FFTTYPE";
    public static final String FLAG_REF_DISPLAY = "FLAG_REF_DISPLAY";
    public static final String FLAG_REF_ENABLE = "FLAG_REF_ENABLE";
    public static final String FLAG_SAMPLE_TYPE = "FLAG_SAMPLE_TYPE";
    public static final String FLAG_SAMPLE_MEAN = "FLAG_SAMPLE_MEAN";
    public static final String FLAG_SAMPLE_ENVELOP = "FLAG_SAMPLE_ENVELOP";

    public static final String FLAG_TRIGGER_TYPE = "FLAG_TRIGGER_TYPE";
    public static final String FLAG_TRIGGER_HOLDOFF = "FLAG_TRIGGER_HOLDOFF";
    public static final String FLAG_TRIGGER_MODE = "FLAG_TRIGGER_MODE";
    public static final String FLAG_TRIGGERCAN_SOURCE = "FLAG_TRIGGERCAN_SOURCE";
    public static final String FLAG_TRIGGERCAN_IDLE = "FLAG_TRIGGERCAN_IDLE";
    public static final String FLAG_TRIGGERCAN_BAUDRATE = "FLAG_TRIGGERCAN_BAUDRATE";
    public static final String FLAG_TRIGGERCAN_TYPE = "FLAG_TRIGGERCAN_TYPE";
    public static final String FLAG_TRIGGERCAN_LEVEL = "FLAG_TRIGGERCAN_LEVEL";
    public static final String FLAG_TRIGGERRUNT_SOURCE = "FLAG_TRIGGERRUNT_SOURCE";
    public static final String FLAG_TRIGGERRUNT_POLAR = "FLAG_TRIGGERRUNT_POLAR";
    public static final String FLAG_TRIGGERRUNT_CONDITION = "FLAG_TRIGGERRUNT_CONDITION";
    public static final String FLAG_TRIGGERRUNT_HTIME = "FLAG_TRIGGERRUNT_HTIME";
    public static final String FLAG_TRIGGERRUNT_LOWTIME = "FLAG_TRIGGERRUNT_LOWTIME";
    public static final String FLAG_TRIGGERRUNT_BTIME = "FLAG_TRIGGERRUNT_BTIME";
    public static final String FLAG_TRIGGERRUNT_HLEVEL = "FLAG_TRIGGERRUNT_HLEVEL";
    public static final String FLAG_TRIGGERRUNT_LLEVEL = "FLAG_TRIGGERRUNT_LLEVEL";
    public static final String FLAG_TRIGGEREDGE_SOURCE = "FLAG_TRIGGEREDGE_SOURCE";
    public static final String FLAG_TRIGGEREDGE_SLOPE = "FLAG_TRIGGEREDGE_SLOPE";
    public static final String FLAG_TRIGGEREDGE_LEVEL = "FLAG_TRIGGEREDGE_LEVEL";
    public static final String FLAG_TRIGGEREDGE_COUPLE = "FLAG_TRIGGEREDGE_COUPLE";
    public static final String FLAG_TRIGGERIIC_SOURCE = "FLAG_TRIGGERIIC_SOURCE";
    public static final String FLAG_TRIGGERIIC_CLOCK = "FLAG_TRIGGERIIC_CLOCK";
    public static final String FLAG_TRIGGERIIC_TYPE = "FLAG_TRIGGERIIC_TYPE";
    public static final String FLAG_TRIGGERIIC_LEVELDATA = "FLAG_TRIGGERIIC_LEVELDATA";
    public static final String FLAG_TRIGGERIIC_LEVELCLOCK = "FLAG_TRIGGERIIC_LEVELCLOCK";
    public static final String FLAG_TRIGGERLIN_SOURCE = "FLAG_TRIGGERLIN_SOURCE";
    public static final String FLAG_TRIGGERLIN_IDLE = "FLAG_TRIGGERLIN_IDLE";
    public static final String FLAG_TRIGGERLIN_BAUDRATE = "FLAG_TRIGGERLIN_BAUDRATE";
    public static final String FLAG_TRIGGERLIN_TYPE = "FLAG_TRIGGERLIN_TYPE";
    public static final String FLAG_TRIGGERLIN_LEVEL = "FLAG_TRIGGERLIN_LEVEL";
    public static final String FLAG_TRIGGERLOGIC_STATUS = "FLAG_TRIGGERLOGIC_STATUS";
    public static final String FLAG_TRIGGERLOGIC_FUNCTION = "FLAG_TRIGGERLOGIC_FUNCTION";
    public static final String FLAG_TRIGGERLOGIC_CONDITION = "FLAG_TRIGGERLOGIC_CONDITION";
    public static final String FLAG_TRIGGERLOGIC_TIME = "FLAG_TRIGGERLOGIC_TIME";
    public static final String FLAG_TRIGGERLOGIC_HTIME = "FLAG_TRIGGERLOGIC_HTIME";
    public static final String FLAG_TRIGGERLOGIC_LOWTIME = "FLAG_TRIGGERLOGIC_LOWTIME";
    public static final String FLAG_TRIGGERLOGIC_BTIME = "FLAG_TRIGGERLOGIC_BTIME";
    public static final String FLAG_TRIGGERLOGIC_LEVEL = "FLAG_TRIGGERLOGIC_LEVEL";
    public static final String FLAG_TRIGGERM429_SOURCE = "FLAG_TRIGGERM429_SOURCE";
    public static final String FLAG_TRIGGERM429_FORMAT = "FLAG_TRIGGERM429_FORMAT";
    public static final String FLAG_TRIGGERM429_DISPLAY = "FLAG_TRIGGERM429_DISPLAY";
    public static final String FLAG_TRIGGERM429_BAUDRATE = "FLAG_TRIGGERM429_BAUDRATE";
    public static final String FLAG_TRIGGERM429_TYPE = "FLAG_TRIGGERM429_TYPE";
    public static final String FLAG_TRIGGERM429_LEVELHIGH = "FLAG_TRIGGERM429_LEVELHIGH";
    public static final String FLAG_TRIGGERM429_LEVELLOW = "FLAG_TRIGGERM429_LEVELLOW";
    public static final String FLAG_TRIGGERM1553B_SOURCE = "FLAG_TRIGGERM1553B_SOURCE";
    public static final String FLAG_TRIGGERM1553B_DISPLAY = "FLAG_TRIGGERM1553B_DISPLAY";
    public static final String FLAG_TRIGGERM1553B_TYPE = "FLAG_TRIGGERM1553B_TYPE";
    public static final String FLAG_TRIGGERM1553B_LEVEL = "FLAG_TRIGGERM1553B_LEVEL";
    public static final String FLAG_TRIGGERNEDGE_SOURCE = "FLAG_TRIGGERNEDGE_SOURCE";
    public static final String FLAG_TRIGGERNEDGE_SLOPE = "FLAG_TRIGGERNEDGE_SLOPE";
    public static final String FLAG_TRIGGERNEDGE_IDLE = "FLAG_TRIGGERNEDGE_IDLE";
    public static final String FLAG_TRIGGERNEDGE_EDGE = "FLAG_TRIGGERNEDGE_EDGE";
    public static final String FLAG_TRIGGERNEDGE_LEVEL = "FLAG_TRIGGERNEDGE_LEVEL";
    public static final String FLAG_TRIGGERPULSE_SOURCE = "FLAG_TRIGGERPULSE_SOURCE";
    public static final String FLAG_TRIGGERPULSE_POLAR = "FLAG_TRIGGERPULSE_POLAR";
    public static final String FLAG_TRIGGERPULSE_WIDTH = "FLAG_TRIGGERPULSE_WIDTH";
    public static final String FLAG_TRIGGERPULSE_HTIME = "FLAG_TRIGGERPULSE_HTIME";
    public static final String FLAG_TRIGGERPULSE_LTIME = "FLAG_TRIGGERPULSE_LTIME";
    public static final String FLAG_TRIGGERPULSE_CONDITION = "FLAG_TRIGGERPULSE_CONDITION";
    public static final String FLAG_TRIGGERPULSE_LEVEL = "FLAG_TRIGGERPULSE_LEVEL";
    public static final String FLAG_TRIGGERSLOPE_SOURCE = "FLAG_TRIGGERSLOPE_SOURCE";
    public static final String FLAG_TRIGGERSLOPE_EDGE = "FLAG_TRIGGERSLOPE_EDGE";
    public static final String FLAG_TRIGGERSLOPE_CONDITION = "FLAG_TRIGGERSLOPE_CONDITION";
    public static final String FLAG_TRIGGERSLOPE_HTIME = "FLAG_TRIGGERSLOPE_HTIME";
    public static final String FLAG_TRIGGERSLOPE_LTIME = "FLAG_TRIGGERSLOPE_LTIME";
    public static final String FLAG_TRIGGERSLOPE_BTIME = "FLAG_TRIGGERSLOPE_BTIME";
    public static final String FLAG_TRIGGERSLOPE_HLEVEL = "FLAG_TRIGGERSLOPE_HLEVEL";
    public static final String FLAG_TRIGGERSLOPE_LLEVEL = "FLAG_TRIGGERSLOPE_LLEVEL";
    public static final String FLAG_TRIGGER_SERIALBUS_TYPE = "FLAG_TRIGGER_SERIALBUS_TYPE";
    public static final String FLAG_TRIGGERSPI_CLOCK = "FLAG_TRIGGERSPI_CLOCK";
    public static final String FLAG_TRIGGERSPI_CLOCKSWITCH = "FLAG_TRIGGERSPI_CLOCKSWITCH";
    public static final String FLAG_TRIGGERSPI_DATA = "FLAG_TRIGGERSPI_DATA";
    public static final String FLAG_TRIGGERSPI_DATASWITCH = "FLAG_TRIGGERSPI_DATASWITCH";
    public static final String FLAG_TRIGGERSPI_CS = "FLAG_TRIGGERSPI_CS";
    public static final String FLAG_TRIGGERSPI_CSWITCH = "FLAG_TRIGGERSPI_CSWITCH";
    public static final String FLAG_TRIGGERSPI_CSENABLE = "FLAG_TRIGGERSPI_CSENABLE";
    public static final String FLAG_TRIGGERSPI_BITS = "FLAG_TRIGGERSPI_BITS";
    public static final String FLAG_TRIGGERSPI_TYPE = "FLAG_TRIGGERSPI_TYPE";
    public static final String FLAG_TRIGGERSPI_LEVELCLOCK = "FLAG_TRIGGERSPI_LEVELCLOCK";
    public static final String FLAG_TRIGGERSPI_LEVELDATA = "FLAG_TRIGGERSPI_LEVELDATA";
    public static final String FLAG_TRIGGERSPI_LEVELCS = "FLAG_TRIGGERSPI_LEVELCS";
    public static final String FLAG_TRIGGERTIMEOUT_SOURCE = "FLAG_TRIGGERTIMEOUT_SOURCE";
    public static final String FLAG_TRIGGERTIMEOUT_POLAR = "FLAG_TRIGGERTIMEOUT_POLAR";
    public static final String FLAG_TRIGGERTIMEOUT_TIME = "FLAG_TRIGGERTIMEOUT_TIME";
    public static final String FLAG_TRIGGERTIMEOUT_LEVEL = "FLAG_TRIGGERTIMEOUT_LEVEL";
    public static final String FLAG_TRIGGERUART_SOURCE = "FLAG_TRIGGERUART_SOURCE";
    public static final String FLAG_TRIGGERUART_IDLE = "FLAG_TRIGGERUART_IDLE";
    public static final String FLAG_TRIGGERUART_CHECK = "FLAG_TRIGGERUART_CHECK";
    public static final String FLAG_TRIGGERUART_BITS = "FLAG_TRIGGERUART_BITS";
    public static final String FLAG_TRIGGERUART_BAUDRATE = "FLAG_TRIGGERUART_BAUDRATE";
    public static final String FLAG_TRIGGERUART_DISPLAY = "FLAG_TRIGGERUART_DISPLAY";
    public static final String FLAG_TRIGGERUART_LEVEL = "FLAG_TRIGGERUART_LEVEL";
    public static final String FLAG_TRIGGERUART_TYPE = "FLAG_TRIGGERUART_TYPE";
    public static final String FLAG_TRIGGERVIDEO_SOURCE = "FLAG_TRIGGERVIDEO_SOURCE";
    public static final String FLAG_TRIGGERVIDEO_POLAR = "FLAG_TRIGGERVIDEO_POLAR";
    public static final String FLAG_TRIGGERVIDEO_STANDARD = "FLAG_TRIGGERVIDEO_STANDARD";
    public static final String FLAG_TRIGGERVIDEO_AMODE = "FLAG_TRIGGERVIDEO_AMODE";
    public static final String FLAG_TRIGGERVIDEO_BMODE = "FLAG_TRIGGERVIDEO_BMODE";
    public static final String FLAG_TRIGGERVIDEO_AFREQUENCE = "FLAG_TRIGGERVIDEO_AFREQUENCE";
    public static final String FLAG_TRIGGERVIDEO_BFREQUENCE = "FLAG_TRIGGERVIDEO_BFREQUENCE";
    public static final String FLAG_TRIGGERVIDEO_LINE = "FLAG_TRIGGERVIDEO_LINE";

    public static final String FLAG_USERSET_LENGTH = "FLAG_USERSET_LENGTH";
    public static final String FLAG_USERSET_FACTORYRESET = "FLAG_USERSET_FACTORYRESET";
    public static final String FLAG_USERSET_SELFADJUST = "FLAG_USERSET_SELFADJUST";
    public static final String FLAG_USERSET_NAME = "FLAG_USERSET_NAME";
    public static final String FLAG_USERSET_SAVE = "FLAG_USERSET_SAVE";
    public static final String FLAG_USERSET_RECOVERY = "FLAG_USERSET_RECOVERY";
    public static final String FLAG_USERSET_AutoZero = "FLAG_USERSET_AutoZero";

    public static final String FLAG_MATH_ADDEXTENT = "FLAG_MATH_ADDEXTENT";
    public static final String FLAG_MATH_ADDOFFSET = "FLAG_MATH_ADDOFFSET";
    public static final String FLAG_MATH_SUBEXTENT = "FLAG_MATH_SUBEXTENT";
    public static final String FLAG_MATH_SUBOFFSET = "FLAG_MATH_SUBOFFSET";
    public static final String FLAG_MATH_MULEXTENT = "FLAG_MATH_MULEXTENT";
    public static final String FLAG_MATH_MULOFFSET = "FLAG_MATH_MULOFFSET";
    public static final String FLAG_MATH_DIVEXTENT = "FLAG_MATH_DIVEXTENT";
    public static final String FLAG_MATH_DIVOFFSET = "FLAG_MATH_DIVOFFSET";

    public static final String FLAG_MENU_HALF_CHANNEL = "FLAG_MENU_HALF_CHANNEL";
    public static final String FLAG_MENU_TRIGPOS = "FLAG_MENU_TRIGPOS";
    public static final String FLAG_MENU_XCURSOR = "FLAG_MENU_XCURSOR";
    public static final String FLAG_MENU_YCURSOR = "FLAG_MENU_YCURSOR";
    public static final String FLAG_MENU_LEVEL = "FLAG_MENU_LEVEL";
    public static final String FLAG_MENU_HOMEPAGE = "FLAG_MENU_HOMEPAGE";
    public static final String FLAG_MENU_RETURN = "FLAG_MENU_RETURN";
    public static final String FLAG_MENU_LOCK = "FLAG_MENU_LOCK";
    public static final String FLAG_MENU_UNLOCK = "FLAG_MENU_UNLOCK";
    public static final String FLAG_MENU_COUNTER = "FLAG_MENU_COUNTER";
    public static final String FLAG_MENU_RESET = "FLAG_MENU_RESET";
    public static final String FLAG_MENU_MEASURE = "FLAG_MENU_MEASURE";
    public static final String FLAG_MENU_TRIGGER = "FLAG_MENU_TRIGGER";
    public static final String FLAG_MENU_CHANNELSELECTOR = "FLAG_MENU_CHANNELSELECTOR";
    public static final String FLAG_MENU_Main = "FLAG_MENU_Main";
    public static final String FLAG_MENU_AUX_TRIGGER="FLAG_MENU_AUX_TRIGGER";
    public static final String FLAG_MENU_AUX_CLOCK="FLAG_MENU_AUX_CLOCK";
    public static final String FLAG_MENU_AUX_INPUTRES="FLAG_MENU_AUX_INPUTRES";

    public static final String FLAG_MENU_QuickBottom = "FLAG_MENU_QuickBottom";
    public static final String FLAG_MENU_CHANNEL = "FLAG_MENU_CHANNEL";

    public static final String FLAG_DISPLAY_TIMEBASE = "FLAG_DISPLAY_TIMEBASE";
    public static final String FLAG_DISPLAY_CCT = "FLAG_DISPLAY_CCT";

    public static final String FLAG_TIMEBASE_EXTENT = "FLAG_TIMEBASE_EXTENT";
    public static final String FLAG_TIMEBASE_POSITION = "FLAG_TIMEBASE_POSITION";
    public static final String FLAG_TIMEBASE_MODE = "FLAG_TIMEBASE_MODE";
    public static final String FLAG_TIMEBASE_ROLL = "FLAG_TIMEBASE_ROLL";
    public static final String FLAG_TIMEBASE_ZOOM_SCALE = "FLAG_TIMEBASE_ZOOM_SCALE";

    public static final String FLAG_MATH_BASE_S1 = "FLAG_MATH_BASE_S1";
    public static final String FLAG_MATH_BASE_S2 = "FLAG_MATH_BASE_S2";
    public static final String FLAG_MATH_BASE_EXTENT = "FLAG_MATH_BASE_EXTENT";
    public static final String FLAG_MATH_BASE_OFFSET = "FLAG_MATH_BASE_OFFSET";
    public static final String FLAG_MATH_BASE_OPERATOR = "FLAG_MATH_BASE_OPERATOR";
    public static final String FLAG_MATH_VREF = "FLAG_MATH_VREF";
    public static final String FLAG_MATH_FFT_Extent = "FLAG_MATH_FFT_Extent";
    public static final String FLAG_MATH_FFT_Offset = "FLAG_MATH_FFT_Offset";
    public static final String FLAG_MATH_FFT_HsCal = "FLAG_MATH_FFT_HsCal";
    public static final String FLAG_MATH_FFT_Position = "FLAG_MATH_FFT_Position";
    public static final String FLAG_MATH_AXB_Source = "FLAG_MATH_AXB_Source";
    public static final String FLAG_MATH_AXB_Extent = "FLAG_MATH_AXB_Extent";
    public static final String FLAG_MATH_AXB_Offset = "FLAG_MATH_AXB_Offset";
    public static final String FLAG_MATH_AXB_A = "FLAG_MATH_AXB_A";
    public static final String FLAG_MATH_AXB_B = "FLAG_MATH_AXB_B";
    public static final String FLAG_MATH_AXB_UNIT = "FLAG_MATH_AXB_UNIT";
    public static final String FLAG_MATH_LABEL = "FLAG_MATH_LABEL";
    public static final String FLAG_MATH_LABEL_CLEAR = "FLAG_MATH_LABEL_CLEAR";

    public static final String FLAG_MATH_ADV_Express = "FLAG_MATH_ADV_Express";
    public static final String FLAG_MATH_ADV_Var1 = "FLAG_MATH_ADV_Var1";
    public static final String FLAG_MATH_ADV_Var2 = "FLAG_MATH_ADV_Var2";
    public static final String FLAG_MATH_ADV_Extent = "FLAG_MATH_ADV_Extent";
    public static final String FLAG_MATH_ADV_Offset = "FLAG_MATH_ADV_Offset";
    public static final String FLAG_MATH_ADV_Unit = "FLAG_MATH_ADV_Unit";

    public static final String FLAG_STOTAGE_SAVE = "FLAG_STOTAGE_SAVE";
    public static final String FLAG_STOTAGE_CAPTURE = "FLAG_STOTAGE_CAPTURE";
    public static final String FLAG_STOTAGE_RECORD = "FLAG_STOTAGE_RECORD";
    public static final String FLAG_STOTAGE_SAVE_SOURCE = "FLAG_STOTAGE_SAVE_SOURCE";
    public static final String FLAG_STOTAGE_SAVE_LOCATION = "FLAG_STOTAGE_SAVE_LOCATION";
    public static final String FLAG_STOTAGE_SAVE_TYPE = "FLAG_STOTAGE_SAVE_TYPE";
    public static final String FLAG_STOTAGE_SAVE_FILENAME = "FLAG_STOTAGE_SAVE_FILENAME";
    public static final String FLAG_STOTAGE_SAVE_START = "FLAG_STOTAGE_SAVE_START";
    public static final String FLAG_STOTAGE_LOAD = "FLAG_STOTAGE_LOAD";
    public static final String FLAG_STOTAGE_CAPTURE_TIME = "FLAG_STOTAGE_CAPTURE_TIME";
    public static final String FLAG_STOTAGE_CAPTURE_INCOLOR = "FLAG_STOTAGE_CAPTURE_INCOLOR";
    public static final String FLAG_STOTAGE_CAPTURE_THUMBNAIL = "FLAG_STOTAGE_CAPTURE_THUMBNAIL";
    public static final String FLAG_STOTAGE_CAPTURE_START = "FLAG_STOTAGE_CAPTURE_START";
    public static final String FLAG_STOTAGE_SAVE_ALLSEGMENTS = "FLAG_STOTAGE_SAVE_ALLSEGMENTS";
    public static final String FLAG_STOTAGE_CONSAVE = "FLAG_STOTAGE_CONSAVE";
    public static final String FLAG_STOTAGE_CONSAVE_START = "FLAG_STOTAGE_CONSAVE_START";
    public static final String FLAG_STOTAGE_CONLOAD = "FLAG_STOTAGE_CONLOAD";

    public static final String FLAG_Bus_Mode = "FLAG_Bus_Mode";
    public static final String FLAG_Bus_Type = "FLAG_Bus_Type";
    public static final String FLAG_Bus_Uart_Rx = "FLAG_Bus_Uart_Rx";
    public static final String FLAG_Bus_Uart_IdLevel = "FLAG_Bus_Uart_IdLevel";
    public static final String FLAG_Bus_Uart_BaudRate = "FLAG_Bus_Uart_BaudRate";
    public static final String FLAG_Bus_Uart_Check = "FLAG_Bus_Uart_Check";
    public static final String FLAG_Bus_Uart_UserBaud = "FLAG_Bus_Uart_UserBaud";
    public static final String FLAG_Bus_Uart_Width = "FLAG_Bus_Uart_Width";
    public static final String FLAG_Bus_Uart_Display = "FLAG_Bus_Uart_Display";

    public static final String FLAG_Bus_Lin_Channel = "FLAG_Bus_Lin_Channel";
    public static final String FLAG_Bus_Lin_IdLevel = "FLAG_Bus_Lin_IdLevel";
    public static final String FLAG_Bus_Lin_BaudRate = "FLAG_Bus_Lin_BaudRate";
    public static final String FLAG_Bus_Lin_Userbaud = "FLAG_Bus_Lin_Userbaud";
    public static final String FLAG_Bus_Lin_TYPE = "FLAG_Bus_Lin_Type";

    public static final String FLAG_Bus_Can_Channel = "FLAG_Bus_Can_Channel";
    public static final String FLAG_Bus_Can_Signal = "FLAG_Bus_Can_Signal";
    public static final String FLAG_Bus_Can_BaudRate = "FLAG_Bus_Can_BaudRate";
    public static final String FLAG_Bus_Can_UserBaud = "FLAG_Bus_Can_UserBaud";
    public static final String FLAG_Bus_Can_SamplePoint = "FLAG_Bus_Can_SamplePoint";
    public static final String FLAG_Bus_Can_FDBaudrate = "FLAG_Bus_Can_FDBaudrate";
    public static final String FLAG_Bus_Can_FDUserBaud = "FLAG_Bus_Can_FDUserBaud";
    public static final String FLAG_Bus_Can_FDSamplePoint = "FLAG_Bus_Can_FDSamplePoint";
    public static final String FLAG_Bus_Can_ISO="FLAG_Bus_Can_ISO";

    public static final String FLAG_Bus_IIC_SDA = "FLAG_Bus_IIC_SDA";
    public static final String FLAG_Bus_IIC_SCL = "FLAG_Bus_IIC_SCL";

    public static final String FLAG_Bus_429_Channel = "FLAG_Bus_429_Channel";
    public static final String FLAG_Bus_429_Format = "FLAG_Bus_429_Format";
    public static final String FLAG_Bus_429_display = "FLAG_Bus_429_display";
    public static final String FLAG_Bus_429_Baudrate = "FLAG_Bus_429_Baudrate";

    public static final String FLAG_Bus_1553B_Channel = "FLAG_Bus_1553B_Channel";
    public static final String FLAG_Bus_1553B_Display = "FLAG_Bus_1553B_Display";


    public static final String FLAG_SAMPLE_IsOpenSegMent = "FLAG_SAMPLE_IsOpenSegMent";
    public static final String FLAG_SAMPLE_SegmentedQTY = "FLAG_SAMPLE_SegmentedQTY";
    public static final String FLAG_SAMPLE_SegmentedDisplayType = "FLAG_SAMPLE_SegmentedDisplayType";
    public static final String FLAG_SAMPLE_SegmentedOrder = "FLAG_SAMPLE_SegmentedOrder";
    public static final String FLAG_SAMPLE_SegmentedPlay = "FLAG_SAMPLE_SegmentedPlay";
    public static final String FLAG_SAMPLE_SegmentedStop = "FLAG_SAMPLE_SegmentedStop";
    public static final String FLAG_SAMPLE_SegmentedFra1 = "FLAG_SAMPLE_SegmentedFra1";
    public static final String FLAG_SAMPLE_SegmentedFra2 = "FLAG_SAMPLE_SegmentedFra2";
    public static final String FLAG_SAMPLE_SegmentedFra3 = "FLAG_SAMPLE_SegmentedFra3";
    public static final String FLAG_SAMPLE_SegmentedPlaySpeed = "FLAG_SAMPLE_SegmentedPlaySpeed";

    public static final String FLAG_TRIGGER_SPI_TYPE = "FLAG_TRIGGER_SPI_TYPE";
    public static final String FLAG_TRIGGER_SPI_DATA = "FLAG_TRIGGER_SPI_DATA";
    public static final String FLAG_TRIGGER_SPI_LEVEL = "FLAG_TRIGGER_SPI_LEVEL";

    public static final String FLAG_REF_Current_Channel = "FLAG_REF_Current_Channel";
    public static final String FLAG_REF_Hscal = "FLAG_REF_Hscal";
    public static final String FLAG_REF_Vscal = "FLAG_REF_Vscal";
    public static final String FLAG_REF_Position = "FLAG_REF_Position";
    public static final String FLAG_REF_Timebase_Position = "FLAG_REF_Timebase_Position";
    public static final String FLAG_REF_LABEL = "FLAG_REF_LABEL";
    public static final String FLAG_REF_LABEL_CLEAR = "FLAG_REF_LABEL_CLEAR";

    public static final String FLAG_MEASURE_ALL_DISPLAY = "FLAG_MEASURE_ALL_DISPLAY";
    public static final String FLAG_MEASURE_COUNT_SOURCE = "FLAG_MEASURE_COUNT_SOURCE";
    public static final String FLAG_MEASURE_COUNT_MODE = "FLAG_MEASURE_COUNT_MODE";

    public static final String FLAG_Measure_STAT_Display="FLAG_Measure_STAT_Display";
    public static final String FLAG_Measure_STAT_Reset="FLAG_Measure_STAT_Reset";
    public static final String FLAG_Measure_STAT_Mean="FLAG_Measure_STAT_Mean";
    public static final String FLAG_Measure_STAT_Max="FLAG_Measure_STAT_Max";
    public static final String FLAG_Measure_STAT_Min="FLAG_Measure_STAT_Min";
    public static final String FLAG_Measure_STAT_Dev="FLAG_Measure_STAT_Dev";
    public static final String FLAG_Measure_STAT_Count="FLAG_Measure_STAT_Count";

    public static final String FLAG_Measure_Setting_Indicator="FLAG_Measure_Setting_Indicator";
    public static final String FLAG_Measure_Setting_Range="FLAG_Measure_Setting_Range";
    //endregion

    private String flag;
    private String param;
    private Object object;

    public String getFlag() {
        return flag;
    }

    public String getParam() {
        return param;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setParam(String param) {
        this.param = param;
    }
    public void setObject(Object object){
        this.object=object;
    }
    public Object getObject(){
        return this.object;
    }

    public CommandMsgToUI() {
    }

    public CommandMsgToUI(String flag, String param) {
        this.flag = flag;
        this.param = param;
    }
}
