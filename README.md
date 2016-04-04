#400行代码实现双人对战五子棋(适合新手入门)
##跟上一篇博客一样,都是看了慕课网的视频之后写的学习记录,记录一下实现的思路,大部分内容比较简单,但也从中学到了很多东西.
##从中能学到的知识点:

 1. Android实现全屏的两种方式



##按惯例首先看一下效果:(素材都是用的慕课网给的)
##顺便也贴一下这个视频的连接,也推荐新手多上慕课看视频,能学到很多东西,质量也很好:[Android-五子连珠](http://www.imooc.com/learn/641)
![这里写图片描述](http://img.blog.csdn.net/20160404232025894)
##下面就一步步来实现吧
###一:首先看到的是全屏的`Activity`,没有状态栏,在Android中要实现全屏我知道的有两种方式,推荐第二种因为第一种存在问题,下面就一一介绍一下:
####1.第一种全屏方式:`Manifest`中设置要全屏的`Activity`的主题为`@android:style/Theme.NoTitleBar.Fullscreen`,同时需要更改`Activity`继承的父类为`Activity`,否则无法启动,如下:
```
<application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity
            android:name=".MainActivity"
            android:label="@string/app_name"
            android:theme="@android:style/Theme.NoTitleBar.Fullscreen">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
```
`Activity`
```
public class MainActivity extends Activity { ...
```
###**缺点**:正是因为这种方法更改了`Activity`的主题和所继承的父类,相应的会引起其他很多问题,如下:
####1.修改了`Activity`主题,会使得在此`Activity`内显示的组件在4.0+设备和5.0+设备下使用同样的资源文件,如下图:
###在5.1版本的设备上进行测试,本应该显示的Dialog是这样的:
![这里写图片描述](http://img.blog.csdn.net/20160404234149074)
###结果设置了上文的主题之后却是这样的:
![这里写图片描述](http://img.blog.csdn.net/20160404234223746)
####2.第二种问题相对于第一种就显得不是那么明显,由于这种全屏方式更换了当前`Activity`的父类,且默认生成的父类应该是`AppCompatActivity`,更换之后如果之前使用的`FragmentManager`对象,那么原本的获取方式是`getSupportFragmentManager()`,换成`Activity`之后就只有`getFragmentManager()`,目前遇到的问题只是这样.

####2.第二种全屏方式:获取`Activity`的`Window`对象,添加全屏的`flag`,如下:
```
Window window = getWindow();
window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
```
###两种方式的效果是一样的,都是示例图中的全屏状态,第二种更为方便,简单,推荐第二种.

##二.主要的布局只有一个自定义的棋盘`Panel`组件,父组件是`RelativeLayout`,有一个背景图,布局文件如下:
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg">

    <com.brioal.gobang.view.Panel
        android:id="@+id/main_panel"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center"
        android:layout_marginBottom="40dp" />
</LinearLayout>

```
####由于游戏结束之后设置显示的`Dialog`不想遮挡最后的棋盘,所以将棋盘设置了一个`layout_marginBottom`使棋盘整体向上移动了`40dp`
####自定义的`Panel`封装了棋盘线的绘制,棋盘点击事件的处理,棋子的绘制,棋子点的存储,胜负情况的判断等,给外部的接口只是游戏的结果和重新开始游戏的方法.

##三.自定义棋盘的代码书写.
###1创建`Panel`class文件,继承`View`,并添加两个构造方法,只需要处理两个参数的构造方法,单个参数的是在代码中创建View时候调用的,两个参数的是在布局文件中定义时候调用的,如下:
```
public class Panel extends View {

public Panel(Context context) {
        this(context, null);
    }

    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }
}
```
###2.接下来定义一下需要用到的参数,由于要绘制棋盘的线条,棋子,等,所以需要定义的参数如下:
```
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
    private int mUnder; // 组件的底部的位置 , 用于确定Dialog的显示位置
    
    public interface onGameListener { // 用于回调的接口
        void onGameOVer(int i);
    }
```
###3.下一步是获取棋盘的尺寸,棋盘是正方形的,并且是以棋盘的宽度为准的,一般尺寸的获取是在`onSizeChanged`里面获取的,这个方法会在进行绘制之前调用,且只调用一次,并且会在屏幕尺寸发生改变的时候调用,如屏幕旋转,对尺寸参数进行处理的代码如下:
```
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
```

###4.尺寸处理完之后就需要绘制棋盘了,看示例图可以直到,棋盘线距离组件的边界都有一个偏移量,并且已经在`onSizeChanged`内处理过了,绘制棋盘很简单,就是一个循环就能完成全部的绘制工作.
####	绘制之前需要先初始化画笔,这里我们就将绘制线和绘制棋子的画笔都初始化一下,细节都在注释里面:
```
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
```
####接下来绘制棋盘,观察棋盘可以知道,横线的起点为偏移量,终点为宽度减去偏移量,起始`Y`坐标与终止`Y`坐标相同,都为行高与当前序号的乘积,竖线绘制的参数与横线的参数完全相同,不同的只是与横线的`x,y`刚好相反,代码如下:
```
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
```
###然后在`onDraw`内调用`drawBoard`即完成了棋盘的绘制,效果如下:
![这里写图片描述](http://img.blog.csdn.net/20160405001244211)
####5.我们之前处理了白子黑子的资源文件,接下来应该处理点击事件,并且将棋子显示到棋盘上
####首先我们使用`List<Point>`来存储棋子的位置,`Point`类为系统自带,在`onTouch`方法中对点击的位置进行计算,生成横纵坐标是棋子在棋盘中的相对位置的`Point`,然后添加到对应的`List<Point>`中去,代码如下,详细的都在注释当中:
```
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
```
####获取到了要显示的点,接下来就是遍历`List`,将点绘制到画布当中去,如下:
```
//绘制棋子
    private void drawPoints(Canvas canvas) {
        for (Point point : mWhites) {
            canvas.drawBitmap(mWhite, offset + point.x * lineHeight - pieceWidth / 2, offset + point.y * lineHeight - pieceWidth / 2, mPaint_point);
        }
        for (Point point : mBlacks) {
            canvas.drawBitmap(mBlack, offset + point.x * lineHeight - pieceWidth / 2, offset + point.y * lineHeight - pieceWidth / 2, mPaint_point);
        }

    }
```
##6.能通过点击交替生成棋子之后需要的就是对游戏结束条件的判断,同时调用接口将结果传出,判断胜利比较麻烦,基本原理是对白子和黑子中的每一个点进行四个方向上的判断,判断是否连成5个或者是连成4个且至少存在两个空白位置,代码如下:
```
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

```
###7.对重新开始一局游戏的处理,如下:
```
 //重新开始游戏
    public void reStartGame() {
        mWhites.clear(); // 清理白子
        mBlacks.clear(); // 清理黑子
        isGameOver = false; // 游戏未结束
        isWhite = false; // 黑子先手
        invalidate(); // 重绘
    }
```
###8.暴露回调接口:
```
public void setOnGameListener(Panel.onGameListener onGameListener) {
        this.onGameListener = onGameListener;
    }
```
###9,返回棋盘的底部坐标:
```
  public int getUnder() {

        return mUnder;
    }
```
###自定义棋盘`View`就完成了,接下来是`Activity`中的处理,如下:
###设置Dialog的位置:
```
Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.x = 0;//设置x坐标
                params.y =panel.getUnder();//设置y坐标
                dialogWindow.setAttributes(params);
```
###余下的内容没有什么需要细说的,就直接上代码:
```
private Panel panel; // 棋盘VIew
    private AlertDialog.Builder builder; //Dialog构建

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("游戏结束"); // 设置Dialog的标题
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 设置退出按钮和点击事件
                MainActivity.this.finish();
            }
        });
        builder.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 设置再来一局的按钮和点击事件
                panel.reStartGame();
            }
        });
        panel = (Panel) findViewById(R.id.main_panel);
        panel.setOnGameListener(new Panel.onGameListener() {
            @Override
            public void onGameOVer(int i) { // 设置监听器
                String text = "";
                if (i == Panel.WHITE_WIN) {
                    //白子胜利
                    text = "白子胜利";
                } else if (i == Panel.BLACK_WIN) {
                    //黑子胜利
                    text = "黑子胜利";
                }
                builder.setMessage(text); // 设置Dialog内容
                builder.setCancelable(false); // 设置不可返回键取消
                AlertDialog dialog = builder.create(); // 构建Dialog
                Window dialogWindow = dialog.getWindow(); 
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.x = 0;//设置x坐标
                params.y = panel.getUnder();//设置y坐标
                dialogWindow.setAttributes(params); // 设置新的LayoutParams
                dialog.setCanceledOnTouchOutside(false); // 设置点击外部不取消
                dialog.show(); // 显示Dialog
            }
        });
    }

```

###至此所有代码就完成了,下面贴一下`MainActivity`和`Panel`的全部代码,注释较全面.
###**`MainActivity`**
```
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.brioal.gobang.view.Panel;

public class MainActivity extends AppCompatActivity {
    private Panel panel; // 棋盘VIew
    private AlertDialog.Builder builder; //Dialog构建

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("游戏结束"); // 设置Dialog的标题
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 设置退出按钮和点击事件
                MainActivity.this.finish();
            }
        });
        builder.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 设置再来一局的按钮和点击事件
                panel.reStartGame();
            }
        });
        panel = (Panel) findViewById(R.id.main_panel);
        panel.setOnGameListener(new Panel.onGameListener() {
            @Override
            public void onGameOVer(int i) { // 设置监听器
                String text = "";
                if (i == Panel.WHITE_WIN) {
                    //白子胜利
                    text = "白子胜利";
                } else if (i == Panel.BLACK_WIN) {
                    //黑子胜利
                    text = "黑子胜利";
                }
                builder.setMessage(text); // 设置Dialog内容
                builder.setCancelable(false); // 设置不可返回键取消
                AlertDialog dialog = builder.create(); // 构建Dialog
                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.x = 0;//设置x坐标
                params.y = panel.getUnder();//设置y坐标
                dialogWindow.setAttributes(params); // 设置新的LayoutParams
                dialog.setCanceledOnTouchOutside(false); // 设置点击外部不取消
                dialog.show(); // 显示Dialog
            }
        });
    }


}

```
###**`Panel.java`**
```
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

```
