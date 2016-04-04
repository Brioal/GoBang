package com.brioal.gobang.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.brioal.gobang.R;

import java.util.ArrayList;
import java.util.List;

public class Panel extends View {
    private static final String TAG = "PanelInfo";
    private int MAX_LINE = 10; // 格子的数量
    private int MAX_IN_LINE = 5; //胜利的条件
    public static int WHITE_WIN = 0; //白子胜利的标志
    public static int BLACK_WIN = 1; // 黑子胜利的标志
    private int panleWidth; // 棋盘的宽度
    private float lineHeight; // 方格的高度
    private Paint mPaint; // 用于绘制线的画笔
    private Paint mPaint_point; // 用于绘制点的画笔
    private Bitmap mWhite; // 白色棋子
    private Bitmap mBlack;//黑色棋子
    private int pieceWidth; // 棋子要显示的高度
    private int offset; // 棋盘离组件边界的偏移
    private List<Point> mWhites; // 存储棋盘上的白子
    private List<Point> mBlacks; // 存储棋盘上的黑子
    private boolean isWhite = false; // 存储是否是白子 , 默认黑子先行
    private boolean isGameOver = false; // 存储游戏是否已经结束
    private onGameListener onGameListener; // 供外部调用的接口参数
    private int mUnder; // 组件的底部的位置 , 用于确定Dialog的显示文职
    public interface onGameListener { // 用于回调的接口
        void onGameOVer(int i);
    }

    public Panel(Context context) {
        this(context, null);
    }

    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnGameListener(Panel.onGameListener onGameListener) {
        this.onGameListener = onGameListener;
    }

    public int getUnder() {

        return mUnder;
    }
    //重新开始游戏
    public void reStartGame() {
        mWhites.clear(); // 清理白子
        mBlacks.clear(); // 清理黑子
        isGameOver = false; // 游戏未结束
        isWhite = false; // 黑子先手
        invalidate(); // 重绘
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(0x88000000); // 设置画笔的颜色
        mPaint.setAntiAlias(true); // 画笔设置抗锯齿
        mPaint.setDither(true); // 画笔设置图像抖动处理,使图像更加平滑
        mPaint.setStyle(Paint.Style.STROKE); // 设置绘制方式
        mPaint.setStrokeWidth(2); // 设置画笔的边界宽度
        mPaint_point = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mWhite = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2); // 获取白棋的资源文件
        mBlack = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1); // 获取黑棋的资源文件
        mWhites = new ArrayList<>();
        mBlacks = new ArrayList<>();
    }

    //判断是否游戏结束,在onDraw方法内调用
    public void checkGameOver() {
        boolean whiteWin = checkFiveInLine(mWhites);
        boolean blackWin = checkFiveInLine(mBlacks);
        if (whiteWin || blackWin) {
            isGameOver = true;
            if (onGameListener != null) {
                onGameListener.onGameOVer(whiteWin ? WHITE_WIN : BLACK_WIN);
            }
        }
    }

    private boolean checkFiveInLine(List<Point> points) {
        for (Point point : points) {
            int x = point.x;
            int y = point.y;
            //水平方向的检查
            boolean isWin1 = checkHorizontal(x, y, points);
            //检查垂直方向
            boolean isWin2 = checkVertical(x, y, points);
            //左斜方向的检查
            boolean isWin3 = checkDiagonalLeft(x, y, points);
            //右斜方向的检查
            boolean isWin4 = checkDiagonalRight(x, y, points);
            //任意方向五子连珠 , 游戏结束
            if (isWin1 || isWin2 || isWin3 || isWin4) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonalRight(int x, int y, List<Point> points) {
        int count = 1;
        int emptyCount = 0;
        //往右上方向的判断
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x + i, y - i);
            if (points.contains(point)) {
                count++;
            } else {
                if (!mWhites.contains(point) && !mBlacks.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        //往左下方向的判断
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x - i, y + i);
            if (points.contains(point)) {
                count++;
            } else {
                if (!mWhites.contains(point) && !mBlacks.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        return false;
    }

    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        int emptyCount = 0;
        //往上遍历
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x, y - i);
            if (points.contains(point)) {
                count++;
            } else {
                if (!mBlacks.contains(point) && !mWhites.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        Log.i(TAG, "checkDiagonalLeft: " + count + ":" + emptyCount);
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        //往下遍历
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x, y + i);

            if (points.contains(point)) {
                count++;
            } else {
                if (!mBlacks.contains(point) && !mWhites.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        Log.i(TAG, "checkDiagonalLeft: " + count + ":" + emptyCount);
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        return false;
    }

    //左斜方向的判断
    private boolean checkDiagonalLeft(int x, int y, List<Point> points) {
        int count = 1;
        //往左上方向遍历
        int emptyCount = 0;
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x - i, y - i);
            if (points.contains(point)) {
                count++;
            } else {
                if (!mBlacks.contains(point) && !mWhites.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        //往右下方向的判断
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x + i, y + i);
            if (points.contains(point)) {
                count++;
            } else {
                if (!mBlacks.contains(point) && !mWhites.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        return false;
    }

    //检查水平方向
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;
        int emptyCount = 0;
        //往左遍历
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x - i, y);
            if (points.contains(point)) { // 是否包含点
                count++;
            } else {
                if (!mBlacks.contains(point) && !mWhites.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        //往右遍历
        for (int i = 1; i < MAX_IN_LINE; i++) {
            Point point = new Point(x + i, y);
            if (points.contains(point)) {
                count++;
            } else {
                if (!mBlacks.contains(point) && !mWhites.contains(point)) {
                    emptyCount++;
                }
                break;
            }
        }
        if ((count == MAX_IN_LINE - 1 && emptyCount > 0) || count == MAX_IN_LINE) {
            return true;
        }
        return false;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        panleWidth = w; // 获取棋盘的宽度
        mUnder = h - (h - panleWidth) / 2;
        lineHeight = panleWidth * 1.0f / MAX_LINE; // 获取格子的高度 10行,则直线有10条 ,格子只有9个
        offset = (int) (lineHeight / 2);
        pieceWidth = (int) (lineHeight * 3 / 4); // 棋子的高度为格子高度的3/4
        mWhite = Bitmap.createScaledBitmap(mWhite, pieceWidth, pieceWidth, false); // 根据棋子宽度进行缩放
        mBlack = Bitmap.createScaledBitmap(mBlack, pieceWidth, pieceWidth, false); //根据棋子宽度进行缩放
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver) {
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {

            Point point = new Point((int) ((event.getX() - offset) / lineHeight), (int) ((event.getY() - offset) / lineHeight));
            if (!mWhites.contains(point) && !mBlacks.contains(point)) { // 如果没有点
                if (isWhite) {
                    mWhites.add(point); // 存入白子
                    isWhite = false;
                } else {
                    mBlacks.add(point); //存入黑子
                    isWhite = true;
                }
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec); // 获取宽度
        int widthMode = MeasureSpec.getMode(widthMeasureSpec); // 获取宽度的类型

        int heightSize = MeasureSpec.getSize(heightMeasureSpec); // 获取高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec); // 获取高度的类型

        int width = Math.min(widthMeasureSpec, heightMeasureSpec);//取宽高的最小值
        if (widthMode == MeasureSpec.UNSPECIFIED) {         //如果宽度是wrap_content , 则最终宽度置高度
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {             //如果高度是wrap_content , 最终宽度置宽度
            width = widthSize;
        }
        setMeasuredDimension(width, width);         //设置值
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPoints(canvas);
        checkGameOver();
    }

    //绘制棋子
    private void drawPoints(Canvas canvas) {
        for (Point point : mWhites) {
            canvas.drawBitmap(mWhite, offset + point.x * lineHeight - pieceWidth / 2, offset + point.y * lineHeight - pieceWidth / 2, mPaint_point);
        }
        for (Point point : mBlacks) {
            canvas.drawBitmap(mBlack, offset + point.x * lineHeight - pieceWidth / 2, offset + point.y * lineHeight - pieceWidth / 2, mPaint_point);
        }

    }

    //绘制棋盘
    private void drawBoard(Canvas canvas) {
        int start_x = offset; // 起始 x坐标
        int end_x = panleWidth - offset; // 终止x坐标
        for (int i = 0; i < MAX_LINE; i++) {
            float start_y = i * lineHeight + offset; // 起始的y坐标
            float end_y = i * lineHeight + offset; // 终止的y坐标
            canvas.drawLine(start_x, start_y, end_x, end_y, mPaint); // 绘制横向的
            canvas.drawLine(start_y, start_x, end_y, end_x, mPaint); // 纵向只需要把 横向的xy交换就行
        }
    }
}
