# **CurtainView** 

- 这是一个Android 自定义控件所画的窗帘

## 截图

<img src="D:\work\AndroidStudioProject\open source code\CurtainView\photo\curtainview_show.jpg" alt="curtainview_show" style="zoom:20%;" />

## 使用

1. 将JitPack存储库添加到您的构建文件

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

2. 添加依赖

```groovy
dependencies {
	        implementation 'com.github.zhuwang0926:CurtainView:1.0.1'
	}
```

3. xml

```xml
<com.hnkj.curtainview.CurtainView
            android:id="@+id/curtain"
            android:layout_width="300dp"
            android:layout_height="253dp"
            android:layout_marginTop="20dp"
            app:curtain_leaves_color="@color/curtain_leaves_color"
            app:curtain_rod_color="@color/curtain_rod_color"
            app:curtain_rod_height="40dp"
            app:curtain_thumb="@drawable/drag"
            app:curtain_type="true"
            app:duration="2500"
            app:max="100"
            app:min="0"
            app:min_progress="83"
            app:progress="100" />
```



4. kotlin

```kotlin
findViewById<CurtainView>(R.id.curtain).apply {
            setCurtainType(false)
            setDurtain(3000)
            setMax(100)
            setMin(0)
            setMinProgress(82f)
            setRodColor(Color.BLUE)
            setRodHeight(80F)
            setProgress(80)
            setThumb(getDrawable(R.drawable.img_curtain_drag))
            setProgressColor(Color.GRAY)
            setOnProgressChangeListener(object :
                CurtainView.OnProgressChangeListener {
                override fun onProgressChanged(seekBar: CurtainView?, progress: Int, isUser: Boolean) {
                    Log.d(TAG, "progress = $progress")
                }

                override fun onStartTrackingTouch(seekBar: CurtainView?) {
                    Log.d(TAG, "onStartTrackingTouch...")
                }

                override fun onStopTrackingTouch(seekBar: CurtainView?) {
                    Log.d(TAG, "onStopTrackingTouch...")
                }
            })

        }
```

5. 自定义属性

```xml
<declare-styleable name="CurtainView">
        <!-- 窗帘最小范围 -->
        <attr name="min" format="integer" />
        <!-- 窗帘最大范围 -->
        <attr name="max" format="integer" />
        <!-- 窗帘当前进度 -->
        <attr name="progress" format="integer" />
        <!-- 最小进度为窗帘两边的距离,应该要大于thumb一半的宽度-->
        <attr name="min_progress" format="float" />
        <!-- 动画时长 -->
        <attr name="duration" format="integer" />
        <!-- 窗帘杆的颜色 -->
        <attr name="curtain_rod_color" format="color" />
        <!-- 窗帘杆的高度 -->
        <attr name="curtain_rod_height" format="dimension" />
        <!-- 窗帘叶子的颜色 -->
        <attr name="curtain_leaves_color" format="color" />
        <!-- 窗帘滑块 -->
        <attr name="curtain_thumb" format="reference" />
        <!-- 窗帘类型 单开帘还是双开帘 -->
        <attr name="curtain_type" format="boolean" />
    </declare-styleable>
```



