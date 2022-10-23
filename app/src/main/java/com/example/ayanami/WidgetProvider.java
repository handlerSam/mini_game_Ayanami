package com.example.ayanami;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;


/*
    SharedPreference 存储位置
        文件名map  变量i_j 存状态 0海洋 1岛屿 2我方舰船 3敌方舰船 4被击中的我方船 5被击中的敌方船 6我方已炮击过的区域
        文件名chessBoard 变量i_j 0空白 1我方棋子 2绫波棋子
        文件名treasureMap 变量i_j 0空白 1未被找到的宝藏 10~18我方找过的区域 19我方找到的宝藏 20~28绫波找过的区域 29绫波找到的宝藏

    stage表示：
    //-1地图已收起 0正在选择舰队 1正在摆放第一艘 2正在摆放第二艘 3该绫波放船了 4该我方炮击了
    //5绫波在思考炮击位置 6绫波炮击结果显示 7玩家胜利 8绫波胜利 9唱歌中 10音频文件加载中
    //11该玩家落子 12绫波在思考落子位置 13绫波落子结果显示 14玩家胜利 15绫波胜利 16平局
    //17存档界面打开 101-109存档确认界面打开 18读档界面打开 111-119读档确认界面打开
    //20该玩家寻宝了 21绫波正在思考寻宝位置 22绫波寻宝位置展示 23玩家胜利 24绫波胜利
 */
public class WidgetProvider extends AppWidgetProvider {
    public static final String CLICKBODY = "com.example.REFRESH_BODY";
    public static final String CLICKHEAD = "com.example.REFRESH_HEAD";
    public static final String CLICK = "com.example.REFRESH_WIDGET";
    public static final String CLICKMAP = "com.example.MAP";//使用的时候调用judgeText
    public static final String CLICKFOLD = "com.example.FLODMAP";
    public static final String CLICKUNFOLD = "com.example.UNFLODMAP";
    public static final String CLICKRESTART = "com.example.RESTART";
    public static final String CHOOSEDESTROYER = "com.example.CHOOSEDESTROYER";
    public static final String CHOOSEBATTLESHIP = "com.example.CHOOSEBATTLESHIP";
    public static final String CHOOSEAIRCRAFTCARRIER = "com.example.AIRCRAFTCARRIER";
    public static final String CHANGEORIENTATION = "com.example.CHANGEORIENTATION";
    public static final String CLICKMAIN = "com.example.CLICKMAIN";
    public static final String CLICKSING = "com.example.CLICKSING";
    public static final String CLICKUNFOLDBOARD = "com.example.CLICKUNFOLDMAP";
    public static final String AIINITIATIVE = "com.example.AIINITIATIVE";
    public static final String PLAYERINITIATIVE = "com.example.PLAYERINITIATIVE";
    public static final String LOAD = "com.example.LOAD";
    public static final String SAVE = "com.example.SAVE";
    public static final String CONFIRMYES = "com.example.CONFIRMYES";
    public static final String CONFIRMNO = "com.example.CONFIRMNO";
    public static final String SAVEDOCUMENT = "com.example.SAVE";
    public static final String UNFOLDTREASUREMAP = "com.example.UNFOLDTREASUREMAP";


    public static final int SHIPX = 0;
    public static final int SHIPY = 1;
    public static final int SHIPORIENTATION = 2;
    public Context CONTEXT;
    public static ArrayList<String> strList = new ArrayList<>();
    public static ArrayList<String> lyricList = new ArrayList<>();
    public static ArrayList<Float> lyricIntervalList = new ArrayList<>();
    public static boolean CANTOUCH = true;
    public static int[][] AIRCRAFTCARRIERHITAREA = {{0,0},{-1,-1},{-1,0},{-1,1},{1,0}};
    public static int[][] BATTLESHIPHITAREA = {{0,0},{-1,-1},{-1,0},{0,-1},{1,1}};
    public int[] ayanamiHitInfo;

    public static int[] HEADDIALOG = {0,3,4,27};
    public static int[] BODYDIALOG = {1,2,24,33,37,40,41,56,60};
    public static int[] BACKGROUNDDIALOG = {5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,25,26,28,29,30,31,32,34,35,36,38,39,53,55,61};
    public static int[] CHOOSE = {43};
    public static int[] SET = {44};
    public static int[] AYANAMISETOVER = {63};
    public static int[] BATTLE = {45, 46};
    public static int[] BATTLEMIDDLE = {47,49,52,58};
    public static int[] HIT = {50,51};
    public static int[] LOSE = {62,54};
    public static int[] WIN = {48};

    public static int TREASURENUMBER = 19;

    //shipId 第一艘船3 第二艘船2
    public static Handler mHandler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewPadding(R.id.mainLayout,0,50,0,50);
            refreshWidget(CONTEXT,remoteViews);
            CANTOUCH = true;
        }
    };

    public Runnable AyanamiSetShip = new Runnable() {
        @Override
        public void run() {
            int shipId = 1;
            while(true){
                int x = getRnd(0,9);
                int y = getRnd(0,9);
                int orientation = findOrientation(CONTEXT,x,y,shipId);
                if(orientation != -1){
                    setShipPosition(CONTEXT,x,y,orientation,shipId,true);
                    //changeShipBlockColor(CONTEXT,x,y,orientation,shipId,R.color.colorHalfRed);
                    if(shipId == 1){
                        shipId = 2;
                    }else{
                        break;
                    }
                }
            }
            setAyanamiShip(CONTEXT,getRnd(1,2));
            int rnd = getDialogRnd(AYANAMISETOVER);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
            changeFace(rnd,CONTEXT);
            refreshWidget(CONTEXT,remoteViews);
            Intent serviceIntent = new Intent(CONTEXT, MyService.class);
            serviceIntent.putExtra("random",rnd);
            serviceStarter(CONTEXT, serviceIntent);
            setAIMap(CONTEXT);
            setStage(CONTEXT,4);
        }
    };

    public Runnable AyanamiSetHit = new Runnable() {
        @Override
        public void run() {
            ayanamiHitInfo = ayanamiGetHitInfo(CONTEXT,getAyanamiShip(CONTEXT));
            changeHitBlockColor(CONTEXT,ayanamiHitInfo[0],ayanamiHitInfo[1],ayanamiHitInfo[2],getAyanamiShip(CONTEXT),R.color.colorHalfRed,true, true);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setTextViewText(R.id.textLog,"看绫波的！");
            changeFace(6,CONTEXT);
            refreshWidget(CONTEXT,remoteViews);
            setStage(CONTEXT,6);
            mHandler.postDelayed(setAyanamiHitOutcome,1000);
        }
    };

    public Runnable setAyanamiHitOutcome = new Runnable() {
        @Override
        public void run() {
            boolean isHit = judgeHit(CONTEXT,ayanamiHitInfo[0],ayanamiHitInfo[1],ayanamiHitInfo[2],getAyanamiShip(CONTEXT),true);
            recoverAyanamiHit(CONTEXT,ayanamiHitInfo[0],ayanamiHitInfo[1],ayanamiHitInfo[2],getAyanamiShip(CONTEXT));
            if(judgeOutcome(CONTEXT,true)){
                setStage(CONTEXT,8);
                int rnd = getDialogRnd(WIN);
                SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info", MODE_PRIVATE).edit();
                SharedPreferences pref = CONTEXT.getSharedPreferences("info", MODE_PRIVATE);
                editor.putInt("victory",0);
                editor.apply();
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,strList.get(rnd)+"\n当前连胜："+pref.getInt("victory",0)+"\n最高连胜："+pref.getInt("maxVictory",0));
                changeFace(rnd,CONTEXT);
                Intent serviceIntent = new Intent(CONTEXT, MyService.class);
                serviceIntent.putExtra("random",rnd);
                serviceStarter(CONTEXT, serviceIntent);
                refreshWidget(CONTEXT,remoteViews);
                showAyanamiShip(CONTEXT);
            }else{
                if(isHit){
                    int rnd = getDialogRnd(HIT);
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
                    changeFace(rnd,CONTEXT);
                    Intent serviceIntent = new Intent(CONTEXT, MyService.class);
                    serviceIntent.putExtra("random",rnd);
                    serviceStarter(CONTEXT, serviceIntent);
                    refreshWidget(CONTEXT,remoteViews);
                }else{
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    int rnd = -1;
                    if(getRnd(0,2) > 1){
                        if(isBattleMiddle(CONTEXT)){
                            rnd = getDialogRnd(BATTLEMIDDLE);
                        }else{
                            rnd = getDialogRnd(BATTLE);
                        }
                    }
                    remoteViews.setTextViewText(R.id.textLog,rnd == -1? "可惜。该指挥官了。":strList.get(rnd));
                    changeFace(rnd == -1? 0:rnd,CONTEXT);
                    if(rnd != -1){
                        Intent serviceIntent = new Intent(CONTEXT, MyService.class);
                        serviceIntent.putExtra("random",rnd);
                        serviceStarter(CONTEXT, serviceIntent);
                    }
                    refreshWidget(CONTEXT,remoteViews);
                }
                setStage(CONTEXT,4);
            }
        }
    };

    public Runnable getLyric = new Runnable() {
        @Override
        public void run() {
            SharedPreferences pref = CONTEXT.getSharedPreferences("info",MODE_PRIVATE);
            SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info",MODE_PRIVATE).edit();
            int number = pref.getInt("song",-1);
            int stage = pref.getInt("stage",-1);
            Log.d("Sam","lyricRun"+number);
            if(number != -1 && stage == 9 && number < lyricList.size()){
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog, lyricList.get(number));
                refreshWidget(CONTEXT,remoteViews);
                if(number == lyricList.size()-1){
                    changeFace(13,CONTEXT);
                }
                if(number+1 < lyricIntervalList.size()){
                    editor.putInt("song",number+1);
                    editor.apply();
                    if(lyricIntervalList.get(number+1) >= 10f){
                        changeFace(0,CONTEXT);
                    }else if(lyricIntervalList.get(number) >= 10f){
                        changeFace(-1,CONTEXT);
                    }
                    mHandler.postDelayed(getLyric,(int)(lyricIntervalList.get(number+1)*1000));
                }

            }
            if(number == 0){
                changeFace(-1,CONTEXT);
            }
        }
    };

    public Runnable AyanamiMove = new Runnable() {
        @Override
        public void run() {
            ayanamiHitInfo = AIMove(CONTEXT);
            changeMoveBlockColor(CONTEXT,ayanamiHitInfo[0],ayanamiHitInfo[1],true,false);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setTextViewText(R.id.textLog,"这里。");
            changeFace(6,CONTEXT);
            refreshWidget(CONTEXT,remoteViews);
            setStage(CONTEXT,12);
            mHandler.postDelayed(setAyanamiMoveOutcome,1000);
        }
    };

    public Runnable AyanamiSearch = new Runnable() {
        @Override
        public void run() {
            ayanamiHitInfo = AIChoosePoint(CONTEXT);
            changeTreasureMapPointColor(CONTEXT,ayanamiHitInfo[0],ayanamiHitInfo[1],R.color.colorHalfRed,0);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setTextViewText(R.id.textLog,"这里。");
            changeFace(6,CONTEXT);
            refreshWidget(CONTEXT,remoteViews);
            setStage(CONTEXT,21);
            mHandler.postDelayed(setAyanamiSearchOutcome,1000);
        }
    };

    public Runnable setAyanamiSearchOutcome = new Runnable() {
        @Override
        public void run() {
            showSearchOutcome(CONTEXT,ayanamiHitInfo[0],ayanamiHitInfo[1],true);
            int outcome = judgeSearchOutcome(CONTEXT);//0无事发生 1玩家胜利 2绫波胜利
            SharedPreferences pref = CONTEXT.getSharedPreferences("info",MODE_PRIVATE);
            SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info",MODE_PRIVATE).edit();
            int[] tempNumber = getTreasureNumber(CONTEXT);
            if(outcome == 0){
                //无事发生
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,"该指挥官了。\n玩家宝藏:"+tempNumber[0]+"\n绫波宝藏:"+tempNumber[1]+"\n剩余宝藏:"+tempNumber[2]);
                changeFace(2,CONTEXT);
                refreshWidget(CONTEXT,remoteViews);
                setStage(CONTEXT,20);
            }else if(outcome == 1){
                //玩家胜利
                editor.putInt("treasureVictory",pref.getInt("treasureVictory",0)+1);
                editor.apply();
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,"心服口服。\n玩家宝藏:"+tempNumber[0]+"\n绫波宝藏:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("treasureVictory",0)+"\\"+pref.getInt("treasureLose",0));
                changeFace(13,CONTEXT);
                refreshWidget(CONTEXT,remoteViews);
                setStage(CONTEXT,23);
            }else if(outcome == 2){
                //绫波胜利
                editor.putInt("treasureLose",pref.getInt("treasureLose",0)+1);
                editor.apply();
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,"绫波赢了。\n玩家宝藏:"+tempNumber[0]+"\n绫波宝藏:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("treasureVictory",0)+"\\"+pref.getInt("treasureLose",0));
                changeFace(19,CONTEXT);
                refreshWidget(CONTEXT,remoteViews);
                setStage(CONTEXT,24);
            }
        }
    };

    public Runnable setAyanamiMoveOutcome = new Runnable() {
        @Override
        public void run() {
            changeMoveBlockColor(CONTEXT,ayanamiHitInfo[0],ayanamiHitInfo[1],true,true);
            int outcome = judgeMoveOutcome(CONTEXT);//0无事发生 1玩家无子可下 2绫波无子可下 3双方无子可下
            if(outcome == 0 || outcome == 2){
                //无事发生
                int[] tempNumber = getChessNumber(CONTEXT);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,"该指挥官了。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n局面分数:"+ayanamiHitInfo[2]);
                changeFace(2,CONTEXT);
                refreshWidget(CONTEXT,remoteViews);
                setStage(CONTEXT,11);
            }else if(outcome == 1){
                //玩家无子可下
                int[] tempNumber = getChessNumber(CONTEXT);
                setStage(CONTEXT,12);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,"还是绫波下吗。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n局面分数:"+ayanamiHitInfo[2]);
                changeFace(0,CONTEXT);
                refreshWidget(CONTEXT,remoteViews);
                mHandler.postDelayed(AyanamiMove,1500);
            }else if(outcome == 3){
                //结算
                int[] tempNumber = getChessNumber(CONTEXT);
                SharedPreferences pref = CONTEXT.getSharedPreferences("info",MODE_PRIVATE);
                SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info",MODE_PRIVATE).edit();
                if(tempNumber[0] > tempNumber[1]){
                    //玩家胜利
                    editor.putInt("chessVictory",pref.getInt("chessVictory",0)+1);
                    editor.apply();
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    remoteViews.setTextViewText(R.id.textLog,"心服口服。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("chessVictory",0)+"\\"+pref.getInt("chessLose",0));
                    changeFace(13,CONTEXT);
                    refreshWidget(CONTEXT,remoteViews);
                    setStage(CONTEXT,14);
                }else if(tempNumber[0] < tempNumber[1]){
                    //绫波胜利
                    editor.putInt("chessLose",pref.getInt("chessLose",0)+1);
                    editor.apply();
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    remoteViews.setTextViewText(R.id.textLog,"绫波赢了。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("chessVictory",0)+"\\"+pref.getInt("chessLose",0));
                    changeFace(13,CONTEXT);
                    refreshWidget(CONTEXT,remoteViews);
                    setStage(CONTEXT,15);
                }else{
                    //平局
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    remoteViews.setTextViewText(R.id.textLog,"是平局呢。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("chessVictory",0)+"\\"+pref.getInt("chessLose",0));
                    changeFace(2,CONTEXT);
                    refreshWidget(CONTEXT,remoteViews);
                    setStage(CONTEXT,16);
                }
            }
        }
    };

    public WidgetProvider(){
        initText();
        initLyric();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        CONTEXT = context;
        for(int appWidgetId : appWidgetIds){
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
            //点击大背景
            Intent btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKMAIN);
            PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.mainLayout, btPendingIntent);

            //点击身体
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKBODY);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.characterBody, btPendingIntent);
            //点击头部
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKHEAD);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.characterHead, btPendingIntent);
            //点击其他地方
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICK);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.characterPicture, btPendingIntent);
            //点击地图方块
            for(int i = 0; i <= 9; i++){
                for(int j = 0; j <= 9; j++){
                    btIntent = new Intent(context, WidgetProvider.class);
                    btIntent.setAction("com.example.MAP"+i+"_"+j);
                    btIntent.putExtra("x",i);
                    btIntent.putExtra("y",j);
                    btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(getId(context,"map"+i+"_"+j), btPendingIntent);
                }
            }
            //点击收起
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKFOLD);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.foldMap, btPendingIntent);
            //点击展开
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKUNFOLD);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.unfoldMap, btPendingIntent);
            //点击重开游戏
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKRESTART);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.restart, btPendingIntent);
            //点击选择驱逐舰
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CHOOSEDESTROYER);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.chooseDestroyer, btPendingIntent);
            //点击选择战列舰
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CHOOSEBATTLESHIP);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.chooseBattleship, btPendingIntent);
            //点击选择航母
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CHOOSEAIRCRAFTCARRIER);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.chooseAircraftCarrier, btPendingIntent);
            //点击改变方向
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CHANGEORIENTATION);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.changeOrientation, btPendingIntent);

            //点击唱歌
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKSING);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.singButton, btPendingIntent);

            //点击展开棋盘
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CLICKUNFOLDBOARD);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.unfoldChessboard, btPendingIntent);

            //点击AI先手
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(AIINITIATIVE);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.AIInitiative, btPendingIntent);
            //点击玩家先手
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(PLAYERINITIATIVE);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.playerInitiative, btPendingIntent);

            //点击保存
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(SAVE);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.save, btPendingIntent);

            //点击存档
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(LOAD);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.load, btPendingIntent);

            //点击某个存档
            for(int i = 1; i <= 9; i++){
                btIntent = new Intent(context, WidgetProvider.class);
                btIntent.setAction("com.example.SAVE0"+i);
                btIntent.putExtra("save",i);
                btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(getId(context,"save0"+i), btPendingIntent);
            }

            //点击存档确认按钮
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CONFIRMYES);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.confirmYes, btPendingIntent);

            //点击存档取消按钮
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(CONFIRMNO);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.confirmNo, btPendingIntent);

            //点击展开藏宝图按钮
            btIntent = new Intent(context, WidgetProvider.class);
            btIntent.setAction(UNFOLDTREASUREMAP);
            btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.unfoldTreasureMap, btPendingIntent);


            ComponentName watchWidget = new ComponentName(context, WidgetProvider.class);
            appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        CONTEXT = context;
        String action = intent.getAction();
        Log.d("Sam","action:"+action);
        int rnd = -1;
        int stage = getStage(context);
        int game = getGame(context);
        if(action.equals(CLICKBODY) && stage == -1){
            rnd = getDialogRnd(BODYDIALOG);
            Intent serviceIntent = new Intent(context, MyService.class);
            serviceIntent.putExtra("voice",1);
            serviceIntent.putExtra("random",rnd);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
            remoteViews.setViewPadding(R.id.mainLayout,0,20,0,80);
            remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
            refreshWidget(context,remoteViews);
            serviceStarter(context, serviceIntent);
            changeFace(rnd,context);
            mHandler.postDelayed(runnable,100);
        }else if(action.equals(CLICKHEAD) && stage == -1){
            rnd = getDialogRnd(HEADDIALOG);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewPadding(R.id.mainLayout,0,20,0,80);
            remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
            refreshWidget(context,remoteViews);
            changeFace(rnd,context);
            Intent serviceIntent = new Intent(context, MyService.class);
            serviceIntent.putExtra("voice",2);
            serviceIntent.putExtra("random",rnd);
            serviceStarter(context, serviceIntent);
            mHandler.postDelayed(runnable,100);
        }else if(action.equals(CLICK) && stage == -1){
            rnd = getDialogRnd(BACKGROUNDDIALOG);
            Intent serviceIntent = new Intent(context, MyService.class);
            serviceIntent.putExtra("voice",3);
            serviceIntent.putExtra("random",rnd);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
            refreshWidget(context,remoteViews);
            changeFace(rnd,context);
            serviceStarter(context, serviceIntent);
        }else if(judgeText(action)){//点击某个格子
            int x = intent.getIntExtra("x",-1);
            int y = intent.getIntExtra("y",-1);
            Log.d("Sam","clicked"+x+" "+y);
            if(game == 1){
                if(stage == 1 || stage == 2){
                    int tempX = getTouchPoint(context,SHIPX);
                    int tempY = getTouchPoint(context,SHIPY);
                    int orientation = getTouchPoint(context,SHIPORIENTATION);
                    if(tempX == x && tempY == y){
                        changeShipBlockColor(context,tempX,tempY,orientation,stage,R.color.colorGreen,false);
                        clearTouchPoint(context);
                        setShipPosition(context,tempX,tempY,orientation,stage, false);
                        setStage(context,(stage+1));
                        addOceanNumberRecord(context,stage,calculateOceanNumber(context,tempX,tempY,orientation,stage));
                        if(stage == 2){//该Ayanami放船了
                            mHandler.postDelayed(AyanamiSetShip,1500);
                            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,"该綾波摆放了吗...");
                            changeFace(0,context);
                            refreshWidget(context,remoteViews);
                        }
                    }else{
                        Log.d("Sam","tempX:"+tempX+" tempY"+tempY);
                        if(tempX != -1 && tempY != -1){
                            changeShipBlockColor(context,tempX,tempY,orientation,stage,R.color.colorOcean, false);
                        }
                        for(int i = 0; i < 4; i++){
                            if(canShipPlace(context,x,y,i,stage, false)){
                                changeShipBlockColor(context,x,y,i,stage,R.color.colorHalfGreen, true);
                                setTouchPoint(context,x,y,i);
                                break;
                            }
                            clearTouchPoint(context);
                        }
                    }
                }else if(stage == 4){
                    int tempX = getTouchPoint(context,SHIPX);
                    int tempY = getTouchPoint(context,SHIPY);
                    int orientation = getTouchPoint(context,SHIPORIENTATION);
                    if(orientation == -1) orientation = 0;
                    int ship = getShip(context);
                    if(tempX == x && tempY == y){
                        changeHitBlockColor(context,tempX,tempY,orientation,ship,R.color.colorDarkBlue,true, false);
                        judgeHit(context,tempX,tempY,orientation,ship,false);
                        clearTouchPoint(context);
                        if(judgeOutcome(context,false)){
                            setStage(context,7);
                            rnd = getDialogRnd(LOSE);
                            SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info", MODE_PRIVATE).edit();
                            SharedPreferences pref = CONTEXT.getSharedPreferences("info", MODE_PRIVATE);
                            editor.putInt("victory",pref.getInt("victory",0)+1);
                            editor.putInt("maxVictory",Math.max(pref.getInt("maxVictory",0),pref.getInt("victory",0)+1));
                            editor.apply();
                            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,strList.get(rnd)+"\n当前连胜："+pref.getInt("victory",0)+"\n最高连胜："+pref.getInt("maxVictory",0));
                            changeFace(rnd,CONTEXT);
                            Intent serviceIntent = new Intent(CONTEXT, MyService.class);
                            serviceIntent.putExtra("random",rnd);
                            serviceStarter(CONTEXT, serviceIntent);
                            refreshWidget(CONTEXT,remoteViews);
                        }else{
                            setStage(context,5);
                            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,"轮到绫波了吗。");
                            changeFace(0,CONTEXT);
                            refreshWidget(CONTEXT,remoteViews);
                            mHandler.postDelayed(AyanamiSetHit,1500);
                        }
                    }else{
                        Log.d("Sam","tempX:"+tempX+" tempY"+tempY);
                        if(tempX != -1 && tempY != -1){
                            recoverHitBlockColor(context,tempX,tempY,orientation,ship);
                        }
                        changeHitBlockColor(context,x,y,orientation,ship,R.color.colorHalfRed,false, false);
                        setTouchPoint(context,x,y,orientation);
                    }
                }
            }else if(game == 2){
                if(stage == 11){
                    int tempX = getTouchPoint(context,SHIPX);
                    int tempY = getTouchPoint(context,SHIPY);
                    if(tempX == x && tempY == y){
                        changeMoveBlockColor(context,x,y,false,true);
                        clearTouchPoint(context);
                        int outcome = judgeMoveOutcome(context);//0无事发生 1玩家无子可下 2绫波无子可下 3双方无子可下

                        if(outcome == 0 || outcome==1){
                            //无事发生
                            setStage(context,12);
                            int[] tempNumber = getChessNumber(context);
                            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,"轮到绫波了吗。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]);
                            changeFace(0,CONTEXT);
                            refreshWidget(CONTEXT,remoteViews);
                            mHandler.postDelayed(AyanamiMove,1500);
                        }else if(outcome == 2){
                            //绫波无子可下
                            int[] tempNumber = getChessNumber(context);
                            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,"唔，绫波没地方下了...\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]);
                            changeFace(2,CONTEXT);
                            refreshWidget(CONTEXT,remoteViews);
                            setStage(CONTEXT,11);
                        }else if(outcome == 3){
                            //结算
                            int[] tempNumber = getChessNumber(CONTEXT);
                            SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info",MODE_PRIVATE).edit();
                            SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
                            if(tempNumber[0] > tempNumber[1]){
                                //玩家胜利
                                editor.putInt("chessVictory",pref.getInt("chessVictory",0)+1);
                                editor.apply();
                                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                                remoteViews.setTextViewText(R.id.textLog,"心服口服。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("chessVictory",0)+"\\"+pref.getInt("chessLose",0));
                                changeFace(13,CONTEXT);
                                refreshWidget(CONTEXT,remoteViews);
                                setStage(CONTEXT,14);
                            }else if(tempNumber[0] < tempNumber[1]){
                                //绫波胜利
                                editor.putInt("chessLose",pref.getInt("chessLose",0)+1);
                                editor.apply();
                                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                                remoteViews.setTextViewText(R.id.textLog,"绫波赢了。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("chessVictory",0)+"\\"+pref.getInt("chessLose",0));
                                changeFace(13,CONTEXT);
                                refreshWidget(CONTEXT,remoteViews);
                                setStage(CONTEXT,15);
                            }else{
                                //平局
                                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                                remoteViews.setTextViewText(R.id.textLog,"是平局呢。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("chessVictory",0)+"\\"+pref.getInt("chessLose",0));
                                changeFace(2,CONTEXT);
                                refreshWidget(CONTEXT,remoteViews);
                                setStage(CONTEXT,16);
                            }
                        }
                    }else if(canMove(context,x,y,false)){
                        Log.d("Sam","tempX:"+tempX+" tempY"+tempY);
                        if(tempX != -1 && tempY != -1){
                            recoverMoveBlockColor(context,tempX,tempY,false);
                        }
                        changeMoveBlockColor(context,x,y,false,false);
                        setTouchPoint(context,x,y,-1);
                    }else{
                        if(tempX != -1 && tempY != -1){
                            recoverMoveBlockColor(context,tempX,tempY,false);
                        }
                        clearTouchPoint(context);
                    }
                }
            }else if(game == 3){
                if(stage == 20){//说明是玩家寻找
                    int tempX = getTouchPoint(context,SHIPX);
                    int tempY = getTouchPoint(context,SHIPY);
                    if(tempX == x && tempY == y){
                        showSearchOutcome(context,x,y,false);
                        clearTouchPoint(context);
                        int outcome = judgeSearchOutcome(context);//0无事发生 1玩家胜利 2绫波胜利
                        SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info",MODE_PRIVATE).edit();
                        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
                        if(outcome == 0){
                            //无事发生
                            setStage(context,21);
                            int[] tempNumber = getTreasureNumber(context);
                            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,"轮到绫波了吗。\n玩家宝藏:"+tempNumber[0]+"\n绫波宝藏:"+tempNumber[1]+"\n剩余宝藏:"+tempNumber[2]);
                            changeFace(0,CONTEXT);
                            refreshWidget(CONTEXT,remoteViews);
                            mHandler.postDelayed(AyanamiSearch,1500);
                        }else if(outcome == 1){
                            //玩家胜利
                            editor.putInt("treasureVictory",pref.getInt("treasureVictory",0)+1);
                            editor.apply();
                            int[] tempNumber = getTreasureNumber(context);
                            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,"心服口服。\n玩家宝藏:"+tempNumber[0]+"\n绫波宝藏:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("treasureVictory",0)+"\\"+pref.getInt("treasureLose",0));
                            changeFace(13,CONTEXT);
                            refreshWidget(CONTEXT,remoteViews);
                            setStage(CONTEXT,23);
                        }else if(outcome == 2){
                            //绫波胜利
                            editor.putInt("treasureLose",pref.getInt("treasureLose",0)+1);
                            editor.apply();
                            int[] tempNumber = getTreasureNumber(context);
                            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                            remoteViews.setTextViewText(R.id.textLog,"绫波赢了。\n玩家宝藏:"+tempNumber[0]+"\n绫波宝藏:"+tempNumber[1]+"\n胜\\负: "+pref.getInt("treasureVictory",0)+"\\"+pref.getInt("treasureLose",0));
                            changeFace(13,CONTEXT);
                            refreshWidget(CONTEXT,remoteViews);
                            setStage(CONTEXT,24);
                        }

                    }else if(getTreasureMapPoint(context,x,y) <= 1){//说明可以被点击
                        Log.d("Sam","tempX:"+tempX+" tempY"+tempY);
                        if(tempX != -1 && tempY != -1){
                            changeTreasureMapPointColor(context,tempX,tempY,R.color.colorOcean,0);
                        }
                        changeTreasureMapPointColor(context,x,y,R.color.colorHalfGreen,0);
                        setTouchPoint(context,x,y,-1);
                    }else{
                        if(tempX != -1 && tempY != -1){
                            changeTreasureMapPointColor(context,tempX,tempY,R.color.colorOcean,0);
                        }
                        clearTouchPoint(context);
                    }
                }

            }

        }else if(action.equals(CLICKFOLD)){
            if(stage != 3 && stage != 5 && stage != 6 && stage != 12 && stage != 13 && stage != 21 && stage != 22){
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.map, View.GONE);
                remoteViews.setViewVisibility(R.id.unfoldMap, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.singButton, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.chooseShip,View.GONE);
                remoteViews.setViewVisibility(R.id.chooseInitiative,View.GONE);
                remoteViews.setViewVisibility(R.id.storageLayout,View.GONE);
                remoteViews.setViewVisibility(R.id.confirmLayout,View.GONE);
                remoteViews.setViewVisibility(R.id.unfoldChessboard,View.VISIBLE);
                remoteViews.setViewVisibility(R.id.unfoldTreasureMap,View.VISIBLE);
                remoteViews.setTextViewText(R.id.textLog,"不玩了吗。好。");
                changeFace(0,CONTEXT);
                refreshWidget(context,remoteViews);
                setStage(context,-1);
                clearMap(context);
                if(game == 1){
                    setMap(context);
                }else if(game == 3){
                    initTreasureMap(context);
                }
                clearInfo(context);
            }
        }else if(action.equals(CLICKUNFOLD) && stage != 9 && stage != 10){
            SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewVisibility(R.id.map, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.save, View.GONE);
            remoteViews.setViewVisibility(R.id.load, View.GONE);
            remoteViews.setViewVisibility(R.id.changeOrientation, View.VISIBLE);
            remoteViews.setTextViewText(R.id.foldMap,"收起地图");
            remoteViews.setViewVisibility(R.id.unfoldMap, View.GONE);
            remoteViews.setViewVisibility(R.id.singButton, View.GONE);
            remoteViews.setViewVisibility(R.id.unfoldChessboard,View.GONE);
            remoteViews.setViewVisibility(R.id.unfoldTreasureMap,View.GONE);
            remoteViews.setTextViewText(R.id.textLog,"要和绫波来一句游戏吗。\n当前连胜："+pref.getInt("victory",0)+"\n最高连胜："+pref.getInt("maxVictory",0));
            changeFace(2,CONTEXT);
            refreshWidget(context,remoteViews);
            clearMap(context);
            setMap(context);
            clearInfo(context);
            setGame(context,1);
        }else if(action.equals(CLICKRESTART)){
            if(game == 1){
                if(stage != 3 && stage != 5 && stage != 6){
                    clearMap(context);
                    setMap(context);
                    clearInfo(context);
                    setStage(context,0);
                    rnd = getDialogRnd(CHOOSE);
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    remoteViews.setViewVisibility(R.id.chooseShip, View.VISIBLE);
                    remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
                    changeFace(rnd,CONTEXT);
                    Intent serviceIntent = new Intent(CONTEXT, MyService.class);
                    serviceIntent.putExtra("random",rnd);
                    serviceStarter(CONTEXT, serviceIntent);
                    refreshWidget(context,remoteViews);
                }
            }else if(game == 2){
                if(stage != 3 && stage != 5 && stage != 6 && stage != 12 && stage != 13){
                    clearMap(context);
                    clearInfo(context);
                    setStage(context,0);
                    rnd = getDialogRnd(CHOOSE);
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    remoteViews.setViewVisibility(R.id.chooseInitiative, View.VISIBLE);
                    remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
                    changeFace(rnd,CONTEXT);
                    Intent serviceIntent = new Intent(CONTEXT, MyService.class);
                    serviceIntent.putExtra("random",rnd);
                    serviceStarter(CONTEXT, serviceIntent);
                    refreshWidget(context,remoteViews);
                }
            }else if(game == 3){
                if(stage != 3 && stage != 5 && stage != 6 && stage != 12 && stage != 13){
                    clearMap(context);
                    clearInfo(context);
                    setStage(context,0);
                    rnd = getDialogRnd(CHOOSE);
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    remoteViews.setViewVisibility(R.id.chooseInitiative, View.VISIBLE);
                    remoteViews.setTextViewText(R.id.textLog,strList.get(rnd));
                    changeFace(rnd,CONTEXT);
                    Intent serviceIntent = new Intent(CONTEXT, MyService.class);
                    serviceIntent.putExtra("random",rnd);
                    serviceStarter(CONTEXT, serviceIntent);
                    refreshWidget(context,remoteViews);
                }
            }
        }else if(action.equals(CHOOSEAIRCRAFTCARRIER)){
            setShip(context,2);
            setStage(context,1);
            setMap(getRndIslandList(25),context);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewVisibility(R.id.chooseShip, View.GONE);
            remoteViews.setTextViewText(R.id.textLog,"指挥官先摆放吧。");
            changeFace(2,CONTEXT);
            refreshWidget(context,remoteViews);
        }else if(action.equals(CHOOSEBATTLESHIP)){
            setShip(context,1);
            setStage(context,1);
            setMap(getRndIslandList(25),context);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewVisibility(R.id.chooseShip, View.GONE);
            remoteViews.setTextViewText(R.id.textLog,"指挥官先摆放吧。");
            changeFace(2,CONTEXT);
            refreshWidget(context,remoteViews);
        }else if(action.equals(CHOOSEDESTROYER)){
            setShip(context,0);
            setStage(context,1);
            setMap(getRndIslandList(25),context);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewVisibility(R.id.chooseShip, View.GONE);
            remoteViews.setTextViewText(R.id.textLog,"指挥官先摆放吧。");
            changeFace(2,CONTEXT);
            refreshWidget(context,remoteViews);
        }else if(action.equals(CHANGEORIENTATION)){
            if(game == 1){
                if(stage == 4){
                    int tempX = getTouchPoint(context,SHIPX);
                    int tempY = getTouchPoint(context,SHIPY);
                    int orientation = getTouchPoint(context,SHIPORIENTATION);
                    int ship = getShip(context);
                    if(tempX != -1 && tempY != -1){
                        recoverHitBlockColor(context,tempX,tempY,orientation,ship);
                        if(orientation == 3){
                            orientation = 0;
                        }else{
                            orientation++;
                        }
                        changeHitBlockColor(context,tempX,tempY,orientation,ship,R.color.colorHalfRed,false, false);
                        setTouchPoint(context,tempX,tempY,orientation);
                    }
                }else if(stage == 1 || stage == 2){
                    int tempX = getTouchPoint(context,SHIPX);
                    int tempY = getTouchPoint(context,SHIPY);
                    int orientation = getTouchPoint(context,SHIPORIENTATION);
                    if(tempX != -1 && tempY != -1){
                        changeShipBlockColor(context,tempX,tempY,orientation,stage,R.color.colorOcean, false);
                        if(orientation == 3) orientation = -1;
                        for(int i = orientation+1; i < 4; i++){
                            if(canShipPlace(context,tempX,tempY,i,stage,false)){
                                changeShipBlockColor(context,tempX,tempY,i,stage,R.color.colorHalfGreen, true);
                                setTouchPoint(context,tempX,tempY,i);
                                break;
                            }
                            if(i >= 3) i = -1;
                        }
                    }
                }
            }
        }else if(action.equals(CLICKSING)){
            if(stage == -1){
                setStage(context,10);
                Intent serviceIntent = new Intent(context, MyService.class);
                serviceIntent.putExtra("voice",4);
                serviceStarter(context, serviceIntent);
                changeFace(0,context);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog, "想听歌了吗...好吧。");
                remoteViews.setTextViewText(R.id.singButton, "止める");
                refreshWidget(CONTEXT,remoteViews);
                SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info",MODE_PRIVATE).edit();
                editor.putInt("song",0);
                editor.apply();
                mHandler.postDelayed(getLyric,(int)(lyricIntervalList.get(0)*1000));
            }else if(stage == 9 || stage == 10){
                SharedPreferences.Editor editor = CONTEXT.getSharedPreferences("info",MODE_PRIVATE).edit();
                editor.putInt("song",-1);
                editor.apply();
                setStage(context,-1);
                changeFace(2,context);
                mHandler.removeCallbacksAndMessages(null);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog, "不听了吗。");
                remoteViews.setTextViewText(R.id.singButton, "歌う");
                refreshWidget(CONTEXT,remoteViews);
                Intent serviceIntent = new Intent(context, MyService.class);
                serviceIntent.putExtra("voice",5);
                serviceStarter(context, serviceIntent);
            }
        }else if(action.equals(CLICKUNFOLDBOARD) && stage != 9 && stage != 10){
            SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewVisibility(R.id.map, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.save, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.load, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.changeOrientation, View.GONE);
            remoteViews.setTextViewText(R.id.foldMap,"收起棋盘");
            remoteViews.setTextViewText(R.id.save,"存档");
            remoteViews.setTextViewText(R.id.load,"读档");
            remoteViews.setViewVisibility(R.id.unfoldMap, View.GONE);
            remoteViews.setViewVisibility(R.id.singButton, View.GONE);
            remoteViews.setViewVisibility(R.id.unfoldChessboard,View.GONE);
            remoteViews.setViewVisibility(R.id.unfoldTreasureMap,View.GONE);
            remoteViews.setTextViewText(R.id.textLog,"要和绫波下棋吗。"+"\n胜\\负: "+pref.getInt("chessVictory",0)+"\\"+pref.getInt("chessLose",0));
            changeFace(2,CONTEXT);
            refreshWidget(context,remoteViews);
            clearMap(context);
            setMap(context);
            clearInfo(context);
            setGame(context,2);
        }else if(action.equals(AIINITIATIVE)){
            if(game == 2){
                setStage(context,12);
                initChess(context);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.chooseInitiative, View.GONE);
                remoteViews.setTextViewText(R.id.textLog,"下哪里好呢...");
                changeFace(0,CONTEXT);
                refreshWidget(context,remoteViews);
                mHandler.postDelayed(AyanamiMove,1500);
            }else if(game == 3){
                setStage(context,21);
                initTreasureMap(context);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.chooseInitiative, View.GONE);
                remoteViews.setTextViewText(R.id.textLog,"找哪里好呢...");
                changeFace(0,CONTEXT);
                refreshWidget(context,remoteViews);
                mHandler.postDelayed(AyanamiSearch,1500);
            }
        }else if(action.equals(PLAYERINITIATIVE)){
            if(game == 2){
                setStage(context,11);
                initChess(context);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.chooseInitiative, View.GONE);
                remoteViews.setTextViewText(R.id.textLog,"指挥官先下吧。");
                changeFace(13,context);
                refreshWidget(context,remoteViews);
            }else if(game == 3){
                setStage(context,20);
                initTreasureMap(context);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.chooseInitiative, View.GONE);
                remoteViews.setTextViewText(R.id.textLog,"指挥官先找吧。");
                changeFace(13,context);
                refreshWidget(context,remoteViews);
            }

        }
        else if(action.equals(SAVE)){
            if(stage == 11){
                setStage(context,17);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,"要保存吗？");
                remoteViews.setTextViewText(R.id.save,"返回");
                refreshSaveData(context);
                remoteViews.setViewVisibility(R.id.storageLayout, View.VISIBLE);
                changeFace(6,context);
                refreshWidget(context,remoteViews);
            }else if(stage == 17){
                setStage(context,11);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                int[] tempNumber = getChessNumber(context);
                remoteViews.setTextViewText(R.id.save,"存档");
                remoteViews.setTextViewText(R.id.textLog,"该指挥官了。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]);
                changeFace(2,context);
                remoteViews.setViewVisibility(R.id.storageLayout, View.GONE);
                refreshWidget(context,remoteViews);
            }
        }else if(action.equals(LOAD)){
            if(stage == 11){
                setStage(context,18);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setTextViewText(R.id.textLog,"要读取吗？");
                remoteViews.setTextViewText(R.id.load,"返回");
                refreshSaveData(context);
                remoteViews.setViewVisibility(R.id.storageLayout, View.VISIBLE);
                changeFace(6,context);
                refreshWidget(context,remoteViews);
            }else if(stage == 18){
                setStage(context,11);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                int[] tempNumber = getChessNumber(context);
                remoteViews.setTextViewText(R.id.load,"读档");
                remoteViews.setTextViewText(R.id.textLog,"该指挥官了。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]);
                changeFace(2,context);
                remoteViews.setViewVisibility(R.id.storageLayout, View.GONE);
                refreshWidget(context,remoteViews);
            }
        }else if(action.equals(CONFIRMYES)){
            if(stage >= 101 && stage <= 109){
                //保存
                setStage(context,11);
                saveChessBoard(context,stage-100);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.confirmLayout, View.GONE);
                remoteViews.setViewVisibility(R.id.storageLayout,View.GONE);
                remoteViews.setTextViewText(R.id.save,"存档");
                int[] tempNumber = getChessNumber(context);
                remoteViews.setTextViewText(R.id.textLog,"已保存。该指挥官了。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]);
                changeFace(2,context);
                refreshWidget(context,remoteViews);
            }else if(stage >= 111 && stage <= 119){
                //读取
                setStage(context,11);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.confirmLayout, View.GONE);
                remoteViews.setViewVisibility(R.id.storageLayout,View.GONE);
                remoteViews.setTextViewText(R.id.load,"读档");
                refreshWidget(context,remoteViews);
                loadChessBoard(context,stage-110);
            }
        }else if(action.equals(CONFIRMNO)){
            if(stage >= 101 && stage <= 109){
                //保存
                setStage(context,17);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.confirmLayout, View.GONE);
                refreshWidget(context,remoteViews);
            }else if(stage >= 111 && stage <= 119){
                //读取
                setStage(context,18);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.confirmLayout, View.GONE);
                refreshWidget(context,remoteViews);
            }
        }else if(judgeSave(action)){//点击某个存档
            int id = intent.getIntExtra("save",-1);
            if(stage == 17){
                //打开的是存档界面
                setStage(context,100+id);
                RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                remoteViews.setViewVisibility(R.id.confirmLayout, View.VISIBLE);
                remoteViews.setTextViewText(R.id.confirmText,"确定要覆盖存档"+id+"吗？");
                remoteViews.setInt(R.id.confirmImage,"setBackgroundResource", R.drawable.ayanami_load);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH)+1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);//注意HOUR是12小时制，HOUR_OF_DAY是24小时制
                int minute = calendar.get(Calendar.MINUTE);
                int[] tempNumber = getChessNumber(context);
                remoteViews.setTextViewText(R.id.saveDate,"日期："+year+"."+month+"."+day);
                remoteViews.setTextViewText(R.id.saveTime,"时间："+hour+":"+minute);
                remoteViews.setTextViewText(R.id.savePortion,"黑 "+tempNumber[0]+"    白 "+tempNumber[1]);
                refreshWidget(context,remoteViews);
            }else if(stage == 18){
                //打开的是读档界面
                SharedPreferences pref = context.getSharedPreferences("save0"+id,MODE_PRIVATE);
                if(pref.getInt("blackNumber",-1) != -1){
                    RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
                    setStage(context,110+id);
                    remoteViews.setViewVisibility(R.id.confirmLayout, View.VISIBLE);
                    remoteViews.setTextViewText(R.id.confirmText,"确定要读取存档"+id+"吗？");
                    remoteViews.setInt(R.id.confirmImage,"setBackgroundResource", R.drawable.ayanami_save);
                    remoteViews.setTextViewText(R.id.saveDate,"日期："+pref.getInt("year",-1)+"."+pref.getInt("month",-1)+"."+pref.getInt("day",-1));
                    remoteViews.setTextViewText(R.id.saveTime,"时间："+pref.getInt("hour",-1)+":"+pref.getInt("minute",-1));
                    remoteViews.setTextViewText(R.id.savePortion,"黑 "+pref.getInt("blackNumber",-1)+"    白 "+pref.getInt("whiteNumber",-1));
                    refreshWidget(context,remoteViews);
                }
            }
        }else if(action.equals(UNFOLDTREASUREMAP)){//展开藏宝图
            SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setViewVisibility(R.id.map, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.save, View.GONE);
            remoteViews.setViewVisibility(R.id.load, View.GONE);
            remoteViews.setViewVisibility(R.id.changeOrientation, View.GONE);
            remoteViews.setTextViewText(R.id.foldMap,"收起地图");
            remoteViews.setViewVisibility(R.id.unfoldMap, View.GONE);
            remoteViews.setViewVisibility(R.id.singButton, View.GONE);
            remoteViews.setViewVisibility(R.id.unfoldChessboard,View.GONE);
            remoteViews.setViewVisibility(R.id.unfoldTreasureMap,View.GONE);
            remoteViews.setTextViewText(R.id.textLog,"要和绫波来比比寻宝吗。\n胜\\负: "+pref.getInt("treasureVictory",0)+"\\"+pref.getInt("treasureLose",0));
            changeFace(2,CONTEXT);
            refreshWidget(context,remoteViews);
            clearMap(context);
            setMap(context);
            clearInfo(context);
            setGame(context,3);
        }

    }

    private static void refreshWidget(Context context, RemoteViews remoteViews){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName watchWidget = new ComponentName(context, WidgetProvider.class);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    public void initText(){
        if(strList.size() == 0){
            strList.add("唔——有点痒……");
            strList.add("指挥官，请不要……乱碰，感觉……有点奇怪。");
            strList.add("还是感觉…有点奇怪，不过…你喜欢就好。");
            strList.add("这是耳朵，不是角的说…");
            strList.add("虽然不太明白这样有什么意义……但是有点高兴。（蹭蹭）");
            strList.add("吹雪级驱逐舰的改良舰绫波……的说，对我来说，战斗没什么好害怕的，所以虽然只是驱逐舰，但是无论什么敌人我都敢一战……");
            strList.add("绫波……的说，他们喜欢叫我“鬼神”就是了。请多指教。");
            strList.add("指挥官，吃了吗？我这里还有点心。");
            strList.add("绫波的耳朵……比较可爱吗？不太懂……指挥官开心就好。");
            strList.add("最近时常会做和大家走散的噩梦，一定是我做的还不够好……");
            strList.add("指挥官？…不，没事。我只是确认一下你是不是还在。");
            strList.add("指挥官……你一直说绫波的耳朵可爱，但是为什么不摸摸看呢？");
            strList.add("我不讨厌战斗，也不喜欢，只是普通地战斗而已……现在的话，为了指挥官……感觉，会变得更强。");
            strList.add("放心吧，这次我无论如何都会活下来，然后回到指挥官面前……因为……我想见指挥官……");
            strList.add("拉菲…房间里的氧气可乐又不够了…");
            strList.add("绫波，改造完毕…的说。感觉变得更强了，不过…指挥官，能摸摸我的头吗？稍微有点…想要撒娇… ");
            strList.add("和大家在一起很开心，所以我…想和大家在一起。");
            strList.add("指挥官不理我的话，会有点…伤心。");
            strList.add("指挥官，一起听歌吗？兵器以外的我，喜欢吗？");
            strList.add("戴上耳机后，感觉全世界就只剩下音乐，还有……眼前的人。");
            strList.add("指挥官，标枪约我等会儿一起去玩，我可以去吗？ ");
            strList.add("梓说她最近在玩“手机游戏”……指挥官，“手机游戏”是什么？ ");
            strList.add("标枪无论做什么的时候都好有精神，有点羡慕…的说。");
            strList.add("如果说有什么担心的事情……大概就是不在指挥官身边吧……");
            strList.add("这是……糖果？谢谢，我会尝尝看的…… ");
            strList.add("除去战斗与训练，世界上好像还存在其他形式的乐趣…… ");
            strList.add("和指挥官在一起，感觉就能有更多干劲—— ");
            strList.add("指挥官的手，很温暖… ");
            strList.add("指挥官，工作之前，要先听一会音乐吗？ ");//v29
            strList.add("指挥官喜欢什么样的歌呢？我的话……唔，摇滚？ ");
            strList.add("现在的话，能感觉到自己不是孤单一人…因为指挥官一直在这里。");
            strList.add("这样和指挥官靠在一起的感觉很好，我很喜欢。");//32
            strList.add("指挥官…需要揉一下肩膀？ ");
            strList.add("指挥官，那里，不可以掀起来……的说。");//34
            strList.add("拉菲说，有时候也需要换换口味，所以…今天要来点甜点吗，指挥官？");
            strList.add("舞蹈和唱歌我都会，露出笑容，好难…但是，标枪说，这也是我的“萌点”，是真的吗，指挥官？");
            strList.add("雪花很漂亮，但是，一落在手上，就消失不见了。");//37
            strList.add("不要把衣服弄乱了，不然绫波也不知道该怎么弄好…");
            strList.add("烦恼的时候，可以找祥凤和Z23聊天，也经常得到吹雪的关心，绫波很高兴的说。");
            strList.add("指挥官要吃苹果吗？很好吃的说。");
            strList.add("好...好难为情...");//41
            strList.add("别...别这样。");
            strList.add("要放弃了吗？");
            strList.add("要选择哪一个呢？");
            strList.add("看起来能打一场好仗。");//45
            strList.add("就这样决定了。");
            strList.add("啊...是这里吗？");
            strList.add("该怎么办才好呢...");
            strList.add("是绫波的胜利。");
            strList.add("好像还没完的样子。");//50
            strList.add("就是这种感觉。");
            strList.add("让它结束吧！");
            strList.add("那个...还没好吗。");
            strList.add("发现新的动画投稿……指挥官，等下一起看直播好吗？");
            strList.add("没关系……我自己很快就会好起来的。");
            strList.add("请把空调开强一些…这样下去的话绫波会蒸发掉的…");
            strList.add("这个饮料，是给绫波的吗？谢谢指挥官。");
            strList.add("绫波，胜利的V…的说。");
            strList.add("绫波还能继续...");
            strList.add("指挥官要做什么...有点在意。");
            strList.add("如果再不收手的话...要生气了。");
            strList.add("这样的时间...也不错。");
            strList.add("漂亮的指挥。这次输的心服口服。");
            strList.add("绫波的舰队已经进入位置。包围完毕，觉悟吧。");
        }
    }

    public void initLyric(){
        if(lyricList.size() == 0){
            lyricList.add("終わりない海路（たびじ）を映す\n映照着无尽航路的");
            lyricIntervalList.add(20.00f);
            lyricList.add("蒼穹の光が\n苍穹之光");
            lyricIntervalList.add(3.14f);
            lyricList.add("濁った波間を射抜いて\n穿透了浑浊的海浪");
            lyricIntervalList.add(2.43f);
            lyricList.add("私を導く\n引导着我");
            lyricIntervalList.add(3.58f);
            lyricList.add("いつからか枝分かれした\n不知何时开始出现了分歧");
            lyricIntervalList.add(2.69f);
            lyricList.add("憧憬の先は\n原本憧憬的前方");
            lyricIntervalList.add(3.08f);
            lyricList.add("涙のコントラストさえ\n就连泪水的对照");
            lyricIntervalList.add(2.88f);
            lyricList.add("必要だと言うの\n都变得必要");
            lyricIntervalList.add(3.20f);
            lyricList.add("あなたの欲しい\n你想要的世界");
            lyricIntervalList.add(2.49f);
            lyricList.add("世界に色はない\n没有色彩");
            lyricIntervalList.add(2.64f);
            lyricList.add("烟る戦火揺らめいた\n战争中升起的浓烟");
            lyricIntervalList.add(3.20f);
            lyricList.add("願う未来のため\n应是为了心中期盼的未来");
            lyricIntervalList.add(3.65f);
            lyricList.add("漕ぎ出せ\n出发吧");
            lyricIntervalList.add(2.24f);
            lyricList.add("遠い彼方一つだった私たち\n向着遥远的彼方");
            lyricIntervalList.add(1.73f);
            lyricList.add("希望と愛抱いてる\n曾是一心的我们怀抱着爱与希望");
            lyricIntervalList.add(4.42f);
            lyricList.add("心臓（こころ）を持てた器（ひと）よ\n抱有心灵的人们哟");
            lyricIntervalList.add(3.26f);
            lyricList.add("違えた海路（たびじ）でも\n就算是在错误的航线上");
            lyricIntervalList.add(3.14f);
            lyricList.add("正義を追い求めて\n也要追求正义");
            lyricIntervalList.add(2.69f);
            lyricList.add("歩むこと止めはしない\n绝不会停下脚步");
            lyricIntervalList.add(3.07f);
            lyricList.add("悠久のカタルシス\n永恒的星光");
            lyricIntervalList.add(2.70f);
            lyricList.add("\n");
            lyricIntervalList.add(3.90f);
            lyricList.add("泡沫の夢を想えば\n回想起如同泡影的梦想");
            lyricIntervalList.add(13.50f);
            lyricList.add("ひと匙の救い\n那渺茫的救赎");
            lyricIntervalList.add(2.94f);
            lyricList.add("想いを束ねた犠牲は\n为了梦想所做出的牺牲");
            lyricIntervalList.add(2.64f);
            lyricList.add("報われるだろうか\n究竟会不会得到回报");
            lyricIntervalList.add(3.13f);
            lyricList.add("流れ着く終着点を\n前进的终点");
            lyricIntervalList.add(2.56f);
            lyricList.add("委ねはしないさ\n不会交予任何人");
            lyricIntervalList.add(3.39f);
            lyricList.add("私のこの手で選ぼう\n我要用自己的双手去选择");
            lyricIntervalList.add(2.56f);
            lyricList.add("希望に棹差して\n顺着希望的潮流");
            lyricIntervalList.add(3.71f);
            lyricList.add("勇気の意味が傷付けることなら\n如果说勇气就意味着伤害");
            lyricIntervalList.add(2.56f);
            lyricList.add("迷い棄てて閃光のように\n我将丢掉迷茫如同光芒一样");
            lyricIntervalList.add(5.89f);
            lyricList.add("願う未来背負っていけ\n为了期盼的未来背负一切");
            lyricIntervalList.add(3.58f);
            lyricList.add("響かせ\n回响吧");
            lyricIntervalList.add(5.44f);
            lyricList.add("遥か向こう現心のあなたに\n传达到远方的你的身边");
            lyricIntervalList.add(1.22f);
            lyricList.add("もう二度と交わらない\n已经不会再见面");
            lyricIntervalList.add(4.42f);
            lyricList.add("レ一ルを辿るとしても\n即使到达了终点");
            lyricIntervalList.add(3.01f);
            lyricList.add("溢れてとめどなく\n也无法停止溢出的情感");
            lyricIntervalList.add(3.20f);
            lyricList.add("理想に瞬くのは\n我明白了");
            lyricIntervalList.add(2.82f);
            lyricList.add("絶望の光ではないのだと\n在理想中点灭的");
            lyricIntervalList.add(3.07f);
            lyricList.add("知っている\n并不是绝望的光");
            lyricIntervalList.add(4.42f);
            lyricList.add("\n");
            lyricIntervalList.add(2.74f);
            lyricList.add("見つめてる\n凝望着");
            lyricIntervalList.add(23.07f);
            lyricList.add("大空の果て\n天空的尽头");
            lyricIntervalList.add(2.32f);
            lyricList.add("聴こえるは鼓動の音\n听到的是心跳声");
            lyricIntervalList.add(3.46f);
            lyricList.add("生命燃やせ高く熱く\n燃烧生命让火焰越高越热");
            lyricIntervalList.add(4.80f);
            lyricList.add("火の鳥のように\n如同凤凰一样");
            lyricIntervalList.add(5.06f);
            lyricList.add("何度でも此処に生きる\n无论几次无论在何处都将重生");
            lyricIntervalList.add(2.43f);
            lyricList.add("漕ぎ出せ\n出发吧");
            lyricIntervalList.add(5.74f);
            lyricList.add("遠い彼方一つだった私たち\n向着遥远的彼方曾是一心的我们");
            lyricIntervalList.add(1.47f);
            lyricList.add("希望と愛抱いてる\n怀抱着爱与希望");
            lyricIntervalList.add(4.34f);
            lyricList.add("心臓（こころ）を信じた器（ひと）よ\n抱有心灵的人们哟");
            lyricIntervalList.add(3.01f);
            lyricList.add("違えた海路（たびじ）でも\n就算是在错误的航线上");
            lyricIntervalList.add(3.52f);
            lyricList.add("正義を追い求めて\n也要追求正义");
            lyricIntervalList.add(2.69f);
            lyricList.add("歩むこと止めはしない\n绝不会停下脚步");
            lyricIntervalList.add(3.01f);
            lyricList.add("悠久のカタルシス\n永恒的星光");
            lyricIntervalList.add(3.01f);
            lyricList.add("碇（いのち）から解かれたら\n如果能从这锚链（命运）中解放");
            lyricIntervalList.add(3.07f);
            lyricList.add("この海で\n就在这片海上");
            lyricIntervalList.add(2.82f);
            lyricList.add("また、逢おう\n重逢吧");
            lyricIntervalList.add(1.48f);
            lyricList.add("谢谢。");
            lyricIntervalList.add(5f);
        }
    }

    private void serviceStarter(Context context, Intent serviceIntent){
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void changeFace(int rnd, Context context){
        int resourceId = -2;
        switch(rnd){
            case 2: case 5: case 11: case 23: case 25: case 29: case 36: case 38: case 43: case 49: case 52: case 58: case 59:
                resourceId = R.drawable.character_face1;
                break;
            case 0: case 1: case 3: case 9: case 12: case 17: case 22: case 33: case 37: case 40: case 41: case 47: case 54: case 60:
                resourceId = R.drawable.character_face2;
                break;
            case 13: case 15: case 16: case 26: case 32: case 44: case 50: case 56: case 57:
                resourceId = R.drawable.character_face3;
                break;
            case 6: case 7: case 8: case 10: case 14: case 18: case 20: case 21: case 24: case 28: case 34: case 35: case 39: case 42: case 45:
            case 46: case 51: case 53: case 55:
                resourceId = R.drawable.character_face4;
                break;
            case 4: case 19: case 27: case 30: case 31: case 48: case 61:
                resourceId = R.drawable.character_face5;
                break;
            case -1:
                resourceId = R.drawable.character_face6;
                break;
        }
        if(resourceId != -2){
            RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
            remoteViews.setImageViewResource(R.id.characterPicture, resourceId);
            //remoteView.setInt(R.id.viewid, "setBackgroundResource", R.color.your_color)
            refreshWidget(context,remoteViews);
        }
    }

    private boolean judgeText(String action){
        return action.substring(0,15).equals(CLICKMAP);
    }

    private boolean judgeSave(String action){
        return action.substring(0,16).equals(SAVEDOCUMENT);
    }

    public static int getId(Context context, String resName) {
        return context.getResources().getIdentifier(resName, "id", context.getPackageName());
    }

    public int[][] getRndIslandList(int islandNumber){
        int[][] islandList = new int[islandNumber][2];
        //int[][] islandList = {{0,6},{0,9},{1,0},{1,4},{2,7},{3,6},{3,8},{4,0},{4,2},{4,6},{5,3},{5,6},{5,7},{5,8},{5,9},{6,3},{7,2},{7,4},{7,6},{8,1},{8,5},{9,2},{9,7}};
        for(int i = 0; i < islandNumber; i++){
            for(int j = 0; j < 2; j++){
                islandList[i][j] = 0;
            }
        }
        for(int i = 0; i < islandNumber; i++){
            int x = getRnd(0,9);
            int y = getRnd(0,9);
            islandList[i][0] = x;
            islandList[i][1] = y;
        }
        return islandList;
    }

    public int getRnd(int min, int max){
        return (int)(Math.random()*(max - min + 1)) + min;
    }

    public void setMap(int[][] islandList, Context context){
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        SharedPreferences.Editor editor = context.getSharedPreferences("map",MODE_PRIVATE).edit();
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                editor.putInt(""+i+"_"+j,0);
                remoteViews.setInt(getId(context,"map"+i+"_"+j),"setBackgroundResource",R.color.colorOcean);
            }
        }
        for (int[] ints : islandList) {
            int x = ints[0];
            int y = ints[1];
            editor.putInt("" + x + "_" + y, 1);
            remoteViews.setInt(getId(context, "map" + x + "_" + y), "setBackgroundResource", R.color.colorSand);
        }
        editor.apply();
        refreshWidget(context,remoteViews);
    }

    public void setMap(Context context){
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        SharedPreferences pref = context.getSharedPreferences("map",MODE_PRIVATE);
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(pref.getInt(""+i+"_"+j,0) == 1){
                    remoteViews.setInt(getId(context,"map"+i+"_"+j), "setBackgroundResource", R.color.colorSand);
                }else{
                    remoteViews.setInt(getId(context,"map"+i+"_"+j), "setBackgroundResource", R.color.colorOcean);
                }
            }
        }
        refreshWidget(context,remoteViews);
    }

    public int getStage(Context context){
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        return pref.getInt("stage",-1);
    }

    public void setStage(Context context, int stage){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        editor.putInt("stage",stage);
        Log.d("Sam",""+stage);
        editor.apply();
    }

    public int getGame(Context context){
        //1战舰 2下棋
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        return pref.getInt("game",-1);
    }

    public void setGame(Context context, int game){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        editor.putInt("game",game);
        editor.apply();
    }
    public int getShip(Context context){
        //-1未选择 0驱逐舰 1战列舰 2航母
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        return pref.getInt("ship",-1);
    }

    public void setShip(Context context, int ship){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        editor.putInt("ship",ship);
        editor.apply();
    }

    public int getAyanamiShip(Context context){
        //-1未选择 0驱逐舰 1战列舰 2航母
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        return pref.getInt("AyanamiShip",-1);
    }

    public void setAyanamiShip(Context context, int ship){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        editor.putInt("AyanamiShip",ship);
        editor.apply();
    }
    //orientation 0右 1左 2上 3下
    public void setShipPosition(Context context, int shipX, int shipY, int orientation, int shipId, boolean isAyanamiSet){
        int xAdd = (orientation == 2)? -1:(orientation == 3? 1:0);
        int yAdd = (orientation == 0)? 1:(orientation == 1? -1:0);
        SharedPreferences.Editor editor = context.getSharedPreferences("map",MODE_PRIVATE).edit();
        for(int i = 0; i <= (3-shipId); i++){
            editor.putInt(""+(shipX+xAdd*i)+"_"+(shipY+yAdd*i),(isAyanamiSet? 3:2));
        }
        editor.apply();
    }

    public void setTouchPoint(Context context, int x, int y, int orientation){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        editor.putInt("touchX",x);
        editor.putInt("touchY",y);
        editor.putInt("touchOrientation",orientation);
        editor.apply();
    }

    public void clearTouchPoint(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        editor.putInt("touchX",-1);
        editor.putInt("touchY",-1);
        editor.apply();
    }

    public int getTouchPoint(Context context, int resource){
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        switch(resource){
            case SHIPX:
                return pref.getInt("touchX",-1);
            case SHIPY:
                return pref.getInt("touchY",-1);
            case SHIPORIENTATION:
                return pref.getInt("touchOrientation",-1);
            default:
                return -1;
        }
    }

    public boolean canShipPlace(Context context, int x, int y, int orientation, int shipId, boolean ayanamiTurn){
        int xAdd = (orientation == 2)? -1:(orientation == 3? 1:0);
        int yAdd = (orientation == 0)? 1:(orientation == 1? -1:0);
        SharedPreferences pref;
        if(ayanamiTurn){
            pref = context.getSharedPreferences("AIMap",MODE_PRIVATE);
        }else{
            pref = context.getSharedPreferences("map",MODE_PRIVATE);
        }
        int temp = pref.getInt(""+x+"_"+y,-1);
        if(ayanamiTurn? (temp == 0 || temp == 2):(temp == 0)){
            for(int i = 1; i <= (3-shipId); i++){
                if((x+xAdd*i) <= 9 && (x+xAdd*i) >= 0 && (y+yAdd*i) <= 9 && (y+yAdd*i) >= 0){
                    if(pref.getInt(""+(x+xAdd*i)+"_"+(y+yAdd*i),-1) != 0){
                        //Log.d("Sam","false1:"+(x+xAdd*i)+"_"+(y+yAdd*i));
                        return false;
                    }
                }else{
                    //Log.d("Sam","false2:"+(x+xAdd*i)+"_"+(y+yAdd*i));
                    return false;
                }
            }
        }else{
            //Log.d("Sam","false3");
            return false;
        }
        return true;
    }

    public int findOrientation(Context context, int x, int y,int shipId){
        int start = getRnd(0,3);
        for(int i = (start == 3? 0:(start+1)); i < 4; i++){
            if(canShipPlace(context, x, y, i, shipId, false)){
                return i;
            }
            if(i == start){
                break;
            }else if(i == 3){
                i = -1;
            }
        }
        return -1;
    }

    public void changeShipBlockColor(Context context, int x, int y, int orientation, int shipId, int colorId, boolean isSetInitPoint){
        int xAdd = (orientation == 2)? -1:(orientation == 3? 1:0);
        int yAdd = (orientation == 0)? 1:(orientation == 1? -1:0);
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        for(int i = 0; i <= (3-shipId); i++){
            if(i == 0 && isSetInitPoint){
                remoteViews.setInt(getId(context,"map"+(x+xAdd*i)+"_"+(y+yAdd*i)),"setBackgroundResource", R.color.colorQuarterGreen);
            }else{
                remoteViews.setInt(getId(context,"map"+(x+xAdd*i)+"_"+(y+yAdd*i)),"setBackgroundResource", colorId);
            }
        }
        refreshWidget(context,remoteViews);
    }

    public void changeHitBlockColor(Context context, int x, int y, int orientation, int ship, int colorId, boolean collisionWithEnemy, boolean isAyanamiHit){
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        switch(ship){
            case 0:
                int addX = (orientation == 0 ? -1:(orientation == 2? 1:0));
                int addY = (orientation == 1 ? 1:(orientation == 3? -1:0));
                for(int i = 0; i <= 9; i++){
                    if((x+addX*i)>=0&&(x+addX*i)<=9&&(y+addY*i)>=0&&(y+addY*i)<=9){
                        int topography = getMapPoint(context,(x+addX*i),(y+addY*i),false);
                        if(topography != 1 && (isAyanamiHit || (topography != 2)) && (!collisionWithEnemy || topography != 3)){
                            remoteViews.setInt(getId(context,"map"+(x+addX*i)+"_"+(y+addY*i)),"setBackgroundResource",colorId);
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }
                break;
            case 1: case 2:
                int[][] hitArea = cloneHitArea(ship == 1? BATTLESHIPHITAREA:AIRCRAFTCARRIERHITAREA);
                for(int i = 0; i < hitArea.length; i++){
                    for(int j = 0; j < orientation; j++){
                        hitArea[i] = rotate90(hitArea[i]);
                    }
                    if((x+hitArea[i][0]>=0) && (x+hitArea[i][0]<=9) && (y+hitArea[i][1]>=0) && (y+hitArea[i][1]<=9)){
                        remoteViews.setInt(getId(context,"map"+(x+hitArea[i][0])+"_"+(y+hitArea[i][1])),"setBackgroundResource",colorId);
                    }
                }
                break;
            default:
        }
        refreshWidget(context,remoteViews);
    }

    public void recoverHitBlockColor(Context context, int x, int y, int orientation, int ship){
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        switch(ship){
            case 0:
                int addX = (orientation == 0 ? -1:(orientation == 2? 1:0));
                int addY = (orientation == 1 ? 1:(orientation == 3? -1:0));
                for(int i = 0; i <= 9; i++){
                    if((x+addX*i)>=0&&(x+addX*i)<=9&&(y+addY*i)>=0&&(y+addY*i)<=9){
                        int topography = getMapPoint(context,(x+addX*i),(y+addY*i),false);
                        if(topography != 1 && topography != 2){
                            int colorId = R.color.colorOcean;
                            switch(topography) {
                                case 0:
                                case 3:
                                    colorId = R.color.colorOcean;
                                    break;
                                case 1:
                                    colorId = R.color.colorSand;
                                    break;
                                case 2:
                                    colorId = R.color.colorGreen;
                                    break;
                                case 4:
                                    colorId = R.color.colorDarkGreen;
                                    break;
                                case 5:
                                    colorId = R.color.colorSink;
                                    break;
                                case 6:
                                    colorId = R.color.colorDarkBlue;
                                    break;
                            }
                            remoteViews.setInt(getId(context,"map"+(x+addX*i)+"_"+(y+addY*i)),"setBackgroundResource",colorId);
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }
                break;
            case 1: case 2:
                int[][] hitArea = cloneHitArea(ship == 1? BATTLESHIPHITAREA:AIRCRAFTCARRIERHITAREA);
                for(int i = 0; i < hitArea.length; i++){
                    for(int j = 0; j < orientation; j++){
                        hitArea[i] = rotate90(hitArea[i]);
                    }
                    if((x+hitArea[i][0]>=0) && (x+hitArea[i][0]<=9) && (y+hitArea[i][1]>=0) && (y+hitArea[i][1]<=9)){
                        int topography = getMapPoint(context, x+hitArea[i][0], y+hitArea[i][1], false);
                        int colorId = R.color.colorOcean;
                        switch(topography) {
                            case 0:
                            case 3:
                                colorId = R.color.colorOcean;
                                break;
                            case 1:
                                colorId = R.color.colorSand;
                                break;
                            case 2:
                                colorId = R.color.colorGreen;
                                break;
                            case 4:
                                colorId = R.color.colorDarkGreen;
                                break;
                            case 5:
                                colorId = R.color.colorSink;
                                break;
                            case 6:
                                colorId = R.color.colorDarkBlue;
                                break;
                        }
                        remoteViews.setInt(getId(context,"map"+(x+hitArea[i][0])+"_"+(y+hitArea[i][1])),"setBackgroundResource",colorId);
                    }
                }
                break;
            default:
        }
        refreshWidget(context,remoteViews);
    }

    public boolean judgeHit(Context context, int x, int y, int orientation, int ship, boolean isAyanamiHit){
        boolean flag = false;
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        SharedPreferences pref = context.getSharedPreferences("map",MODE_PRIVATE);
        switch(ship){
            case 0:
                int addX = (orientation == 0 ? -1:(orientation == 2? 1:0));
                int addY = (orientation == 1 ? 1:(orientation == 3? -1:0));
                int i;
                for(i = 0; i <= 9; i++){
                    if((x+addX*i)>=0&&(x+addX*i)<=9&&(y+addY*i)>=0&&(y+addY*i)<=9){
                        int topography = getMapPoint(context,(x+addX*i),(y+addY*i),false);
                        if(topography != 1 && (isAyanamiHit? (topography != 3):(topography != 2))){
                            if((isAyanamiHit? (topography == 2):(topography == 3))){//命中
                                remoteViews.setInt(getId(context,"map"+(x+addX*i)+"_"+(y+addY*i)),"setBackgroundResource",(isAyanamiHit? R.color.colorDarkGreen:R.color.colorSink));
                                setMapPoint(context,(x+addX*i),(y+addY*i),(isAyanamiHit? 4:5));
                                flag = true;
                                setSink(context, isAyanamiHit);
                                break;
                            }else{
                                if(!isAyanamiHit){
                                    int colorId = R.color.colorDarkBlue;
                                    switch(topography){
                                        case 4:
                                            colorId = R.color.colorDarkGreen;
                                            break;
                                        case 5:
                                            colorId = R.color.colorSink;
                                            break;
                                    }
                                    remoteViews.setInt(getId(context,"map"+(x+addX*i)+"_"+(y+addY*i)),"setBackgroundResource", colorId);
                                    if(getMapPoint(context,(x+addX*i),(y+addY*i), false) == 0){
                                        setMapPoint(context,(x+addX*i),(y+addY*i),6);
                                    }
                                }
                            }
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }
                if(!isAyanamiHit){
                    for(i++; i <= 9; i++){
                        if((x+addX*i)>=0&&(x+addX*i)<=9&&(y+addY*i)>=0&&(y+addY*i)<=9){
                            int topography = getMapPoint(context,(x+addX*i),(y+addY*i),false);
                            if(topography != 1 && (isAyanamiHit? (topography != 3):(topography != 2))){
                                int colorId = R.color.colorOcean;
                                switch(topography){
                                    case 0: case 3:
                                        colorId = R.color.colorOcean;
                                        break;
                                    case 1:
                                        colorId = R.color.colorSand;
                                        break;
                                    case 2:
                                        colorId = R.color.colorGreen;
                                        break;
                                    case 4:
                                        colorId = R.color.colorDarkGreen;
                                        break;
                                    case 5:
                                        colorId = R.color.colorSink;
                                        break;
                                    case 6:
                                        colorId = R.color.colorDarkBlue;
                                }
                                remoteViews.setInt(getId(context,"map"+(x+addX*i)+"_"+(y+addY*i)),"setBackgroundResource", colorId);
                            }else{
                                break;
                            }
                        }else{
                            break;
                        }
                    }
                }

                break;
            case 1: case 2:
                int[][] hitArea = cloneHitArea(ship == 1? BATTLESHIPHITAREA:AIRCRAFTCARRIERHITAREA);
                for(i = 0; i < hitArea.length; i++){
                    for(int j = 0; j < orientation; j++){
                        hitArea[i] = rotate90(hitArea[i]);
                    }
                    if((x+hitArea[i][0]>=0) && (x+hitArea[i][0]<=9) && (y+hitArea[i][1]>=0) && (y+hitArea[i][1]<=9)){
                        int topography = pref.getInt(""+(x+hitArea[i][0])+"_"+(y+hitArea[i][1]),-1);
                        if(topography == (isAyanamiHit? 2:3)){//命中
                            remoteViews.setInt(getId(context,"map"+(x+hitArea[i][0])+"_"+(y+hitArea[i][1])),"setBackgroundResource",(isAyanamiHit? R.color.colorDarkGreen:R.color.colorSink));
                            setMapPoint(context,(x+hitArea[i][0]),(y+hitArea[i][1]),(isAyanamiHit? 4:5));
                            setSink(context, isAyanamiHit);
                            flag = true;
                        }else{
                            if(!isAyanamiHit){
                                int colorId = R.color.colorOcean;
                                switch(topography){
                                    case 0: case 3:
                                        colorId = (isAyanamiHit? R.color.colorOcean:R.color.colorDarkBlue);
                                        break;
                                    case 1:
                                        colorId = R.color.colorSand;
                                        break;
                                    case 2:
                                        colorId = R.color.colorGreen;
                                        break;
                                    case 4:
                                        colorId = R.color.colorDarkGreen;
                                        break;
                                    case 5:
                                        colorId = R.color.colorSink;
                                        break;
                                    case 6:
                                        colorId = R.color.colorDarkBlue;
                                }
                                if(getMapPoint(context,(x+hitArea[i][0]),(y+hitArea[i][1]),false) == 0){
                                    setMapPoint(context,(x+hitArea[i][0]),(y+hitArea[i][1]),6);
                                }
                                remoteViews.setInt(getId(context,"map"+(x+hitArea[i][0])+"_"+(y+hitArea[i][1])),"setBackgroundResource",colorId);

                            }
                        }
                    }
                }
                break;
            default:
        }
        refreshWidget(context,remoteViews);
        return flag;
    }

    public int[] rotate90(int[] hitArea){
        //顺时针
        return new int[]{hitArea[1],-hitArea[0]};
    }

    public void clearMap(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("map",MODE_PRIVATE).edit();
        SharedPreferences.Editor editor2 = context.getSharedPreferences("AIMap",MODE_PRIVATE).edit();
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                editor.putInt(""+i+"_"+j,0);
                editor2.putInt(""+i+"_"+j,0);
            }
        }
        editor.apply();
        editor2.apply();
    }

    public void clearInfo(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        editor.putInt("stage",-1);
        editor.putInt("ship",-1);
        editor.putInt("touchX",-1);
        editor.putInt("touchY",-1);
        editor.putInt("touchOrientation",-1);
        editor.putInt("AyanamiShip",-1);
        editor.putInt("playerSink", 0);
        editor.putInt("ayanamiSink",0);
        editor.putInt("playerScore",0);//寻宝游戏的得分
        editor.putInt("AyanamiScore",0);
        editor.apply();
    }

    public int getMapPoint(Context context, int x, int y, boolean isAiMap){
        SharedPreferences pref = context.getSharedPreferences(isAiMap? "AImap":"map",MODE_PRIVATE);
        return pref.getInt(""+x+"_"+y,-1);
    }

    public int[][] cloneHitArea(int [][] hitArea){
        int[][] ans = new int[hitArea.length][hitArea[0].length];
        for(int i = 0; i < ans.length; i++){
            System.arraycopy(hitArea[i], 0, ans[i], 0, ans[0].length);
        }
        return ans;
    }

    public void setMapPoint(Context context, int x, int y, int topography){
        SharedPreferences.Editor editor = context.getSharedPreferences("map",MODE_PRIVATE).edit();
        editor.putInt(""+x+"_"+y,topography);
        editor.apply();
    }

    public void recoverAyanamiHit(Context context, int x, int y, int orientation, int ship){
        SharedPreferences.Editor editor2 = context.getSharedPreferences("AIMap",MODE_PRIVATE).edit();
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        switch(ship){
            case 0:
                int addX = (orientation == 0 ? -1:(orientation == 2? 1:0));
                int addY = (orientation == 1 ? 1:(orientation == 3? -1:0));
                for(int i = 0; i <= 9; i++){
                    if((x+addX*i)>=0&&(x+addX*i)<=9&&(y+addY*i)>=0&&(y+addY*i)<=9){
                        editor2.putInt(""+(x+addX*i)+"_"+(y+addY*i),1);
                        int topography = getMapPoint(context,(x+addX*i),(y+addY*i),false);
                        int colorId = R.color.colorOcean;
                        switch(topography) {
                            case 0: case 3:
                                colorId = R.color.colorOcean;
                                break;
                            case 1:
                                colorId = R.color.colorSand;
                                break;
                            case 2:
                                colorId = R.color.colorGreen;
                                break;
                            case 4:
                                colorId = R.color.colorDarkGreen;
                                break;
                            case 5:
                                colorId = R.color.colorSink;
                                break;
                            case 6:
                                colorId = R.color.colorDarkBlue;
                                break;
                        }
                        remoteViews.setInt(getId(context,"map"+(x+addX*i)+"_"+(y+addY*i)),"setBackgroundResource",colorId);
                    }else{
                        break;
                    }
                }
                break;
            case 1: case 2:
                int[][] hitArea = cloneHitArea(ship == 1? BATTLESHIPHITAREA:AIRCRAFTCARRIERHITAREA);
                for(int i = 0; i < hitArea.length; i++){
                    for(int j = 0; j < orientation; j++){
                        hitArea[i] = rotate90(hitArea[i]);
                    }
                    if((x+hitArea[i][0]>=0) && (x+hitArea[i][0]<=9) && (y+hitArea[i][1]>=0) && (y+hitArea[i][1]<=9)){
                        editor2.putInt(""+(x+hitArea[i][0])+"_"+(y+hitArea[i][1]),1);
                        int topography = getMapPoint(context, x+hitArea[i][0], y+hitArea[i][1],false);
                        int colorId = R.color.colorOcean;
                        switch(topography) {
                            case 0:
                            case 3:
                                colorId = R.color.colorOcean;
                                break;
                            case 1:
                                colorId = R.color.colorSand;
                                break;
                            case 2:
                                colorId = R.color.colorGreen;
                                break;
                            case 4:
                                colorId = R.color.colorDarkGreen;
                                break;
                            case 5:
                                colorId = R.color.colorSink;
                                break;
                            case 6:
                                colorId = R.color.colorDarkBlue;
                                break;
                        }
                        remoteViews.setInt(getId(context,"map"+(x+hitArea[i][0])+"_"+(y+hitArea[i][1])),"setBackgroundResource",colorId);
                    }
                }
                break;
            default:
        }
        editor2.apply();
        refreshWidget(context,remoteViews);
    }

    public void setSink(Context context, boolean isAyanamiHit){
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        if (isAyanamiHit){
            editor.putInt("playerSink", pref.getInt("playerSink",0)+1);
        }else{
            editor.putInt("ayanamiSink",pref.getInt("ayanamiSink",0)+1);
        }
        editor.apply();
    }

    public boolean judgeOutcome(Context context, boolean isAyanamiHit){
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        if(isAyanamiHit){
            return (pref.getInt("playerSink",0) == 5);
        }else{
            return (pref.getInt("ayanamiSink",0) == 5);
        }
    }

    public boolean isBattleMiddle(Context context){
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
            return (pref.getInt("playerSink",0) >= 1 || pref.getInt("ayanamiSink",0) >= 1);
    }

    public float canShipPlaceNumber(Context context, int x, int y, int oceanNumberImpact){
        float sum = 0;
        for(int j = 1; j <= 2; j++){
            for(int i = 0; i < 4; i++){
                if(canShipPlace(context,x,y,i,j,true)){
                    sum += getOceanNumberProbability(context,x,y,i,j)*oceanNumberImpact;
                }
            }
        }
        return sum;
    }

    public float calculateProbability(Context context, int x, int y, int orientation, int ship){
        float sum = 0.0f;
        float WEIGHT = 500.0f;
        int flexibility = 50;
        int oceanNumberImpact = 10;
        boolean haveSinkShip = false;
        int sinkX = -1;
        int sinkY = -1;
        SharedPreferences pref = context.getSharedPreferences("AIMap",MODE_PRIVATE);
        switch(ship){
            case 0:
                int addX = (orientation == 0 ? -1:(orientation == 2? 1:0));
                int addY = (orientation == 1 ? 1:(orientation == 3? -1:0));
                int i;
                for(i = 0; i <= 9; i++){
                    if((x+addX*i)>=0&&(x+addX*i)<=9&&(y+addY*i)>=0&&(y+addY*i)<=9){
                        int topography = getMapPoint(context,(x+addX*i),(y+addY*i),true);
                        if(getMapPoint(context,(x+addX*i),(y+addY*i),false) == 4 && !haveSinkShip){
                            haveSinkShip = true;
                            sinkX = x+addX*i;
                            sinkY = y+addY*i;
                            i = -1;
                            sum = 0.0f;
                            continue;
                        }
                        if(getMapPoint(context,(x+addX*i),(y+addY*i),false) == 2 && haveSinkShip){
                            if(Math.abs(sinkX + sinkY-(x+addX*i)-(y+addY*i)) <= 1){
                                sum += WEIGHT;
                            }
                        }
                        if(topography == 0){
                            sum += canShipPlaceNumber(context,x+addX*i,y+addY*i, oceanNumberImpact);
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }
                break;
            case 1: case 2:
                int[][] hitArea = cloneHitArea(ship == 1? BATTLESHIPHITAREA:AIRCRAFTCARRIERHITAREA);
                for(i = 0; i < hitArea.length; i++){
                    for(int j = 0; j < orientation; j++){
                        hitArea[i] = rotate90(hitArea[i]);
                    }
                    if((x+hitArea[i][0]>=0) && (x+hitArea[i][0]<=9) && (y+hitArea[i][1]>=0) && (y+hitArea[i][1]<=9)){
                        int topography = pref.getInt(""+(x+hitArea[i][0])+"_"+(y+hitArea[i][1]),-1);
                        if(getMapPoint(context,(x+hitArea[i][0]),(y+hitArea[i][1]),false) == 4 && !haveSinkShip){
                            haveSinkShip = true;
                            sinkX = x+hitArea[i][0];
                            sinkY = y+hitArea[i][1];
                            i = -1;
                            sum = 0.0f;
                            hitArea = cloneHitArea(ship == 1? BATTLESHIPHITAREA:AIRCRAFTCARRIERHITAREA);
                            continue;
                        }
                        if(getMapPoint(context,(x+hitArea[i][0]),(y+hitArea[i][1]),false) == 2 && haveSinkShip){
                            if(Math.abs(sinkX + sinkY-(x+hitArea[i][0])-(y+hitArea[i][1])) <= 1){
                                sum += WEIGHT;
                            }
                        }
                        if(topography == 0){
                            sum += canShipPlaceNumber(context,x+hitArea[i][0],y+hitArea[i][1], oceanNumberImpact);
                        }
                    }
                }
                break;
            default:
        }
        return sum + 1.0f*getRnd(0,flexibility);
    }

    public void setAIMap(Context context){
        SharedPreferences pref = context.getSharedPreferences("map",MODE_PRIVATE);
        SharedPreferences.Editor editor2 = context.getSharedPreferences("AIMap",MODE_PRIVATE).edit();
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                int temp = pref.getInt(""+i+"_"+j,0);
                if(temp == 0 || temp == 2){
                    editor2.putInt(""+i+"_"+j,0);
                }else{
                    editor2.putInt(""+i+"_"+j,1);
                }
            }
        }
        editor2.apply();
    }

    public int[] ayanamiGetHitInfo(Context context, int ship){
        float maxSum = -1;
        int maxI = 0;
        int maxJ = 0;
        int maxK = 0;
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                for(int k = 0; k <= 3; k++){
                    float cal = calculateProbability(context,i,j,k,ship);
                    if(cal > maxSum){
                        maxSum = cal;
                        maxI = i;
                        maxJ = j;
                        maxK = k;
                    }
                }
            }
        }
        Log.d("Sam","maxHit: "+maxSum);
        return new int[]{maxI,maxJ,maxK};
    }

    public void showAyanamiShip(Context context){
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        SharedPreferences pref = context.getSharedPreferences("map",MODE_PRIVATE);
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(pref.getInt(""+i+"_"+j,0) == 3){
                    remoteViews.setInt(getId(context,"map"+i+"_"+j), "setBackgroundResource", R.color.colorRed);
                }
            }
        }
        refreshWidget(context,remoteViews);
    }

    public int getDialogRnd(int[] dialog){
        return dialog[getRnd(0,dialog.length-1)];
    }

    public int calculateOceanNumber(Context context, int x, int y, int orientation, int ship){
        int sum = 0;
        int xAdd = (orientation == 2)? -1:(orientation == 3? 1:0);
        int yAdd = (orientation == 0)? 1:(orientation == 1? -1:0);
        int xScan = 0;
        int yScan = 0;
        if(xAdd != 0){
            yScan = 1;
        }else{
            xScan = 1;
        }
        for(int i = -1; i <= (4-ship); i++){
            for(int j = -1; j <=1 ; j++){
                int x1 = x+xAdd*i+xScan*j;
                int y1 = y+yAdd*i+yScan*j;
                if(x1 >= 0 && x1 <= 9 && y1 >= 0 && y1 <= 9){
                    if(getMapPoint(context,x1,y1,false) == 0){
                        sum++;
                    }
                }
            }
        }
        return sum;
    }

    public void addOceanNumberRecord(Context context, int ship, int oceanNumber){
        SharedPreferences pref = context.getSharedPreferences("OceanNumberRecord",MODE_PRIVATE);
        SharedPreferences.Editor editor = context.getSharedPreferences("OceanNumberRecord",MODE_PRIVATE).edit();
        //1_0:第一艘船（3格），船周围海的数量为0
        editor.putInt(""+ship+"_"+oceanNumber, pref.getInt(""+ship+"_"+oceanNumber,0) + 1);
        editor.putInt("ship"+ship+"totalNumber", pref.getInt("ship"+ship+"totalNumber",0) + 1);
        editor.apply();
    }

    public float getOceanNumberProbability(Context context, int x, int y, int orientation, int ship){
        SharedPreferences pref = context.getSharedPreferences("OceanNumberRecord",MODE_PRIVATE);
        int number = calculateOceanNumber(context, x, y, orientation, ship);
        int n = ship == 1? 13:11;
        return 1.0f*(pref.getInt(""+ship+"_"+number,0) - 1.0f*pref.getInt("ship"+ship+"totalNumber",0)/n + 20)/(20*n);
    }

    public void initChess(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("chessBoard",MODE_PRIVATE).edit();
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                editor.putInt(""+i+"_"+j,0);
                remoteViews.setInt(getId(context,"map"+(i)+"_"+(j)),"setBackgroundResource", R.color.colorOcean);
            }
        }
        remoteViews.setInt(getId(context,"map"+(4)+"_"+(4)),"setBackgroundResource", R.color.colorLightWhite);
        remoteViews.setInt(getId(context,"map"+(4)+"_"+(5)),"setBackgroundResource", R.color.colorBlack);
        remoteViews.setInt(getId(context,"map"+(5)+"_"+(4)),"setBackgroundResource", R.color.colorBlack);
        remoteViews.setInt(getId(context,"map"+(5)+"_"+(5)),"setBackgroundResource", R.color.colorLightWhite);
        editor.putInt(""+4+"_"+4,2);
        editor.putInt(""+4+"_"+5,1);
        editor.putInt(""+5+"_"+4,1);
        editor.putInt(""+5+"_"+5,2);
        editor.apply();
        refreshWidget(context,remoteViews);
    }

    public int[] AIMove(Context context){
        AlphaTree tree = new AlphaTree(context,true);
        int[] answer = new int[]{tree.bestChoice[0],tree.bestChoice[1],tree.bestScore};
        return answer;
    }

    public boolean canMove(Context context, int x, int y, boolean isAyanami){
        int myChess = isAyanami? 2:1;
        int enemyChess = isAyanami? 1:2;
        SharedPreferences pref = context.getSharedPreferences("chessBoard",MODE_PRIVATE);
        if(pref.getInt(""+x+"_"+y,0) != 0){
            return false;
        }
        if(x >= 1 && pref.getInt(""+(x-1)+"_"+y,0) == enemyChess){
            for(int i = x-2; i >= 0; i--){
                if(pref.getInt(""+i+"_"+y,0) == myChess){
                    return true;
                }else if(pref.getInt(""+i+"_"+y,0) == 0){
                    break;
                }
            }
        }
        if(x <= 8 && pref.getInt(""+(x+1)+"_"+y,0) == enemyChess){
            for(int i = x+2; i <= 9; i++){
                if(pref.getInt(""+i+"_"+y,0) == myChess){
                    return true;
                }else if(pref.getInt(""+i+"_"+y,0) == 0){
                    break;
                }
            }
        }
        if(y >= 1 && pref.getInt(""+x+"_"+(y-1),0) == enemyChess){
            for(int i = y-2; i >= 0; i--){
                if(pref.getInt(""+x+"_"+i,0) == myChess){
                    return true;
                }else if(pref.getInt(""+x+"_"+i,0) == 0){
                    break;
                }
            }
        }
        if(y <= 8 && pref.getInt(""+x+"_"+(y+1),0) == enemyChess){
            for(int i = y+2; i <= 9; i++){
                if(pref.getInt(""+x+"_"+i,0) == myChess){
                    return true;
                }else if(pref.getInt(""+x+"_"+i,0) == 0){
                    break;
                }
            }
        }
        if(x >= 1 && y >= 1 && pref.getInt(""+(x-1)+"_"+(y-1),0) == enemyChess){
            for(int i = -1; x+i >= 0 && y+i >= 0; i--){
                if(pref.getInt("" + (x + i) + "_" + (y + i),0) == myChess){
                    return true;
                }else if(pref.getInt("" + (x + i) + "_" + (y + i),0) == 0){
                    break;
                }
            }
        }
        if(x <= 8 && y >= 1 && pref.getInt(""+(x+1)+"_"+(y-1),0) == enemyChess){
            for(int i = 1; x+i <= 9 && y-i >= 0; i++){
                if(pref.getInt(""+(x+i)+"_"+(y-i),0) == myChess){
                    return true;
                }else if(pref.getInt(""+(x+i)+"_"+(y-i),0) == 0){
                    break;
                }
            }
        }
        if(x >= 1 && y <= 8 && pref.getInt(""+(x-1)+"_"+(y+1),0) == enemyChess){
            for(int i = 1; x-i >= 0 && y+i >= 0; i++){
                if(pref.getInt(""+(x-i)+"_"+(y+i),0) == myChess){
                    return true;
                }else if(pref.getInt(""+(x-i)+"_"+(y+i),0) == 0){
                    break;
                }
            }
        }
        if(x <= 8 && y <= 8 && pref.getInt(""+(x+1)+"_"+(y+1),0) == enemyChess){
            for(int i = 1; x+i <= 9 && y+i <= 9; i++){
                if(pref.getInt(""+(x+i)+"_"+(y+i),0) == myChess){
                    return true;
                }else if(pref.getInt(""+(x+i)+"_"+(y+i),0) == 0){
                    break;
                }
            }
        }
        return false;
    }

    public void changeMoveBlockColor(Context context, int x, int y, boolean isAyanamiMove, boolean isRealMove){
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        int myChess = isAyanamiMove? 2:1;
        int myChessColor = isAyanamiMove? R.color.colorLightWhite:R.color.colorBlack;
        SharedPreferences pref = context.getSharedPreferences("chessBoard",MODE_PRIVATE);
        SharedPreferences.Editor editor = context.getSharedPreferences("chessBoard",MODE_PRIVATE).edit();
        remoteViews.setInt(getId(context,"map"+(x)+"_"+(y)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorDarkGray);
        if(isRealMove)editor.putInt(""+x+"_"+y,myChess);
        boolean flag = false;
        for(int i = x-1; i >= 0; i--){
            int temp = pref.getInt(""+i+"_"+y,0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = x-1; i >= 0; i--){
                int temp = pref.getInt(""+i+"_"+y,0);
                if(temp == myChess){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(i)+"_"+(y)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+i+"_"+y,myChess);
            }
        }


        flag = false;
        for(int i = x+1; i <= 9; i++){
            int temp = pref.getInt(""+i+"_"+y,0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = x+1; i <= 9; i++){
                int temp = pref.getInt(""+i+"_"+y,0);
                if(temp == myChess){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(i)+"_"+(y)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+i+"_"+y,myChess);
            }
        }

        flag = false;
        for(int i = y-1; i >= 0; i--){
            int temp = pref.getInt(""+x+"_"+i,0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = y-1; i >= 0; i--){
                int temp = pref.getInt(""+x+"_"+i,0);
                if(temp == myChess){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(x)+"_"+(i)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+x+"_"+i,myChess);
            }
        }

        flag = false;
        for(int i = y+1; i <= 9; i++){
            int temp = pref.getInt(""+x+"_"+i,0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = y+1; i <= 9; i++){
                int temp = pref.getInt(""+x+"_"+i,0);
                if(temp == myChess){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(x)+"_"+(i)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+x+"_"+i,myChess);
            }
        }

        flag = false;
        for(int i = -1; x+i >= 0 && y+i >= 0; i--){
            int temp = pref.getInt(""+(x+i)+"_"+(y+i),0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = -1; x+i >= 0 && y+i >= 0; i--){
                int temp = pref.getInt(""+(x+i)+"_"+(y+i),0);
                if(temp == myChess || temp == 0){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(x+i)+"_"+(y+i)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+(x+i)+"_"+(y+i),myChess);
            }
        }

        flag = false;
        for(int i = 1; x+i <= 9 && y-i >= 0; i++){
            int temp = pref.getInt(""+(x+i)+"_"+(y-i),0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = 1; x+i <= 9 && y-i >= 0; i++){
                int temp = pref.getInt(""+(x+i)+"_"+(y-i),0);
                if(temp == myChess || temp == 0){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(x+i)+"_"+(y-i)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+(x+i)+"_"+(y-i),myChess);
            }
        }

        flag = false;
        for(int i = 1; x-i >= 0 && y+i >= 0; i++){
            int temp = pref.getInt(""+(x-i)+"_"+(y+i),0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = 1; x-i >= 0 && y+i >= 0; i++){
                int temp = pref.getInt(""+(x-i)+"_"+(y+i),0);
                if(temp == myChess || temp == 0){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(x-i)+"_"+(y+i)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+(x-i)+"_"+(y+i),myChess);
            }
        }

        flag = false;
        for(int i = 1; x+i <= 9 && y+i <= 9; i++){
            int temp = pref.getInt(""+(x+i)+"_"+(y+i),0);
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = 1; x+i <= 9 && y+i <= 9; i++){
                int temp = pref.getInt(""+(x+i)+"_"+(y+i),0);
                if(temp == myChess || temp == 0){
                    break;
                }
                remoteViews.setInt(getId(context,"map"+(x+i)+"_"+(y+i)),"setBackgroundResource", isRealMove? myChessColor:R.color.colorGray);
                if(isRealMove)editor.putInt(""+(x+i)+"_"+(y+i),myChess);
            }
        }

        refreshWidget(context,remoteViews);
        editor.apply();
    }

    public void recoverMoveBlockColor(Context context, int x, int y, boolean isAyanamiMove){
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
        int myChess = isAyanamiMove? 2:1;
        int enemyChessColor = isAyanamiMove? R.color.colorBlack:R.color.colorLightWhite;
        SharedPreferences pref = context.getSharedPreferences("chessBoard",MODE_PRIVATE);
        remoteViews.setInt(getId(context,"map"+(x)+"_"+(y)),"setBackgroundResource", R.color.colorOcean);
        for(int i = x-1; i >= 0; i--){
            int temp = pref.getInt(""+i+"_"+y,0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(i)+"_"+(y)),"setBackgroundResource", enemyChessColor);
        }
        for(int i = x+1; i <= 9; i++){
            int temp = pref.getInt(""+i+"_"+y,0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(i)+"_"+(y)),"setBackgroundResource", enemyChessColor);
        }

        for(int i = y-1; i >= 0; i--){
            int temp = pref.getInt(""+x+"_"+i,0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(x)+"_"+(i)),"setBackgroundResource", enemyChessColor);
        }

        for(int i = y+1; i <= 9; i++){
            int temp = pref.getInt(""+x+"_"+i,0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(x)+"_"+(i)),"setBackgroundResource", enemyChessColor);
        }

        for(int i = -1; x+i >= 0 && y+i >= 0; i--){
            int temp = pref.getInt(""+(x+i)+"_"+(y+i),0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(x+i)+"_"+(y+i)),"setBackgroundResource", enemyChessColor);
        }

        for(int i = 1; x+i <= 9 && y-i >= 0; i++){
            int temp = pref.getInt(""+(x+i)+"_"+(y-i),0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(x+i)+"_"+(y-i)),"setBackgroundResource", enemyChessColor);
        }

        for(int i = 1; x-i >= 0 && y+i >= 0; i++){
            int temp = pref.getInt(""+(x-i)+"_"+(y+i),0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(x-i)+"_"+(y+i)),"setBackgroundResource", enemyChessColor);
        }

        for(int i = 1; x+i <= 9 && y+i <= 9; i++){
            int temp = pref.getInt(""+(x+i)+"_"+(y+i),0);
            if(temp == myChess || temp == 0){
                break;
            }
            remoteViews.setInt(getId(context,"map"+(x+i)+"_"+(y+i)),"setBackgroundResource", enemyChessColor);
        }
        refreshWidget(context,remoteViews);
    }

    public int judgeMoveOutcome(Context context){
        //0无事发生 1玩家无子可下 2绫波无子可下 3双方无子可下
        boolean canPlayerPlace = false;
        boolean canAyanamiPlace = false;
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                if(!canAyanamiPlace && canMove(context,i,j,true))canAyanamiPlace = true;
                if(!canPlayerPlace && canMove(context,i,j,false))canPlayerPlace = true;
            }
        }
        if(!canAyanamiPlace && !canPlayerPlace){
            return 3;
        }else if(!canAyanamiPlace){
            return 2;
        }else if(!canPlayerPlace){
            return 1;
        }else{
            return 0;
        }
    }

    public int[] getChessNumber(Context context){
        int[] number = {0,0};
        SharedPreferences pref = context.getSharedPreferences("chessBoard",MODE_PRIVATE);
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                int temp = pref.getInt(""+i+"_"+j,0);
                if(temp > 0) number[temp-1]++;
            }
        }
        return number;
    }

    public void saveChessBoard(Context context, int id){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);//注意HOUR是12小时制，HOUR_OF_DAY是24小时制
        int minute = calendar.get(Calendar.MINUTE);
        SharedPreferences.Editor editor = context.getSharedPreferences("save0"+id,MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("chessBoard",MODE_PRIVATE);
        editor.putInt("year",year);
        editor.putInt("month",month);
        editor.putInt("day",day);
        editor.putInt("hour",hour);
        editor.putInt("minute",minute);
        int[] tempNumber = getChessNumber(CONTEXT);
        editor.putInt("blackNumber",tempNumber[0]);
        editor.putInt("whiteNumber",tempNumber[1]);
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                editor.putInt(""+i+"_"+j, pref.getInt(""+i+"_"+j,0));
            }
        }
        editor.apply();
    }

    public void loadChessBoard(Context context, int id){
        SharedPreferences.Editor editor = context.getSharedPreferences("chessBoard",MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("save0"+id,MODE_PRIVATE);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                editor.putInt(""+i+"_"+j, pref.getInt(""+i+"_"+j,0));
                int number = pref.getInt(""+i+"_"+j,0);
                remoteViews.setInt(getId(context,"map"+(i)+"_"+(j)),"setBackgroundResource", number == 0? R.color.colorOcean:(number == 1 ? R.color.colorBlack:R.color.colorLightWhite));
            }
        }
        editor.apply();
        clearInfo(context);
        setStage(context,11);
        remoteViews.setViewVisibility(R.id.chooseInitiative, View.GONE);
        int[] tempNumber = getChessNumber(context);
        remoteViews.setTextViewText(R.id.textLog,"已读取。该指挥官了。\n玩家棋子:"+tempNumber[0]+"\n绫波棋子:"+tempNumber[1]);
        changeFace(2,context);
        refreshWidget(context,remoteViews);
    }

    public void refreshSaveData(Context context){
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        for(int i = 1; i <= 9; i++){
            SharedPreferences pref = context.getSharedPreferences("save0"+i,MODE_PRIVATE);
            if(pref.getInt("blackNumber",-1) != -1){
                remoteViews.setTextViewText(getId(context,"save0"+i+"date"),"日期："+pref.getInt("year",-1)+"."+pref.getInt("month",-1)+"."+pref.getInt("day",-1));
                remoteViews.setTextViewText(getId(context,"save0"+i+"time"),"时间："+pref.getInt("hour",-1)+":"+pref.getInt("minute",-1));
                remoteViews.setTextViewText(getId(context,"save0"+i+"portion"),"黑 "+pref.getInt("blackNumber",-1)+"    白 "+pref.getInt("whiteNumber",-1));
            }else{
                remoteViews.setTextViewText(getId(context,"save0"+i+"date"),"暂无存档");
                remoteViews.setTextViewText(getId(context,"save0"+i+"time"),"");
                remoteViews.setTextViewText(getId(context,"save0"+i+"portion"),"");
            }
        }
        refreshWidget(context,remoteViews);
    }

    public void initTreasureMap(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("treasureMap",MODE_PRIVATE).edit();
        RemoteViews remoteViews = new RemoteViews(CONTEXT.getPackageName(), R.layout.main_layout);
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                editor.putInt(""+i+"_"+j,0);
                remoteViews.setImageViewResource(getId(context,"map"+(i)+"_"+(j)),R.drawable.white_frame);
                remoteViews.setInt(getId(context,"map"+(i)+"_"+(j)),"setBackgroundResource", R.color.colorOcean);
            }
        }
        editor.apply();
        createTreasure(context,TREASURENUMBER);
        refreshWidget(context,remoteViews);
    }

    public void createTreasure(Context context, int number){
        SharedPreferences pref = context.getSharedPreferences("treasureMap",MODE_PRIVATE);
        SharedPreferences.Editor editor = context.getSharedPreferences("treasureMap",MODE_PRIVATE).edit();
        int left = number;
        while(left > 0){
            int x = getRnd(0,9);
            int y = getRnd(0,9);
            if(pref.getInt(""+x+"_"+y,0) == 0){
                editor.putInt(""+x+"_"+y,1);
                left--;
            }
        }
        editor.apply();
    }

    public int[] AIChoosePoint(Context context){
        int[] answer = new int[]{0,0};
        double highestNumber = 0;
        int highestPointX = -1;
        int highestPointY = -1;
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                int temp = getTreasureMapPoint(context,i,j);
                if(temp >= 10 && temp != 19 && temp != 29){
                    int haveFound = 0;
                    int emptySpace = 0;
                    for(int k = -1; k <= 1; k++){
                        for(int l = -1; l <= 1; l++){
                            int temp2 = getTreasureMapPoint(context,i+k,j+l);
                            if(temp2 == 29||temp2 == 19){
                                haveFound++;
                            }else if(temp2 <= 1){
                                emptySpace++;
                            }
                        }
                    }
                    double temp3 = 1.0d * ((temp % 10) - haveFound) / emptySpace;
                    if(temp3 > highestNumber){
                        highestPointX = i;
                        highestPointY = j;
                        highestNumber = temp3;
                    }
                }
            }
        }
        if(highestNumber > 0.01d){
            for(int i = -1; i <= 1; i++){
                for(int j = -1; j <= 1; j++){
                    if(getTreasureMapPoint(context,highestPointX+i,highestPointY+j) <= 1){
                        if(highestPointX+i >= 0 && highestPointX+i <= 9 && highestPointY+j>=0 && highestPointY+j<=9) {
                            answer[0] = highestPointX + i;
                            answer[1] = highestPointY + j;
                            return answer;
                        }
                    }
                }
            }
        }
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(getTreasureMapPoint(context,i,j) <= 1){
                    answer[0] = i;
                    answer[1] = j;
                    return answer;
                }
            }
        }
        return answer;
    }

    public int getTreasureMapPoint(Context context, int x, int y){
        SharedPreferences pref = context.getSharedPreferences("treasureMap",MODE_PRIVATE);
        return pref.getInt(""+x+"_"+y,0);
    }

    public void changeTreasureMapPoint(Context context, int x, int y, int number){
        SharedPreferences.Editor editor = context.getSharedPreferences("treasureMap",MODE_PRIVATE).edit();
        editor.putInt(""+x+"_"+y,number);
        editor.apply();
    }

    public void changeTreasureMapPointColor(Context context, int x, int y, int color, int number){
        Log.d("Sam","chessNumber:"+number);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_layout);
        remoteViews.setInt(getId(context,"map"+(x)+"_"+(y)),"setBackgroundResource", color);
        //remoteViews.setImageViewResource(getId(context,"map"+(x)+"_"+(y)), getDrawableByString(context, (number == 0)? "white_frame":("white_frame_"+number)));
        remoteViews.setImageViewResource(getId(context,"map"+(x)+"_"+(y)), getDrawableByString(context, (number == 0)? "white_frame":("white_frame_"+number)));
        refreshWidget(context,remoteViews);
    }

    public int getDrawableByString(Context context, String name){
        Resources res = context.getResources();
        return res.getIdentifier(name,"drawable",context.getPackageName());
    }

    public int getColorByString(Context context, String str){
        Resources res = context.getResources();
        int resourceId = res.getIdentifier(str,"color",context.getPackageName());
        return res.getColor(resourceId);
    }

    public int judgeSearchOutcome(Context context){
        //加起来双方找到的宝藏总数，如果达到最大数了就结束游戏；
        //当其中一方的宝藏数+剩余宝藏数<另一方时，结束游戏。
        //0无事发生 1玩家胜利 2绫波胜利
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        int playerScore = pref.getInt("playerScore",0);//寻宝游戏的得分
        int AyanamiScore = pref.getInt("AyanamiScore",0);
        int leftScore = TREASURENUMBER - playerScore - AyanamiScore;
        if(leftScore == 0){
            return (AyanamiScore > playerScore)? 2:1;
        }else if(AyanamiScore + leftScore < playerScore){
            return 1;
        }else if(playerScore + leftScore < AyanamiScore){
            return 2;
        }
        return 0;
    }

    public int[] getTreasureNumber(Context context){
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        int playerScore = pref.getInt("playerScore",0);//寻宝游戏的得分
        int AyanamiScore = pref.getInt("AyanamiScore",0);
        int leftScore = TREASURENUMBER - playerScore - AyanamiScore;
        return new int[]{playerScore,AyanamiScore,leftScore};
    }

    public void addTreasureNumber(Context context, boolean isAyanamiFind){
        SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
        SharedPreferences.Editor editor = context.getSharedPreferences("info",MODE_PRIVATE).edit();
        if(isAyanamiFind){
            editor.putInt("AyanamiScore",pref.getInt("AyanamiScore",0)+1);
        }else{
            editor.putInt("playerScore",pref.getInt("playerScore",0)+1);
        }
        editor.apply();
    }

    public void showSearchOutcome(Context context,int x, int y, boolean isAyanamiSearch){
        if(getTreasureMapPoint(context,x,y) == 1){
            changeTreasureMapPointColor(context,x,y,isAyanamiSearch? R.color.colorRed:R.color.colorGreen,0);
            addTreasureNumber(context,isAyanamiSearch);
            changeTreasureMapPoint(context,x,y,isAyanamiSearch? 29:19);
        }else{
            int count = 0;
            for(int i = -1; i <= 1; i++){
                for(int j = -1; j <= 1; j++){
                    if(i != 0 || j != 0){
                        int temp = getTreasureMapPoint(context, x+i, y+j);
                        if(temp == 1 || temp == 19 || temp == 29){
                            count++;
                        }
                    }
                }
            }
            changeTreasureMapPointColor(context,x,y,isAyanamiSearch? R.color.colorDarkRed:R.color.colorDarkGreen,count);
            changeTreasureMapPoint(context,x,y,isAyanamiSearch? (20+count):(10+count));
        }

    }
}
