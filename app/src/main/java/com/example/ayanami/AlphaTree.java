package com.example.ayanami;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

public class AlphaTree {
    Context context;
    int myChess;
    int enemyChess;
    boolean isAyanamiAI;
    int maxChildrenNumber = 50;
    int maxDeep = 5;
    int bestScore = -11000;
    int[][] chessBoardScore = {
            {100, -5, 10,  5,  5,  5,  5, 10, -5,100},
            { -5,-45,  1,  1,  1,  1,  1,  1,-45, -5},
            { 10,  1,  3,  2,  2,  2,  2,  3,  1, 10},
            {  5,  1,  2,  1,  1,  1,  1,  2,  1,  5},
            {  5,  1,  2,  1,  1,  1,  1,  2,  1,  5},
            {  5,  1,  2,  1,  1,  1,  1,  2,  1,  5},
            {  5,  1,  2,  1,  1,  1,  1,  2,  1,  5},
            { 10,  1,  3,  2,  2,  2,  2,  3,  1, 10},
            { -5,-45,  1,  1,  1,  1,  1,  1,-45, -5},
            {100, -5, 10,  5,  5,  5,  5, 10, -5,100}};
    int[] bestChoice = new int[2];

    public AlphaTree(Context context, boolean isAyanamiAI){
        this.context = context;
        this.myChess = isAyanamiAI? 2:1;
        this.enemyChess = isAyanamiAI? 1:2;
        this.isAyanamiAI = isAyanamiAI;
        Node root = new Node();
        root.deep = 0;
        root.chessBoard = initBoard();
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                if(canMove(context,root.chessBoard,i,j,true)){
                    bestChoice[0] = i;
                    bestChoice[1] = j;
                    break;
                }
            }
        }
        getChildren(root,false,0);
        Log.d("Sam","bestChoice:"+bestChoice[0]+"_"+bestChoice[1]);
    }

    public void getChildren(Node root, boolean isNextEnemy, int deep){
        //如果下一步我方下，list存最大的几个值，如果敌方下则存最小的几个值
        //int[] scoreList = new int[maxChildrenNumber];
        //for(int i = 0; i < maxChildrenNumber; i++)scoreList[i] = isNextEnemy? 500:-500;
        outer:for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                if(canMove(context,root.chessBoard,i,j,isAyanamiAI^isNextEnemy)){
                    Node child = new Node();
                    if(deep < maxDeep){
                        //如果深度没有达标，继续往下遍历
                        child = new Node();
                        child.father = root;
                        child.range[1] = root.range[1];
                        child.range[0] = root.range[0];
                        child.deep = deep + 1;
                        //child.position[0] = i;
                        //child.position[1] = j;
                        child.chessBoard = addPoint(root.chessBoard,i,j,isAyanamiAI^isNextEnemy);
                        root.children.add(child);
                        //Log.d("Map","deep:"+deep+" x: "+i+" y: "+j);
                        //if(deep == 4 || deep == 5){
                        //    printChess(child);
                        //}
                        getChildren(child,!isNextEnemy,deep+1);
                        if(root.range[0] >= root.range[1])break outer;
                    }else{
                        //深度达标了，计算下这里的得分
                        int[][] tempChessBoard = addPoint(root.chessBoard,i,j,isAyanamiAI^isNextEnemy);
                        int score = assess(tempChessBoard);
                        //更新root的range，深度为偶数更新下界，为奇数更新上界
                        if(deep % 2 == 0){
                            root.range[0] = Math.max(root.range[0],score);
                            if(root.father != null && root.range[0] >= root.father.range[1]){
                                //Log.d("Map","cut!");
                                break outer;
                            }
                        }else{
                            root.range[1] = Math.min(root.range[1],score);
                            if(root.father != null && root.range[1] <= root.father.range[0]){
                                //Log.d("Map","cut!");
                                break outer;
                            }
                        }
                    }
                    if(deep == 0 && child.range[1] > bestScore){
                        bestScore = child.range[1];
                        bestChoice[0] = i;
                        bestChoice[1] = j;
                    }
                }
            }
        }
        if(root.children.size() == 0){
            if(deep < maxDeep){
                //如果一方没有子可以下了，生成一个空子
                Node child = new Node();
                child.father = root;
                child.range[1] = root.range[1];
                child.range[0] = root.range[0];
                child.deep = deep + 1;
                //child.position[0] = i;
                //child.position[1] = j;
                int[][] newChessBoard = new int[10][10];
                for(int i = 0; i <= 9; i++){
                    for(int j = 0; j <= 9; j++){
                        newChessBoard[i][j] = root.chessBoard[i][j];
                    }
                }
                child.chessBoard = newChessBoard;
                root.children.add(child);
                //Log.d("Map","deep:"+deep+" x: "+i+" y: "+j);
                //if(deep == 4 || deep == 5){
                //    printChess(child);
                //}
                getChildren(child,!isNextEnemy,deep+1);
            }else{
                int score = assess(root.chessBoard);
                //更新root的range，深度为偶数更新下界，为奇数更新上界
                if(deep % 2 == 0){
                    root.range[0] = Math.max(root.range[0],score);
                }else{
                    root.range[1] = Math.min(root.range[1],score);
                }
            }
        }
        if(root.father != null){
            //更新root.father的range，深度为偶数时更新上界，为奇数更新下界
            if(deep % 2 == 0){
                root.father.range[1] = Math.min(root.father.range[1],root.range[0]);
            }else{
                root.father.range[0] = Math.max(root.father.range[0],root.range[1]);
            }
        }
    }

    public int[][] initBoard(){
        int[][] chessBoard = new int[10][10];
        SharedPreferences pref = context.getSharedPreferences("chessBoard",MODE_PRIVATE);
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                chessBoard[i][j] = pref.getInt(""+i+"_"+j,0);
            }
        }
        return chessBoard;
    }

    public int assess(int[][] chessBoard){
        int score = 0;
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                if(chessBoard[i][j] == myChess){
                    score += chessBoardScore[i][j];
                }else if(chessBoard[i][j] == enemyChess){
                    score -= chessBoardScore[i][j];
                }
            }
        }
        return score;
    }

    public boolean canMove(Context context,int[][] chessBoard, int x, int y, boolean isAyanami){
        int myChess = isAyanami? 2:1;
        int enemyChess = isAyanami? 1:2;
        if(chessBoard[x][y] != 0){
            return false;
        }
        if(x >= 1 && chessBoard[x-1][y] == enemyChess){
            for(int i = x-2; i >= 0; i--){
                if(chessBoard[i][y] == myChess){
                    return true;
                }else if(chessBoard[i][y] == 0){
                    break;
                }
            }
        }
        if(x <= 8 && chessBoard[x+1][y] == enemyChess){
            for(int i = x+2; i <= 9; i++){
                if(chessBoard[i][y] == myChess){
                    return true;
                }else if(chessBoard[i][y] == 0){
                    break;
                }
            }
        }
        if(y >= 1 && chessBoard[x][y-1] == enemyChess){
            for(int i = y-2; i >= 0; i--){
                if(chessBoard[x][i] == myChess){
                    return true;
                }else if(chessBoard[x][i] == 0){
                    break;
                }
            }
        }
        if(y <= 8 && chessBoard[x][y+1] == enemyChess){
            for(int i = y+2; i <= 9; i++){
                if(chessBoard[x][i] == myChess){
                    return true;
                }else if(chessBoard[x][i] == 0){
                    break;
                }
            }
        }
        if(x >= 1 && y >= 1 && chessBoard[x-1][y-1] == enemyChess){
            for(int i = -1; x+i >= 0 && y+i >= 0; i--){
                if(chessBoard[x+i][y+i] == myChess){
                    return true;
                }else if(chessBoard[x+i][y+i] == 0){
                    break;
                }
            }
        }
        if(x <= 8 && y >= 1 && chessBoard[x+1][y-1] == enemyChess){
            for(int i = 2; x+i <= 9 && y-i >= 0; i++){
                if(chessBoard[x+i][y-i] == myChess){
                    return true;
                }else if(chessBoard[x+i][y-i] == 0){
                    break;
                }
            }
        }
        if(x >= 1 && y <= 8 && chessBoard[x-1][y+1] == enemyChess){
            for(int i = 2; x-i >= 0 && y+i <= 9; i++){
                if(chessBoard[x-i][y+i] == myChess){
                    return true;
                }else if(chessBoard[x-i][y+i] == 0){
                    break;
                }
            }
        }
        if(x <= 8 && y <= 8 && chessBoard[x+1][y+1] == enemyChess){
            for(int i = 2; x+i <= 9 && y+i <= 9; i++){
                if(chessBoard[x+i][y+i] == myChess){
                    return true;
                }else if(chessBoard[x+i][y+i] == 0){
                    break;
                }
            }
        }
        return false;
    }

    public int[][] addPoint(int[][] chessBoard, int x, int y, boolean isAyanamiMove){
        int[][] newChessBoard = new int[10][10];
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                newChessBoard[i][j] = chessBoard[i][j];
            }
        }
        changeBlock(newChessBoard, x, y, isAyanamiMove);
        return newChessBoard;
    }

    public void changeBlock(int[][] chessBoard, int x, int y, boolean isAyanamiMove){
        int myChess = isAyanamiMove? 2:1;
        chessBoard[x][y] = myChess;
        boolean flag = false;
        for(int i = x-1; i >= 0; i--){
            int temp = chessBoard[i][y];
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
                int temp = chessBoard[i][y];
                if(temp == myChess){
                    break;
                }
                chessBoard[i][y] = myChess;
            }
        }
        flag = false;
        for(int i = x+1; i <= 9; i++){
            int temp = chessBoard[i][y];
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
                int temp = chessBoard[i][y];
                if(temp == myChess){
                    break;
                }
                chessBoard[i][y] = myChess;
            }
        }

        flag = false;
        for(int i = y-1; i >= 0; i--){
            int temp = chessBoard[x][i];
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
                int temp = chessBoard[x][i];
                if(temp == myChess){
                    break;
                }
                chessBoard[x][i] = myChess;
            }
        }

        flag = false;
        for(int i = y+1; i <= 9; i++){
            int temp = chessBoard[x][i];
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
                int temp = chessBoard[x][i];
                if(temp == myChess){
                    break;
                }
                chessBoard[x][i] = myChess;
            }
        }

        flag = false;
        for(int i = -1; x+i >= 0 && y+i >= 0; i--){
            int temp = chessBoard[x+i][y+i];
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
                int temp = chessBoard[x+i][y+i];
                if(temp == myChess || temp == 0){
                    break;
                }
                chessBoard[x+i][y+i] = myChess;
            }
        }

        flag = false;
        for(int i = 1; x+i <= 9 && y-i >= 0; i++){
            int temp = chessBoard[x+i][y-i];
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
                int temp = chessBoard[x+i][y-i];
                if(temp == myChess || temp == 0){
                    break;
                }
                chessBoard[x+i][y-i] = myChess;
            }
        }

        flag = false;
        for(int i = 1; x-i >= 0 && y+i <= 9; i++){
            int temp = chessBoard[x-i][y+i];
            if(temp == myChess){
                flag = true;
                break;
            }else if(temp == 0){
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = 1; x-i >= 0 && y+i <= 9; i++){
                int temp = chessBoard[x-i][y+i];
                if(temp == myChess || temp == 0){
                    break;
                }
                chessBoard[x-i][y+i] = myChess;
            }
        }

        flag = false;
        for(int i = 1; x+i <= 9 && y+i <= 9; i++){
            int temp = chessBoard[x+i][y+i];
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
                int temp = chessBoard[x+i][y+i];
                if(temp == myChess || temp == 0){
                    break;
                }
                chessBoard[x+i][y+i] = myChess;
            }
        }
    }

    public void compareWithScoreList(int[] scoreList, int score, boolean isNextEnemy, Node root, int[][] newChessBoard){
        //如果下一步我方下，list存最大的几个值，如果敌方下则存最小的几个值
        if(isNextEnemy? (score < scoreList[0]):(score > scoreList[0])){
            int reserve = 0;
            scoreList[0] = score;
            Node child = new Node();
            child.father = root;
            child.chessBoard = newChessBoard;
            if(root.children.size() < maxChildrenNumber){
                //没有填满，则前maxChildrenNumber - root.children.size() - 1 次交换，节点不交换
                reserve = maxChildrenNumber - root.children.size() - 1;
            }else{
                //如果填满，则删掉最后一个
                root.children.remove(root.children.size()-1);
            }
            root.children.add(child);
            int position = root.children.size()-1;
            for(int j = 0; j < maxChildrenNumber-1; j++){
                if(isNextEnemy? (scoreList[j+1] > scoreList[j]):(scoreList[j+1] < scoreList[j])){
                    int temp = scoreList[j+1];
                    scoreList[j+1] = scoreList[j];
                    scoreList[j] = temp;
                    if(reserve == 0){
                        Collections.swap(root.children,position,position-1);
                        position--;
                    }else{
                        reserve--;
                    }
                }else{
                    break;
                }
            }
        }
    }

    public void printChess(Node root){
        Log.d("Map","deep "+root.deep);
        StringBuilder str = new StringBuilder();
        str.append("chess:\n");
        for(int i = 0; i <= 9; i++){
            for(int j = 0; j <= 9; j++){
                str.append(root.chessBoard[i][j]);
            }
            str.append("\n");
        }
        Log.d("Map",str.toString());
    }
}

class Node{
    int[] position = new int[2];
    int deep;
    int[] range = new int[]{-10000,10000};
    Node father;
    int[][] chessBoard = new int[10][10];
    ArrayList<Node> children = new ArrayList<>();
}